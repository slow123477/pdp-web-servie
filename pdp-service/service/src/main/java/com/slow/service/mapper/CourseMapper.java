package com.slow.service.mapper;

import com.github.pagehelper.Page;
import com.slow.pojo.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程 Mapper
 */
public interface CourseMapper {

    /**
     * 条件分页查询课程列表
     */
    Page<Course> selectList(@Param("userId") Long userId,
                            @Param("semester") String semester,
                            @Param("courseType") String courseType);

    /**
     * 根据ID和用户ID查询课程
     */
    Course selectById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 添加课程
     */
    int insert(Course course);

    /**
     * 修改课程
     */
    int update(Course course);

    /**
     * 根据ID和用户ID删除课程
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
