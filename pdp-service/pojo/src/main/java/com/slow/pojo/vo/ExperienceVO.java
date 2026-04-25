package com.slow.pojo.vo;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 经历视图对象
 */
@Data
public class ExperienceVO {

    private Long id;
    private String title;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String result;

    @JsonRawValue
    private String attachments;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
