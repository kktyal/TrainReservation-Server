package project.server.controllers.train;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import project.server.SessionConst;
import project.server.entities.train.ReservationEntity;
import project.server.entities.train.TrainChargeEntity;
import project.server.lang.Pair;
import project.server.services.train.TrainService;
import project.server.vos.member.MemberVo;
import project.server.vos.train.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/train")
public class TrainController {

    @Autowired
    private TrainService trainService;


    @ResponseBody
    @PostMapping("/test")
    public ReservationEntity test() {
        return trainService.test().get();
    }


    //Input : 기차 조회 하기
    //Output : 기차 조회 결과
    @ResponseBody
    @PostMapping("/inquiry")
    public String inquiry(@RequestBody InquiryVo inquiryVo, HttpServletRequest request) throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConst.CNT, inquiryVo);

        List<ApiVo> api = trainService.api(inquiryVo);
        JSONObject object = new JSONObject();
        object = api.size() != 0 ? object.put("result", "success") : object.put("result", "fail");
        object.put("data", api);

        return object.toString();
    }

    // input : 기차 시간 조회
    // output : 기차 시간 조회결과
    @ResponseBody
    @PostMapping("/time")
    public String time(@RequestBody TrainTimeVo time) {
        List<TrainTimeVo> trainTimeEntities = trainService.selectTime(time.getTrainNo());
        JSONObject object = new JSONObject();
        object = trainTimeEntities.size() != 0 ? object.put("result", "success") : object.put("result", "fail");
        object.put("data", trainTimeEntities);
        return object.toString();
    }

    // input : 기차 요금 조회
    // output : 기차 요금 조회결과
    @ResponseBody
    @PostMapping("/charge")
    public Map<String, Object> charge(@RequestBody TrainChargeVo charge) {
        Optional<TrainChargeEntity> trainChargeVo = trainService.selectCharge(charge);
        JSONObject object = new JSONObject();
//        object = trainChargeVo.isEmpty()  ? object.put("result", "fail") : object.put("result", "success");
        Map<String, Object> result = new HashMap<>();
        result.put("result", trainChargeVo.isEmpty() ? "fail" : "success");
        result.put("data", trainChargeVo);
//        object.put("data",objectMapper.writeValueAsString(trainChargeVo).replaceAll((),""));
        return result;
    }

    // input : 기차 자리 조회
    // output : 기차 자리 조회결과
    @ResponseBody
    @PostMapping("/seat")
    public String seat(@RequestBody ReservationVo vo) {
        JSONObject object = new JSONObject();

        if (vo.getSeat() == null || vo.getDate() == null || vo.getArriveTime() == null || vo.getDepartTime() == null) {
            object.put("result", "fail");
            return object.toString();
        }
        List<ReservationVo> resultVo = trainService.selectSeat(vo);

        object.put("result", "success");
        object.put("data", resultVo);
        return object.toString();
    }


    //기차 예매하기
    @ResponseBody
    @PostMapping("/reservation")
    public String reservation(@RequestBody List<ReservationVo> reservationVo, HttpServletRequest request) {
        JSONObject object = new JSONObject();
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);
        InquiryVo inquiryVo = (InquiryVo) session.getAttribute(SessionConst.CNT);
        int cnt = inquiryVo.getOld() + inquiryVo.getAdult() + inquiryVo.getKid();
        if (cnt != reservationVo.size()) {
            object.put("result", "inputFail");
            return object.toString();
        }
        for (ReservationVo vo : reservationVo) {
            vo.setMemberId(memberVo.getId());
        }
        Pair<String, Integer> pair = trainService.reservation(reservationVo, inquiryVo);

        if (pair.getValue() == -1) {
            object.put("result", "seat is duplicated");
            return object.toString();
        }
        object.put("reservationId", pair.getKey());
        object.put("result", pair.getValue() == 0 ? "fail" : "success");
        return object.toString();
    }
    //예약내역 페이지
    @ResponseBody
    @PostMapping("/reservation/detail")
    public String reservationDetail(@RequestBody ReservationEntity entity){
        System.out.println("reservationVo.getReservationId() = " + entity.getReservationId());
        List<ReservationVo> resultVo = trainService.reservationDetail(entity);
        JSONObject object = new JSONObject();
        object.put("result", resultVo.size()== 0 ? "fail" : "success");
        object.put("data", resultVo);
        return object.toString();
    }

    //결제하기
    @ResponseBody
    @PostMapping("/reservation/payment")
    public String reservationCancel(@RequestBody ReservationEntity reservationId) {
        int payment = trainService.payment(reservationId);

        JSONObject object = new JSONObject();
        object.put("result", payment == 0 ? "fail" : "success");
        return object.toString();
    }

    //승차권 환불 페이지
    @ResponseBody
    @PostMapping("/reservation/payment/refund/page")
    public String reservationRefundPage(@RequestBody ReservationVo inputVo) {
        List<ReservationVo> resultVo = trainService.reservationRefundPage(inputVo);
        JSONObject object = new JSONObject();
        object.put("result", resultVo.size() == 0 ? "fail" : "success");
        object.put("data", resultVo);
        return object.toString();
    }
    //발권 승차권 페이지
    @ResponseBody
    @PostMapping("/ticket/detail")
    public String ticketDetail(@RequestBody ReservationVo inputVo) {
        List<ReservationVo> resultVo = trainService.ticketDetail(inputVo);
        JSONObject object = new JSONObject();
        object.put("result", resultVo.size() == 0 ? "fail" : "success");
        object.put("data", resultVo);
        return object.toString();
    }
    //승차권 확인 페이지
    @ResponseBody
    @PostMapping("/reservation/list")
    public String reservation(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);
        System.out.println("memberVo = " + memberVo.getId());

        List<ReservationVo> resultVo = trainService.selectReservationListsByMemberId(memberVo.getId());

        JSONObject object = new JSONObject();
        object.put("result", resultVo.size() == 0 ? "fail" : "success");
        object.put("data", resultVo);
        return object.toString();

    }
    //승차권 환불
    @ResponseBody
    @PostMapping("/reservation/payment/refund")
    public String paymentRefund(@RequestBody ReservationVo inputVo){
        int result = trainService.refund(inputVo);
        JSONObject object = new JSONObject();
        object.put("result", result == 0 ? "fail" : "success");
        return object.toString();
    }
    // 예약 취소
    @ResponseBody
    @PostMapping("/reservation/cancel")
    public String reservationCancel(@RequestBody ReservationVo inputVo){
        int result = trainService.cancel(inputVo);
        JSONObject object = new JSONObject();
        object.put("result", result == 0 ? "fail" : "success");
        return object.toString();
    }
}
