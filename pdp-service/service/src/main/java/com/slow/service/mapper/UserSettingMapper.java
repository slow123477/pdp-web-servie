package com.slow.service.mapper;

import com.slow.pojo.entity.UserSetting;
import org.apache.ibatis.annotations.Param;

/**
 * 用户设置 Mapper
 */
public interface UserSettingMapper {

    /**
     * 根据用户ID查询设置
     */
    UserSetting selectByUserId(@Param("userId") Long userId);

    /**
     * 插入用户设置
     */
    int insert(UserSetting userSetting);

    /**
     * 根据用户ID更新设置
     */
    int updateByUserId(UserSetting userSetting);
}
