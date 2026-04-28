package com.slow.service.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 成就 Excel 数据模型
 */
@Data
public class AchievementExcelData {

    @ExcelProperty("成就名称")
    private String name;

    @ExcelProperty("级别")
    private String level;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("颁发机构")
    private String issuer;

    @ExcelProperty("获得日期")
    private String awardDate;

    @ExcelProperty("描述")
    private String description;
}
