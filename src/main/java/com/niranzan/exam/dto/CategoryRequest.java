package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Category creation/update request")
public class CategoryRequest {
    
    @Schema(description = "Category name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Mathematics")
    private String name;
    
    @Schema(description = "Category description", example = "Questions related to mathematics")
    private String description;
    
    @Schema(description = "Whether this is a common category (admin only, ignored for organizers)", example = "false")
    private Boolean isCommon;
}

