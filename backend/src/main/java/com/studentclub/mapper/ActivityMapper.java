package com.studentclub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studentclub.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 活动Mapper
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    
    @Select("SELECT a.*, c.name as club_name " +
            "FROM t_activity a " +
            "LEFT JOIN t_club c ON a.club_id = c.id " +
            "WHERE a.deleted = 0 " +
            "AND (#{clubId} IS NULL OR a.club_id = #{clubId}) " +
            "AND (#{keyword} IS NULL OR a.title LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY a.start_time DESC")
    IPage<Activity> selectActivityPage(Page<Activity> page, @Param("clubId") Long clubId, @Param("keyword") String keyword);
    
    @Select("SELECT a.*, c.name as club_name " +
            "FROM t_activity a " +
            "LEFT JOIN t_club c ON a.club_id = c.id " +
            "WHERE a.id = #{id} AND a.deleted = 0")
    Activity selectActivityDetail(@Param("id") Long id);
}
