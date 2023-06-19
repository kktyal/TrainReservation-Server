package project.server.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import project.Utils;

import project.server.SessionConst;
import project.server.entities.member.MemberEntity;
import project.server.enums.CommonResult;
import project.server.enums.SessionAuthorizedResult;
import project.server.vos.member.MemberVo;
import project.server.vos.train.CntVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.NoSuchElementException;

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

            return Utils.getJsonObject(SessionAuthorizedResult.CNT_NO_SEESION).toString();
        }
        if (!(temp instanceof CntVo)) {

            return Utils.getJsonObject(SessionAuthorizedResult.CNT_SESSION_EXPIRED).toString();
        }

        System.out.println(((CntVo) temp).sum());
        return ((CntVo) temp).sum()!=0
                ? Utils.getJsonObject(CommonResult.SUCCESS).toString()
                : Utils.getJsonObject(SessionAuthorizedResult.CNT_SESSION_EXPIRED).toString();
    }



//    @ExceptionHandler()
//    public ResponseEntity<Object> SQLException(SQLException e) {
//
//        return exceptionReturn(e);
//
//    }
//    @ExceptionHandler()
//    public ResponseEntity<Object> NullPointerException(NullPointerException e) {
//
//        return exceptionReturn(e);
//
//    }
//    @ExceptionHandler()
//    public ResponseEntity<Object> NoSuchElementException(NoSuchElementException e) {
//
//        return exceptionReturn(e);
//
//    }
//    @ExceptionHandler()
//    public ResponseEntity<Object> Exception(Exception e) {
//
//        return exceptionReturn(e);
//
//    }
//
//    private static ResponseEntity<Object> exceptionReturn(Exception e) {
//        String msg = "SYS_ERR: " + e.getMessage();
//        StackTraceElement[] stack = e.getStackTrace();
//        String errorMethod = "";
//        for (StackTraceElement stackTraceElement : stack) {
//            final String clsName = stackTraceElement.getClassName();
//            if (!clsName.startsWith("project.server.services")) {
//                continue;
//            }
//            errorMethod = clsName + "." + stackTraceElement.getMethodName();
//            break;
//        }
//
//        JSONObject result = new JSONObject();
//        result.put("result", CommonResult.FAILURE.name().toLowerCase());
//        result.put("message", msg);
//        result.put("method", errorMethod);
//        result.put("error", e.getClass());
//
//        return new ResponseEntity<>(
//                result.toString(),
//                HttpStatus.BAD_REQUEST
//        );
//    }
}
