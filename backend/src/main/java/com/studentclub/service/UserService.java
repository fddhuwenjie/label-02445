package com.studentclub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studentclub.common.PageResult;
import com.studentclub.dto.LoginDTO;
import com.studentclub.dto.RegisterDTO;
import com.studentclub.entity.User;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    Map<String, Object> login(LoginDTO dto);
    
    /**
     * 用户注册
     */
    void register(RegisterDTO dto);
    
    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);
    
    /**
     * 更新用户信息
     */
    void updateUserInfo(User user);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 分页查询用户
     */
    PageResult<User> pageUsers(Integer page, Integer size, String keyword, String role, Integer status);
    
    /**
     * 搜索用户
     */
    java.util.List<User> searchUsers(String keyword, Integer size);
    
    /**
     * 添加用户
     */
    void addUser(User user);
    
    /**
     * 更新用户（管理员）
     */
    void updateUser(User user);
    
    /**
     * 更新用户状态
     */
    void updateStatus(Long userId, Integer status);
}
