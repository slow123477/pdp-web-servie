package com.slow.pojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
public class Role {

    private Long id;
    private Long userId;
    private String roleName;
    private String organization;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isCurrent;
    private String responsibilities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
