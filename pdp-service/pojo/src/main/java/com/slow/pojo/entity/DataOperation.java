package com.slow.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据导入导出记录实体
 */
@Data
public class DataOperation {

    private Long id;
    private Long userId;
    private String operationType;
    private String dataType;
    private String fileUrl;
    private String status;
    private LocalDateTime createdAt;
}
