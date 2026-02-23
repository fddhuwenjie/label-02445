package com.studentclub.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studentclub.common.PageResult;
import com.studentclub.dto.LoginDTO;
import com.studentclub.dto.RegisterDTO;
import com.studentclub.entity.User;
import com.studentclub.mapper.UserMapper;
import com.studentclub.service.UserService;
import com.studentclub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", sanitizeUser(user));
        return result;
    }
    
    @Override
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查学号是否存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getStudentId, dto.getStudentId())) > 0) {
            throw new RuntimeException("学号已被注册");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setStudentId(dto.getStudentId());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole("STUDENT");
        user.setStatus(1);
        
        save(user);
    }
    
    @Override
    public User getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return sanitizeUser(user);
    }
    
    @Override
    public void updateUserInfo(User user) {
        User existing = getById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        
        existing.setRealName(user.getRealName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setAvatar(user.getAvatar());
        
        updateById(existing);
    }
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setPassword(BCrypt.hashpw(newPassword));
        updateById(user);
    }
    
    @Override
    public PageResult<User> pageUsers(Integer page, Integer size, String keyword, String role, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentId, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> result = page(pageParam, wrapper);
        result.getRecords().forEach(this::sanitizeUser);
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    @Override
    public java.util.List<User> searchUsers(String keyword, Integer size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getRealName, keyword)
                    .or().like(User::getStudentId, keyword));
        }
        wrapper.eq(User::getStatus, 1);
        wrapper.last("LIMIT " + size);
        
        java.util.List<User> users = list(wrapper);
        users.forEach(this::sanitizeUser);
        return users;
    }
    
    @Override
    public void addUser(User user) {
        // 检查用户名是否存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查学号是否存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getStudentId, user.getStudentId())) > 0) {
            throw new RuntimeException("学号已被注册");
        }
        
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        user.setStatus(1);
        save(user);
    }
    
    @Override
    public void updateUser(User user) {
        User existing = getById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        
        existing.setRealName(user.getRealName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole());
        
        // 如果提供了新密码则更新
        if (StringUtils.hasText(user.getNewPassword())) {
            existing.setPassword(BCrypt.hashpw(user.getNewPassword()));
        }
        
        updateById(existing);
    }
    
    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        updateById(user);
    }
    
    private User sanitizeUser(User user) {
        user.setPassword(null);
        return user;
    }
}
