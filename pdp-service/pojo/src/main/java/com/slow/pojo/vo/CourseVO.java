package com.slow.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程视图对象
 */
@Data
public class CourseVO {

    private Long id;
    private String courseName;
    private String courseType;
    private BigDecimal credits;
    private String semester;
    private String academicYear;
    private BigDecimal score;
    private BigDecimal gradePoint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
