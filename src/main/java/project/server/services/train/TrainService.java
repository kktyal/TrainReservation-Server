package project.server.services.train;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Utils;
import project.server.ServerApplication;
import project.server.entities.train.*;
import project.server.enums.CommonResult;
import project.server.enums.interfaces.IResult;
import project.server.enums.trainResult.*;
import project.server.mappers.train.ITrainMapper;
import project.server.vos.train.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

import project.server.lang.Pair;

@Service
@Slf4j
public class TrainService {
    private final ITrainMapper trainMapper;

    public TrainService(ITrainMapper trainMapper) {
        this.trainMapper = trainMapper;
    }

//    public Optional<ReservationEntity> test() {
//        LocalDate currentDate = LocalDate.now();
//        UUID uuid = UUID.randomUUID();
//        String random = currentDate.toString().concat(uuid.toString());
//        System.out.println(random);
//        return trainMapper.selectReservationId("20230613");
//    }

    public List<ReservationEntity> test(){
        return trainMapper.test();
    }

    public Pair<Enum<? extends IResult>, Integer> cancel(ReservationVo input) {
        Optional<ReservationEntity> hasReservationId = trainMapper.selectReservationId(input.getReservationId());
        if (hasReservationId.isEmpty()) {
            return new Pair<>(TrainResult.NO_SEARCH_DATA, null);
        }
        Optional<ReservationEntity> isCanceled = trainMapper.selectReservationIdAndDisuse(input.getReservationId());
        if(isCanceled.isEmpty()){
            return new Pair<>(TrainResult.INVALID_RESERVATION_ID, null);
        }

        int result = trainMapper.updateCancelToReservation(input.getReservationId());
        disuse();
        return Utils.getIngegerPair(result);
    }

    public Pair<Enum<? extends IResult>, Integer> refund(ReservationVo input) {

        Optional<ReservationEntity> hasReservationId = trainMapper.selectReservationId(input.getReservationId());
        if (hasReservationId.isEmpty()) {
            return new Pair<>(TrainResult.NO_SEARCH_DATA, null);
        }
        Optional<ReservationEntity> isRefund = trainMapper.selectReservationIdAndDisuse(input.getReservationId());
        if(isRefund.isEmpty()){
            return new Pair<>(TrainResult.INVALID_RESERVATION_ID, null);
        }

        int result = trainMapper.updateRefundToPayment(hasReservationId.get().getPaymentId());
        disuse();
        return Utils.getIngegerPair(result);
    }

    @Transactional
    public Pair<Enum<? extends IResult>, Integer> payment(ReservationEntity input) {
        Pair<Enum<? extends IResult>, Integer> pair = new Pair<>(null, null);

        Optional<ReservationEntity> hasReservationId = trainMapper.selectReservationId(input.getReservationId());
        if (hasReservationId.isEmpty()) {
            return new Pair<>(TrainResult.NO_SEARCH_DATA, null);
        }
        

        Optional<ReservationEntity> isPayment = trainMapper.selectIsPayment(input.getReservationId());
        if(isPayment.isEmpty()){
            pair.setKey(PaymentResult.IS_COMPLETED_PAYMENT);
            return pair;
        }


        PaymentEntity payment = new PaymentEntity();

        // 총 금액을 알기 위해 ticket에서 age, price 조회
        int totalPrice = 0;
        List<TicketEntity> ticketEntities = trainMapper.selectTicketByReservationId(input.getReservationId());
        for (TicketEntity ticketEntity : ticketEntities) {
           totalPrice+=ticketEntity.getDiscountedPrice();
        }
        payment.setTotalPrice(totalPrice);

        // paymentid 세팅
        String paymentId;
        do {
            paymentId = createId();
        } while (trainMapper.selectPaymentId(paymentId).isPresent());
        Date createdDate = new Date();
        payment.setPaymentId(paymentId);
        payment.setCreatedDate(createdDate);
        int savePaymentResult = trainMapper.savePayment(payment);

        input.setPaymentId(paymentId);
        int updateReservationPaymentIdResult = trainMapper.updateReservationPaymentId(input);
        if (savePaymentResult == 1 && updateReservationPaymentIdResult == 1) {
            pair.setKey(CommonResult.SUCCESS);
            pair.setValue(savePaymentResult + updateReservationPaymentIdResult);
        } else {
            pair.setKey(TrainResult.NO_SEARCH_DATA);

        }
        return pair;


    }

