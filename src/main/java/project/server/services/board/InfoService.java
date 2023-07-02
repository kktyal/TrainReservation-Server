package project.server.services.board;

import org.springframework.stereotype.Service;
import project.server.entities.info.BoardEntity;
import project.server.entities.info.EnquiryEntity;
import project.server.mappers.info.IInfoMapper;
import project.server.vos.info.EnquiryVo;

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

    public List<EnquiryVo> findEnquiryAll(){
        return boardMapper.selectEnquiryAll();
    }
    public List<EnquiryVo> findEnquiryByMemberId(int memberId){
        return boardMapper.selectEnquiryByMemberId(memberId);
    }

//    public int isExist(int index){
//        return boardMapper.selectAnswer(index);
//    }
}
