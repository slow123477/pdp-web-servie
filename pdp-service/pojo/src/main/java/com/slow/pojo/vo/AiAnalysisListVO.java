package com.slow.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 分析历史列表视图对象
 */
@Data
public class AiAnalysisListVO {

    private Long id;
    private String analysisType;
    private LocalDateTime createdAt;
}