    @Transactional
    public Pair<Enum<? extends IResult>, String> reservation(List<ReservationVo> reservationVo, CntVo count) {

        Pair<Enum<? extends IResult>, String> pair = new Pair<>(null, null);

        //검증
        for (ReservationVo vo : reservationVo) {
            if (trainMapper.duplicatedTicket(vo).size() != 0) {
                pair.setKey(ReservationResult.SEAT_DUPLICATED);
                return pair;
            }
        }

        // reservation_id 생성
        String reservationId;
        do {
            reservationId = createId();
        } while (trainMapper.selectReservationId(reservationId).isPresent());
        // 만료 날짜 생성

        Date createdDate = new Date();
        Date expiredDate ;
        expiredDate = DateUtils.addMinutes(createdDate, 10);

        //예약
        ReservationVo vo = reservationVo.get(0);

        vo.setReservationId(reservationId);
        vo.setCreatedDate(createdDate);
        vo.setExpiredDate(expiredDate);


        vo.setDisuse(false);
        int reservationCnt = trainMapper.saveReservation(vo);


        TrainChargeVo charge = new TrainChargeVo();
        charge.setArriveStation(vo.getArriveStation());
        charge.setDepartStation(vo.getDepartStation());


        //티켓
        TrainChargeEntity trainChargeEntity = selectCharge(charge).getValue();

        int adultCnt = count.getAdult();
        int oldCnt = count.getOld();
        int childCnt = count.getChild();

        double oldDiscountRate = 0.0;
        double childDiscountRate = 0.0;
        List<AgeEntity> ageEntityList = trainMapper.selectAge();
        for (AgeEntity ageEntity : ageEntityList) {
            if(ageEntity.getKinds().equals("child")){
                childDiscountRate = ageEntity.getDiscountRate();
            }else if(ageEntity.getKinds().equals("old")){
                oldDiscountRate = ageEntity.getDiscountRate();
            }
        }
        System.out.println("childDiscountRate = " + childDiscountRate);
        System.out.println("oldDiscountRate = " + oldDiscountRate);


        int ticketCnt = 0;
        for (ReservationVo seat : reservationVo) {
            String ticketId;
            do {
                ticketId = createId();
            } while (trainMapper.selectTicketId(ticketId).isPresent());

            TicketEntity ticket = new TicketEntity();
            ticket.setTicketId(ticketId);
            ticket.setTrainNo(seat.getTrainNo());
            ticket.setCarriage(seat.getCarriage());
            ticket.setSeat(seat.getSeat());
            ticket.setReservationId(reservationId);
            if (ticket.getCarriage() == 2) {
                ticket.setPrice(trainChargeEntity.getChargeVip());
            } else {
                ticket.setPrice(trainChargeEntity.getCharge());
            }
            if (adultCnt > 0) {
                ticket.setAge("adult");
                ticket.setDiscountedPrice(ticket.getPrice());

                adultCnt--;
            } else if (oldCnt > 0) {
                ticket.setAge("old");
                ticket.setDiscountedPrice((int) Math.floor((ticket.getPrice() * oldDiscountRate) / 100) * 100);

                oldCnt--;
            } else {
                ticket.setAge("child");
                ticket.setDiscountedPrice((int) Math.floor((ticket.getPrice() * childDiscountRate) / 100) * 100);

            }
            ticketCnt += trainMapper.saveTicket(ticket);
        }
        if (reservationCnt != 1 && ticketCnt != adultCnt + childCnt + oldCnt) {
            pair.setKey(CommonResult.FAILURE);
            return pair;
        }
        pair.setKey(CommonResult.SUCCESS);
        pair.setValue(reservationId);
        return pair;
    }

    @Transactional
    public Pair<Enum<? extends IResult>, List<?>> reservationRefundPage(ReservationEntity input) {
        disuse();


        List<ReservationVo> result = trainMapper.showReservation(input.getReservationId());
        for (ReservationVo vo : result) {
            vo.setTicketId(null);
            vo.setTrainNo(null);
        }

        return Utils.getListPair(result);
    }

    @Transactional
    public Pair<Enum<? extends IResult>, List<?>> reservationDetail(ReservationEntity input) {
        disuse();

        List<ReservationVo> result = trainMapper.showReservation(input.getReservationId());
        for (ReservationVo vo : result) {
            vo.setTicketId(null);
        }

        return Utils.getListPair(result);
    }

    @Transactional
    public Pair<Enum<? extends IResult>, List<?>> ticketDetail(ReservationVo inputVo) {
        disuse();

        List<ReservationVo> result = trainMapper.showReservation(inputVo.getReservationId());

        return Utils.getListPair(result);
    }

    @Transactional
    public Pair<Enum<? extends IResult>, List<?>> selectReservationListsByMemberId(Integer memberId) {
        disuse();
        List<ReservationVo> result = trainMapper.selectReservationByMemberId(memberId);

        return Utils.getListPair(result);
    }


