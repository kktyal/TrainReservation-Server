package project.server.mappers.board;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import project.server.entities.board.BoardEntity;
import project.server.vos.board.BoardVo;

import java.util.List;

@Mapper
public interface IBoardMapper {

    @Select("select `index`,`title`,`create_date` from `board`")
    List<BoardEntity> selectAll();

    @Select("select * from `board` where `title` like '%${title}%' or `content` like '%${title}%'")
    List<BoardEntity> selectBySearch(String search);

    @Select("select * from `board` where `index` = ${index}")
    List<BoardEntity> selectByIndex(int index);

}
