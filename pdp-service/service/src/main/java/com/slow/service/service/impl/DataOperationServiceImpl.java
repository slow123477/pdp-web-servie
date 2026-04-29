package com.slow.service.service.impl;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.slow.common.exception.BusinessException;
import com.slow.common.result.PageResult;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.DataOperationQueryDTO;
import com.slow.pojo.entity.Achievement;
import com.slow.pojo.entity.Course;
import com.slow.pojo.entity.DataOperation;
import com.slow.pojo.entity.Experience;
import com.slow.pojo.entity.Role;
import com.slow.pojo.vo.DataExportResultVO;
import com.slow.pojo.vo.DataImportResultVO;
import com.slow.pojo.vo.DataOperationVO;
import com.slow.service.excel.AchievementExcelData;
import com.slow.service.excel.CourseExcelData;
import com.slow.service.excel.ExperienceExcelData;
import com.slow.service.excel.RoleExcelData;
import com.slow.service.mapper.AchievementMapper;
import com.slow.service.mapper.CourseMapper;
import com.slow.service.mapper.DataOperationMapper;
import com.slow.service.mapper.ExperienceMapper;
import com.slow.service.mapper.RoleMapper;
import com.slow.service.service.DataOperationService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 数据操作 Service 实现
 */
@Slf4j
@Service
public class DataOperationServiceImpl implements DataOperationService {

    private static final Set<String> VALID_DATA_TYPES = Set.of("课程", "成绩", "经历", "成就", "角色");

    @Value("${app.upload.path:${user.dir}/uploads}")
    private String uploadPath;

    @Autowired
    private DataOperationMapper dataOperationMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ExperienceMapper experienceMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataImportResultVO importData(MultipartFile file, String dataType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        if (!VALID_DATA_TYPES.contains(dataType)) {
            throw new BusinessException("不支持的数据类型：" + dataType);
        }

        Long userId = getCurrentUserId();

        // 1. 保存上传文件到磁盘（transferTo 会消费 MultipartFile 的流，必须先做）
        Path savedPath = saveFile(file, "import");
        String relativePath = uploadPathToRelative(savedPath);
        String fileUrl = "/upload/" + relativePath;

        // 2. 创建操作记录
        DataOperation operation = new DataOperation();
        operation.setUserId(userId);
        operation.setOperationType("import");
        operation.setDataType(dataType);
        operation.setFileUrl(fileUrl);
        operation.setStatus("pending");
        dataOperationMapper.insert(operation);

        try {
            // 3. 根据数据类型解析并导入（从已保存的磁盘文件读取）
            switch (dataType) {
                case "课程", "成绩" -> importCourses(savedPath, userId);
                case "经历" -> importExperiences(savedPath, userId);
                case "成就" -> importAchievements(savedPath, userId);
                case "角色" -> importRoles(savedPath, userId);
                default -> throw new BusinessException("不支持的数据类型");
            }

            // 4. 更新状态为成功
            operation.setStatus("success");
            dataOperationMapper.updateStatus(operation);

            DataImportResultVO result = new DataImportResultVO();
            result.setOperationId(operation.getId());
            result.setStatus("success");
            return result;
        } catch (Exception e) {
            log.error("数据导入失败", e);
            operation.setStatus("fail");
            dataOperationMapper.updateStatus(operation);
            throw new BusinessException("导入失败：" + e.getMessage());
        }
    }

