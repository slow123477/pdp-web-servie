package com.slow.service.service;

import com.slow.common.result.PageResult;
import com.slow.pojo.dto.ExperienceAddDTO;
import com.slow.pojo.dto.ExperienceQueryDTO;
import com.slow.pojo.dto.ExperienceUpdateDTO;
import com.slow.pojo.vo.ExperienceVO;

/**
 * 经历 Service
 */
public interface ExperienceService {

    /**
     * 经历列表查询（条件分页）
     */
    PageResult list(ExperienceQueryDTO queryDTO);

    /**
     * 根据ID查询经历
     */
    ExperienceVO getById(Long id);

    /**
     * 添加经历
     */
    void add(ExperienceAddDTO addDTO);

    /**
     * 修改经历
     */
    void update(ExperienceUpdateDTO updateDTO);

    /**
     * 删除经历
     */
    void delete(Long id);
}
