package com.slow.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 添加成就请求参数
 */
@Data
public class AchievementAddDTO {

    @NotBlank(message = "成就名称不能为空")
    private String name;

    @NotBlank(message = "级别不能为空")
    private String level;

    private String category;

    private String issuer;

    private LocalDate awardDate;

    private String description;

    private String certificateUrl;
}
