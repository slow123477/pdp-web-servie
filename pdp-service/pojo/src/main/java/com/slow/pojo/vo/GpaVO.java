package com.slow.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * GPA 计算结果视图对象
 */
@Data
public class GpaVO {

    private BigDecimal cumulativeGpa;
    private BigDecimal totalCredits;
    private List<SemesterGpaVO> semesterGpas;
}
