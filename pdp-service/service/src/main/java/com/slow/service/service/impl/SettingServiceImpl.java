package com.slow.service.service.impl;

import com.alibaba.fastjson2.JSON;
import com.slow.common.exception.BusinessException;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.UserSettingUpdateDTO;
import com.slow.pojo.entity.UserSetting;
import com.slow.pojo.vo.UserSettingVO;
import com.slow.service.mapper.UserSettingMapper;
import com.slow.service.service.SettingService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 设置 Service 实现
 */
@Slf4j
@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    private UserSettingMapper userSettingMapper;

    @Override
    public UserSettingVO getSetting() {
        Long userId = getCurrentUserId();
        UserSetting userSetting = userSettingMapper.selectByUserId(userId);

        if (userSetting == null) {
            UserSettingVO vo = new UserSettingVO();
            vo.setUserId(userId);
            vo.setGpaStandard("4.0");
            vo.setTheme("light");
            return vo;
        }

        UserSettingVO vo = new UserSettingVO();
        BeanUtils.copyProperties(userSetting, vo);
        vo.setUserId(userId);
        return vo;
    }

    @Override
    public void updateSetting(UserSettingUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        UserSetting existing = userSettingMapper.selectByUserId(userId);

        if (existing == null) {
            UserSetting defaultSetting = new UserSetting();
            defaultSetting.setUserId(userId);
            defaultSetting.setGpaStandard("4.0");
            defaultSetting.setTheme("light");
            userSettingMapper.insert(defaultSetting);
        }

        UserSetting userSetting = new UserSetting();
        BeanUtils.copyProperties(updateDTO, userSetting);
        userSetting.setUserId(userId);

        if (updateDTO.getGpaScale() != null) {
            userSetting.setGpaScale(JSON.toJSONString(updateDTO.getGpaScale()));
        }
        if (updateDTO.getAiDimensions() != null) {
            userSetting.setAiDimensions(JSON.toJSONString(updateDTO.getAiDimensions()));
        }

        userSettingMapper.updateByUserId(userSetting);
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
