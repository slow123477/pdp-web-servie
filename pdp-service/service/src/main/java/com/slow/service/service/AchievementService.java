package com.slow.service.service;

import com.slow.common.result.PageResult;
import com.slow.pojo.dto.AchievementAddDTO;
import com.slow.pojo.dto.AchievementQueryDTO;
import com.slow.pojo.dto.AchievementUpdateDTO;
import com.slow.pojo.vo.AchievementVO;

/**
 * 成就 Service
 */
public interface AchievementService {

    /**
     * 成就列表查询（条件分页）
     */
    PageResult list(AchievementQueryDTO queryDTO);

    /**
     * 根据ID查询成就
     */
    AchievementVO getById(Long id);

    /**
     * 添加成就
     */
    void add(AchievementAddDTO addDTO);

    /**
     * 修改成就
     */
    void update(AchievementUpdateDTO updateDTO);

    /**
     * 删除成就
     */
    void delete(Long id);
}
