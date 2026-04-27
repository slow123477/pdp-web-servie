package com.slow.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 添加角色请求参数
 */
@Data
public class RoleAddDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String organization;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;

    private Integer isCurrent;

    private String responsibilities;
}
