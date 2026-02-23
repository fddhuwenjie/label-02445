package com.studentclub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studentclub.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 公告Mapper
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    
    @Select("SELECT a.*, c.name as club_name, u.real_name as publisher_name " +
            "FROM t_announcement a " +
            "LEFT JOIN t_club c ON a.club_id = c.id " +
            "LEFT JOIN t_user u ON a.publisher_id = u.id " +
            "WHERE a.deleted = 0 " +
            "AND (#{clubId} IS NULL OR a.club_id = #{clubId} OR a.club_id IS NULL) " +
            "AND a.status = 1 " +
            "ORDER BY a.created_at DESC")
    IPage<Announcement> selectAnnouncementPage(Page<Announcement> page, @Param("clubId") Long clubId);
}
