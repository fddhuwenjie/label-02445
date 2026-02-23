package com.studentclub.controller;

import com.studentclub.common.PageResult;
import com.studentclub.common.Result;
import com.studentclub.dto.ActivityDTO;
import com.studentclub.entity.Activity;
import com.studentclub.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    
    @PostMapping
    public Result<Void> createActivity(@Valid @RequestBody ActivityDTO dto) {
        activityService.createActivity(dto);
        return Result.success("活动创建成功", null);
    }
    
    @PutMapping
    public Result<Void> updateActivity(@Valid @RequestBody ActivityDTO dto) {
        activityService.updateActivity(dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return Result.success();
    }
    
    @GetMapping("/detail/{id}")
    public Result<Activity> getActivityDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(activityService.getActivityDetail(id, userId));
    }
    
    @GetMapping("/list")
    public Result<PageResult<Activity>> listActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) String keyword) {
        return Result.success(activityService.pageActivities(page, size, clubId, keyword));
    }
    
    @PostMapping("/{id}/register")
    public Result<Void> register(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        activityService.register(id, userId);
        return Result.success("报名成功", null);
    }
    
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelRegister(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        activityService.cancelRegister(id, userId);
        return Result.success("已取消报名", null);
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        activityService.updateStatus(id, status);
        return Result.success();
    }
}
