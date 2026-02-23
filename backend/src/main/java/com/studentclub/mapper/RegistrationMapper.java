package com.studentclub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studentclub.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 活动报名Mapper
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    
    @Select("SELECT r.*, u.username as user_name, u.real_name as user_real_name, a.title as activity_title " +
            "FROM t_registration r " +
            "LEFT JOIN t_user u ON r.user_id = u.id " +
            "LEFT JOIN t_activity a ON r.activity_id = a.id " +
            "WHERE r.activity_id = #{activityId} " +
            "ORDER BY r.registered_at DESC")
    IPage<Registration> selectByActivity(Page<Registration> page, @Param("activityId") Long activityId);
}
