package com.slow.service.service;

import com.slow.pojo.vo.DashboardVO;

/**
 * 仪表盘 Service
 */
public interface DashboardService {

    /**
     * 获取仪表盘聚合数据
     */
    DashboardVO getDashboardData();
}
