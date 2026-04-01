package com.studentclub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studentclub.annotation.RequiresAdmin;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Result<Map<String, Object>> getOverview(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        Map<String, Object> data = new HashMap<>();
        
        if ("ADMIN".equals(role)) {
            data.put("totalUsers", userService.count());
            data.put("studentCount", userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT")));
            data.put("totalClubs", clubService.count());
            data.put("activeClubs", clubService.count(new LambdaQueryWrapper<Club>().eq(Club::getStatus, 1)));
            data.put("pendingClubs", clubService.count(new LambdaQueryWrapper<Club>().eq(Club::getStatus, 0)));
            data.put("totalActivities", activityService.count());
            data.put("ongoingActivities", activityService.count(new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 1)));
            data.put("pendingApplications", membershipService.count(new LambdaQueryWrapper<Membership>().eq(Membership::getStatus, 0)));
        } else {
            List<Membership> myMemberships = membershipService.list(new LambdaQueryWrapper<Membership>()
                    .eq(Membership::getUserId, userId)
                    .eq(Membership::getStatus, 1));
            List<Long> myClubIds = myMemberships.stream().map(Membership::getClubId).collect(Collectors.toList());
            List<Long> managedClubIds = myMemberships.stream()
                    .filter(m -> "ADMIN".equals(m.getRole()))
                    .map(Membership::getClubId).collect(Collectors.toList());
            
            data.put("myClubCount", myMemberships.size());
            data.put("activeClubs", clubService.count(new LambdaQueryWrapper<Club>().eq(Club::getStatus, 1)));
            data.put("ongoingActivities", activityService.count(new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 1)));
            
            if (!managedClubIds.isEmpty()) {
                data.put("pendingApplications", membershipService.count(new LambdaQueryWrapper<Membership>()
                        .eq(Membership::getStatus, 0)
                        .in(Membership::getClubId, managedClubIds)));
            } else {
                data.put("pendingApplications", 0);
            }
        }
        
        return Result.success(data);
    }
}
