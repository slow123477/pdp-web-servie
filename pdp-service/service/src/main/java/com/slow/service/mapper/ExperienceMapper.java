package com.slow.service.mapper;

import com.github.pagehelper.Page;
import com.slow.pojo.entity.Experience;
import org.apache.ibatis.annotations.Param;

/**
 * 经历 Mapper
 */
public interface ExperienceMapper {

    /**
     * 条件分页查询经历列表
     */
    Page<Experience> selectList(@Param("userId") Long userId,
                                 @Param("type") String type);

    /**
     * 根据ID和用户ID查询经历
     */
    Experience selectById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 添加经历
     */
    int insert(Experience experience);

    /**
     * 修改经历
     */
    int update(Experience experience);

    /**
     * 根据ID和用户ID删除经历
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
