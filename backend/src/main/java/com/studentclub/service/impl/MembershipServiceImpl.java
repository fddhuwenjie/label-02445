package com.studentclub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studentclub.common.PageResult;
import com.studentclub.entity.Club;
import com.studentclub.entity.Membership;
import com.studentclub.mapper.ClubMapper;
import com.studentclub.mapper.MembershipMapper;
import com.studentclub.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * 社团成员服务实现
 */
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl extends ServiceImpl<MembershipMapper, Membership> implements MembershipService {
    
    private final ClubMapper clubMapper;
    
    @Override
    public void applyJoin(Long clubId, Long userId) {
        // 检查社团是否存在
        Club club = clubMapper.selectById(clubId);
        if (club == null || club.getStatus() != 1) {
            throw new RuntimeException("社团不存在或未通过审核");
        }
        
        // 检查是否已申请或已是成员
        Membership existing = getOne(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getClubId, clubId)
                .eq(Membership::getUserId, userId));
        
        if (existing != null) {
            if (existing.getStatus() == 0) {
                throw new RuntimeException("您已提交申请，请等待审核");
            } else if (existing.getStatus() == 1) {
                throw new RuntimeException("您已是该社团成员");
            }
        }
        
        Membership membership = new Membership();
        membership.setUserId(userId);
        membership.setClubId(clubId);
        membership.setRole("MEMBER");
        membership.setStatus(0); // 待审核
        
        save(membership);
    }
    
    @Override
    @Transactional
    public void auditApplication(Long id, Integer status) {
        Membership membership = getById(id);
        if (membership == null) {
            throw new RuntimeException("申请记录不存在");
        }
        
        membership.setStatus(status);
        if (status == 1) {
            membership.setJoinedAt(LocalDateTime.now());
            // 更新社团成员数
            Club club = clubMapper.selectById(membership.getClubId());
            club.setMemberCount(club.getMemberCount() + 1);
            clubMapper.updateById(club);
        }
        
        updateById(membership);
    }
    
    @Override
    @Transactional
    public void quitClub(Long clubId, Long userId) {
        Membership membership = getOne(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getClubId, clubId)
                .eq(Membership::getUserId, userId)
                .eq(Membership::getStatus, 1));
        
        if (membership == null) {
            throw new RuntimeException("您不是该社团成员");
        }
        
        if ("LEADER".equals(membership.getRole())) {
            throw new RuntimeException("社团负责人不能直接退出，请先转让负责人");
        }
        
        membership.setStatus(3); // 已退出
        updateById(membership);
        
        // 更新社团成员数
        Club club = clubMapper.selectById(clubId);
        club.setMemberCount(Math.max(0, club.getMemberCount() - 1));
        clubMapper.updateById(club);
    }
    
    @Override
    @Transactional
    public void removeMember(Long id) {
        Membership membership = getById(id);
        if (membership == null) {
            throw new RuntimeException("成员记录不存在");
        }
        
        if ("LEADER".equals(membership.getRole())) {
            throw new RuntimeException("不能移除社团负责人");
        }
        
        membership.setStatus(3);
        updateById(membership);
        
        // 更新社团成员数
        Club club = clubMapper.selectById(membership.getClubId());
        club.setMemberCount(Math.max(0, club.getMemberCount() - 1));
        clubMapper.updateById(club);
    }
    
    @Override
    @Transactional
    public void addMember(Membership membership) {
        // 检查是否已是成员
        Membership existing = getOne(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getClubId, membership.getClubId())
                .eq(Membership::getUserId, membership.getUserId())
                .in(Membership::getStatus, 0, 1));
        
        if (existing != null) {
            throw new RuntimeException("该用户已是成员或正在申请中");
        }
        
        membership.setStatus(1); // 直接通过
        membership.setJoinedAt(LocalDateTime.now());
        save(membership);
        
        // 更新社团成员数
        Club club = clubMapper.selectById(membership.getClubId());
        club.setMemberCount(club.getMemberCount() + 1);
        clubMapper.updateById(club);
    }
    
    @Override
    public void updateRole(Long id, String role) {
        Membership membership = getById(id);
        if (membership == null) {
            throw new RuntimeException("成员记录不存在");
        }
        
        if ("LEADER".equals(membership.getRole())) {
            throw new RuntimeException("不能修改社团负责人角色");
        }
        
        membership.setRole(role);
        updateById(membership);
    }
    
    @Override
    public PageResult<Membership> getClubMembers(Long clubId, Integer page, Integer size, Integer status) {
        Page<Membership> pageParam = new Page<>(page, size);
        IPage<Membership> result = baseMapper.selectMemberPage(pageParam, clubId, status);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    @Override
    public PageResult<Membership> getUserMemberships(Long userId, Integer page, Integer size, Integer status) {
        Page<Membership> pageParam = new Page<>(page, size);
        IPage<Membership> result = baseMapper.selectUserMemberships(pageParam, userId, status);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    @Override
    public boolean isMember(Long clubId, Long userId) {
        return count(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getClubId, clubId)
                .eq(Membership::getUserId, userId)
                .eq(Membership::getStatus, 1)) > 0;
    }
    
    @Override
    public boolean isClubAdmin(Long clubId, Long userId) {
        return count(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getClubId, clubId)
                .eq(Membership::getUserId, userId)
                .eq(Membership::getStatus, 1)
                .in(Membership::getRole, "LEADER", "ADMIN")) > 0;
    }
    
    @Override
    public List<Long> getManagedClubIds(Long userId) {
        return list(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getUserId, userId)
                .eq(Membership::getStatus, 1)
                .in(Membership::getRole, "LEADER", "ADMIN"))
                .stream()
                .map(Membership::getClubId)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> getJoinedClubIds(Long userId) {
        return list(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getUserId, userId)
                .eq(Membership::getStatus, 1))
                .stream()
                .map(Membership::getClubId)
                .distinct()
                .collect(Collectors.toList());
    }
}
