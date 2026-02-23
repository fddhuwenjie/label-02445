package com.studentclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社团实体类
 */
@Data
@TableName("t_club")
public class Club {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private String logo;
    
    private String category;
    
    private Long founderId;
    
    private Long leaderId;
    
    private Integer status;
    
    private Integer memberCount;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String leaderName;
    
    @TableField(exist = false)
    private String founderName;
}
