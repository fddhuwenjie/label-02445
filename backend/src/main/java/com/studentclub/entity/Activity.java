package com.studentclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动实体类
 */
@Data
@TableName("t_activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long clubId;
    
    private String title;
    
    private String description;
    
    private String location;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer maxParticipants;
    
    private Integer currentParticipants;
    
    private Integer status;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String clubName;
    
    @TableField(exist = false)
    private Boolean registered;
}
