package com.slow.pojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成就实体
 */
@Data
public class Achievement {

    private Long id;
    private Long userId;
    private String name;
    private String level;
    private String category;
    private String issuer;
    private LocalDate awardDate;
    private String description;
    private String certificateUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
