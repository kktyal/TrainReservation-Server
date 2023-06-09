package project.server.mappers.member;

import org.apache.ibatis.annotations.Mapper;
import project.server.entities.member.MemberAuthCodeEntity;
import project.server.vos.member.MemberVo;


import java.util.Optional;

@Mapper
public interface IMemberMapper {
    int test();
    int insertAuthCode(MemberAuthCodeEntity memberAuthCodeEntity);
    Optional<MemberAuthCodeEntity> matchEmailCode (String email);
    Optional<MemberVo> findById(int id);
    Optional<MemberVo> findByEmail(String email);
    Optional<MemberVo> findByPhone(String phone);
    Optional<MemberVo> findByEmailAndId(MemberVo memberVo);
    int save(MemberVo memberVo);
    int updatePw(MemberVo memberVo);
    int updateEmailAuth(MemberAuthCodeEntity memberAuthCodeEntity);
}
