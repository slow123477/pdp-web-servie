package com.slow.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 修改课程请求参数
 */
@Data
public class CourseUpdateDTO {

    @NotNull(message = "课程ID不能为空")
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private String courseType;

    @NotNull(message = "学分不能为空")
    private BigDecimal credits;

    @NotBlank(message = "学期不能为空")
    private String semester;

    private String academicYear;

    private BigDecimal score;

    private BigDecimal gradePoint;
}
