package com.slow.pojo.dto;

import lombok.Data;

/**
 * 课程列表查询参数
 */
@Data
public class CourseQueryDTO {

    private String semester;
    private String courseType;
    private Integer page = 1;
    private Integer pageSize = 10;
}
