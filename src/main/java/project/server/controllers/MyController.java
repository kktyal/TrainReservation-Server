package project.server.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import project.server.utils.Utils;

import project.server.utils.SessionConst;
import project.server.entities.member.MemberEntity;
import project.server.enums.CommonResult;
import project.server.enums.SessionAuthorizedResult;
import project.server.vos.train.CntVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MyController {

    @ResponseBody
    @PostMapping("/authorized/member")
    public String authorizedLoginSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if(session == null){

            return Utils.getJsonObject(SessionAuthorizedResult.NO_SESSION).toString();
        }

        Object temp = null;
        try {
            temp = session.getAttribute(SessionConst.LOGIN_MEMBER);
        } catch (IllegalStateException e) {

            return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_SESSION_EXPIRED).toString();
        }
        if (temp == null) {

            return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_NO_SESSION).toString();
        }
        if (!(temp instanceof MemberEntity)) {

            return Utils.getJsonObject(SessionAuthorizedResult.MEMBER_SESSION_EXPIRED).toString();
        }

        return  Utils.getJsonObject(CommonResult.SUCCESS).toString();

    }

    @ResponseBody
    @PostMapping("/authorized/cnt")
    public String authorizedCntSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if(session == null){

            return Utils.getJsonObject(SessionAuthorizedResult.NO_SESSION).toString();
        }

        Object temp = null;
        try {
            temp = session.getAttribute(SessionConst.CNT);
        } catch (IllegalStateException e) {

            return Utils.getJsonObject(SessionAuthorizedResult.CNT_SESSION_EXPIRED).toString();
        }
        if (temp == null) {

            return Utils.getJsonObject(SessionAuthorizedResult.CNT_NO_SESSION).toString();
        }
        if (!(temp instanceof CntVo)) {

            return Utils.getJsonObject(SessionAuthorizedResult.CNT_SESSION_EXPIRED).toString();
        }

        System.out.println(((CntVo) temp).sum());
        return ((CntVo) temp).sum()!=0
                ? Utils.getJsonObject(CommonResult.SUCCESS).toString()
                : Utils.getJsonObject(SessionAuthorizedResult.CNT_SESSION_EXPIRED).toString();
    }



}
