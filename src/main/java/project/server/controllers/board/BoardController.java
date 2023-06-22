package project.server.controllers.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import project.Utils;
import project.server.controllers.MyController;
import project.server.entities.board.BoardEntity;
import project.server.enums.CommonResult;
import project.server.services.board.BoardService;
import project.server.vos.board.BoardVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("board")
public class BoardController  extends MyController {

    @Autowired
    private BoardService boardService;

    @ResponseBody
    @PostMapping("select")
    public String selectAll(){
        List<BoardEntity> selectAll = boardService.findBoards();
        return Utils.getJsonObject(CommonResult.SUCCESS,selectAll).toString();
    }
    @ResponseBody
    @PostMapping("select/search")
    public String selectBySearch(@RequestBody BoardVo vo, HttpServletResponse response){
        List<BoardEntity> selectAll = boardService.findBySearch(vo.getSearch());
        return Utils.getJsonObject(CommonResult.SUCCESS,selectAll).toString();
    }
    @ResponseBody
    @PostMapping("select/index")
    public String selectByIndex(@RequestBody BoardVo vo){
        List<BoardEntity> selectByIndex = boardService.findByIndex(vo.getIndex());

        return Utils.getJsonObject(CommonResult.SUCCESS,selectByIndex).toString();
    }



}
