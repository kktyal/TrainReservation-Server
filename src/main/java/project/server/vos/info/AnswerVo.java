package project.server.vos.info;

import lombok.Data;
import project.server.entities.info.AnswerEntity;

@Data
public class AnswerVo extends AnswerEntity {
    String formattedCreatedDate;
    String formattedUpdatedDate;
}
