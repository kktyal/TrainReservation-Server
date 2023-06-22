package project.server.vos.board;

import lombok.Data;
import project.server.entities.board.BoardEntity;
@Data
public class BoardVo extends BoardEntity {
    private String search;
}
