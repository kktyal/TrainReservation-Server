package project.server.mappers.info;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import project.server.entities.info.BoardEntity;
import project.server.entities.info.EnquiryEntity;
import project.server.vos.info.EnquiryVo;

import java.util.List;

@Mapper
public interface IInfoMapper {

    @Select("select `index`,`title`,`create_date` from `info`.`board`")
    List<BoardEntity> selectAll();

    @Select("select * from `info`.`board` where `title` like '%${title}%' or `content` like '%${title}%'")
    List<BoardEntity> selectBySearch(String search);

    @Select("select * from `info`.`board` where `index` = ${index}")
    List<BoardEntity> selectByIndex(int index);


    @Select("select `e`.`index`, `e`.`title`, `e`.`create_date`, `e`.`author`, ifnull(`a`.`index`, 0) as `answered` \n" +
            "from `info`.`enquiry` as `e`\n" +
            "         left join `info`.`answer` as `a`\n" +
            "                   on `e`.`index` = `a`.`enquiry_index`\n")
    List<EnquiryVo> selectEnquiryAll();

    @Select("select `e`.`index`, `e`.`title`, `e`.`create_date`, `e`.`author`, ifnull(`a`.`index`, 0) as `answered`\n" +
            "from `info`.`enquiry` as `e`\n" +
            "         left join `info`.`answer` as `a`\n" +
            "                   on `e`.`index` = `a`.`enquiry_index`" +
            " where `e`.`author` = ${memberId}")
    List<EnquiryVo> selectEnquiryByMemberId(int memberId);

}
