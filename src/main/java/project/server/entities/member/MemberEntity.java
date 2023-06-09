package project.server.entities.member;

import lombok.Data;

@Data
public class MemberEntity {
    private Integer id;
    private String email;
    private String phone;
    private String pw;
    private String name;
    private String gender;
    private String birth;
}
