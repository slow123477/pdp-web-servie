package com.slow.service.service.impl;

import com.slow.common.exception.BusinessException;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.entity.Course;
import com.slow.pojo.vo.GpaVO;
import com.slow.pojo.vo.SemesterGpaVO;
import com.slow.pojo.vo.UserSettingVO;
import com.slow.service.mapper.CourseMapper;
import com.slow.service.service.GpaService;
import com.slow.service.service.SettingService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GPA 计算 Service 实现
 */
@Slf4j
@Service
public class GpaServiceImpl implements GpaService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private SettingService settingService;

    @Override
    public GpaVO calculate(String standard) {
        Long userId = getCurrentUserId();
        List<Course> courses = courseMapper.selectList(userId, null, null);

        String gpaStandard = resolveStandard(standard);

        GpaVO gpaVO = new GpaVO();
        gpaVO.setCumulativeGpa(calculateWeightedGpa(courses, gpaStandard));
        gpaVO.setTotalCredits(calculateTotalCredits(courses));
        gpaVO.setSemesterGpas(calculateSemesterGpas(courses, gpaStandard));

        return gpaVO;
    }

    @Override
    public List<SemesterGpaVO> trend() {
        GpaVO gpaVO = calculate(null);
        return gpaVO.getSemesterGpas();
    }

    /**
     * 解析 GPA 计算标准
     */
    private String resolveStandard(String standard) {
        if (standard != null && !standard.isEmpty()) {
            return standard;
        }
        try {
            UserSettingVO setting = settingService.getSetting();
            if (setting != null && setting.getGpaStandard() != null && !setting.getGpaStandard().isEmpty()) {
                return setting.getGpaStandard();
            }
        } catch (Exception e) {
            log.warn("获取用户GPA设置失败，使用默认标准", e);
        }
        return "4.0";
    }

    /**
     * 计算加权 GPA
     */
    private BigDecimal calculateWeightedGpa(List<Course> courses, String standard) {
        BigDecimal totalWeighted = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Course course : courses) {
            if (course.getCredits() == null) {
                continue;
            }
            BigDecimal value = getWeightedValue(course, standard);
            if (value == null) {
                continue;
            }
            totalWeighted = totalWeighted.add(value.multiply(course.getCredits()));
            totalCredits = totalCredits.add(course.getCredits());
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return totalWeighted.divide(totalCredits, 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算总学分
     */
    private BigDecimal calculateTotalCredits(List<Course> courses) {
        return courses.stream()
                .filter(c -> c.getCredits() != null)
                .map(Course::getCredits)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 按学期计算 GPA 列表
     */
    private List<SemesterGpaVO> calculateSemesterGpas(List<Course> courses, String standard) {
        Map<String, List<Course>> grouped = courses.stream()
                .filter(c -> c.getSemester() != null && !c.getSemester().isEmpty())
                .collect(Collectors.groupingBy(Course::getSemester));

        List<SemesterGpaVO> result = new ArrayList<>();

        for (Map.Entry<String, List<Course>> entry : grouped.entrySet()) {
            String semester = entry.getKey();
            List<Course> semesterCourses = entry.getValue();

            BigDecimal gpa = calculateWeightedGpa(semesterCourses, standard);
            BigDecimal credits = calculateTotalCredits(semesterCourses);

            SemesterGpaVO vo = new SemesterGpaVO();
            vo.setSemester(semester);
            vo.setGpa(gpa);
            vo.setCredits(credits);
            result.add(vo);
        }

        result.sort(Comparator.comparingInt(this::extractSemesterSortKey));
        return result;
    }

    /**
     * 获取单门课程的加权值（score 或 gradePoint）
     */
    private BigDecimal getWeightedValue(Course course, String standard) {
        if ("weighted".equals(standard)) {
            return course.getScore();
        }
        return course.getGradePoint();
    }

    /**
     * 提取学期排序键（用于中文学期名排序）
     * <p>大一上 -> 11, 大一下 -> 12, 大二上 -> 21, ...
     */
    private int extractSemesterSortKey(SemesterGpaVO vo) {
        String semester = vo.getSemester();
        if (semester == null || semester.length() < 2) {
            return 0;
        }

        int grade = 0;
        int term = 0;

        char gradeChar = semester.charAt(1);
        switch (gradeChar) {
            case '一':
                grade = 1;
                break;
            case '二':
                grade = 2;
                break;
            case '三':
                grade = 3;
                break;
            case '四':
                grade = 4;
                break;
            case '五':
                grade = 5;
                break;
            default:
                try {
                    grade = Integer.parseInt(String.valueOf(gradeChar));
                } catch (NumberFormatException e) {
                    grade = 0;
                }
        }

        if (semester.endsWith("上")) {
            term = 1;
        } else if (semester.endsWith("下")) {
            term = 2;
        } else {
            try {
                char lastChar = semester.charAt(semester.length() - 1);
                term = Integer.parseInt(String.valueOf(lastChar));
            } catch (NumberFormatException e) {
                term = 0;
            }
        }

        return grade * 10 + term;
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
