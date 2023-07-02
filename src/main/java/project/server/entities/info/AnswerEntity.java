package project.server.entities.info;

import lombok.Data;

import java.util.Date;

@Data
public class AnswerEntity {
    private int index;
    private int enquiryIndex;
    private String answer;
    private Date createDate;
    private Date updateDate;
    private int author;
    private boolean isDelete;
}
