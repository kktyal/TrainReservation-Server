package project.server.mappers.info;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import project.server.entities.info.BoardEntity;

import java.util.List;

@Mapper
public interface IInfoMapper {

    @Select("select `index`,`title`,`create_date` from `info`.`board`")
    List<BoardEntity> selectAll();

    @Select("select * from `info`.`board` where `title` like '%${title}%' or `content` like '%${title}%'")
    List<BoardEntity> selectBySearch(String search);

    @Select("select * from `info`.`board` where `index` = ${index}")
    List<BoardEntity> selectByIndex(int index);

}
