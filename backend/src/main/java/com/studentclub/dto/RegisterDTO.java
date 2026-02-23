package com.studentclub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @NotBlank(message = "学号不能为空")
    private String studentId;
    
    private String email;
    
    private String phone;
}
