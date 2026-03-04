package com.studentclub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studentclub.entity.Club;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 社团Mapper
 */
@Mapper
public interface ClubMapper extends BaseMapper<Club> {
    
    @Select("SELECT c.*, u1.real_name as leader_name, u2.real_name as founder_name " +
            "FROM t_club c " +
            "LEFT JOIN t_user u1 ON c.leader_id = u1.id " +
            "LEFT JOIN t_user u2 ON c.founder_id = u2.id " +
            "WHERE c.deleted = 0 " +
            "AND (#{keyword} IS NULL OR c.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR c.status = #{status}) " +
            "ORDER BY c.created_at DESC")
    IPage<Club> selectClubPage(Page<Club> page, @Param("keyword") String keyword, @Param("status") Integer status);
    
    @Select("SELECT c.*, u1.real_name as leader_name, u2.real_name as founder_name " +
            "FROM t_club c " +
            "LEFT JOIN t_user u1 ON c.leader_id = u1.id " +
            "LEFT JOIN t_user u2 ON c.founder_id = u2.id " +
            "WHERE c.id = #{id} AND c.deleted = 0")
    Club selectClubDetail(@Param("id") Long id);
    
    @Select("<script>" +
            "SELECT c.*, u1.real_name as leader_name, u2.real_name as founder_name FROM t_club c " +
            "LEFT JOIN t_user u1 ON c.leader_id = u1.id " +
            "LEFT JOIN t_user u2 ON c.founder_id = u2.id " +
            "WHERE c.deleted = 0 AND c.id IN " +
            "<foreach collection='clubIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "<if test='keyword != null and keyword != \"\"'> AND c.name LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "<if test='status != null'> AND c.status = #{status}</if>" +
            " ORDER BY c.created_at DESC" +
            "</script>")
    IPage<Club> selectClubPageByClubIds(Page<Club> page, @Param("clubIds") List<Long> clubIds, @Param("keyword") String keyword, @Param("status") Integer status);
}
