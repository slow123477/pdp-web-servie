package com.slow.service.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程/成绩 Excel 数据模型
 */
@Data
public class CourseExcelData {

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("课程类型")
    private String courseType;

    @ExcelProperty("学分")
    private BigDecimal credits;

    @ExcelProperty("学期")
    private String semester;

    @ExcelProperty("学年")
    private String academicYear;

    @ExcelProperty("成绩")
    private BigDecimal score;

    @ExcelProperty("绩点")
    private BigDecimal gradePoint;
}
