package com.studentclub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studentclub.common.PageResult;
import com.studentclub.dto.ActivityDTO;
import com.studentclub.entity.Activity;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {
    
    /**
     * 创建活动
     */
    void createActivity(ActivityDTO dto);
    
    /**
     * 更新活动
     */
    void updateActivity(ActivityDTO dto);
    
    /**
     * 删除活动
     */
    void deleteActivity(Long id);
    
    /**
     * 获取活动详情
     */
    Activity getActivityDetail(Long id, Long userId);
    
    /**
     * 分页查询活动
     */
    PageResult<Activity> pageActivities(Integer page, Integer size, Long clubId, String keyword);
    
    /**
     * 报名活动
     */
    void register(Long activityId, Long userId);
    
    /**
     * 取消报名
     */
    void cancelRegister(Long activityId, Long userId);
    
    /**
     * 更新活动状态
     */
    void updateStatus(Long id, Integer status);
}
