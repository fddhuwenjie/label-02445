package com.studentclub.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 社团DTO
 */
@Data
public class ClubDTO {
    private Long id;
    
    @NotBlank(message = "社团名称不能为空")
    private String name;
    
    private String description;
    
    private String logo;
    
    private String category;
    
    private Long leaderId;
}
