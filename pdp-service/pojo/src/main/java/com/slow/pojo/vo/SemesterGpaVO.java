package com.slow.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 学期 GPA 视图对象
 */
@Data
public class SemesterGpaVO {

    private String semester;
    private BigDecimal gpa;
    private BigDecimal credits;
}
