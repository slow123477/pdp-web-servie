package com.slow.pojo.vo;

import lombok.Data;

/**
 * 数据导出结果视图对象
 */
@Data
public class DataExportResultVO {

    private String downloadUrl;
    private Long operationId;
}
