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
import project.server.lang.Pair;
import project.server.services.member.MemberService;
import project.server.vos.member.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

@Slf4j
@Controller
@RequestMapping("/member")
public class MemberController {

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
        String result = memberService.emailSend(memberAuthCodeEntity);
        JSONObject object = getJsonObject(result);
        return object.toString();
    }

    //비밀번호 번경전 (email과 id 존재유무)
    @ResponseBody
    @PostMapping("updatePw/exist")
    public String selectEmailAndId(@RequestBody MemberVo memberVo) {
        String result = memberService.existEmailAndId(memberVo);
        JSONObject object = getJsonObject(result);
        return object.toString();
    }

    //회원가입 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/register/matchEmailCode")
    public String emailCheckByRegister(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity, HttpServletRequest request) throws SQLException {

        String result = memberService.matchEmailCode(memberAuthCodeEntity);
        JSONObject object = getJsonObject(result);
      // 이메일 세션 안함!!
//        if (result.equals("success")) {
//            // 세션이 있으면 세션 반환, 세션이 없으면 신규 세션 생성
//            HttpSession session = request.getSession(true);
//            // 세션에 값 삽입
//            session.setAttribute(SessionConst.EMAIL_AUTH, result);
//        }
        return object.toString();
    }

    //아이디 찾기 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/findId/matchEmailCode")
    public String emailCheckByFindId(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity, HttpServletRequest request) throws SQLException {

        String result = memberService.matchEmailCode(memberAuthCodeEntity);
        JSONObject object = getJsonObject(result);
        // 이메일 세션 안함!!
//        if (result.equals("success")) {
//            // 세션이 있으면 세션 반환, 세션이 없으면 신규 세션 생성
//            HttpSession session = request.getSession(true);
//            // 세션에 값 삽입
//            session.setAttribute(SessionConst.EMAIL_AUTH, result);
//        }
        return object.toString();
    }

    //비밀번호 변경 이메일 인증 코드 확인
    @ResponseBody
    @PostMapping("/updatePw/matchEmailCode")
    public String emailCheckByupdatePw(@RequestBody MemberAuthCodeEntity memberAuthCodeEntity, HttpServletRequest request) throws SQLException {

        String result = memberService.matchEmailCode(memberAuthCodeEntity);
        JSONObject object = getJsonObject(result);
        // 이메일 세션 안함!!
//        if (result.equals("success")) {
//            // 세션이 있으면 세션 반환, 세션이 없으면 신규 세션 생성
//            HttpSession session = request.getSession(true);
//            // 세션에 값 삽입
//            session.setAttribute(SessionConst.EMAIL_AUTH, result);
//        }
        return object.toString();
    }


    //회원가입
    @ResponseBody
    @PostMapping("/register")
    public String register(@RequestBody MemberVo memberVo) throws  SQLException {
        log.info("hello");
        String result = memberService.register(memberVo);
        JSONObject object = getJsonObject(result);
        return object.toString();
    }

    @ResponseBody
    @PostMapping("/isDuplication/phone")
    public String isDuplicationPhone(@RequestBody MemberVo memberVo) throws SQLException {
        // 중복 확인
        String result = memberService.isDuplicationPhone(memberVo);
        // json 성공 실패 여부 반환, 성공시 login 세션 생성
        JSONObject object = getJsonObject(result);
        return object.toString();
    }

    @ResponseBody
    @PostMapping("/isDuplication/email")
    public String isDuplicationEmail(@RequestBody MemberVo memberVo) throws SQLException {
        // 중복 확인
        String result = memberService.isDuplicationEmail(memberVo);
        // json 성공 실패 여부 반환, 성공시 login 세션 생성
        JSONObject object = getJsonObject(result);
        return object.toString();
    }

    //비번 변경
    @ResponseBody
    @PostMapping("/updatePw/applyNewPw")
    public String updatePw(@RequestBody MemberVo memberVo) throws SQLException {

        String result = memberService.updatePw(memberVo);
        // json 성공 실패 여부 반환, 성공시 login 세션 생성
        JSONObject object = getJsonObject(result);
        return object.toString();
    }
    //로그인
    @ResponseBody
    @PostMapping("/login")
    public String login(@RequestBody MemberVo memberVo, HttpServletRequest request) throws SQLException {

        Pair<String, MemberVo> login = memberService.login(memberVo);
        if(login.getKey().equals("success")){
            HttpSession session = request.getSession(true);
            login.getValue().setPw("");
            session.setAttribute(SessionConst.LOGIN_MEMBER,login.getValue());
        }
        System.out.println(login.getValue().getId());
        // json 성공 실패 여부 반환, 성공시 login 세션 생성
        JSONObject object = getJsonObject(login.getKey());
        return object.toString();
    }







    private static JSONObject getJsonObject(String result) {
        JSONObject object = new JSONObject();
        object.put("result", result);
        return object;
    }
}
