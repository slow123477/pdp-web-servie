package com.slow.pojo.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成就视图对象
 */
@Data
public class AchievementVO {

    private Long id;
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
