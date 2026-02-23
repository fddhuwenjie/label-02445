package com.studentclub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studentclub.common.PageResult;
import com.studentclub.dto.ActivityDTO;
import com.studentclub.entity.Activity;
import com.studentclub.entity.Registration;
import com.studentclub.mapper.ActivityMapper;
import com.studentclub.mapper.RegistrationMapper;
import com.studentclub.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 活动服务实现
 */
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {
    
    private final RegistrationMapper registrationMapper;
    
    @Override
    public void createActivity(ActivityDTO dto) {
        Activity activity = new Activity();
        activity.setClubId(dto.getClubId());
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setLocation(dto.getLocation());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setMaxParticipants(dto.getMaxParticipants() != null ? dto.getMaxParticipants() : 0);
        activity.setCurrentParticipants(0);
        activity.setStatus(1); // 报名中
        
        save(activity);
    }
    
    @Override
    public void updateActivity(ActivityDTO dto) {
        Activity activity = getById(dto.getId());
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setLocation(dto.getLocation());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setMaxParticipants(dto.getMaxParticipants());
        
        updateById(activity);
    }
    
    @Override
    public void deleteActivity(Long id) {
        removeById(id);
    }
    
    @Override
    public Activity getActivityDetail(Long id, Long userId) {
        Activity activity = baseMapper.selectActivityDetail(id);
        if (activity != null && userId != null) {
            // 检查用户是否已报名
            activity.setRegistered(registrationMapper.selectCount(
                    new LambdaQueryWrapper<Registration>()
                            .eq(Registration::getActivityId, id)
                            .eq(Registration::getUserId, userId)
                            .eq(Registration::getStatus, 1)) > 0);
        }
        return activity;
    }
    
    @Override
    public PageResult<Activity> pageActivities(Integer page, Integer size, Long clubId, String keyword) {
        Page<Activity> pageParam = new Page<>(page, size);
        IPage<Activity> result = baseMapper.selectActivityPage(pageParam, clubId, keyword);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    @Override
    @Transactional
    public void register(Long activityId, Long userId) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        if (activity.getStatus() != 1) {
            throw new RuntimeException("活动不在报名中");
        }
        
        if (activity.getMaxParticipants() > 0 && activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new RuntimeException("报名人数已满");
        }
        
        // 检查是否已报名
        Registration existing = registrationMapper.selectOne(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getActivityId, activityId)
                        .eq(Registration::getUserId, userId));
        
        if (existing != null && existing.getStatus() == 1) {
            throw new RuntimeException("您已报名该活动");
        }
        
        if (existing != null) {
            existing.setStatus(1);
            existing.setRegisteredAt(LocalDateTime.now());
            registrationMapper.updateById(existing);
        } else {
            Registration registration = new Registration();
            registration.setActivityId(activityId);
            registration.setUserId(userId);
            registration.setStatus(1);
            registration.setRegisteredAt(LocalDateTime.now());
            registrationMapper.insert(registration);
        }
        
        // 更新报名人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        updateById(activity);
    }
    
    @Override
    @Transactional
    public void cancelRegister(Long activityId, Long userId) {
        Registration registration = registrationMapper.selectOne(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getActivityId, activityId)
                        .eq(Registration::getUserId, userId)
                        .eq(Registration::getStatus, 1));
        
        if (registration == null) {
            throw new RuntimeException("您未报名该活动");
        }
        
        registration.setStatus(0);
        registrationMapper.updateById(registration);
        
        // 更新报名人数
        Activity activity = getById(activityId);
        activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
        updateById(activity);
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        Activity activity = getById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        activity.setStatus(status);
        updateById(activity);
    }
}
