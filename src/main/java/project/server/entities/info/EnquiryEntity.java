package project.server.entities.info;

import lombok.Data;

import java.util.Date;

@Data
public class EnquiryEntity {
    private int index;
    private String title;
    private String content;
    private Date createDate;
    private int author;
}