    @Override
    public DataExportResultVO exportData(String dataType) {
        if (!VALID_DATA_TYPES.contains(dataType)) {
            throw new BusinessException("不支持的数据类型：" + dataType);
        }

        Long userId = getCurrentUserId();

        // 1. 创建操作记录
        DataOperation operation = new DataOperation();
        operation.setUserId(userId);
        operation.setOperationType("export");
        operation.setDataType(dataType);
        operation.setStatus("pending");
        dataOperationMapper.insert(operation);

        try {
            // 2. 生成导出文件
            String filename = generateFilename(dataType, "xlsx");
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path targetDir = Paths.get(uploadPath, "export", dateDir);
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(filename);

            // 3. 查询数据并写入 Excel
            switch (dataType) {
                case "课程", "成绩" -> exportCourses(userId, targetPath);
                case "经历" -> exportExperiences(userId, targetPath);
                case "成就" -> exportAchievements(userId, targetPath);
                case "角色" -> exportRoles(userId, targetPath);
                default -> throw new BusinessException("不支持的数据类型");
            }

            String fileUrl = "/upload/export/" + dateDir + "/" + filename;

            // 4. 更新记录状态
            operation.setStatus("success");
            operation.setFileUrl(fileUrl);
            dataOperationMapper.updateStatus(operation);

            DataExportResultVO result = new DataExportResultVO();
            result.setDownloadUrl(fileUrl);
            result.setOperationId(operation.getId());
            return result;
        } catch (Exception e) {
            log.error("数据导出失败", e);
            operation.setStatus("fail");
            dataOperationMapper.updateStatus(operation);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    @Override
    public PageResult listOperations(DataOperationQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<DataOperation> page = dataOperationMapper.selectList(userId, queryDTO.getOperationType(), queryDTO.getStatus());

        List<DataOperationVO> rows = page.getResult().stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageResult(page.getTotal(), rows);
    }

    // ==================== 导入逻辑 ====================

    private void importCourses(Path filePath, Long userId) {
        List<CourseExcelData> list = EasyExcel.read(filePath.toString())
                .head(CourseExcelData.class)
                .sheet()
                .doReadSync();

        for (CourseExcelData data : list) {
            if (data.getCourseName() == null || data.getCourseName().trim().isEmpty()) {
                continue;
            }
            Course course = new Course();
            BeanUtils.copyProperties(data, course);
            course.setUserId(userId);
            courseMapper.insert(course);
        }
    }

    private void importExperiences(Path filePath, Long userId) {
        List<ExperienceExcelData> list = EasyExcel.read(filePath.toString())
                .head(ExperienceExcelData.class)
                .sheet()
                .doReadSync();

        for (ExperienceExcelData data : list) {
            if (data.getTitle() == null || data.getTitle().trim().isEmpty()) {
                continue;
            }
            Experience experience = new Experience();
            BeanUtils.copyProperties(data, experience);
            experience.setUserId(userId);
            experience.setStartDate(parseDate(data.getStartDate()));
            experience.setEndDate(parseDate(data.getEndDate()));
            experienceMapper.insert(experience);
        }
    }

    private void importAchievements(Path filePath, Long userId) {
        List<AchievementExcelData> list = EasyExcel.read(filePath.toString())
                .head(AchievementExcelData.class)
                .sheet()
                .doReadSync();

        for (AchievementExcelData data : list) {
            if (data.getName() == null || data.getName().trim().isEmpty()) {
                continue;
            }
            Achievement achievement = new Achievement();
            BeanUtils.copyProperties(data, achievement);
            achievement.setUserId(userId);
            achievement.setAwardDate(parseDate(data.getAwardDate()));
            achievementMapper.insert(achievement);
        }
    }

    private void importRoles(Path filePath, Long userId) {
        List<RoleExcelData> list = EasyExcel.read(filePath.toString())
                .head(RoleExcelData.class)
                .sheet()
                .doReadSync();

        for (RoleExcelData data : list) {
            if (data.getRoleName() == null || data.getRoleName().trim().isEmpty()) {
                continue;
            }
            Role role = new Role();
            BeanUtils.copyProperties(data, role);
            role.setUserId(userId);
            role.setStartDate(parseDate(data.getStartDate()));
            role.setEndDate(parseDate(data.getEndDate()));
            if (data.getIsCurrent() == null) {
                role.setIsCurrent(0);
            }
            roleMapper.insert(role);
        }
    }

    // ==================== 导出逻辑 ====================

    private void exportCourses(Long userId, Path targetPath) {
        PageHelper.startPage(1, 10000);
        Page<Course> page = courseMapper.selectList(userId, null, null);

        List<CourseExcelData> list = page.getResult().stream().map(course -> {
            CourseExcelData data = new CourseExcelData();
            BeanUtils.copyProperties(course, data);
            return data;
        }).collect(Collectors.toList());

        EasyExcel.write(targetPath.toString(), CourseExcelData.class).sheet("课程数据").doWrite(list);
    }

    private void exportExperiences(Long userId, Path targetPath) {
        PageHelper.startPage(1, 10000);
        Page<Experience> page = experienceMapper.selectList(userId, null);

        List<ExperienceExcelData> list = page.getResult().stream().map(exp -> {
            ExperienceExcelData data = new ExperienceExcelData();
            BeanUtils.copyProperties(exp, data);
            data.setStartDate(formatDate(exp.getStartDate()));
            data.setEndDate(formatDate(exp.getEndDate()));
            return data;
        }).collect(Collectors.toList());

        EasyExcel.write(targetPath.toString(), ExperienceExcelData.class).sheet("经历数据").doWrite(list);
    }

    private void exportAchievements(Long userId, Path targetPath) {
        PageHelper.startPage(1, 10000);
        Page<Achievement> page = achievementMapper.selectList(userId, null, null);

        List<AchievementExcelData> list = page.getResult().stream().map(ach -> {
            AchievementExcelData data = new AchievementExcelData();
            BeanUtils.copyProperties(ach, data);
            data.setAwardDate(formatDate(ach.getAwardDate()));
            return data;
        }).collect(Collectors.toList());

        EasyExcel.write(targetPath.toString(), AchievementExcelData.class).sheet("成就数据").doWrite(list);
    }

    private void exportRoles(Long userId, Path targetPath) {
        PageHelper.startPage(1, 10000);
        Page<Role> page = roleMapper.selectList(userId, null);

        List<RoleExcelData> list = page.getResult().stream().map(role -> {
            RoleExcelData data = new RoleExcelData();
            BeanUtils.copyProperties(role, data);
            data.setStartDate(formatDate(role.getStartDate()));
            data.setEndDate(formatDate(role.getEndDate()));
            return data;
        }).collect(Collectors.toList());

        EasyExcel.write(targetPath.toString(), RoleExcelData.class).sheet("角色数据").doWrite(list);
    }

    // ==================== 工具方法 ====================

    private Path saveFile(MultipartFile file, String subDir) {
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);

        try {
            Path targetDir = Paths.get(uploadPath, subDir, dateDir);
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(filename);
            file.transferTo(targetPath.toFile());
            return targetPath;
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件保存失败");
        }
    }

    private String uploadPathToRelative(Path savedPath) {
        Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path absolute = savedPath.toAbsolutePath().normalize();
        return base.relativize(absolute).toString().replace('\\', '/');
    }

    private String generateFilename(String dataType, String ext) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String typeEn = switch (dataType) {
            case "课程", "成绩" -> "courses";
            case "经历" -> "experiences";
            case "成就" -> "achievements";
            case "角色" -> "roles";
            default -> "data";
        };
        return typeEn + "_" + timestamp + "." + ext;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "";
    }

    private Long getCurrentUserId() {
        Claims claims = ThreadLocalUtil.get();
        if (claims == null) {
            throw new BusinessException("未登录");
        }
        Object idObj = claims.get("id");
        if (idObj == null) {
            throw new BusinessException("未登录");
        }
        return Long.valueOf(idObj.toString());
    }

    private DataOperationVO convertToVO(DataOperation operation) {
        DataOperationVO vo = new DataOperationVO();
        BeanUtils.copyProperties(operation, vo);
        return vo;
    }
}