    public Pair<Enum<? extends IResult>, TrainChargeEntity> selectCharge(TrainChargeVo chargeVo) {
        Integer departStationIndex = transferStationName(chargeVo.getDepartStation()).getIndex();
        departStationIndex = departStationIndex != null ? departStationIndex : 0;
        Integer arriveStationIndex = transferStationName(chargeVo.getArriveStation()).getIndex();
        arriveStationIndex = arriveStationIndex != null ? arriveStationIndex : 0;


        Pair<Enum<? extends IResult>, TrainChargeEntity> pair = new Pair<>(null, null);
        chargeVo.setDepart(departStationIndex);
        chargeVo.setArrive(arriveStationIndex);
        Optional<TrainChargeEntity> result = trainMapper.selectCharge(chargeVo);

        if (result.isPresent()) {
            pair.setKey(CommonResult.SUCCESS);
            pair.setValue(result.get());
        } else {
            pair.setKey(InquiryResult.NO_SEARCH_DATA);
            pair.setValue(new TrainChargeEntity());
        }

        return pair;

    }

    public Pair<Enum<? extends IResult>, List<?>> selectTime(int srtNo) {
        List<TrainTimeVo> result = trainMapper.selectTime(srtNo);

        return Utils.getListPair(result);
    }
    public List<String>getPremiumSeats(){
        return this.trainMapper.selectPremiumSeats();
    }

    public List<String>getStandardSeats(){
        return this.trainMapper.selectStandardSeats();
    }

    @Transactional
    public Pair<Enum<? extends IResult>, List<ReservationVo>> selectSeat(ReservationVo vo) {


        disuse();
        ApiVo apiVo = new ApiVo();
        apiVo.setTrainno(vo.getTrainNo());
        apiVo.setDate(vo.getDate());
        apiVo.setDepplandtime(vo.getDepartTime());
        apiVo.setArrplandtime(vo.getArriveTime());

        List<ReservationVo> soldSeat = trainMapper.findSoldSeat(apiVo);

        Pair<Enum<? extends IResult>, List<ReservationVo>> pair = new Pair<>(null, null);
        pair.setKey(CommonResult.SUCCESS);
        pair.setValue(soldSeat);
        return pair;
    }

    @Transactional
    public Pair<Enum<? extends IResult>, List<ApiVo>> api(CntVo cntVo) throws IOException {

        disuse();

        Pair<Enum<? extends IResult>, List<ApiVo>> pair = getApi(cntVo);

        if (pair.getKey() == InquiryResult.NO_SEARCH_DATA) {
            System.out.println("1");
            pair.setKey(InquiryResult.NO_SEARCH_DATA);
            pair.setValue(null);
            return pair;
        }
        if (pair.getKey() == InquiryResult.API_ERROR) {
            System.out.println("2");
            pair.setKey(InquiryResult.API_ERROR);
            pair.setValue(null);
            return pair;
        }




        for (ApiVo apiVo : pair.getValue()) {
            int standard = 0;
            int premium = 0;
            List<ReservationVo> soldSeat = trainMapper.findSoldSeat(apiVo);

            for (ReservationVo reservationVo : soldSeat) {
                if (reservationVo.getCarriage() != 2)
                    standard++;
                else
                    premium++;
            }
            if (cntVo.getOld() + cntVo.getChild() + cntVo.getAdult() + premium > ServerApplication.premiumSeats.size()) {
                apiVo.setPremium(true);
            }
            if (cntVo.getOld() + cntVo.getChild() + cntVo.getAdult() + standard > ServerApplication.standardSeats.size()) {
                apiVo.setStandard(true);
            }
        }
        pair.setKey(CommonResult.SUCCESS);
        pair.setValue(pair.getValue());
        return pair;
    }

