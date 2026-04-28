package com.slow.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘聚合数据视图对象
 */
@Data
public class DashboardVO {

    /** 累计 GPA */
    private BigDecimal cumulativeGpa;

    /** GPA 变化趋势，如 "+0.15" */
    private String gpaTrend;

    /** 总学分 */
    private BigDecimal totalCredits;

    /** 本学期已修学分 */
    private BigDecimal currentCredits;

    /** 经历总数 */
    private Integer experienceCount;

    /** 经历分类统计，如 { "竞赛": 4, "项目": 5 } */
    private Map<String, Integer> experienceBreakdown;

    /** 成就总数 */
    private Integer achievementCount;

    /** 成就级别统计，如 { "国家级": 2, "省级": 3 } */
    private Map<String, Integer> achievementBreakdown;

    /** 角色总数 */
    private Integer roleCount;

    /** 最近动态列表 */
    private List<RecentActivityVO> recentActivities;

    /** GPA 历史数据 */
    private List<SemesterGpaVO> gpaHistory;

    /** AI 分析摘要 */
    private String aiSummary;

    /**
     * 最近动态条目
     */
    @Data
    public static class RecentActivityVO {

        /** 日期，格式 yyyy-MM-dd */
        private String date;

        /** 动态内容 */
        private String content;

        /** 类型：experience / achievement / role */
        private String type;
    }
}
