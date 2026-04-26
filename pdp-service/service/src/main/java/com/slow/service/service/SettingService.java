package com.slow.service.service;

import com.slow.pojo.dto.UserSettingUpdateDTO;
import com.slow.pojo.vo.UserSettingVO;

/**
 * 设置 Service
 */
public interface SettingService {

    /**
     * 查询当前用户设置
     */
    UserSettingVO getSetting();

    /**
     * 修改当前用户设置
     */
    void updateSetting(UserSettingUpdateDTO updateDTO);
}
