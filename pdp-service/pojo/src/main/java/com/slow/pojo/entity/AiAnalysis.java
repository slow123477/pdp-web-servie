package com.slow.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 分析记录实体
 */
@Data
public class AiAnalysis {

    private Long id;
    private Long userId;
    private String analysisType;
    private String dimensions;
    private String reportContent;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String competitiveness;
    private LocalDateTime createdAt;
}
