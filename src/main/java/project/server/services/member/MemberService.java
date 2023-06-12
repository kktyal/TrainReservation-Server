package project.server.services.member;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import project.Utils;
import project.server.entities.member.MemberAuthCodeEntity;
import project.server.lang.Pair;
import project.server.mappers.member.IMemberMapper;
import project.server.vos.member.MemberVo;

import javax.mail.MessagingException;

import java.time.Year;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class MemberService {

    private final IMemberMapper memberMapper;//변수로 만들때 I는 빼고 만드는게 국룰


    public MemberService(IMemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    public int test(){
        return memberMapper.test();
    }
    //이메일 보내기
    public String emailSend(MemberAuthCodeEntity memberAuthCodeEntity){
        //암호 코드 생성
        memberAuthCodeEntity.setAuthCode(Utils.createAuthCode());

        //이메일 보내기
        Utils.EmailSender emailSender = new Utils.EmailSender();
        try {
            emailSender.sendEmail("smtp.gmail.com", "587", "cks12369@gmail.com", "nmldsxpagfkwsnwu",
                    memberAuthCodeEntity.getEmail(), "Test Subject", "암호는" + memberAuthCodeEntity.getAuthCode() + "입니다.");
        } catch (MessagingException e) {
            // 에러 처리
            return "SendError";
        }

        Date createdOn = new Date();
        Date expiresOn = DateUtils.addMinutes(createdOn, 5);
        System.out.println("expiresOn = " + expiresOn);
        memberAuthCodeEntity.setCreatedOn(createdOn);
        memberAuthCodeEntity.setExpiresOn(expiresOn);
        memberAuthCodeEntity.setExpired(false);

        int result = memberMapper.insertAuthCode(memberAuthCodeEntity);



        return result != 0 ? "success" : "DataBaseError";

    }
    //이메일 인증 비교
    public Pair<String,Integer> matchEmailCodeAndGiveId(MemberAuthCodeEntity memberAuthCodeEntity){
        Optional<MemberAuthCodeEntity> selectVo = memberMapper.matchEmailCode(memberAuthCodeEntity.getEmail());
        Pair<String, Integer> pair = new Pair("","");
        if(selectVo.isEmpty()){
            pair.setKey("EmailFail");
            return pair;
        }else if(selectVo.get().getExpiresOn().compareTo(new Date()) < 0){
            selectVo.get().setExpired(true);
            memberMapper.updateEmailAuth(selectVo.get());
            pair.setKey("expired");
            return pair;

        }else if(selectVo.get().getAuthCode().equals(memberAuthCodeEntity.getAuthCode())){
            Optional<MemberVo> user = memberMapper.findByEmail(memberAuthCodeEntity.getEmail());
            pair.setKey("success");
            pair.setValue(user.get().getId());
            return pair;
        }else{
            pair.setKey("CodeFail");
            return pair;
        }

    }


    //이메일 인증 비교
    public String matchEmailCode(MemberAuthCodeEntity memberAuthCodeEntity){
        Optional<MemberAuthCodeEntity> selectVo = memberMapper.matchEmailCode(memberAuthCodeEntity.getEmail());

        if(selectVo.isEmpty()){
            return "EmailFail";
        }else if(selectVo.get().getExpiresOn().compareTo(new Date()) < 0){
            selectVo.get().setExpired(true);
            memberMapper.updateEmailAuth(selectVo.get());
            return "expired";

        }else if(selectVo.get().getAuthCode().equals(memberAuthCodeEntity.getAuthCode())){
            Optional<MemberVo> user = memberMapper.findByEmail(memberAuthCodeEntity.getEmail());

            return "success";
        }else{
            return "CodeFail";
        }

    }

    //회원가입
    public String register(MemberVo memberVo){

        //회원번호 생성
        while (true) {
            int currentYear = Year.now().getValue() % 10000;
            Random random = new Random();
            int randomDigits = random.nextInt(100000);
            int randomNumber = (currentYear * 100000) + randomDigits;


            //중복체크
            MemberVo vo = new MemberVo();
            vo.setId(randomNumber);
            String result = isDuplicationId(vo);

            if (result.equals("Not Duplication")) {
                memberVo.setId(randomNumber);
                break;
            }
        }
        //비밀번호 암호화
        memberVo.setPw(Utils.hashSha512(memberVo.getPw()));

        int result = memberMapper.save(memberVo);
        return result != 0 ? "success" : "DataBaseError";
    }

    //폰 중복확인
    public String isDuplicationPhone(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findByPhone(memberVo.getPhone());
        if(selectVo.isEmpty()){
            return "Not Duplication";
        }else {
            return "Duplication";
        }

    }
    //이메일 중복확인
    public String isDuplicationEmail(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findByEmail(memberVo.getEmail());
        if(selectVo.isEmpty()){
            return "Not Duplication";
        }else {
            return "Duplication";
        }

    }
    //폰 중복확인
    public String isDuplicationId(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findById(memberVo.getId());
        if(selectVo.isEmpty()){
            return "Not Duplication";
        }else {
            return "Duplication";
        }

    }


    // 비번 번경 전 아이디와 이메일 유무 확인
    public String existEmailAndId(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findByEmailAndId(memberVo);
        if(selectVo.isEmpty()){
            return "Not Exist";
        }else {
            return "Exist";
        }
    }

    public String updatePw(MemberVo memberVo){
        memberVo.setPw(Utils.hashSha512(memberVo.getPw()));
        int result = memberMapper.updatePw(memberVo);
        return result != 0 ? "success" : "DataBaseError";
    }

    public Pair<String, MemberVo> login(MemberVo memberVo){
        memberVo.setPw(Utils.hashSha512(memberVo.getPw()));


        Pair<String, MemberVo> pair = new Pair<>("",null);

        Optional<MemberVo> selectVo;
        if(memberVo.getId()!=null){
            selectVo  = memberMapper.findById(memberVo.getId());
        }else if (memberVo.getPhone()!=null) {
            selectVo = memberMapper.findByPhone(memberVo.getPhone());
        }else if(memberVo.getEmail()!=null) {
            selectVo = memberMapper.findByEmail(memberVo.getEmail());
        }else {
            pair.setKey("Bad Request");
            return pair;
        }
        if (selectVo.isEmpty()){
            pair.setKey("No Id");
            return pair;
        }


        if (selectVo.get().getPw().equals(memberVo.getPw())) {
            pair.setKey("success");
            pair.setValue(selectVo.get());
        } else {
            pair.setKey("fail");
        }
        return pair;

    }

}
