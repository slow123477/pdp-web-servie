package com.slow.service.service.impl;

import com.slow.common.exception.BusinessException;
import com.slow.service.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传 Service 实现
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Value("${app.upload.path:${user.dir}/uploads}")
    private String uploadPath;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);

        // 生成按日期分层的子目录：yyyy/MM/dd
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);

        try {
            Path targetDir = Paths.get(uploadPath, dateDir);
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(filename);
            file.transferTo(targetPath.toFile());
            log.info("文件上传成功: {}", targetPath);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件保存失败");
        }

        return "/upload/" + dateDir + "/" + filename;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
