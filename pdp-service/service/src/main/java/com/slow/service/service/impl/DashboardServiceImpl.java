package com.slow.service.service.impl;

import com.slow.common.exception.BusinessException;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.entity.Achievement;
import com.slow.pojo.entity.AiAnalysis;
import com.slow.pojo.entity.Experience;
import com.slow.pojo.entity.Role;
import com.slow.pojo.vo.DashboardVO;
import com.slow.pojo.vo.GpaVO;
import com.slow.pojo.vo.SemesterGpaVO;
import com.slow.service.mapper.AchievementMapper;
import com.slow.service.mapper.AiAnalysisMapper;
import com.slow.service.mapper.ExperienceMapper;
import com.slow.service.mapper.RoleMapper;
import com.slow.service.service.DashboardService;
import com.slow.service.service.GpaService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘 Service 实现
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private GpaService gpaService;

    @Autowired
    private ExperienceMapper experienceMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AiAnalysisMapper aiAnalysisMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardVO getDashboardData() {
        Long userId = getCurrentUserId();
        DashboardVO vo = new DashboardVO();

        // 1. GPA 数据（复用 GpaService）
        buildGpaData(vo);

        // 2. 经历统计
        buildExperienceData(vo, userId);

        // 3. 成就统计
        buildAchievementData(vo, userId);

        // 4. 角色统计
        buildRoleData(vo, userId);

        // 5. 最近动态
        buildRecentActivities(vo, userId);

        // 6. AI 摘要
        buildAiSummary(vo, userId);

        return vo;
    }

    /**
     * 构建 GPA 相关数据
     */
    private void buildGpaData(DashboardVO vo) {
        try {
            GpaVO gpaVO = gpaService.calculate(null);
            vo.setCumulativeGpa(gpaVO.getCumulativeGpa());
            vo.setTotalCredits(gpaVO.getTotalCredits());

            List<SemesterGpaVO> semesterGpas = gpaVO.getSemesterGpas();
            if (semesterGpas == null) {
                semesterGpas = Collections.emptyList();
            }
            vo.setGpaHistory(semesterGpas);

            // 本学期已修学分：取最后一个学期的学分
            if (!semesterGpas.isEmpty()) {
                SemesterGpaVO last = semesterGpas.get(semesterGpas.size() - 1);
                vo.setCurrentCredits(last.getCredits() != null ? last.getCredits() : BigDecimal.ZERO);
            } else {
                vo.setCurrentCredits(BigDecimal.ZERO);
            }

            // GPA 趋势：最后两个学期的差值
            vo.setGpaTrend(calculateGpaTrend(semesterGpas));
        } catch (Exception e) {
            log.warn("获取GPA数据失败", e);
            vo.setCumulativeGpa(BigDecimal.ZERO);
            vo.setTotalCredits(BigDecimal.ZERO);
            vo.setCurrentCredits(BigDecimal.ZERO);
            vo.setGpaTrend("0");
            vo.setGpaHistory(Collections.emptyList());
        }
    }

    /**
     * 计算 GPA 趋势字符串
     */
    private String calculateGpaTrend(List<SemesterGpaVO> semesterGpas) {
        if (semesterGpas == null || semesterGpas.size() < 2) {
            return "0";
        }
        SemesterGpaVO last = semesterGpas.get(semesterGpas.size() - 1);
        SemesterGpaVO prev = semesterGpas.get(semesterGpas.size() - 2);

        BigDecimal lastGpa = last.getGpa() != null ? last.getGpa() : BigDecimal.ZERO;
        BigDecimal prevGpa = prev.getGpa() != null ? prev.getGpa() : BigDecimal.ZERO;
        BigDecimal diff = lastGpa.subtract(prevGpa);

        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        String sign = diff.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return sign + diff.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /**
     * 构建经历统计数据
     */
    private void buildExperienceData(DashboardVO vo, Long userId) {
        try {
            List<Experience> experiences = experienceMapper.selectList(userId, null);
            if (experiences == null) {
                experiences = Collections.emptyList();
            }
            vo.setExperienceCount(experiences.size());

            Map<String, Integer> breakdown = experiences.stream()
                    .filter(e -> e.getType() != null && !e.getType().isEmpty())
                    .collect(Collectors.toMap(
                            Experience::getType,
                            e -> 1,
                            Integer::sum
                    ));
            vo.setExperienceBreakdown(breakdown);
        } catch (Exception e) {
            log.warn("获取经历数据失败", e);
            vo.setExperienceCount(0);
            vo.setExperienceBreakdown(Collections.emptyMap());
        }
    }

    /**
     * 构建成就统计数据
     */
    private void buildAchievementData(DashboardVO vo, Long userId) {
        try {
            List<Achievement> achievements = achievementMapper.selectList(userId, null, null);
            if (achievements == null) {
                achievements = Collections.emptyList();
            }
            vo.setAchievementCount(achievements.size());

            Map<String, Integer> breakdown = achievements.stream()
                    .filter(a -> a.getLevel() != null && !a.getLevel().isEmpty())
                    .collect(Collectors.toMap(
                            Achievement::getLevel,
                            a -> 1,
                            Integer::sum
                    ));
            vo.setAchievementBreakdown(breakdown);
        } catch (Exception e) {
            log.warn("获取成就数据失败", e);
            vo.setAchievementCount(0);
            vo.setAchievementBreakdown(Collections.emptyMap());
        }
    }

    /**
     * 构建角色统计数据
     */
    private void buildRoleData(DashboardVO vo, Long userId) {
        try {
            List<Role> roles = roleMapper.selectList(userId, null);
            if (roles == null) {
                roles = Collections.emptyList();
            }
            vo.setRoleCount(roles.size());
        } catch (Exception e) {
            log.warn("获取角色数据失败", e);
            vo.setRoleCount(0);
        }
    }

    /**
     * 构建最近动态
     */
    private void buildRecentActivities(DashboardVO vo, Long userId) {
        List<DashboardVO.RecentActivityVO> activities = new ArrayList<>();

        try {
            List<Experience> experiences = experienceMapper.selectList(userId, null);
            if (experiences != null) {
                for (Experience e : experiences) {
                    if (e.getStartDate() == null && e.getEndDate() == null) {
                        continue;
                    }
                    DashboardVO.RecentActivityVO activity = new DashboardVO.RecentActivityVO();
                    activity.setDate((e.getEndDate() != null ? e.getEndDate() : e.getStartDate()).format(DATE_FORMATTER));
                    activity.setContent(e.getTitle() != null ? e.getTitle() : e.getResult());
                    activity.setType("experience");
                    activities.add(activity);
                }
            }
        } catch (Exception e) {
            log.warn("获取经历动态失败", e);
        }

        try {
            List<Achievement> achievements = achievementMapper.selectList(userId, null, null);
            if (achievements != null) {
                for (Achievement a : achievements) {
                    if (a.getAwardDate() == null) {
                        continue;
                    }
                    DashboardVO.RecentActivityVO activity = new DashboardVO.RecentActivityVO();
                    activity.setDate(a.getAwardDate().format(DATE_FORMATTER));
                    activity.setContent(a.getName());
                    activity.setType("achievement");
                    activities.add(activity);
                }
            }
        } catch (Exception e) {
            log.warn("获取成就动态失败", e);
        }

        try {
            List<Role> roles = roleMapper.selectList(userId, null);
            if (roles != null) {
                for (Role r : roles) {
                    if (r.getStartDate() == null) {
                        continue;
                    }
                    DashboardVO.RecentActivityVO activity = new DashboardVO.RecentActivityVO();
                    activity.setDate(r.getStartDate().format(DATE_FORMATTER));
                    activity.setContent(r.getRoleName());
                    activity.setType("role");
                    activities.add(activity);
                }
            }
        } catch (Exception e) {
            log.warn("获取角色动态失败", e);
        }

        // 按日期倒序，取前 5 条
        activities.sort(Comparator.comparing(DashboardVO.RecentActivityVO::getDate).reversed());
        if (activities.size() > 5) {
            activities = activities.subList(0, 5);
        }
        vo.setRecentActivities(activities);
    }

    /**
     * 构建 AI 摘要
     */
    private void buildAiSummary(DashboardVO vo, Long userId) {
        try {
            List<AiAnalysis> list = aiAnalysisMapper.selectList(userId);
            if (list == null || list.isEmpty()) {
                vo.setAiSummary("");
                return;
            }
            // 按创建时间倒序取最新一条
            AiAnalysis latest = list.stream()
                    .max(Comparator.comparing(AiAnalysis::getCreatedAt))
                    .orElse(null);
            if (latest != null && latest.getReportContent() != null) {
                vo.setAiSummary(latest.getReportContent());
            } else {
                vo.setAiSummary("");
            }
        } catch (Exception e) {
            log.warn("获取AI分析摘要失败", e);
            vo.setAiSummary("");
        }
    }

    /**
     * 从 ThreadLocal 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Claims claims = ThreadLocalUtil.get();
        if (claims == null) {
            throw new BusinessException("未登录");
        }
        Object idObj = claims.get("id");
        if (idObj == null) {
            throw new BusinessException("未登录");
        }
        return Long.valueOf(idObj.toString());
    }
}
