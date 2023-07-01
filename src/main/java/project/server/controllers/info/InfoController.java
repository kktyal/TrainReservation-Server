package project.server.controllers.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.Utils;
import project.server.controllers.MyController;
import project.server.entities.info.BoardEntity;
import project.server.enums.CommonResult;
import project.server.services.board.InfoService;
import project.server.vos.info.BoardVo;

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





}
