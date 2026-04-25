package com.slow.pojo.dto;

import lombok.Data;

/**
 * 经历列表查询参数
 */
@Data
public class ExperienceQueryDTO {

    private String type;
    private Integer page = 1;
    private Integer pageSize = 10;
}
