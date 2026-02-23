package com.studentclub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studentclub.entity.Membership;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 社团成员Mapper
 */
@Mapper
public interface MembershipMapper extends BaseMapper<Membership> {
    
    @Select("SELECT m.*, u.username as user_name, u.real_name as user_real_name, " +
            "u.student_id as user_student_id, c.name as club_name " +
            "FROM t_membership m " +
            "LEFT JOIN t_user u ON m.user_id = u.id " +
            "LEFT JOIN t_club c ON m.club_id = c.id " +
            "WHERE m.club_id = #{clubId} " +
            "AND (#{status} IS NULL OR m.status = #{status}) " +
            "ORDER BY m.created_at DESC")
    IPage<Membership> selectMemberPage(Page<Membership> page, @Param("clubId") Long clubId, @Param("status") Integer status);
    
    @Select("SELECT m.*, c.name as club_name " +
            "FROM t_membership m " +
            "LEFT JOIN t_club c ON m.club_id = c.id " +
            "WHERE m.user_id = #{userId} " +
            "AND (#{status} IS NULL OR m.status = #{status}) " +
            "ORDER BY m.created_at DESC")
    IPage<Membership> selectUserMemberships(Page<Membership> page, @Param("userId") Long userId, @Param("status") Integer status);
}
