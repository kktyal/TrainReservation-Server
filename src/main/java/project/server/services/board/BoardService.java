package project.server.services.board;

import org.springframework.stereotype.Service;
import project.server.entities.board.BoardEntity;
import project.server.mappers.board.IBoardMapper;

import java.util.List;

@Service
public class BoardService {
    private final IBoardMapper boardMapper;//변수로 만들때 I는 빼고 만드는게 국룰

    public BoardService(IBoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    public List<BoardEntity> findBoards(){
        return boardMapper.selectAll();
    }
    public List<BoardEntity> findBySearch(String search){
        return boardMapper.selectBySearch(search);
    }
    public List<BoardEntity> findByIndex(int index){
        return boardMapper.selectByIndex(index);
    }


}
