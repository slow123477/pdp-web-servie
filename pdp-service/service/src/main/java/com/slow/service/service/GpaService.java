package com.slow.service.service;

import com.slow.pojo.vo.GpaVO;
import com.slow.pojo.vo.SemesterGpaVO;

import java.util.List;

/**
 * GPA 计算 Service
 */
public interface GpaService {

    /**
     * 计算 GPA（累计 GPA + 各学期 GPA）
     *
     * @param standard GPA 标准：4.0 / 5.0 / weighted，为空时使用用户默认设置
     */
    GpaVO calculate(String standard);

    /**
     * 获取 GPA 趋势数据（各学期 GPA 列表）
     */
    List<SemesterGpaVO> trend();
}
