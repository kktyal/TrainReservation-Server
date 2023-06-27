package project.server.controllers.train;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.Utils;
import project.server.ServerApplication;
import project.server.SessionConst;
import project.server.controllers.MyController;
import project.server.entities.train.ReservationEntity;
import project.server.entities.train.TrainChargeEntity;
import project.server.enums.CommonResult;
import project.server.enums.SessionAuthorizedResult;
import project.server.enums.interfaces.IResult;
import project.server.enums.trainResult.ReservationResult;
import project.server.lang.Pair;
import project.server.services.train.TrainService;
import project.server.validators.train.TrainValidator;
import project.server.vos.member.MemberVo;
import project.server.vos.train.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;


@Controller
@RequestMapping("/train")
public class TrainController extends MyController {

    @Autowired
    private TrainService trainService;

//    @ResponseBody
//    @PostMapping("/test")
//    public ReservationEntity test() {
//        return trainService.test().get();
//    }

    //Input : 기차 조회 하기
    //Output : 기차 조회 결과
    @ResponseBody
    @PostMapping("/inquiry")
    public String inquiry(@RequestBody CntVo cntVo, HttpServletRequest request) throws Throwable {

        if (!ServerApplication.trainStations.contains(cntVo.getDepartStation()) ||
                !ServerApplication.trainStations.contains(cntVo.getArriveStation()) ||
                !TrainValidator.DATE.matches(cntVo.getDate()) ||
                !TrainValidator.CNT.matches(cntVo.getAdult().toString()) ||
                !TrainValidator.CNT.matches(cntVo.getChild().toString()) ||
                !TrainValidator.CNT.matches(cntVo.getOld().toString())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConst.CNT, cntVo);

        Pair<Enum<? extends IResult>, List<ApiVo>> api = trainService.api(cntVo);

        return Utils.getJsonObject(api.getKey(), api.getValue()).toString();
    }

    // input : 기차 시간 조회
    // output : 기차 시간 조회결과
    @ResponseBody
    @PostMapping("/time")
    public String time(@RequestBody TrainTimeVo time) {
        if (!ServerApplication.trainNos.contains(time.getTrainNo())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, List<?>> result = trainService.selectTime(time.getTrainNo());
        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }

    // input : 기차 요금 조회
    // output : 기차 요금 조회결과
    @ResponseBody
    @PostMapping("/charge")
    public String charge(@RequestBody TrainChargeVo charge) {

        if (!ServerApplication.trainStations.contains(charge.getDepartStation())||
                !ServerApplication.trainStations.contains(charge.getArriveStation())){

            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, TrainChargeEntity> result = trainService.selectCharge(charge);

        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();

    }

    // input : 기차 자리 조회
    // output : 기차 자리 조회결과
    @ResponseBody
    @PostMapping("/seat")
    public String seat(@RequestBody ReservationVo vo) {
        //검증
        if(!ServerApplication.trainNos.contains(vo.getTrainNo())||
        !TrainValidator.DATE.matches(vo.getDate())||
        !TrainValidator.TIME.matches(vo.getArriveTime())||
        !TrainValidator.TIME.matches(vo.getDepartTime())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, List<ReservationVo>> result = trainService.selectSeat(vo);

        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }


    //기차 예매하기
    @ResponseBody
    @PostMapping("/reservation")
    public String reservation(@RequestBody List<ReservationVo> reservationVo, HttpServletRequest request) {

        for (ReservationVo vo : reservationVo) {
            if(!ServerApplication.trainNos.contains(vo.getTrainNo())||
            !ServerApplication.trainStations.contains(vo.getArriveStation())||
            !ServerApplication.trainStations.contains(vo.getDepartStation())||
            !TrainValidator.TIME.matches(vo.getDepartTime())||
            !TrainValidator.TIME.matches(vo.getArriveTime())||
            !TrainValidator.DATE.matches(vo.getDate())||
            !TrainValidator.CARRIAGE.matches(vo.getCarriage().toString())||
            !TrainValidator.SEAT.matches(vo.getSeat())){
                return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
            }
        }


        //세션 검사
        if (!authorizedCntSession(request).equals(Utils.getJsonObject(CommonResult.SUCCESS).toString())) {
            return Utils.getJsonObject(SessionAuthorizedResult.CNT_NO_SESSION).toString();
        }
        if (!authorizedLoginSession(request).equals(Utils.getJsonObject(CommonResult.SUCCESS).toString())) {
            return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        }


        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);
        CntVo cntVo = (CntVo) session.getAttribute(SessionConst.CNT);

        //인원수 안맞을 때
        int cnt = cntVo.getOld() + cntVo.getAdult() + cntVo.getChild();
        if (cnt != reservationVo.size()) {
            return Utils.getJsonObject(ReservationResult.NO_MATCH_CNT).toString();
        }
        for (ReservationVo vo : reservationVo) {
            vo.setMemberId(memberVo.getId());
        }

        Pair<Enum<? extends IResult>, String> result = trainService.reservation(reservationVo, cntVo);

        if (result.getKey().equals(ReservationResult.SEAT_DUPLICATED)) {
            return Utils.getJsonObject(result.getKey()).toString();
        }

        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }
    //승차권 환불
    @ResponseBody
    @PostMapping("/reservation/payment/refund")
    public String paymentRefund(@RequestBody ReservationVo inputVo) {

        if(!TrainValidator.RESERVATIONID.matches(inputVo.getReservationId())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, Integer> result = trainService.refund(inputVo);
        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }
    // 예약 취소
    @ResponseBody
    @PostMapping("/reservation/cancel")
    public String reservationCancel(@RequestBody ReservationVo inputVo) {

        if(!TrainValidator.RESERVATIONID.matches(inputVo.getReservationId())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, Integer> result = trainService.cancel(inputVo);
        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }
    //결제하기
    @ResponseBody
    @PostMapping("/reservation/payment")
    public String reservationCancel(@RequestBody ReservationEntity reservationId) {

        if(!TrainValidator.RESERVATIONID.matches(reservationId.getReservationId())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, Integer> result = trainService.payment(reservationId);

        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }

    //예약내역 페이지
    @ResponseBody
    @PostMapping("/reservation/detail")
    public String reservationDetail(@RequestBody ReservationEntity entity) {

        if(!TrainValidator.RESERVATIONID.matches(entity.getReservationId())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Pair<Enum<? extends IResult>, List<?>> result = trainService.reservationDetail(entity);
        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }


    //승차권 환불 페이지
    @ResponseBody
    @PostMapping("/reservation/payment/refund/page")
    public String reservationRefundPage(@RequestBody ReservationVo inputVo) {

        if(!TrainValidator.RESERVATIONID.matches(inputVo.getReservationId())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, List<?>> result = trainService.reservationRefundPage(inputVo);
        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }

    //발권 승차권 페이지
    @ResponseBody
    @PostMapping("/ticket")
    public String ticketDetail(@RequestBody ReservationVo inputVo) {

        if(!TrainValidator.RESERVATIONID.matches(inputVo.getReservationId())){
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, List<?>> result = trainService.ticketDetail(inputVo);
        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();
    }

    //승차권 확인 페이지
    @ResponseBody
    @PostMapping("/reservation/list")
    public String reservation(HttpServletRequest request) {

        if (!authorizedLoginSession(request).equals(Utils.getJsonObject(CommonResult.SUCCESS).toString())) {
            return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        }

        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);

        Pair<Enum<? extends IResult>, List<?>> result = trainService.selectReservationListsByMemberId(memberVo.getId());


        return Utils.getJsonObject(result.getKey(), result.getValue()).toString();

    }



}
