package com.slow.service.service;

import com.slow.common.result.PageResult;
import com.slow.pojo.dto.DataOperationQueryDTO;
import com.slow.pojo.vo.DataExportResultVO;
import com.slow.pojo.vo.DataImportResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据操作 Service
 */
public interface DataOperationService {

    /**
     * 导入数据
     * @param file 上传的文件
     * @param dataType 数据类型
     * @return 导入结果
     */
    DataImportResultVO importData(MultipartFile file, String dataType);

    /**
     * 导出数据
     * @param dataType 数据类型
     * @return 导出结果
     */
    DataExportResultVO exportData(String dataType);

    /**
     * 查询操作记录列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult listOperations(DataOperationQueryDTO queryDTO);
}
