package com.slow.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 分析报告视图对象
 */
@Data
public class AiAnalysisVO {

    private Long id;
    private String analysisType;
    private Map<String, Object> dimensions;
    private String reportContent;
    private List<Map<String, String>> strengths;
    private List<Map<String, String>> weaknesses;
    private List<Map<String, String>> suggestions;
    private Map<String, Object> competitiveness;
    private LocalDateTime createdAt;
}
