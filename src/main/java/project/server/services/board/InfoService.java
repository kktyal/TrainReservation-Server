package project.server.services.board;

import org.springframework.stereotype.Service;
import project.server.entities.info.BoardEntity;
import project.server.mappers.info.IInfoMapper;

import java.util.List;

@Service
public class InfoService {
    private final IInfoMapper boardMapper;//변수로 만들때 I는 빼고 만드는게 국룰

    public InfoService(IInfoMapper boardMapper) {
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
