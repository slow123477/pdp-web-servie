package com.slow.service.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.slow.common.exception.BusinessException;
import com.slow.common.result.PageResult;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.AiAnalysisGenerateDTO;
import com.slow.pojo.entity.Achievement;
import com.slow.pojo.entity.AiAnalysis;
import com.slow.pojo.entity.Course;
import com.slow.pojo.entity.Experience;
import com.slow.pojo.entity.Role;
import com.slow.pojo.vo.AiAnalysisListVO;
import com.slow.pojo.vo.AiAnalysisVO;
import com.slow.service.mapper.AchievementMapper;
import com.slow.service.mapper.AiAnalysisMapper;
import com.slow.service.mapper.CourseMapper;
import com.slow.service.mapper.ExperienceMapper;
import com.slow.service.mapper.RoleMapper;
import com.slow.service.service.AiAnalysisService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 分析 Service 实现
 */
@Slf4j
@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final ChatClient chatClient;

    @Autowired
    private AiAnalysisMapper aiAnalysisMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ExperienceMapper experienceMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private RoleMapper roleMapper;

    private static final Map<String, String> DIMENSION_MAP = new HashMap<>();

    static {
        DIMENSION_MAP.put("ability_assessment", "能力评估");
        DIMENSION_MAP.put("development_direction", "发展方向");
        DIMENSION_MAP.put("resume_optimization", "简历优化建议");
        DIMENSION_MAP.put("competitiveness_analysis", "竞争力分析");
    }

    public AiAnalysisServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public AiAnalysisVO generate(AiAnalysisGenerateDTO generateDTO) {
        Long userId = getCurrentUserId();

        // 1. 查询用户数据
        List<Course> courses = courseMapper.selectList(userId, null, null);
        List<Experience> experiences = experienceMapper.selectList(userId, null);
        List<Achievement> achievements = achievementMapper.selectList(userId, null, null);
        List<Role> roles = roleMapper.selectList(userId, null);

        // 2. 构建 Prompt
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(courses, experiences, achievements, roles, generateDTO.getDimensions());

        log.info("AI 分析 Prompt 长度: system={}, user={}", systemPrompt.length(), userPrompt.length());

        // 3. 调用 AI
        String aiResponse;
        try {
            aiResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 服务调用失败", e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试");
        }

        log.info("AI 响应长度: {}", aiResponse != null ? aiResponse.length() : 0);

        // 4. 解析 JSON
        JSONObject jsonObject = parseAiResponse(aiResponse);

        // 5. 构建 Entity 并保存
        AiAnalysis aiAnalysis = new AiAnalysis();
        aiAnalysis.setUserId(userId);
        aiAnalysis.setAnalysisType(buildAnalysisType(generateDTO.getDimensions()));
        aiAnalysis.setDimensions(jsonObject.getString("dimensions"));
        aiAnalysis.setReportContent(jsonObject.getString("reportContent"));
        aiAnalysis.setStrengths(jsonObject.getString("strengths"));
        aiAnalysis.setWeaknesses(jsonObject.getString("weaknesses"));
        aiAnalysis.setSuggestions(jsonObject.getString("suggestions"));
        aiAnalysis.setCompetitiveness(jsonObject.getString("competitiveness"));
        aiAnalysisMapper.insert(aiAnalysis);

        // 6. 返回 VO
        return convertToVO(aiAnalysis);
    }

    @Override
    public PageResult list(Integer page, Integer pageSize) {
        Long userId = getCurrentUserId();
        PageHelper.startPage(page, pageSize);
        Page<AiAnalysis> pageResult = aiAnalysisMapper.selectList(userId);
        List<AiAnalysisListVO> rows = pageResult.getResult().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        return new PageResult(pageResult.getTotal(), rows);
    }

    @Override
    public AiAnalysisVO getById(Long id) {
        Long userId = getCurrentUserId();
        AiAnalysis aiAnalysis = aiAnalysisMapper.selectById(id, userId);
        if (aiAnalysis == null) {
            throw new BusinessException("分析记录不存在");
        }
        return convertToVO(aiAnalysis);
    }

    @Override
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        int rows = aiAnalysisMapper.deleteById(id, userId);
        if (rows == 0) {
            throw new BusinessException("分析记录不存在");
        }
    }

    /**
     * 构建 System Prompt
     */
    private String buildSystemPrompt() {
        return """
            你是一位资深的大学生职业规划导师，擅长根据学生的综合数据生成深度个人发展分析报告。

            你必须严格按照以下 JSON 格式返回结果，不要添加任何额外说明、markdown 代码块标记或注释：
            {
              "dimensions": {"学习能力": 0-100的整数, "团队协作": 0-100的整数, "领导力": 0-100的整数, "创新思维": 0-100的整数, "技术深度": 0-100的整数, "沟通表达": 0-100的整数},
              "reportContent": "使用HTML标签格式的完整报告内容，包含标题、段落、列表等",
              "strengths": [{"title": "核心优势标题（10字以内）", "desc": "详细描述该优势，结合具体数据说明（50字左右）"}],
              "weaknesses": [{"title": "待提升领域标题（10字以内）", "desc": "详细描述该不足，结合具体数据说明（50字左右）"}],
              "suggestions": [{"title": "发展建议标题（10字以内）", "desc": "具体可执行的建议（50字左右）"}],
              "competitiveness": {"保研": 0-100的整数, "就业": 0-100的整数, "出国": 0-100的整数}
            }

            要求：
            1. dimensions 至少包含6个维度评分，根据学生数据客观评估
            2. strengths 和 weaknesses 各2-4条，必须结合学生提供的具体课程、经历、成就数据
            3. suggestions 至少3条，要具体可执行
            4. reportContent 为完整HTML报告，包含分析摘要、详细解读
            5. 所有评分和评估必须基于实际数据，不要虚构
            """;
    }

    /**
     * 构建 User Prompt
     */
    private String buildUserPrompt(List<Course> courses, List<Experience> experiences,
                                    List<Achievement> achievements, List<Role> roles,
                                    List<String> dimensions) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下学生的个人发展数据，生成结构化分析报告。\n\n");

        // 分析维度
        sb.append("【分析维度】\n");
        if (dimensions != null && !dimensions.isEmpty()) {
            sb.append(dimensions.stream()
                    .map(DIMENSION_MAP::get)
                    .collect(Collectors.joining("、")));
        } else {
            sb.append("能力评估、发展方向、竞争力分析");
        }
        sb.append("\n\n");

        // 课程数据
        sb.append("【课程记录】\n");
        if (courses == null || courses.isEmpty()) {
            sb.append("暂无课程记录\n");
        } else {
            for (Course c : courses) {
                sb.append(String.format("- %s（%s，%s学分，成绩：%s，绩点：%s）\n",
                        c.getCourseName(), c.getCourseType(), c.getCredits(),
                        c.getScore() != null ? c.getScore() : "未录入",
                        c.getGradePoint() != null ? c.getGradePoint() : "未计算"));
            }
        }
        sb.append("\n");

        // 经历数据
        sb.append("【经历记录】\n");
        if (experiences == null || experiences.isEmpty()) {
            sb.append("暂无经历记录\n");
        } else {
            for (Experience e : experiences) {
                sb.append(String.format("- %s（%s，%s 至 %s）\n  描述：%s\n  成果：%s\n",
                        e.getTitle(), e.getType(), e.getStartDate(),
                        e.getEndDate() != null ? e.getEndDate() : "至今",
                        e.getDescription() != null ? e.getDescription() : "无",
                        e.getResult() != null ? e.getResult() : "无"));
            }
        }
        sb.append("\n");

        // 成就数据
        sb.append("【成就记录】\n");
        if (achievements == null || achievements.isEmpty()) {
            sb.append("暂无成就记录\n");
        } else {
            for (Achievement a : achievements) {
                sb.append(String.format("- %s（%s级，%s）\n  颁发机构：%s\n  描述：%s\n",
                        a.getName(), a.getLevel(), a.getCategory() != null ? a.getCategory() : "未分类",
                        a.getIssuer() != null ? a.getIssuer() : "未知",
                        a.getDescription() != null ? a.getDescription() : "无"));
            }
        }
        sb.append("\n");

        // 角色数据
        sb.append("【角色记录】\n");
        if (roles == null || roles.isEmpty()) {
            sb.append("暂无角色记录\n");
        } else {
            for (Role r : roles) {
                sb.append(String.format("- %s（%s，%s 至 %s）\n  职责：%s\n",
                        r.getRoleName(), r.getOrganization() != null ? r.getOrganization() : "无",
                        r.getStartDate(), r.getEndDate() != null ? r.getEndDate() : "至今",
                        r.getResponsibilities() != null ? r.getResponsibilities() : "无"));
            }
        }

        return sb.toString();
    }

    /**
     * 解析 AI 响应
     */
    private JSONObject parseAiResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new BusinessException("AI 返回内容为空");
        }

        // 清理 markdown 代码块标记
        String cleaned = aiResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            JSONObject jsonObject = JSON.parseObject(cleaned);
            // 校验必要字段
            if (!jsonObject.containsKey("dimensions") || !jsonObject.containsKey("reportContent")) {
                throw new BusinessException("AI 返回格式不正确，缺少必要字段");
            }
            return jsonObject;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 响应解析失败，原始响应: {}", aiResponse);
            throw new BusinessException("AI 分析结果解析失败，请重试");
        }
    }

    /**
     * 解析 JSON 数组为 Map 列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseMapList(String json) {
        return (List<Map<String, String>>) (Object) JSON.parseArray(json, Map.class);
    }

    /**
     * 构建分析类型描述
     */
    private String buildAnalysisType(List<String> dimensions) {
        if (dimensions == null || dimensions.isEmpty()) {
            return "综合能力分析";
        }
        return dimensions.stream()
                .map(DIMENSION_MAP::get)
                .collect(Collectors.joining(" · "));
    }

    /**
     * Entity 转完整 VO
     */
    private AiAnalysisVO convertToVO(AiAnalysis aiAnalysis) {
        AiAnalysisVO vo = new AiAnalysisVO();
        BeanUtils.copyProperties(aiAnalysis, vo);
        // JSON 字符串转对象
        if (aiAnalysis.getDimensions() != null) {
            vo.setDimensions(JSON.parseObject(aiAnalysis.getDimensions()));
        }
        if (aiAnalysis.getStrengths() != null) {
            vo.setStrengths(parseMapList(aiAnalysis.getStrengths()));
        }
        if (aiAnalysis.getWeaknesses() != null) {
            vo.setWeaknesses(parseMapList(aiAnalysis.getWeaknesses()));
        }
        if (aiAnalysis.getSuggestions() != null) {
            vo.setSuggestions(parseMapList(aiAnalysis.getSuggestions()));
        }
        if (aiAnalysis.getCompetitiveness() != null) {
            vo.setCompetitiveness(JSON.parseObject(aiAnalysis.getCompetitiveness()));
        }
        return vo;
    }

    /**
     * Entity 转列表 VO
     */
    private AiAnalysisListVO convertToListVO(AiAnalysis aiAnalysis) {
        AiAnalysisListVO vo = new AiAnalysisListVO();
        BeanUtils.copyProperties(aiAnalysis, vo);
        return vo;
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
