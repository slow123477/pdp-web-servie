package com.slow.service.mapper;

import com.github.pagehelper.Page;
import com.slow.pojo.entity.AiAnalysis;
import org.apache.ibatis.annotations.Param;

/**
 * AI 分析 Mapper
 */
public interface AiAnalysisMapper {

    /**
     * 分页查询 AI 分析历史列表
     */
    Page<AiAnalysis> selectList(@Param("userId") Long userId);

    /**
     * 根据ID和用户ID查询分析记录
     */
    AiAnalysis selectById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 添加分析记录
     */
    int insert(AiAnalysis aiAnalysis);

    /**
     * 根据ID和用户ID删除分析记录
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
