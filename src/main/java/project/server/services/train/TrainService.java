package project.server.services.train;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.jdbc.Null;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.server.entities.train.*;
import project.server.enums.CommonResult;
import project.server.enums.interfaces.IResult;
import project.server.enums.trainResult.TrainResult;
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

    public Optional<ReservationEntity> test() {
        LocalDate currentDate = LocalDate.now();
        UUID uuid = UUID.randomUUID();
        String random = currentDate.toString().concat(uuid.toString());
        System.out.println(random);
        return trainMapper.selectReservationId("20230613");
    }

    public int cancel(ReservationVo inputVo) {
        return trainMapper.updateCancelToReservation(inputVo.getReservationId());
    }

    public int refund(ReservationVo inputVo) {
        ReservationEntity reservation = trainMapper.selectReservationId(inputVo.getReservationId()).get();
        return trainMapper.updateRefundToPayment(reservation.getPaymentId());
    }

    @Transactional
    public int payment(ReservationEntity reservationEntity) {

        PaymentEntity payment = new PaymentEntity();

        // 총 금액을 알기 위해 ticket에서 age, price 조회
        int totalPrice = 0;
        List<TicketEntity> ticketEntities = trainMapper.selectTicketByReservationId(reservationEntity.getReservationId());
        for (TicketEntity ticketEntity : ticketEntities) {
            if (ticketEntity.getAge().equals("adult")) {
                totalPrice += ticketEntity.getPrice();
            } else if (ticketEntity.getAge().equals("old")) {
                totalPrice += (int) ticketEntity.getPrice() * 0.8;
            } else {
                totalPrice += (int) ticketEntity.getPrice() * 0.7;
            }
        }
        payment.setTotalPrice((int) Math.floor(totalPrice / 100) * 100);

        // paymentid 세팅
        String paymentId;
        do {
            paymentId = createId();
        } while (!trainMapper.selectPaymentId(paymentId).isEmpty());
        payment.setPaymentId(paymentId);
        System.out.println("paymentId = " + paymentId);
        System.out.println("totalPrice = " + totalPrice);

        int savePaymentResult = trainMapper.savePayment(payment);

        reservationEntity.setPaymentId(paymentId);
        int updateReservationPaymentIdResult = trainMapper.updateReservationPaymentId(reservationEntity);
        if (savePaymentResult == 1 && updateReservationPaymentIdResult == 1) {
            return 1;
        } else return 0;


    }

    public String createId() {
        LocalDate currentDate = LocalDate.now();
        UUID uuid = UUID.randomUUID();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String result = currentDate.format(formatter).concat(uuid.toString()).replaceAll("-", "");

        return result;
    }

    @Transactional
    public Pair<String, Integer> reservation(List<ReservationVo> reservationVo, CntVo count) {

        Pair<String, Integer> pair = new Pair<>(null, null);
        //검증
        for (ReservationVo vo : reservationVo) {
            if (trainMapper.duplicatedTicket(vo).size() != 0) {
                pair.setValue(-1);
                return pair;
            }
        }

        // reservation_id 생성
        String reservationId;
        do {
            reservationId = createId();
        } while (!trainMapper.selectReservationId(reservationId).isEmpty());
        // 만료 날짜 생성
        Date expiredDate = new Date();
        expiredDate = DateUtils.addMinutes(expiredDate, 10);

        //예약
        ReservationVo vo = reservationVo.get(0);
        ReservationEntity reservation = new ReservationEntity();
        vo.setReservationId(reservationId);
        vo.setExpiredDate(expiredDate);


        vo.setDisuse(false);
        int reservationCnt = trainMapper.saveReservation(vo);


        TrainChargeVo charge = new TrainChargeVo();
        charge.setArriveName(vo.getArriveStation());
        charge.setDepartName(vo.getDepartStation());


        //티켓
        TrainChargeEntity trainChargeEntity = selectCharge(charge).getValue();

        int adultCnt = count.getAdult();
        int oldCnt = count.getOld();
        int kidCnt = count.getKid();


        int ticketCnt = 0;
        for (ReservationVo seat : reservationVo) {
            String ticketId;
            do {
                ticketId = createId();
            } while (!trainMapper.selectTicketId(ticketId).isEmpty());

            TicketEntity ticket = new TicketEntity();
            ticket.setTicketId(ticketId);
            ticket.setTrainNo(seat.getTrainNo());
            ticket.setCarriage(seat.getCarriage());
            ticket.setSeat(seat.getSeat());
            ticket.setReservationId(reservationId);
            if (ticket.getCarriage() == 2) {
                ticket.setPrice(trainChargeEntity.getChargeVip());
                if (adultCnt > 0) {
                    ticket.setAge("adult");

                    adultCnt--;
                } else if (oldCnt > 0) {
                    ticket.setAge("old");

                    oldCnt--;
                } else {
                    ticket.setAge("kid");

                }
            } else {
                ticket.setPrice(trainChargeEntity.getCharge());
                if (adultCnt > 0) {
                    ticket.setAge("adult");

                    adultCnt--;
                } else if (oldCnt > 0) {
                    ticket.setAge("old");

                    oldCnt--;
                } else {
                    ticket.setAge("kid");

                }
            }
            ticketCnt += trainMapper.saveTicket(ticket);
        }
        pair.setKey(reservationId);
        pair.setValue(reservationCnt + ticketCnt);
        return pair;
    }

    public List<ReservationVo> reservationRefundPage(ReservationEntity input) {
        List<ReservationVo> resultVo = trainMapper.showReservation(input.getReservationId());
        System.out.println("inputVo.getReservationId() = " + input.getReservationId());
        for (ReservationVo vo : resultVo) {
            vo.setTicketId(null);
            vo.setTrainNo(null);
        }
        return resultVo;
    }

    public List<ReservationVo> reservationDetail(ReservationEntity input) {
        List<ReservationVo> resultVo = trainMapper.showReservation(input.getReservationId());
        System.out.println("inputVo.getReservationId() = " + input.getReservationId());
        for (ReservationVo vo : resultVo) {
            vo.setTicketId(null);
        }
        return resultVo;
    }

    public List<ReservationVo> ticketDetail(ReservationVo inputVo) {
        System.out.println("inputVo.getR = " + inputVo.getReservationId());
        return trainMapper.showReservation(inputVo.getReservationId());

    }

    @Transactional
    public List<ReservationVo> selectReservationListsByMemberId(Integer memberId) {
        disuse();
        return trainMapper.selectReservationByMemberId(memberId);
    }

    public Pair<Enum<? extends IResult>,TrainChargeEntity> selectCharge(TrainChargeVo chargeVo) {
        Pair<Enum<? extends IResult>, TrainChargeEntity> pair = new Pair<>(null, null);
        chargeVo.setDepart(transferStationName(chargeVo.getDepartName()).getIndex());
        chargeVo.setArrive(transferStationName(chargeVo.getArriveName()).getIndex());
        Optional<TrainChargeEntity> result = trainMapper.selectCharge(chargeVo);
        if(result.isEmpty()){
            pair.setKey(TrainResult.NO_SUCH_ELEMENT);
        }
        pair.setKey(CommonResult.SUCCESS);
        pair.setValue(result.get());
        return pair;

    }

    public Pair<Enum<? extends IResult>, List<TrainTimeVo>> selectTime(int srtNo) {
        Pair<Enum<? extends IResult>, List<TrainTimeVo>> pair = new Pair<>(null, null);
        List<TrainTimeVo> trainTimeEntities = trainMapper.selectTime(srtNo);
        if(trainTimeEntities.size()==0){
            pair.setKey(TrainResult.NO_SUCH_ELEMENT);
        }
        pair.setKey(CommonResult.SUCCESS);
        pair.setValue(trainTimeEntities);
        return pair;
    }

    @Transactional
    public List<ReservationVo> selectSeat(ReservationVo vo) {
        disuse();
        ApiVo apiVo = new ApiVo();
        apiVo.setTrainno(vo.getTrainNo());
        apiVo.setDate(vo.getDate());
        apiVo.setDepplandtime(vo.getDepartTime());
        apiVo.setArrplandtime(vo.getArriveTime());
        return trainMapper.findSoldSeat(apiVo);
    }

    public int disuse() {
        Date now = new Date();
        return trainMapper.updateDisuse(now);
    }


    @Transactional
    public Pair<Enum<? extends IResult>, List<ApiVo>> api(CntVo cntVo) throws IOException {
        Pair<Enum<? extends IResult>, List<ApiVo>> pair = new Pair<>(CommonResult.SUCCESS, null);
        List<ApiVo> api = getApi(cntVo);
//        try {
//            api = getApi(cntVo);
//        } catch (Exception e) {
//           throw new IOException();
//        }

        if (api.size() == 0) {
            pair.setKey(TrainResult.NO_SUCH_ELEMENT);
            return pair;
        }

        int result = disuse();

        for (ApiVo apiVo : api) {
            int common = 0;
            int vip = 0;
            List<ReservationVo> soldSeat = trainMapper.findSoldSeat(apiVo);

            for (ReservationVo reservationVo : soldSeat) {
                if (reservationVo.getCarriage() != 2)
                    common++;
                else
                    vip++;
            }
            if (cntVo.getOld() + cntVo.getKid() + cntVo.getAdult() + vip >= 9) {
                apiVo.setVip(true);
            }
            if (cntVo.getOld() + cntVo.getKid() + cntVo.getAdult() + common >= 20) {
                apiVo.setCommon(true);
            }
        }

        pair.setValue(api);
        return pair;
    }

    public TrainStationEntity transferStationName(String stationName) {
        System.out.println(stationName);
        return trainMapper.findByName(stationName).get();
    }

    public List<ApiVo> getApi(CntVo cntVo) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/TrainInfoService/getStrtpntAlocFndTrainInfo"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=ovMDJk4e%2BY2bRcCR4qJTYDuTlnmFIMOPMmZsPd1rbUJylcSo%2FUyXnFaJWPu1yt4M1ZdnTfH20zVHD91u9HQt1Q%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("20", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*데이터 타입(xml, json)*/
        urlBuilder.append("&" + URLEncoder.encode("depPlaceId", "UTF-8") + "=" + URLEncoder.encode(transferStationName(cntVo.getDepartStation()).getStationCode(), "UTF-8")); /*출발기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
        urlBuilder.append("&" + URLEncoder.encode("arrPlaceId", "UTF-8") + "=" + URLEncoder.encode(transferStationName(cntVo.getArriveStation()).getStationCode(), "UTF-8")); /*도착기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
        urlBuilder.append("&" + URLEncoder.encode("depPlandTime", "UTF-8") + "=" + URLEncoder.encode(cntVo.getDate(), "UTF-8")); /*출발일(YYYYMMDD)*/
        urlBuilder.append("&" + URLEncoder.encode("trainGradeCode", "UTF-8") + "=" + URLEncoder.encode("17", "UTF-8")); /*차량종류코드*/
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


            resultVo.add(apiVo);
        }
        return resultVo;
    }
}
