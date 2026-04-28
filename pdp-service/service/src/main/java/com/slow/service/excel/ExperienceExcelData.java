package com.slow.service.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 经历 Excel 数据模型
 */
@Data
public class ExperienceExcelData {

    @ExcelProperty("经历标题")
    private String title;

    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("开始日期")
    private String startDate;

    @ExcelProperty("结束日期")
    private String endDate;

    @ExcelProperty("详细描述")
    private String description;

    @ExcelProperty("成果/奖项")
    private String result;
}
