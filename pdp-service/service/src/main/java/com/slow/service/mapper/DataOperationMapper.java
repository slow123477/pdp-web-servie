package com.slow.service.mapper;

import com.github.pagehelper.Page;
import com.slow.pojo.entity.DataOperation;
import org.apache.ibatis.annotations.Param;

/**
 * 数据操作记录 Mapper
 */
public interface DataOperationMapper {

    /**
     * 添加操作记录
     */
    int insert(DataOperation dataOperation);

    /**
     * 条件分页查询操作记录列表
     */
    Page<DataOperation> selectList(@Param("userId") Long userId,
                                    @Param("operationType") String operationType,
                                    @Param("status") String status);

    /**
     * 根据ID查询操作记录
     */
    DataOperation selectById(@Param("id") Long id);

    /**
     * 更新操作记录状态
     */
    int updateStatus(DataOperation dataOperation);
}
