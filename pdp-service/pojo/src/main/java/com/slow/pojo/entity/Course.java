package com.slow.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
public class Course {

    private Long id;
    private Long userId;
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
