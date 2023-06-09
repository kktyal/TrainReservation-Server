package project.server.entities.member;

import lombok.Data;

import java.util.Date;

@Data
public class MemberAuthCodeEntity {
    private String email;
    private String authCode;
    private Date createdOn;
    private Date expiresOn;
    private boolean isExpired;
}
