package com.studentclub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动DTO
 */
@Data
public class ActivityDTO {
    private Long id;
    
    @NotNull(message = "社团ID不能为空")
    private Long clubId;
    
    @NotBlank(message = "活动标题不能为空")
    private String title;
    
    private String description;
    
    private String location;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    private Integer maxParticipants;
}
