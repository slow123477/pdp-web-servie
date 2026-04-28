package com.slow.pojo.dto;

import lombok.Data;

/**
 * 数据操作记录查询参数
 */
@Data
public class DataOperationQueryDTO {

    private String operationType;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
