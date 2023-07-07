package project.server.services.member;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import project.Utils;
import project.server.entities.member.MemberAuthCodeEntity;
import project.server.enums.CommonResult;
import project.server.enums.DataBaseResult;
import project.server.enums.member.*;
import project.server.enums.interfaces.IResult;
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
    public Enum<? extends IResult> emailSend(MemberAuthCodeEntity memberAuthCodeEntity){

        //암호 코드 생성
        String[] split = memberAuthCodeEntity.getEmail().split("@");

        if (split[1].equals(Utils.ADMIN)) {
            memberAuthCodeEntity.setAuthCode("admin000");
        } else {
            memberAuthCodeEntity.setAuthCode(Utils.createAuthCode());
        }



        //이메일 보내기
        Utils.EmailSender emailSender = new Utils.EmailSender();
        try {
            emailSender.sendEmail("smtp.gmail.com", "587", "cks12369@gmail.com", "nmldsxpagfkwsnwu",
                    memberAuthCodeEntity.getEmail(), "Test Subject", "암호는" + memberAuthCodeEntity.getAuthCode() + "입니다.");
        } catch (MessagingException e) {
            // 에러 처리
            return EmailSendResult.NOT_SENDING_EMAIL;
        }

        Date createdOn = new Date();
        Date expiresOn = DateUtils.addMinutes(createdOn, 5);
        System.out.println("expiresOn = " + expiresOn);
        memberAuthCodeEntity.setCreatedOn(createdOn);
        memberAuthCodeEntity.setExpiresOn(expiresOn);
        memberAuthCodeEntity.setExpired(false);

        int result = memberMapper.insertAuthCode(memberAuthCodeEntity);

        return result != 0 ? CommonResult.SUCCESS : DataBaseResult.DATABASE_INSERT_ERROR;

    }
    //이메일 인증 비교 후 아이디 주기
    public Pair<Enum<? extends IResult>,Integer> matchEmailCodeAndGiveId(MemberAuthCodeEntity memberAuthCodeEntity){
        Optional<MemberAuthCodeEntity> selectVo = memberMapper.matchEmailCode(memberAuthCodeEntity.getEmail());
        Pair<Enum<? extends IResult>, Integer> pair = new Pair("","");
        if(selectVo.isEmpty()){
            pair.setKey(MatchAuthCodeResult.EMAIL_NO_MATCH);
            return pair;
        }else if(selectVo.get().getExpiresOn().compareTo(new Date()) < 0){
            selectVo.get().setExpired(true);
            memberMapper.updateEmailAuth(selectVo.get());
            pair.setKey(MatchAuthCodeResult.AUTH_CODE_EXPIRED);
            return pair;

        }else if(selectVo.get().getAuthCode().equals(memberAuthCodeEntity.getAuthCode())){
            Optional<MemberVo> user = memberMapper.findByEmail(memberAuthCodeEntity.getEmail());
            if (user.isEmpty()){
                pair.setKey(ExistEmailAndId.NO_REGISTER);
                return pair;
            }else{
                pair.setKey(CommonResult.SUCCESS);
                pair.setValue(user.get().getId());
                return pair;
            }

        }else{
            pair.setKey(DataBaseResult.DATABASE_SELECT_ERROR);
            return pair;
        }

    }


    //이메일 인증 비교
    public Enum<? extends IResult> matchAuthCode(MemberAuthCodeEntity memberAuthCodeEntity){
        Optional<MemberAuthCodeEntity> selectVo = memberMapper.matchEmailCode(memberAuthCodeEntity.getEmail());

        if(selectVo.isEmpty()){
            return MatchAuthCodeResult.EMAIL_NO_MATCH;
        }else if(selectVo.get().getExpiresOn().compareTo(new Date()) < 0){
            selectVo.get().setExpired(true);
            memberMapper.updateEmailAuth(selectVo.get());
            return MatchAuthCodeResult.AUTH_CODE_EXPIRED;

        }else if(selectVo.get().getAuthCode().equals(memberAuthCodeEntity.getAuthCode())){
            Optional<MemberVo> user = memberMapper.findByEmail(memberAuthCodeEntity.getEmail());

            return CommonResult.SUCCESS;
        }else{
            return DataBaseResult.DATABASE_SELECT_ERROR;
        }

    }

    //회원가입
    public Enum<? extends IResult> register(MemberVo memberVo){

        //회원번호 생성
        while (true) {
            int currentYear = Year.now().getValue() % 10000;
            Random random = new Random();
            int randomDigits = random.nextInt(100000);
            int randomNumber = (currentYear * 100000) + randomDigits;


            //중복체크
            MemberVo vo = new MemberVo();
            vo.setId(randomNumber);
            Enum<? extends IResult> result = isDuplicationId(vo);

            if (result.equals(CommonResult.SUCCESS)) {
                memberVo.setId(randomNumber);
                break;
            }
        }
        if(memberMapper.findByEmail(memberVo.getEmail()).isPresent()){
            return IsDuplicated.EMAIL_DUPLICATED;
        }
        if(memberMapper.findByPhone(memberVo.getPhone()).isPresent()){
            return IsDuplicated.PHONE_DUPLICATED;
        }
        String[] split = memberVo.getEmail().split("@");
        System.out.println("split[1] = " + split[1]);
        if(split[1].equals(Utils.ADMIN)){
            memberVo.setIsAdmin(1);
        }

        //비밀번호 암호화
        memberVo.setPw(Utils.hashSha512(memberVo.getPw()));

        int result = memberMapper.save(memberVo);
        return result != 0 ? CommonResult.SUCCESS : DataBaseResult.DATABASE_INSERT_ERROR;
    }

    //폰 중복확인
    public Enum<? extends IResult> isDuplicationPhone(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findByPhone(memberVo.getPhone());
        if(selectVo.isEmpty()){
            return CommonResult.SUCCESS;
        }else {
            return IsDuplicated.PHONE_DUPLICATED;
        }

    }
    //이메일 중복확인
    public Enum<? extends IResult> isDuplicationEmail(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findByEmail(memberVo.getEmail());
        if(selectVo.isEmpty()){
            return CommonResult.SUCCESS;
        }else {
            return IsDuplicated.EMAIL_DUPLICATED;
        }

    }
    //폰 중복확인
    public Enum<? extends IResult> isDuplicationId(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findById(memberVo.getId());
        if(selectVo.isEmpty()){
            return CommonResult.SUCCESS;
        }else {
            return IsDuplicated.ID_DUPLICATED;
        }
    }


    // 비번 번경 전 아이디와 이메일 유무 확인
    public Enum<? extends IResult> existEmailAndId(MemberVo memberVo){
        Optional<MemberVo> selectVo = memberMapper.findByEmailAndId(memberVo);
        if(selectVo.isEmpty()){
            return ExistEmailAndId.NO_EXIST;
        }else {
            return CommonResult.SUCCESS;
        }
    }

    public Enum<? extends IResult> updatePw(MemberVo memberVo){
        memberVo.setPw(Utils.hashSha512(memberVo.getPw()));
        int result = memberMapper.updatePw(memberVo);
        return result != 0 ? CommonResult.SUCCESS : DataBaseResult.DATABASE_UPDATE_ERROR;
    }

    public Pair<Enum<? extends IResult>, MemberVo> login(MemberVo memberVo){
        memberVo.setPw(Utils.hashSha512(memberVo.getPw()));

        Pair<Enum<? extends IResult>, MemberVo> pair = new Pair<>(null,null);

        Optional<MemberVo> selectVo;
        if(memberVo.getId()!=null){
            selectVo  = memberMapper.findById(memberVo.getId());
        }else if (memberVo.getPhone()!=null) {
            selectVo = memberMapper.findByPhone(memberVo.getPhone());
        }else if(memberVo.getEmail()!=null) {
            selectVo = memberMapper.findByEmail(memberVo.getEmail());
        }else {
            pair.setKey(LoginResult.ID_NO_MATCH);
            return pair;
        }
        if (selectVo.isEmpty()){
            pair.setKey(LoginResult.ID_NO_MATCH);
            return pair;
        }

        if (selectVo.get().getPw().equals(memberVo.getPw())) {
            pair.setKey(CommonResult.SUCCESS);
            selectVo.get().setPw("");
            pair.setValue(selectVo.get());
        } else {
            pair.setKey(LoginResult.PASSWORD_NO_MATCH);
        }
        return pair;

    }


}
