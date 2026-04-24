package com.slow.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户设置实体
 */
@Data
public class UserSetting {

    private Long id;
    private Long userId;
    private String gpaStandard;
    private String gpaScale;
    private String aiDimensions;
    private String theme;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
