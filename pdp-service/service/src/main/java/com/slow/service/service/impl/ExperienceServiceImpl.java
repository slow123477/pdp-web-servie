package com.slow.service.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.slow.common.exception.BusinessException;
import com.slow.common.result.PageResult;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.ExperienceAddDTO;
import com.slow.pojo.dto.ExperienceQueryDTO;
import com.slow.pojo.dto.ExperienceUpdateDTO;
import com.slow.pojo.entity.Experience;
import com.slow.pojo.vo.ExperienceVO;
import com.alibaba.fastjson2.JSON;
import com.slow.service.mapper.ExperienceMapper;
import com.slow.service.service.ExperienceService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 经历 Service 实现
 */
@Slf4j
@Service
public class ExperienceServiceImpl implements ExperienceService {

    @Autowired
    private ExperienceMapper experienceMapper;

    @Override
    public PageResult list(ExperienceQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Experience> page = experienceMapper.selectList(userId, queryDTO.getType());
        List<ExperienceVO> rows = page.getResult().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), rows);
    }

    @Override
    public ExperienceVO getById(Long id) {
        Long userId = getCurrentUserId();
        Experience experience = experienceMapper.selectById(id, userId);
        if (experience == null) {
            throw new BusinessException("经历不存在");
        }
        return convertToVO(experience);
    }

    @Override
    public void add(ExperienceAddDTO addDTO) {
        Long userId = getCurrentUserId();
        Experience experience = new Experience();
        BeanUtils.copyProperties(addDTO, experience);
        experience.setUserId(userId);
        if (addDTO.getAttachments() != null) {
            experience.setAttachments(JSON.toJSONString(addDTO.getAttachments()));
        }
        experienceMapper.insert(experience);
    }

    @Override
    public void update(ExperienceUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        Experience existing = experienceMapper.selectById(updateDTO.getId(), userId);
        if (existing == null) {
            throw new BusinessException("经历不存在");
        }
        Experience experience = new Experience();
        BeanUtils.copyProperties(updateDTO, experience);
        experience.setUserId(userId);
        if (updateDTO.getAttachments() != null) {
            experience.setAttachments(JSON.toJSONString(updateDTO.getAttachments()));
        }
        experienceMapper.update(experience);
    }

    @Override
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        int rows = experienceMapper.deleteById(id, userId);
        if (rows == 0) {
            throw new BusinessException("经历不存在");
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
    private ExperienceVO convertToVO(Experience experience) {
        ExperienceVO vo = new ExperienceVO();
        BeanUtils.copyProperties(experience, vo);
        return vo;
    }
}
