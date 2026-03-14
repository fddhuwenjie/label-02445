package com.studentclub.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studentclub.common.PageResult;
import com.studentclub.entity.Announcement;
import com.studentclub.mapper.AnnouncementMapper;
import com.studentclub.service.AnnouncementService;
import org.springframework.stereotype.Service;

/**
 * 公告服务实现
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
    
    @Override
    public void publish(Announcement announcement) {
        announcement.setStatus(1);
        save(announcement);
    }
    
    @Override
    public void updateAnnouncement(Announcement announcement) {
        Announcement existing = getById(announcement.getId());
        if (existing == null) {
            throw new RuntimeException("公告不存在");
        }
        // 锁定 clubId 和 publisherId，不允许通过更新修改
        existing.setTitle(announcement.getTitle());
        existing.setContent(announcement.getContent());
        updateById(existing);
    }
    
    @Override
    public void deleteAnnouncement(Long id) {
        removeById(id);
    }
    
    @Override
    public PageResult<Announcement> pageAnnouncements(Integer page, Integer size, Long clubId) {
        Page<Announcement> pageParam = new Page<>(page, size);
        IPage<Announcement> result = baseMapper.selectAnnouncementPage(pageParam, clubId);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
}
