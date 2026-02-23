package com.studentclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社团成员实体类
 */
@Data
@TableName("t_membership")
public class Membership {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long clubId;
    
    private String role;
    
    private Integer status;
    
    private LocalDateTime joinedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userRealName;
    
    @TableField(exist = false)
    private String userStudentId;
    
    @TableField(exist = false)
    private String clubName;
}
