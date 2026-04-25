package com.slow.service.mapper;

import com.github.pagehelper.Page;
import com.slow.pojo.entity.Achievement;
import org.apache.ibatis.annotations.Param;

/**
 * 成就 Mapper
 */
public interface AchievementMapper {

    /**
     * 条件分页查询成就列表
     */
    Page<Achievement> selectList(@Param("userId") Long userId,
                                  @Param("level") String level,
                                  @Param("category") String category);

    /**
     * 根据ID和用户ID查询成就
     */
    Achievement selectById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 添加成就
     */
    int insert(Achievement achievement);

    /**
     * 修改成就
     */
    int update(Achievement achievement);

    /**
     * 根据ID和用户ID删除成就
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
