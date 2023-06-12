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
import project.server.entities.train.TrainTimeEntity;
import project.server.services.train.TrainService;
import project.server.vos.member.MemberVo;
import project.server.vos.train.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/train")
public class TrainController {

    @Autowired
    private TrainService trainService;

    @ResponseBody
    @PostMapping("/test")
    public ReservationEntity test(){
        return trainService.test().get();
    }

    @ResponseBody
    @PostMapping("/inquiry")
    public String inquiry(@RequestBody InquiryVo inquiryVo, HttpServletRequest request) throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConst.CNT,inquiryVo);

        List<ApiVo> api = trainService.api(inquiryVo);
        JSONObject object = new JSONObject();
        object = api.size() != 0 ? object.put("result", "success") : object.put("result", "fail");
        object.put("data",api);

        return object.toString();
    }

    @ResponseBody
    @PostMapping("/time")
    public String time(@RequestBody TrainTimeVo time){
        List<TrainTimeVo> trainTimeEntities = trainService.selectTime(time.getTrainNo());
        JSONObject object = new JSONObject();
        object = trainTimeEntities.size() != 0 ? object.put("result", "success") : object.put("result", "fail");
        object.put("data",trainTimeEntities);
        return object.toString();
    }
    @ResponseBody
    @PostMapping("/charge")
    public String charge(@RequestBody TrainChargeVo charge){
        Optional<TrainChargeEntity> trainChargeVo = trainService.selectCharge(charge);
        JSONObject object = new JSONObject();
        object = trainChargeVo.isEmpty()  ? object.put("result", "fail") : object.put("result", "success");
        object.put("data",trainChargeVo.get());
        return object.toString();

    }
    @ResponseBody
    @PostMapping("/reservation")
    public String reservation(@RequestBody List<ReservationVo> reservationVo, HttpServletRequest request){
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);
        InquiryVo inquiryVo = (InquiryVo) session.getAttribute(SessionConst.CNT);
        int cnt = inquiryVo.getOld()+inquiryVo.getAdult()+inquiryVo.getKid();



        for (ReservationVo vo : reservationVo) {
            vo.setMemberId(memberVo.getId());
        }
        int reservation = trainService.reservation(reservationVo);

        System.out.println("memberVo.getId() = " + memberVo.getId());


        JSONObject object = new JSONObject();

        if(cnt != reservationVo.size()){
            object.put("result","inputFail");
            return object.toString();
        }
        object = reservation==0  ? object.put("result", "fail") : object.put("result", "success");

        return object.toString();
    }



}
