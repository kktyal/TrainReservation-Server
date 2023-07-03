package project.server.services.board;

import org.springframework.stereotype.Service;
import project.server.entities.info.AnswerEntity;
import project.server.entities.info.BoardEntity;
import project.server.entities.info.EnquiryEntity;
import project.server.mappers.info.IInfoMapper;
import project.server.vos.info.EnquiryVo;
import project.server.vos.member.MemberVo;

import java.util.Date;
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


    public List<EnquiryVo> findEnquiry(MemberVo member){
        return boardMapper.selectEnquiry(member);
    }
    public List<EnquiryVo> findEnquiryBySearch (String input){

        return boardMapper.selectEnquiryBySearch(input);
    }
    public List<EnquiryVo> findEnquiryByIndex (int index){
        return boardMapper.selectEnquiryByIndex(index);
    }
    public List<AnswerEntity> findAnswerByIndex (int index){
        return boardMapper.selectAnswerByIndex(index);
    }
    public int enquiryInsert(int id, EnquiryEntity input){

        Date date = new Date();
        input.setCreateDate(date);
        input.setAuthor(id);
        return boardMapper.insertEnquiry(input);
    }


}
