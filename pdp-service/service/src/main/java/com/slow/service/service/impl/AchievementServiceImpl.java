package com.slow.service.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.slow.common.exception.BusinessException;
import com.slow.common.result.PageResult;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.AchievementAddDTO;
import com.slow.pojo.dto.AchievementQueryDTO;
import com.slow.pojo.dto.AchievementUpdateDTO;
import com.slow.pojo.entity.Achievement;
import com.slow.pojo.vo.AchievementVO;
import com.slow.service.mapper.AchievementMapper;
import com.slow.service.service.AchievementService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 成就 Service 实现
 */
@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Override
    public PageResult list(AchievementQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Achievement> page = achievementMapper.selectList(userId, queryDTO.getLevel(), queryDTO.getCategory());
        List<AchievementVO> rows = page.getResult().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), rows);
    }

    @Override
    public AchievementVO getById(Long id) {
        Long userId = getCurrentUserId();
        Achievement achievement = achievementMapper.selectById(id, userId);
        if (achievement == null) {
            throw new BusinessException("成就不存在");
        }
        return convertToVO(achievement);
    }

    @Override
    public void add(AchievementAddDTO addDTO) {
        Long userId = getCurrentUserId();
        Achievement achievement = new Achievement();
        BeanUtils.copyProperties(addDTO, achievement);
        achievement.setUserId(userId);
        achievementMapper.insert(achievement);
    }

    @Override
    public void update(AchievementUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        Achievement existing = achievementMapper.selectById(updateDTO.getId(), userId);
        if (existing == null) {
            throw new BusinessException("成就不存在");
        }
        Achievement achievement = new Achievement();
        BeanUtils.copyProperties(updateDTO, achievement);
        achievement.setUserId(userId);
        achievementMapper.update(achievement);
    }

    @Override
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        int rows = achievementMapper.deleteById(id, userId);
        if (rows == 0) {
            throw new BusinessException("成就不存在");
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
    private AchievementVO convertToVO(Achievement achievement) {
        AchievementVO vo = new AchievementVO();
        BeanUtils.copyProperties(achievement, vo);
        return vo;
    }
}
