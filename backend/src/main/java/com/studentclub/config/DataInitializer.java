package com.studentclub.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studentclub.entity.User;
import com.studentclub.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 确保测试账号密码正确
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserMapper userMapper;
    
    @Override
    public void run(String... args) {
        initTestUsers();
    }
    
    private void initTestUsers() {
        // 初始化管理员账号
        initUser("admin", "admin123", "系统管理员", "ADMIN001", "ADMIN");
        // 初始化社团负责人账号
        initUser("leader", "leader123", "张三", "2021001001", "LEADER");
        // 初始化学生账号
        initUser("student", "student123", "李四", "2021001002", "STUDENT");
        
        log.info("测试账号初始化完成");
        log.info("管理员: admin / admin123");
        log.info("社团负责人: leader / leader123");
        log.info("学生: student / student123");
    }
    
    private void initUser(String username, String password, String realName, String studentId, String role) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        
        if (existing == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(BCrypt.hashpw(password));
            user.setRealName(realName);
            user.setStudentId(studentId);
            user.setRole(role);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("创建用户: {}", username);
        } else {
            // 更新密码确保正确
            existing.setPassword(BCrypt.hashpw(password));
            userMapper.updateById(existing);
            log.info("更新用户密码: {}", username);
        }
    }
}
