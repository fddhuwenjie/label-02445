package com.studentclub.controller;

import com.studentclub.common.PageResult;
import com.studentclub.common.Result;
import com.studentclub.entity.Announcement;
import com.studentclub.service.AnnouncementService;
import com.studentclub.service.MembershipService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    
    private final AnnouncementService announcementService;
    private final MembershipService membershipService;
    
    @PostMapping
    public Result<Void> publish(@RequestBody Announcement announcement, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (announcement.getClubId() != null) {
            checkClubAdmin(announcement.getClubId(), userId, role);
        } else if (!"ADMIN".equals(role)) {
            throw new RuntimeException("只有管理员可以发布全站公告");
        }
        announcement.setPublisherId(userId);
        announcementService.publish(announcement);
        return Result.success("公告发布成功", null);
    }
    
    @PutMapping
    public Result<Void> update(@RequestBody Announcement announcement, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Announcement existing = announcementService.getById(announcement.getId());
        if (existing == null) {
            throw new RuntimeException("公告不存在");
        }
        if (existing.getClubId() != null) {
            checkClubAdmin(existing.getClubId(), userId, role);
        } else if (!"ADMIN".equals(role)) {
            throw new RuntimeException("只有管理员可以修改全站公告");
        }
        announcementService.updateAnnouncement(announcement);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Announcement existing = announcementService.getById(id);
        if (existing == null) {
            throw new RuntimeException("公告不存在");
        }
        if (existing.getClubId() != null) {
            checkClubAdmin(existing.getClubId(), userId, role);
        } else if (!"ADMIN".equals(role)) {
            throw new RuntimeException("只有管理员可以删除全站公告");
        }
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
    
    @GetMapping("/list")
    public Result<PageResult<Announcement>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long clubId) {
        return Result.success(announcementService.pageAnnouncements(page, size, clubId));
    }
    
    private void checkClubAdmin(Long clubId, Long userId, String role) {
        if ("ADMIN".equals(role)) {
            return;
        }
        if (!membershipService.isClubAdmin(clubId, userId)) {
            throw new RuntimeException("无权限操作");
        }
    }
}
