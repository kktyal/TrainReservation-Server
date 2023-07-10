package project.server.services.info;

import org.springframework.stereotype.Service;
import project.server.utils.Utils;
import project.server.entities.info.AnswerEntity;
import project.server.entities.info.BoardEntity;
import project.server.entities.info.EnquiryEntity;
import project.server.enums.info.AnswerInsertResult;
import project.server.enums.interfaces.IResult;
import project.server.utils.lang.Pair;
import project.server.mappers.info.IInfoMapper;
import project.server.vos.info.AnswerVo;
import project.server.vos.info.EnquiryVo;
import project.server.vos.member.MemberVo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class InfoService {
    private final IInfoMapper infoMapper;//변수로 만들때 I는 빼고 만드는게 국룰

    public InfoService(IInfoMapper boardMapper) {
        this.infoMapper = boardMapper;
    }

    public List<BoardEntity> findBoards() {
        return infoMapper.selectAll();
    }

    public List<BoardEntity> findBySearch(String search) {
        return infoMapper.selectBySearch(search);
    }

    public List<BoardEntity> findByIndex(int index) {
        return infoMapper.selectByIndex(index);
    }


    public List<EnquiryVo> findEnquiry(MemberVo member) {
        return infoMapper.selectEnquiry(member);
    }

    public List<EnquiryVo> findEnquiryBySearch(EnquiryVo input) {

        return infoMapper.selectEnquiryBySearch(input);
    }


    public List<EnquiryVo> findEnquiryByIndex(int index) {
        return infoMapper.selectEnquiryByIndex(index);
    }

    public List<AnswerVo> findAnswerByIndex(int index) {
        return infoMapper.selectAnswerByIndex(index);
    }

    public Pair<Enum<? extends IResult>, Integer> enquiryInsert(EnquiryEntity input) {
        Date date = new Date();
        input.setCreateDate(date);
        int result = infoMapper.insertEnquiry(input);
        return Utils.getIngegerPair(result);
    }

    public Pair<Enum<? extends IResult>, Integer> answerInsert(AnswerEntity input) {
        Optional<AnswerEntity> isAnswered = infoMapper.isAnswered(input.getEnquiryIndex());
        if (isAnswered.isPresent()) {
            return new Pair<>(AnswerInsertResult.COMPLETED_ANSWER, null);
        }

        Date date = new Date();
        input.setCreateDate(date);

        int answer = infoMapper.insertAnswer(input);
        return Utils.getIngegerPair(answer);
    }

    public Pair<Enum<? extends IResult>, Integer> answerUpdate(AnswerEntity input) {
        Optional<AnswerEntity> isAnswered = infoMapper.isAnswered(input.getEnquiryIndex());
        if (isAnswered.isEmpty()) {
            return new Pair<>(AnswerInsertResult.NO_ANSWER, null);
        }
        Date date = new Date();
        input.setUpdateDate(date);
        int result = infoMapper.updateAnswer(input);
        return Utils.getIngegerPair(result);
    }

    public Pair<Enum<? extends IResult>, Integer> answerDelete(AnswerEntity input) {
        Optional<AnswerEntity> isAnswered = infoMapper.isAnswered(input.getEnquiryIndex());
        if (isAnswered.isEmpty()) {
            return new Pair<>(AnswerInsertResult.NO_ANSWER, null);
        }
        int result = infoMapper.deleteAnswer(input);
        return Utils.getIngegerPair(result);
    }


}
