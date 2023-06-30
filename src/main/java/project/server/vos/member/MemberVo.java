package project.server.vos.member;

import lombok.Data;
import project.server.entities.member.MemberEntity;

import java.util.Date;

@Data
public class MemberVo extends MemberEntity {
    private String authCode;
    private Date createdOn;
    private Date expiresOn;
    private boolean isExpired;
    private int reservationCnt;
}
