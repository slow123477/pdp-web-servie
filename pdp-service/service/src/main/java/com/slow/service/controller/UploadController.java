package com.slow.service.controller;

import com.slow.common.result.Result;
import com.slow.service.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 Controller
 */
@Tag(name = "文件上传", description = "通用文件上传接口")
@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 通用文件上传
     */
    @Operation(summary = "通用文件上传", description = "上传任意文件，返回文件访问路径")
    @PostMapping("/upload")
    public Result<String> upload(
            @Parameter(description = "文件")
            @RequestParam("file") MultipartFile file) {
        String url = uploadService.uploadFile(file);
        return Result.success("上传成功", url);
    }
}
