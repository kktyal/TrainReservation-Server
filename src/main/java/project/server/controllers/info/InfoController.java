package project.server.controllers.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.Utils;
import project.server.SessionConst;
import project.server.controllers.MyController;
import project.server.entities.info.BoardEntity;
import project.server.entities.info.EnquiryEntity;
import project.server.enums.CommonResult;
import project.server.services.board.InfoService;
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
    public String test(){
        return "hello";
    }

    @ResponseBody
    @PostMapping("/board/select")
    public String selectAll(){
        List<BoardEntity> selectAll = infoService.findBoards();
        return Utils.getJsonObject(CommonResult.SUCCESS,selectAll).toString();
    }
    @ResponseBody
    @PostMapping("/board/select/search")
    public String selectBySearch(@RequestBody BoardVo vo){
        List<BoardEntity> selectAll = infoService.findBySearch(vo.getSearch());
        return Utils.getJsonObject(CommonResult.SUCCESS,selectAll).toString();
    }
    @ResponseBody
    @PostMapping("/board/select/index")
    public String selectByIndex(@RequestBody BoardVo vo){
        List<BoardEntity> selectByIndex = infoService.findByIndex(vo.getIndex());
        return Utils.getJsonObject(CommonResult.SUCCESS,selectByIndex).toString();
    }




    //문의사항 전체 조회
    @ResponseBody
    @PostMapping("/enquiry/select")
    public String enquirySelectAll(){
        List<EnquiryVo> result = infoService.findEnquiryAll();
        return Utils.getJsonObject(CommonResult.SUCCESS,result).toString();
    }

    //문의사항 memberId 조회 todo : 세션 검증해서 로그인 없으면 no-session
    @ResponseBody
    @PostMapping("/enquiry/select/memberId")
    public String enquirySelectByMemberId(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        MemberVo memberVo = (MemberVo) session.getAttribute(SessionConst.LOGIN_MEMBER);

        List<EnquiryVo> result = infoService.findEnquiryByMemberId(memberVo.getId());
        return Utils.getJsonObject(CommonResult.SUCCESS,result).toString();
    }
//    @ResponseBody
//    @PostMapping("/answer/exist")
//    public String isAnswerExist(@RequestBody EnquiryEntity entity){
//        infoService.isExist(entity.getIndex());
//
//    }



}
