-- ============================================
-- 个人发展规划网站 — 数据库初始化脚本
-- MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS `personal_dev_plan`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `personal_dev_plan`;

-- --------------------------------------------
-- 1. 用户表 users
-- --------------------------------------------
CREATE TABLE `users` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username`        VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `email`           VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `student_id`      VARCHAR(50)  UNIQUE COMMENT '学号',
    `password`        VARCHAR(255) NOT NULL COMMENT '加密密码(BCrypt)',
    `real_name`       VARCHAR(50)  COMMENT '真实姓名',
    `avatar`          VARCHAR(255) COMMENT '头像URL',
    `major`           VARCHAR(100) COMMENT '专业',
    `grade_year`      TINYINT COMMENT '年级: 1/2/3/4',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_email` (`email`),
    INDEX `idx_student_id` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户表';

-- --------------------------------------------
-- 2. 课程表 courses
-- --------------------------------------------
CREATE TABLE `courses` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    `user_id`         BIGINT NOT NULL COMMENT '所属用户ID',
    `course_name`     VARCHAR(200) NOT NULL COMMENT '课程名称',
    `course_type`     VARCHAR(20)  NOT NULL COMMENT '类型: 必修/选修/通识',
    `credits`         DECIMAL(3,1) NOT NULL COMMENT '学分',
    `semester`        VARCHAR(20)  NOT NULL COMMENT '学期, 如2024-2025-1',
    `academic_year`   VARCHAR(20)  COMMENT '学年, 如2024-2025',
    `score`           DECIMAL(5,1) COMMENT '成绩(0-100)',
    `grade_point`     DECIMAL(3,2) COMMENT '绩点',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_semester` (`semester`),
    INDEX `idx_course_type` (`course_type`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '课程表';

-- --------------------------------------------
-- 3. 经历表 experiences
-- --------------------------------------------
CREATE TABLE `experiences` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '经历ID',
    `user_id`         BIGINT NOT NULL COMMENT '所属用户ID',
    `title`           VARCHAR(200) NOT NULL COMMENT '经历标题',
    `type`            VARCHAR(20)  NOT NULL COMMENT '类型: 竞赛/项目/实习',
    `start_date`      DATE NOT NULL COMMENT '开始日期',
    `end_date`        DATE COMMENT '结束日期(null=至今)',
    `description`     TEXT COMMENT '详细描述',
    `result`          VARCHAR(500) COMMENT '成果/奖项',
    `attachments`     JSON COMMENT '证明材料JSON数组',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_start_date` (`start_date`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '经历表';

-- --------------------------------------------
-- 4. 成就表 achievements
-- --------------------------------------------
CREATE TABLE `achievements` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成就ID',
    `user_id`         BIGINT NOT NULL COMMENT '所属用户ID',
    `name`            VARCHAR(200) NOT NULL COMMENT '成就名称',
    `level`           VARCHAR(20)  NOT NULL COMMENT '级别: 国家级/省级/校级/院级',
    `category`        VARCHAR(50)  COMMENT '分类: 学术/竞赛/志愿/其他',
    `issuer`          VARCHAR(200) COMMENT '颁发机构',
    `award_date`      DATE COMMENT '获得日期',
    `description`     TEXT COMMENT '描述',
    `certificate_url` VARCHAR(255) COMMENT '证书图片URL',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_level` (`level`),
    INDEX `idx_category` (`category`),
    INDEX `idx_award_date` (`award_date`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '成就表';

-- --------------------------------------------
-- 5. 角色表 roles
-- --------------------------------------------
CREATE TABLE `roles` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    `user_id`         BIGINT NOT NULL COMMENT '所属用户ID',
    `role_name`       VARCHAR(100) NOT NULL COMMENT '角色名称',
    `organization`    VARCHAR(200) COMMENT '所属组织/单位',
    `start_date`      DATE NOT NULL COMMENT '开始日期',
    `end_date`        DATE COMMENT '结束日期',
    `is_current`      TINYINT(1) DEFAULT 0 COMMENT '是否在职: 0否 1是',
    `responsibilities` TEXT COMMENT '职责描述',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_current` (`is_current`),
    INDEX `idx_start_date` (`start_date`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '角色表';

-- --------------------------------------------
-- 6. AI 分析记录表 ai_analysis
-- --------------------------------------------
CREATE TABLE `ai_analysis` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分析记录ID',
    `user_id`         BIGINT NOT NULL COMMENT '所属用户ID',
    `analysis_type`   VARCHAR(50) COMMENT '分析维度组合描述',
    `dimensions`      JSON COMMENT '雷达图维度数据',
    `report_content`  TEXT COMMENT '完整报告内容',
    `strengths`       JSON COMMENT '核心优势JSON数组',
    `weaknesses`      JSON COMMENT '待提升领域JSON数组',
    `suggestions`     JSON COMMENT '发展建议JSON数组',
    `competitiveness` JSON COMMENT '竞争力评估JSON',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI分析记录表';

-- --------------------------------------------
-- 7. 用户设置表 user_settings
-- --------------------------------------------
CREATE TABLE `user_settings` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设置ID',
    `user_id`         BIGINT NOT NULL UNIQUE COMMENT '所属用户ID',
    `gpa_standard`    VARCHAR(20) DEFAULT '4.0' COMMENT 'GPA标准: 4.0/5.0/百分制加权',
    `gpa_scale`       JSON COMMENT '绩点对照表配置',
    `ai_dimensions`   JSON COMMENT 'AI分析维度开关配置',
    `theme`           VARCHAR(20) DEFAULT 'light' COMMENT '主题: light/dark',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户设置表';

-- --------------------------------------------
-- 8. 数据导入导出记录表 data_operations
-- --------------------------------------------
CREATE TABLE `data_operations` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    `user_id`         BIGINT NOT NULL COMMENT '所属用户ID',
    `operation_type`  VARCHAR(10) NOT NULL COMMENT '操作类型: import/export',
    `data_type`       VARCHAR(20) NOT NULL COMMENT '数据类型: 课程/成绩/经历/成就/角色',
    `file_url`        VARCHAR(255) COMMENT '文件地址',
    `status`          VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/success/fail',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '数据导入导出记录表';