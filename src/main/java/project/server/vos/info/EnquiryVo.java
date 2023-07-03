package project.server.vos.info;

import lombok.Data;
import project.server.entities.info.EnquiryEntity;

@Data
public class EnquiryVo extends EnquiryEntity {
    private int answered;
    private String search;
}
