package com.slow.pojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 经历实体
 */
@Data
public class Experience {

    private Long id;
    private Long userId;
    private String title;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String result;
    private String attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
