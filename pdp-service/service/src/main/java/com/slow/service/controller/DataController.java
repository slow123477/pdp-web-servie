package com.slow.service.controller;

import com.slow.common.result.PageResult;
import com.slow.common.result.Result;
import com.slow.pojo.dto.DataOperationQueryDTO;
import com.slow.pojo.vo.DataExportResultVO;
import com.slow.pojo.vo.DataImportResultVO;
import com.slow.service.service.DataOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据操作 Controller
 */
@Tag(name = "数据操作", description = "数据导入导出")
@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataOperationService dataOperationService;

    /**
     * 数据导入
     */
    @Operation(summary = "数据导入", description = "导入 CSV/Excel 格式的数据文件")
    @PostMapping("/import")
    public Result<DataImportResultVO> importData(
            @Parameter(description = "文件")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "数据类型：课程/成绩/经历/成就/角色")
            @RequestParam("dataType") String dataType) {
        DataImportResultVO result = dataOperationService.importData(file, dataType);
        return Result.success("导入成功", result);
    }

    /**
     * 数据导出
     */
    @Operation(summary = "数据导出", description = "导出指定类型的数据为 Excel 文件")
    @GetMapping("/export")
    public Result<DataExportResultVO> exportData(
            @Parameter(description = "数据类型：课程/成绩/经历/成就/角色")
            @RequestParam("dataType") String dataType) {
        DataExportResultVO result = dataOperationService.exportData(dataType);
        return Result.success("导出成功", result);
    }

    /**
     * 导入导出记录查询
     */
    @Operation(summary = "导入导出记录查询", description = "查询数据导入导出操作历史记录")
    @GetMapping("/operations")
    public Result<PageResult> listOperations(DataOperationQueryDTO queryDTO) {
        PageResult pageResult = dataOperationService.listOperations(queryDTO);
        return Result.success(pageResult);
    }
}
