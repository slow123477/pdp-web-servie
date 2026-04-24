package com.slow.service.service;

import com.slow.common.result.PageResult;
import com.slow.pojo.dto.CourseAddDTO;
import com.slow.pojo.dto.CourseQueryDTO;
import com.slow.pojo.dto.CourseUpdateDTO;
import com.slow.pojo.vo.CourseVO;

/**
 * 课程 Service
 */
public interface CourseService {

    /**
     * 课程列表查询（条件分页）
     */
    PageResult list(CourseQueryDTO queryDTO);

    /**
     * 根据ID查询课程
     */
    CourseVO getById(Long id);

    /**
     * 添加课程
     */
    void add(CourseAddDTO addDTO);

    /**
     * 修改课程
     */
    void update(CourseUpdateDTO updateDTO);

    /**
     * 删除课程
     */
    void delete(Long id);
}
