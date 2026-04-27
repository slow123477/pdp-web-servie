package com.slow.pojo.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 角色视图对象
 */
@Data
public class RoleVO {

    private Long id;
    private String roleName;
    private String organization;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isCurrent;
    private String responsibilities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