    public Pair<Enum<? extends IResult>, List<ApiVo>> getApi(CntVo cntVo) throws IOException {
        Pair<Enum<? extends IResult>, List<ApiVo>> pair = new Pair<>(null, null);

        String departStationCode = transferStationName(cntVo.getDepartStation()).getStationCode();
        departStationCode = departStationCode != null ? departStationCode : "";
        String arriveStationCode = transferStationName(cntVo.getArriveStation()).getStationCode();
        arriveStationCode = arriveStationCode != null ? arriveStationCode : "";
        System.out.println("arriveStationCode = " + arriveStationCode);
        System.out.println("departStationCode = " + departStationCode);
        System.out.println("cntVo.getDate() = " + cntVo.getDate());


//        /*URL*/
//        String urlBuilder = "http://apis.data.go.kr/1613000/TrainInfoService/getStrtpntAlocFndTrainInfo" + "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=ovMDJk4e%2BY2bRcCR4qJTYDuTlnmFIMOPMmZsPd1rbUJylcSo%2FUyXnFaJWPu1yt4M1ZdnTfH20zVHD91u9HQt1Q%3D%3D" + /*Service Key*/
//                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8") + /*페이지번호*/
//                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("50", "UTF-8") + /*한 페이지 결과 수*/
//                "&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8") + /*데이터 타입(xml, json)*/
//                "&" + URLEncoder.encode("depPlaceId", "UTF-8") + "=" + URLEncoder.encode(departStationCode, "UTF-8") + /*출발기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
//                "&" + URLEncoder.encode("arrPlaceId", "UTF-8") + "=" + URLEncoder.encode(arriveStationCode, "UTF-8") + /*도착기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
//                "&" + URLEncoder.encode("depPlandTime", "UTF-8") + "=" + URLEncoder.encode(cntVo.getDate(), "UTF-8") + /*출발일(YYYYMMDD)*/
//                "&" + URLEncoder.encode("trainGradeCode", "UTF-8") + "=" + URLEncoder.encode("17", "UTF-8"); /*차량종류코드*/
//        URL url = new URL(urlBuilder);
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/TrainInfoService/getStrtpntAlocFndTrainInfo"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=Gcqxaud5Om2bmKrfPZ29wv5Ri2exsBYPXbPm%2BNCLjb3qvthZoLIJN86AEVCHUhKIc3OMmRUdMVCm%2Bkq70SzBJQ%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("50", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*데이터 타입(xml, json)*/
        urlBuilder.append("&" + URLEncoder.encode("depPlaceId","UTF-8") + "=" + URLEncoder.encode(departStationCode, "UTF-8")); /*출발기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
        urlBuilder.append("&" + URLEncoder.encode("arrPlaceId","UTF-8") + "=" + URLEncoder.encode(arriveStationCode, "UTF-8")); /*도착기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
        urlBuilder.append("&" + URLEncoder.encode("depPlandTime","UTF-8") + "=" + URLEncoder.encode(cntVo.getDate(), "UTF-8")); /*출발일(YYYYMMDD)*/
        urlBuilder.append("&" + URLEncoder.encode("trainGradeCode","UTF-8") + "=" + URLEncoder.encode("17", "UTF-8")); /*차량종류코드*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        System.out.println("Response code: " + conn.getResponseCode());

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            System.out.println("3");
            pair.setKey(InquiryResult.API_ERROR);

            return pair;
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();


        List<ApiVo> resultVo = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String data = sb.toString();


        JsonNode rootNode = objectMapper.readTree(data);
        int totalCount = rootNode.path("response").path("body").path("totalCount").asInt();

        if (totalCount == 0) {
            pair.setKey(InquiryResult.NO_SEARCH_DATA);

            return pair;
        }


        JSONObject jsonObject = new JSONObject(data);
        JSONArray itemArray = jsonObject.getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items")
                .getJSONArray("item");


        // "item" 배열 순회
        for (int i = 0; i < itemArray.length(); i++) {
            JSONObject itemObject = itemArray.getJSONObject(i);
            ApiVo apiVo = objectMapper.readValue(itemObject.toString(), ApiVo.class);
            apiVo.setDate(apiVo.getArrplandtime().substring(0, 8));
            apiVo.setArrplandtime(apiVo.getArrplandtime().substring(8));
            apiVo.setDepplandtime(apiVo.getDepplandtime().substring(8));

            if(apiVo.getArrplacename().equals("지제")){
                apiVo.setArrplacename("평택지제");
            }
            if(apiVo.getDepplacename().equals("지제")){
                apiVo.setDepplacename("평택지제");
            }
            resultVo.add(apiVo);
        }
        pair.setKey(CommonResult.SUCCESS);
        pair.setValue(resultVo);
        return pair;
    }

    public TrainStationEntity transferStationName(String stationName) {
        return trainMapper.findByName(stationName).orElse(new TrainStationEntity());
    }

    public String createId() {
        LocalDate currentDate = LocalDate.now();
        UUID uuid = UUID.randomUUID();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");


        return currentDate.format(formatter).concat(uuid.toString()).replaceAll("-", "");
    }

    public void disuse() {
        Date now = new Date();
        trainMapper.updateDisuse(now);
    }
    public int selectReservationCntByMemberId(Integer memberId){
        return trainMapper.selectReservationCntByMemberId(memberId);
    }
    public List<String> getAllTranStations() {
        return this.trainMapper.selectTrainStations();
    }
    public List<Integer> getAllTrainNos(){
        return this.trainMapper.selectTrainNos();
    }
}
