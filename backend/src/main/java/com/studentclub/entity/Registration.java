package com.studentclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动报名实体类
 */
@Data
@TableName("t_registration")
public class Registration {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long activityId;
    
    private Long userId;
    
    private Integer status;
    
    private LocalDateTime registeredAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userRealName;
    
    @TableField(exist = false)
    private String activityTitle;
}
