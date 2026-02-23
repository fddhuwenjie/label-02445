package com.studentclub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studentclub.common.Result;
import com.studentclub.entity.Activity;
import com.studentclub.entity.Club;
import com.studentclub.entity.Membership;
import com.studentclub.entity.User;
import com.studentclub.service.ActivityService;
import com.studentclub.service.ClubService;
import com.studentclub.service.MembershipService;
import com.studentclub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final UserService userService;
    private final ClubService clubService;
    private final ActivityService activityService;
    private final MembershipService membershipService;
    
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> data = new HashMap<>();
        
        // 用户统计
        data.put("totalUsers", userService.count());
        data.put("studentCount", userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT")));
        
        // 社团统计
        data.put("totalClubs", clubService.count());
        data.put("activeClubs", clubService.count(new LambdaQueryWrapper<Club>().eq(Club::getStatus, 1)));
        data.put("pendingClubs", clubService.count(new LambdaQueryWrapper<Club>().eq(Club::getStatus, 0)));
        
        // 活动统计
        data.put("totalActivities", activityService.count());
        data.put("ongoingActivities", activityService.count(new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 1)));
        
        // 成员申请统计
        data.put("pendingApplications", membershipService.count(new LambdaQueryWrapper<Membership>().eq(Membership::getStatus, 0)));
        
        return Result.success(data);
    }
}
