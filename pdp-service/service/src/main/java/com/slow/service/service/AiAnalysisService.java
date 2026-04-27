package com.slow.service.service;

import com.slow.common.result.PageResult;
import com.slow.pojo.dto.AiAnalysisGenerateDTO;
import com.slow.pojo.vo.AiAnalysisListVO;
import com.slow.pojo.vo.AiAnalysisVO;

/**
 * AI 分析 Service
 */
public interface AiAnalysisService {

    /**
     * 生成 AI 分析报告
     */
    AiAnalysisVO generate(AiAnalysisGenerateDTO generateDTO);

    /**
     * 查询分析历史列表（分页）
     */
    PageResult list(Integer page, Integer pageSize);

    /**
     * 根据ID查询分析详情
     */
    AiAnalysisVO getById(Long id);

    /**
     * 删除分析记录
     */
    void delete(Long id);
}
