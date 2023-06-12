package project.server.services.train;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.server.entities.train.ReservationEntity;
import project.server.entities.train.TicketEntity;
import project.server.entities.train.TrainChargeEntity;
import project.server.entities.train.TrainStationEntity;
import project.server.mappers.train.ITrainMapper;
import project.server.vos.train.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

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
        return trainMapper.duplicatedMemberId("20230613");
    }

    public String createId() {
        LocalDate currentDate = LocalDate.now();
        UUID uuid = UUID.randomUUID();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String result = currentDate.format(formatter).concat(uuid.toString()).replaceAll("-","");;
        return result;
    }


    @Transactional
    public int reservation(List<ReservationVo> reservationVo) {
        // reservation_id 생성
        String reservationId;
        do {
            reservationId = createId();
        } while (!trainMapper.duplicatedMemberId(reservationId).isEmpty());
        // 만료 날짜 생성
        Date date = new Date();
        date = DateUtils.addMinutes(date, 10);

        ReservationVo vo = reservationVo.get(0);
        ReservationEntity reservation = new ReservationEntity();
        reservation.setReservationId(reservationId);
        reservation.setDepartTime(vo.getDepartTime());
        reservation.setDepartStation(vo.getDepartStation());
        reservation.setArriveTime(vo.getArriveTime());
        reservation.setArriveStation(vo.getArriveStation());
        reservation.setDate(vo.getDate());
        reservation.setExpiredDate(date);
        reservation.setMemberId(vo.getMemberId());
        System.out.println("vo.getMemberId() = " + vo.getMemberId());
        System.out.println("reservation.getMemberId() = " + reservation.getMemberId());
        reservation.setDisuse(false);
        int reservationCnt = trainMapper.saveReservation(reservation);
        int ticketCnt = 0;
        for (ReservationVo seat : reservationVo) {
            String ticketId;
            do {
                ticketId = createId();
            } while (!trainMapper.duplicatedTicketId(ticketId).isEmpty());

            TicketEntity ticket = new TicketEntity();
            ticket.setTicketId(ticketId);
            ticket.setTrainNo(seat.getTrainNo());
            ticket.setCarriage(seat.getCarriage());
            ticket.setSeat(seat.getSeat());
            ticket.setReservationId(reservationId);
            ticketCnt += trainMapper.saveTicket(ticket);
        }

        return reservationCnt+ticketCnt;
    }

    public Optional<TrainChargeEntity> selectCharge(TrainChargeVo chargeVo) {
        chargeVo.setDepart(transferStationName(chargeVo.getDepartName()).getIndex());
        chargeVo.setArrive(transferStationName(chargeVo.getArriveName()).getIndex());

        return trainMapper.selectCharge(chargeVo);

    }


    public List<TrainTimeVo> selectTime(int srtNo) {
        List<TrainTimeVo> trainTimeEntities = trainMapper.selectTime(srtNo);
        System.out.println("trainTimeEntities.size() = " + trainTimeEntities.size());


        return trainTimeEntities;
    }

    public int disuse() {
        Date now = new Date();
        return trainMapper.updateDisuse(now);
    }

    @Transactional
    public List<ApiVo> api(InquiryVo inquiryVo) throws IOException {
        List<ApiVo> api = getApi(inquiryVo);
        int result = disuse();
        System.out.println("result = " + result);
        //todo: 매진 좌석 선택
        for (ApiVo apiVo : api) {
            int common = 0;
            int vip = 0;
            List<ReservationVo> soldSeat = trainMapper.findSoldSeat(apiVo);
            System.out.println(soldSeat.size());
            for (ReservationVo reservationVo : soldSeat) {
                if (reservationVo.getCarriage() != 2)
                    common++;
                else
                    vip++;
            }
            System.out.println("common = " + common);
            System.out.println("vip = " + vip);
            if (inquiryVo.getOld() + inquiryVo.getKid() + inquiryVo.getAdult() + vip >= 9) {
                apiVo.setVip(true);
            }
            if (inquiryVo.getOld() + inquiryVo.getKid() + inquiryVo.getAdult() + common >= 20) {
                apiVo.setCommon(true);
            }
        }


        return api;
    }

    public TrainStationEntity transferStationName(String stationName) {
        System.out.println(stationName);
        return trainMapper.findByName(stationName).get();
    }

    public List<ApiVo> getApi(InquiryVo inquiryVo) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/TrainInfoService/getStrtpntAlocFndTrainInfo"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=ovMDJk4e%2BY2bRcCR4qJTYDuTlnmFIMOPMmZsPd1rbUJylcSo%2FUyXnFaJWPu1yt4M1ZdnTfH20zVHD91u9HQt1Q%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("20", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*데이터 타입(xml, json)*/
        urlBuilder.append("&" + URLEncoder.encode("depPlaceId", "UTF-8") + "=" + URLEncoder.encode(transferStationName(inquiryVo.getDepartStationName()).getStationCode(), "UTF-8")); /*출발기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
        urlBuilder.append("&" + URLEncoder.encode("arrPlaceId", "UTF-8") + "=" + URLEncoder.encode(transferStationName(inquiryVo.getArriveStationName()).getStationCode(), "UTF-8")); /*도착기차역ID [상세기능3. 시/도별 기차역 목록조회]에서 조회 가능*/
        urlBuilder.append("&" + URLEncoder.encode("depPlandTime", "UTF-8") + "=" + URLEncoder.encode(inquiryVo.getFullDate().substring(0, 8), "UTF-8")); /*출발일(YYYYMMDD)*/
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
