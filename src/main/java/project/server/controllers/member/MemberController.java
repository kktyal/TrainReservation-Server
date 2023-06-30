package project.server.controllers.member;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.Utils;
import project.server.SessionConst;
import project.server.controllers.MyController;
import project.server.entities.member.MemberAuthCodeEntity;
import project.server.enums.CommonResult;
import project.server.enums.interfaces.IResult;
import project.server.lang.Pair;
import project.server.services.member.MemberService;
import project.server.services.train.TrainService;
import project.server.validators.member.MemberValidator;
import project.server.vos.member.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping("/member")
public class MemberController extends MyController {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private MemberService memberService;
    @Autowired
    private TrainService trainService;

    @ResponseBody
    @PostMapping("/test")
    public int test() {
        return memberService.test();
    }

    //이메일 보내기
    @ResponseBody
    @PostMapping("emailSend")
    public String emailSend(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity) {
        if (!MemberValidator.EMAIL.matches(memberAuthCodeEntity.getEmail())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.emailSend(memberAuthCodeEntity);
        return Utils.getJsonObject(result).toString();
    }

    //회원가입 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/matchEmailCode")
    public String emailCheckByRegister(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity) {
        if (!MemberValidator.EMAIL.matches(memberAuthCodeEntity.getEmail()) ||
                !MemberValidator.AUTH.matches(memberAuthCodeEntity.getAuthCode())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.matchAuthCode(memberAuthCodeEntity);
        return Utils.getJsonObject(result).toString();
    }

    //아이디 찾기 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/matchEmailCode/showMemberId")
    public String emailCheckByFindId(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity) {

        if (!MemberValidator.EMAIL.matches(memberAuthCodeEntity.getEmail()) ||
                !MemberValidator.AUTH.matches(memberAuthCodeEntity.getAuthCode())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Pair<Enum<? extends IResult>, Integer> pair = memberService.matchEmailCodeAndGiveId(memberAuthCodeEntity);
//        Map<String, Integer> data = new HashMap<>();



//
//        data.put("memberId", pair.getValue());
        return Utils.getJsonObject(pair.getKey(), pair.getValue()).toString();
    }


    //비밀번호 번경전 (email과 id 존재유무)
    @ResponseBody
    @PostMapping("/findEmailAndMemberId")
    public String selectEmailAndId(@RequestBody MemberVo memberVo) {
        if (!MemberValidator.EMAIL.matches(memberVo.getEmail()) ||
                !MemberValidator.ID.matches(memberVo.getId().toString())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.existEmailAndId(memberVo);
        return Utils.getJsonObject(result).toString();
    }


    //회원가입
    @ResponseBody
    @PostMapping("/register")
    public String register(@RequestBody MemberVo memberVo) {

        if (!MemberValidator.EMAIL.matches(memberVo.getEmail()) || !MemberValidator.PHONE.matches(memberVo.getPhone())
                || !MemberValidator.NAME.matches(memberVo.getName()) || !MemberValidator.GENDER.matches(memberVo.getGender())
                || !MemberValidator.BIRTH.matches(memberVo.getBirth()) ||!MemberValidator.PASSWORD.matches(memberVo.getPw())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.register(memberVo);
        return Utils.getJsonObject(result).toString();
    }

    @ResponseBody
    @PostMapping("/isDuplicated/phone")
    public String isDuplicationPhone(@RequestBody MemberVo memberVo) {

        if (!MemberValidator.PHONE.matches(memberVo.getPhone())) {
            System.out.println("memberVo.getPhone() = " + memberVo.getPhone());
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.isDuplicationPhone(memberVo);
        return Utils.getJsonObject(result).toString();
    }

    @ResponseBody
    @PostMapping("/isDuplicated/email")
    public String isDuplicationEmail(@RequestBody MemberVo memberVo) {
        if (!MemberValidator.EMAIL.matches(memberVo.getEmail())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.isDuplicationEmail(memberVo);
        return Utils.getJsonObject(result).toString();
    }

    //비번 변경
    @ResponseBody
    @PostMapping("/updatePassword")
    public String updatePw(@RequestBody MemberVo memberVo, HttpServletResponse response) {

        if (!MemberValidator.ID.matches(memberVo.getId().toString())||!MemberValidator.PASSWORD.matches(memberVo.getPw())) {
            return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.updatePw(memberVo);
        return Utils.getJsonObject(result).toString();
    }

    //로그인
    @ResponseBody
    @PostMapping("/login")
    public String login(@RequestBody MemberVo memberVo, HttpServletRequest request) {
        if (memberVo.getId() != null) {
            if (!MemberValidator.ID.matches(memberVo.getId().toString())) {
                return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
            }
        } else if (memberVo.getPhone() != null) {
            if (!MemberValidator.PHONE.matches(memberVo.getPhone())) {
                return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
            }
        } else if (memberVo.getEmail() != null) {
            if (!MemberValidator.EMAIL.matches(memberVo.getEmail())) {
                return Utils.getJsonObject(CommonResult.INPUT_ERROR).toString();
            }
        }
        Pair<Enum<? extends IResult>, MemberVo> result = memberService.login(memberVo);
        if (result.getKey().equals(CommonResult.SUCCESS)) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SessionConst.LOGIN_MEMBER, result.getValue());

            JSONObject jsonObject = new JSONObject();
            int reservationCnt = trainService.selectReservationCntByMemberId(memberVo.getId());
            jsonObject.put("memberName", result.getValue().getName());
            jsonObject.put("reservationCnt",reservationCnt);

            return Utils.getJsonObject(result.getKey(),jsonObject).toString();

        }





        return Utils.getJsonObject(result.getKey()).toString();
    }


}
