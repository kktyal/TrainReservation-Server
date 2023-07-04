package project.server.controllers.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.Utils;
import project.server.SessionConst;
import project.server.controllers.MyController;
import project.server.entities.info.AnswerEntity;
import project.server.entities.info.BoardEntity;
import project.server.entities.info.EnquiryEntity;
import project.server.enums.CommonResult;
import project.server.enums.SessionAuthorizedResult;
import project.server.enums.interfaces.IResult;
import project.server.lang.Pair;
import project.server.services.info.InfoService;
import project.server.vos.info.BoardVo;
import project.server.vos.info.EnquiryVo;
import project.server.vos.member.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/info")
public class InfoController extends MyController {

    @Autowired
    private InfoService infoService;

    @ResponseBody
    @GetMapping("/test")
    public String test() {
        return "hello";
    }

    @ResponseBody
    @PostMapping("/board/select")
    public String selectAll() {
        List<BoardEntity> selectAll = infoService.findBoards();
        return Utils.getJsonObject(CommonResult.SUCCESS, selectAll).toString();
    }

    @ResponseBody
    @PostMapping("/board/select/search")
    public String selectBySearch(@RequestBody BoardVo vo) {
        List<BoardEntity> selectAll = infoService.findBySearch(vo.getSearch());
        return Utils.getJsonObject(CommonResult.SUCCESS, selectAll).toString();
    }

    @ResponseBody
    @PostMapping("/board/select/index")
    public String selectByIndex(@RequestBody BoardVo vo) {
        List<BoardEntity> selectByIndex = infoService.findByIndex(vo.getIndex());
        return Utils.getJsonObject(CommonResult.SUCCESS, selectByIndex).toString();
    }


    //문의사항 조회, memberId 있으면 사용자 문의사항 조회, 없으면 전체 조회 todo : 세션 검증해서 로그인 없으면 no-session
    @ResponseBody
    @PostMapping("/enquiry/select")
    public String enquirySelect(HttpServletRequest request) {

        // 세션 있으면
        if (isSession(request)) return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        HttpSession session = request.getSession(false);
        MemberVo memberVo  = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (isAdmin(memberVo)) {
            memberVo.setId(null);
        }

        List<EnquiryVo> result = infoService.findEnquiry(memberVo);
        return Utils.getJsonObject(CommonResult.SUCCESS, result).toString();
    }

    @ResponseBody
    @PostMapping("/enquiry/select/search")
    public String enquirySelectBySearch(@RequestBody EnquiryVo input,HttpServletRequest request) {
        if (isSession(request)) return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        HttpSession session = request.getSession(false);
        MemberVo memberVo  = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (!isAdmin(memberVo)) {
            input.setId(memberVo.getId());
        }

        List<EnquiryVo> result = infoService.findEnquiryBySearch(input);
        return Utils.getJsonObject(CommonResult.SUCCESS, result).toString();
    }

    @ResponseBody
    @PostMapping("/enquiry/select/index")
    public String enquirySelectByIndex(@RequestBody EnquiryEntity input) {
        List<EnquiryVo> result = infoService.findEnquiryByIndex(input.getIndex());
        return Utils.getJsonObject(CommonResult.SUCCESS, result).toString();
    }

    @ResponseBody
    @PostMapping("/answer/select/index")
    public String answerSelectByIndex(@RequestBody AnswerEntity input) {
        List<AnswerEntity> result = infoService.findAnswerByIndex(input.getEnquiryIndex());
        return Utils.getJsonObject(CommonResult.SUCCESS, result).toString();
    }


    @ResponseBody
    @PostMapping("/enquiry/insert")
    public String enquiryInsert(@RequestBody EnquiryEntity input, HttpServletRequest request) {
        if (isSession(request)) return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);

        input.setAuthor(memberVo.getId());

        Pair<Enum<? extends IResult>, Integer> result = infoService.enquiryInsert(input);
        return Utils.getJsonObject(result.getKey()).toString();
    }

    @ResponseBody
    @PostMapping("/answer/insert")
    public String answerInsert(@RequestBody AnswerEntity input, HttpServletRequest request) {
        if (isSession(request)) return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (!isAdmin(memberVo)) return Utils.getJsonObject(SessionAuthorizedResult.NO_ADMIN_SESSION).toString();
        input.setAuthor(memberVo.getId());


        Pair<Enum<? extends IResult>, Integer> result = infoService.answerInsert(input);

        return Utils.getJsonObject(result.getKey()).toString();
    }

    @ResponseBody
    @PostMapping("/answer/update")
    public String answerUpdate(@RequestBody AnswerEntity input, HttpServletRequest request) {
        if (isSession(request)) return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (!isAdmin(memberVo)) return Utils.getJsonObject(SessionAuthorizedResult.NO_ADMIN_SESSION).toString();
        input.setAuthor(memberVo.getId());

        Pair<Enum<? extends IResult>, Integer> result = infoService.answerUpdate(input);
        return Utils.getJsonObject(result.getKey()).toString();
    }

    @ResponseBody
    @PostMapping("/answer/delete")
    public String answerDelete(@RequestBody AnswerEntity input, HttpServletRequest request) {
        if (isSession(request)) return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (!isAdmin(memberVo)) return Utils.getJsonObject(SessionAuthorizedResult.NO_ADMIN_SESSION).toString();

        Pair<Enum<? extends IResult>, Integer> result = infoService.answerDelete(input);
        return Utils.getJsonObject(result.getKey()).toString();
    }


    private static boolean isAdmin(MemberVo memberVo) {
        return memberVo.getIsAdmin().equals(1);
    }

    private boolean isSession(HttpServletRequest request) {
        return !authorizedLoginSession(request).equals(Utils.getJsonObject(CommonResult.SUCCESS).toString());
    }


}
