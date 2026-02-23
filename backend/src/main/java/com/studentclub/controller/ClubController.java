package com.studentclub.controller;

import com.studentclub.common.PageResult;
import com.studentclub.common.Result;
import com.studentclub.dto.ClubDTO;
import com.studentclub.entity.Club;
import com.studentclub.service.ClubService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社团控制器
 */
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {
    
    private final ClubService clubService;
    
    @PostMapping
    public Result<Void> createClub(@Valid @RequestBody ClubDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        clubService.createClub(dto, userId);
        return Result.success("社团创建成功，等待审核", null);
    }
    
    @PutMapping
    public Result<Void> updateClub(@Valid @RequestBody ClubDTO dto) {
        clubService.updateClub(dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return Result.success();
    }
    
    @GetMapping("/detail/{id}")
    public Result<Club> getClubDetail(@PathVariable Long id) {
        return Result.success(clubService.getClubDetail(id));
    }
    
    @GetMapping("/list")
    public Result<PageResult<Club>> listClubs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(clubService.pageClubs(page, size, keyword, status));
    }
    
    @PutMapping("/{id}/audit")
    public Result<Void> auditClub(@PathVariable Long id, @RequestParam Integer status) {
        clubService.auditClub(id, status);
        return Result.success();
    }
    
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(clubService.getCategories());
    }
    
    @GetMapping("/my")
    public Result<List<Club>> getMyClubs(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(clubService.getMyClubs(userId, role));
    }
}
