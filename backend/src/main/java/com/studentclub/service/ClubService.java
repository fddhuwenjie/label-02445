package com.studentclub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studentclub.common.PageResult;
import com.studentclub.dto.ClubDTO;
import com.studentclub.entity.Club;

import java.util.List;

/**
 * 社团服务接口
 */
public interface ClubService extends IService<Club> {
    
    /**
     * 创建社团
     */
    void createClub(ClubDTO dto, Long userId);
    
    /**
     * 更新社团
     */
    void updateClub(ClubDTO dto);
    
    /**
     * 删除社团
     */
    void deleteClub(Long id);
    
    /**
     * 获取社团详情
     */
    Club getClubDetail(Long id);
    
    /**
     * 分页查询社团
     * @param scope 非管理员时的展示范围：all-全部, joined-我加入的, managed-我管理的
     */
    PageResult<Club> pageClubs(Integer page, Integer size, String keyword, Integer status, String scope, Long userId, String userRole);
    
    /**
     * 审核社团
     */
    void auditClub(Long id, Integer status);
    
    /**
     * 获取所有社团类别
     */
    List<String> getCategories();
    
    /**
     * 获取用户管理的社团
     */
    List<Club> getMyClubs(Long userId, String role);
}
