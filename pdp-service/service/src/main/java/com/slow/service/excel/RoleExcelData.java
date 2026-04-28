package com.slow.service.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 角色 Excel 数据模型
 */
@Data
public class RoleExcelData {

    @ExcelProperty("角色名称")
    private String roleName;

    @ExcelProperty("所属组织")
    private String organization;

    @ExcelProperty("开始日期")
    private String startDate;

    @ExcelProperty("结束日期")
    private String endDate;

    @ExcelProperty("是否在职")
    private Integer isCurrent;

    @ExcelProperty("职责描述")
    private String responsibilities;
}
