package project.server.vos.info;

import lombok.Data;
import project.server.entities.info.BoardEntity;
@Data
public class BoardVo extends BoardEntity {
    private String search;
}
