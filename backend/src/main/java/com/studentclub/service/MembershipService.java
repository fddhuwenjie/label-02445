package com.studentclub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studentclub.common.PageResult;
import com.studentclub.entity.Membership;

/**
 * 社团成员服务接口
 */
public interface MembershipService extends IService<Membership> {
    
    /**
     * 申请加入社团
     */
    void applyJoin(Long clubId, Long userId);
    
    /**
     * 审核申请
     */
    void auditApplication(Long id, Integer status);
    
    /**
     * 退出社团
     */
    void quitClub(Long clubId, Long userId);
    
    /**
     * 移除成员
     */
    void removeMember(Long id);
    
    /**
     * 添加成员
     */
    void addMember(Membership membership);
    
    /**
     * 修改成员角色
     */
    void updateRole(Long id, String role);
    
    /**
     * 获取社团成员列表
     */
    PageResult<Membership> getClubMembers(Long clubId, Integer page, Integer size, Integer status);
    
    /**
     * 获取用户加入的社团
     */
    PageResult<Membership> getUserMemberships(Long userId, Integer page, Integer size, Integer status);
    
    /**
     * 检查用户是否是社团成员
     */
    boolean isMember(Long clubId, Long userId);
    
    /**
     * 检查用户是否是社团管理员
     */
    boolean isClubAdmin(Long clubId, Long userId);
    
    /**
     * 获取用户拥有管理权限的社团ID列表（LEADER 或 ADMIN 角色）
     */
    java.util.List<Long> getManagedClubIds(Long userId);
    
    /**
     * 获取用户已加入的社团ID列表（status=1）
     */
    java.util.List<Long> getJoinedClubIds(Long userId);
}
