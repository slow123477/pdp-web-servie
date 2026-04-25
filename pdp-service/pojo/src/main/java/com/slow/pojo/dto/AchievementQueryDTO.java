package com.slow.pojo.dto;

import lombok.Data;

/**
 * 成就列表查询参数
 */
@Data
public class AchievementQueryDTO {

    private String level;
    private String category;
    private Integer page = 1;
    private Integer pageSize = 10;
}
