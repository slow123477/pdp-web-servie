package com.slow.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 分析报告生成请求参数
 */
@Data
public class AiAnalysisGenerateDTO {

    /**
     * 分析维度列表，如 ability_assessment, development_direction
     */
    private List<String> dimensions;
}
