package com.studentclub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studentclub.common.PageResult;
import com.studentclub.dto.ClubDTO;
import com.studentclub.entity.Club;
import com.studentclub.entity.Membership;
import com.studentclub.entity.User;
import com.studentclub.mapper.ClubMapper;
import com.studentclub.mapper.MembershipMapper;
import com.studentclub.mapper.UserMapper;
import com.studentclub.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 社团服务实现
 */
@Service
@RequiredArgsConstructor
public class ClubServiceImpl extends ServiceImpl<ClubMapper, Club> implements ClubService {
    
    private final MembershipMapper membershipMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public void createClub(ClubDTO dto, Long userId) {
        // 检查社团名称是否存在
        if (count(new LambdaQueryWrapper<Club>().eq(Club::getName, dto.getName())) > 0) {
            throw new RuntimeException("社团名称已存在");
        }
        
        Club club = new Club();
        club.setName(dto.getName());
        club.setDescription(dto.getDescription());
        club.setLogo(dto.getLogo());
        club.setCategory(dto.getCategory());
        club.setFounderId(userId);
        club.setLeaderId(userId);
        club.setStatus(0); // 待审核
        club.setMemberCount(1);
        
        save(club);
        
        // 创建者自动成为社团负责人
        Membership membership = new Membership();
        membership.setUserId(userId);
        membership.setClubId(club.getId());
        membership.setRole("LEADER");
        membership.setStatus(1);
        membership.setJoinedAt(LocalDateTime.now());
        membershipMapper.insert(membership);
        
        // 更新用户角色为LEADER
        User user = userMapper.selectById(userId);
        if ("STUDENT".equals(user.getRole())) {
            user.setRole("LEADER");
            userMapper.updateById(user);
        }
    }
    
    @Override
    public void updateClub(ClubDTO dto) {
        Club club = getById(dto.getId());
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }
        
        club.setName(dto.getName());
        club.setDescription(dto.getDescription());
        club.setLogo(dto.getLogo());
        club.setCategory(dto.getCategory());
        
        if (dto.getLeaderId() != null) {
            club.setLeaderId(dto.getLeaderId());
        }
        
        updateById(club);
    }
    
    @Override
    public void deleteClub(Long id) {
        removeById(id);
    }
    
    @Override
    public Club getClubDetail(Long id) {
        return baseMapper.selectClubDetail(id);
    }
    
    @Override
    public PageResult<Club> pageClubs(Integer page, Integer size, String keyword, Integer status) {
        Page<Club> pageParam = new Page<>(page, size);
        IPage<Club> result = baseMapper.selectClubPage(pageParam, keyword, status);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    @Override
    public void auditClub(Long id, Integer status) {
        Club club = getById(id);
        if (club == null) {
            throw new RuntimeException("社团不存在");
        }
        club.setStatus(status);
        updateById(club);
    }
    
    @Override
    public List<String> getCategories() {
        return Arrays.asList("学术科技", "文化艺术", "体育运动", "公益服务", "创新创业", "其他");
    }
    
    @Override
    public List<Club> getMyClubs(Long userId, String role) {
        // 管理员可以看到所有已审核的社团
        if ("ADMIN".equals(role)) {
            return list(new LambdaQueryWrapper<Club>()
                    .eq(Club::getStatus, 1));
        }
        // 普通用户只能看到自己是负责人的社团
        return list(new LambdaQueryWrapper<Club>()
                .eq(Club::getLeaderId, userId)
                .eq(Club::getStatus, 1));
    }
}
