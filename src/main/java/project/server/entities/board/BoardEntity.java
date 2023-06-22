package project.server.entities.board;

import lombok.Data;

@Data
public class BoardEntity {
    private int index;
    private String title;
    private String content;
    private String createDate;
    private int author;
}
