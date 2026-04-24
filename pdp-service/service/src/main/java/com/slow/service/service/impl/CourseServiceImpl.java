package com.slow.service.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.slow.common.exception.BusinessException;
import com.slow.common.result.PageResult;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.CourseAddDTO;
import com.slow.pojo.dto.CourseQueryDTO;
import com.slow.pojo.dto.CourseUpdateDTO;
import com.slow.pojo.entity.Course;
import com.slow.pojo.vo.CourseVO;
import com.slow.service.mapper.CourseMapper;
import com.slow.service.service.CourseService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程 Service 实现
 */
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public PageResult list(CourseQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Course> page = courseMapper.selectList(userId, queryDTO.getSemester(), queryDTO.getCourseType());
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public CourseVO getById(Long id) {
        Long userId = getCurrentUserId();
        Course course = courseMapper.selectById(id, userId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return convertToVO(course);
    }

    @Override
    public void add(CourseAddDTO addDTO) {
        Long userId = getCurrentUserId();
        Course course = new Course();
        BeanUtils.copyProperties(addDTO, course);
        course.setUserId(userId);
        courseMapper.insert(course);
    }

    @Override
    public void update(CourseUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        Course existing = courseMapper.selectById(updateDTO.getId(), userId);
        if (existing == null) {
            throw new BusinessException("课程不存在");
        }
        Course course = new Course();
        BeanUtils.copyProperties(updateDTO, course);
        course.setUserId(userId);
        courseMapper.update(course);
    }

    @Override
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        int rows = courseMapper.deleteById(id, userId);
        if (rows == 0) {
            throw new BusinessException("课程不存在");
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

    /**
     * Entity 转 VO
     */
    private CourseVO convertToVO(Course course) {
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        return vo;
    }
}
