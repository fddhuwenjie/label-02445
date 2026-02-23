package com.studentclub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studentclub.common.PageResult;
import com.studentclub.entity.Announcement;

/**
 * 公告服务接口
 */
public interface AnnouncementService extends IService<Announcement> {
    
    /**
     * 发布公告
     */
    void publish(Announcement announcement);
    
    /**
     * 更新公告
     */
    void updateAnnouncement(Announcement announcement);
    
    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);
    
    /**
     * 分页查询公告
     */
    PageResult<Announcement> pageAnnouncements(Integer page, Integer size, Long clubId);
}
