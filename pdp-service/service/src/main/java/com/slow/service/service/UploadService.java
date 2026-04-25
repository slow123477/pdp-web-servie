package com.slow.service.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 Service
 */
public interface UploadService {

    /**
     * 通用文件上传
     * @param file 上传的文件
     * @return 文件访问路径
     */
    String uploadFile(MultipartFile file);
}
