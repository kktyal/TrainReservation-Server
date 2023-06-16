package project.server.controllers.member;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import project.server.SessionConst;
import project.server.entities.member.MemberAuthCodeEntity;
import project.server.enums.CommonResult;
import project.server.enums.interfaces.IResult;
import project.server.lang.Pair;
import project.server.services.member.MemberService;
import project.server.vos.member.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static project.Validator.isValidInteger;
import static project.Validator.isValidString;

@Slf4j
@Controller
@RequestMapping("/member")
public class MemberController {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private MemberService memberService;

    @ResponseBody
    @PostMapping("/test")
    public int test() {
        return memberService.test();
    }

    //이메일 보내기
    @ResponseBody
    @PostMapping("emailSend")
    public String emailSend(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity) {

        if (!isValidString(memberAuthCodeEntity.getEmail())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        Enum<? extends IResult> result = memberService.emailSend(memberAuthCodeEntity);
        return getJsonObject(result).toString();
    }


    //비밀번호 번경전 (email과 id 존재유무)
    @ResponseBody
    @PostMapping("updatePw/exist")
    public String selectEmailAndId(@RequestBody MemberVo memberVo) {
//        if(!Validator.isValid(memberVo, "id")) {
//            return getJsonObject(CommonResult.INPUT_ERROR).toString();
//        }
//        if(!Validator.isValid(memberVo, "email")) {
//            return getJsonObject(CommonResult.INPUT_ERROR).toString();
//        }
        if (!isValidString(memberVo.getEmail()) || !isValidInteger(memberVo.getId())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Enum<? extends IResult> result = memberService.existEmailAndId(memberVo);
        return getJsonObject(result).toString();
    }


    //회원가입 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/register/matchEmailCode")
    public String emailCheckByRegister(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity) {
        if (!isValidString(memberAuthCodeEntity.getEmail()) || !isValidString(memberAuthCodeEntity.getAuthCode())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }


        Enum<? extends IResult> result = memberService.matchAuthCode(memberAuthCodeEntity);
        return getJsonObject(result).toString();
    }

    //아이디 찾기 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/findId/matchEmailCode")
    public String emailCheckByFindId(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity) {

        if (!isValidString(memberAuthCodeEntity.getEmail()) || !isValidString(memberAuthCodeEntity.getAuthCode())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, Integer> pair = memberService.matchEmailCodeAndGiveId(memberAuthCodeEntity);
        JSONObject object = getJsonObject(pair.getKey());

        if (pair.getValue() != null) {
            object.put("data", pair.getValue());
        }
        return object.toString();
    }

    //비밀번호 변경 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/updatePw/matchEmailCode")
    public String emailCheckByUpdatePw(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity) {

        if (!isValidString(memberAuthCodeEntity.getEmail()) || !isValidString(memberAuthCodeEntity.getAuthCode())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Enum<? extends IResult> result = memberService.matchAuthCode(memberAuthCodeEntity);
        return getJsonObject(result).toString();
    }


    //회원가입
    @ResponseBody
    @PostMapping("/register")
    public String register(@RequestBody MemberVo memberVo) {

        if (!isValidString(memberVo.getEmail()) || !isValidString(memberVo.getPhone())
                || !isValidString(memberVo.getPw()) || !isValidString(memberVo.getName())
                || !isValidString(memberVo.getGender()) || !isValidString(memberVo.getBirth())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Enum<? extends IResult> result = memberService.register(memberVo);
        return getJsonObject(result).toString();
    }

    @ResponseBody
    @PostMapping("/isDuplication/phone")
    public String isDuplicationPhone(@RequestBody MemberVo memberVo) {

        if (!isValidString(memberVo.getPhone())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        // 중복 확인
        Enum<? extends IResult> result = memberService.isDuplicationPhone(memberVo);
        // json 성공 실패 여부 반환, 성공시 login 세션 생성

        return getJsonObject(result).toString();
    }

    @ResponseBody
    @PostMapping("/isDuplication/email")
    public String isDuplicationEmail(@RequestBody MemberVo memberVo) {

        if (!isValidString(memberVo.getEmail())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        // 중복 확인
        Enum<? extends IResult> result = memberService.isDuplicationEmail(memberVo);
        // json 성공 실패 여부 반환, 성공시 login 세션 생성
        return getJsonObject(result).toString();
    }

    //비번 변경
    @ResponseBody
    @PostMapping("/updatePw/applyNewPw")
    public String updatePw(@RequestBody MemberVo memberVo) {

        if (!isValidInteger(memberVo.getId()) || !isValidString(memberVo.getPhone())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Enum<? extends IResult> result = memberService.updatePw(memberVo);
        // json 성공 실패 여부 반환, 성공시 login 세션 생성
        return getJsonObject(result).toString();
    }

    //로그인
    @ResponseBody
    @PostMapping("/login")
    public String login(@RequestBody MemberVo memberVo, HttpServletRequest request) {

        if (!isValidInteger(memberVo.getId()) && !isValidString(memberVo.getPhone()) && !isValidString(memberVo.getEmail())) {
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }
        if(!isValidString(memberVo.getPw())){
            return getJsonObject(CommonResult.INPUT_ERROR).toString();
        }

        Pair<Enum<? extends IResult>, MemberVo> result = memberService.login(memberVo);
        if (result.getKey().equals(CommonResult.SUCCESS)) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SessionConst.LOGIN_MEMBER, result.getValue());
        }

        // json 성공 실패 여부 반환, 성공시 login 세션 생성
        return getJsonObject(result.getKey()).toString();
    }



    private static JSONObject getJsonObject(Enum<? extends IResult> result) {
        JSONObject object = new JSONObject();

        if (result.equals(CommonResult.SUCCESS)) {
            object.put("result", result.name().toLowerCase());
        } else {
            object.put("result", CommonResult.FAILURE.name().toLowerCase());
            object.put("message", result.name().toLowerCase());
        }
        return object;
    }
}
