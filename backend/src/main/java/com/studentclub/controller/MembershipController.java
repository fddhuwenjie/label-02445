package com.studentclub.controller;

import com.studentclub.common.PageResult;
import com.studentclub.common.Result;
import com.studentclub.entity.Membership;
import com.studentclub.service.MembershipService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 社团成员控制器
 */
@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {
    
    private final MembershipService membershipService;
    
    @PostMapping("/apply/{clubId}")
    public Result<Void> applyJoin(@PathVariable Long clubId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        membershipService.applyJoin(clubId, userId);
        return Result.success("申请已提交，等待审核", null);
    }
    
    @PutMapping("/{id}/audit")
    public Result<Void> auditApplication(@PathVariable Long id, @RequestParam Integer status) {
        membershipService.auditApplication(id, status);
        return Result.success();
    }
    
    @PostMapping("/quit/{clubId}")
    public Result<Void> quitClub(@PathVariable Long clubId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        membershipService.quitClub(clubId, userId);
        return Result.success("已退出社团", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> removeMember(@PathVariable Long id) {
        membershipService.removeMember(id);
        return Result.success();
    }
    
    @PostMapping("/add")
    public Result<Void> addMember(@RequestBody Membership membership) {
        membershipService.addMember(membership);
        return Result.success("添加成功", null);
    }
    
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam String role) {
        membershipService.updateRole(id, role);
        return Result.success();
    }
    
    @GetMapping("/club/{clubId}")
    public Result<PageResult<Membership>> getClubMembers(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(membershipService.getClubMembers(clubId, page, size, status));
    }
    
    @GetMapping("/my")
    public Result<PageResult<Membership>> getMyMemberships(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(membershipService.getUserMemberships(userId, page, size, status));
    }
    
    @GetMapping("/check/{clubId}")
    public Result<Boolean> checkMembership(@PathVariable Long clubId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(membershipService.isMember(clubId, userId));
    }
}
