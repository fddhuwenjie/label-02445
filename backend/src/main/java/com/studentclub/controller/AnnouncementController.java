package com.studentclub.controller;

import com.studentclub.common.PageResult;
import com.studentclub.common.Result;
import com.studentclub.entity.Announcement;
import com.studentclub.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
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
    
    @PostMapping
    public Result<Void> publish(@RequestBody Announcement announcement, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        announcement.setPublisherId(userId);
        announcementService.publish(announcement);
        return Result.success("公告发布成功", null);
    }
    
    @PutMapping
    public Result<Void> update(@RequestBody Announcement announcement) {
        announcementService.updateAnnouncement(announcement);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
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
}
