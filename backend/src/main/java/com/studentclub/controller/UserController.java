package com.studentclub.controller;

import com.studentclub.common.PageResult;
import com.studentclub.common.Result;
import com.studentclub.entity.User;
import com.studentclub.service.UserService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }
    
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        userService.updateUserInfo(user);
        return Result.success();
    }
    
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success("密码修改成功", null);
    }
    
    @GetMapping("/list")
    public Result<PageResult<User>> listUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        checkAdmin(request);
        return Result.success(userService.pageUsers(page, size, keyword, role, status));
    }
    
    @GetMapping("/search")
    public Result<java.util.List<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(userService.searchUsers(keyword, size));
    }
    
    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody User user, HttpServletRequest request) {
        checkAdmin(request);
        userService.addUser(user);
        return Result.success("添加成功", null);
    }
    
    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody User user, HttpServletRequest request) {
        checkAdmin(request);
        userService.updateUser(user);
        return Result.success();
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        checkAdmin(request);
        userService.updateStatus(id, status);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        // 不能删除管理员
        User user = userService.getById(id);
        if (user != null && "ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不能删除管理员账号");
        }
        userService.removeById(id);
        return Result.success();
    }
    
    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权限操作");
        }
    }
}
