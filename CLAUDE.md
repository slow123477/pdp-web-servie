# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供在本仓库中进行代码工作的指导。

## 项目概览

这是一个个人发展规划网站（Personal Development Planning），采用 Vue 3 前端 + Spring Boot 后端架构。

## 仓库结构

- `pdp-web/` — 前端（Vue 3 + Vite + Element Plus）
- `pdp-service/` — 后端（Spring Boot 3.2.5，Java 17，Maven 多模块）
- `sql/` — 数据库脚本
- `接口文档.md` — API 接口文档
- `数据库设计.md` — 数据库设计文档

### 后端模块（`pdp-service/`）

Maven 多模块项目：

- `common/` — 公共工具类：`Result<T>` 统一响应包装类、`PageResult`、`JwtUtil`、`ThreadLocalUtil`、`BusinessException`、`GlobalExceptionHandler`
- `pojo/` — DTO、实体类、VO。包路径：`com.slow.pojo`
- `service/` — 主 Spring Boot 应用。包路径：`com.slow.service`。包含控制器、配置、拦截器、Mapper（XML 位于 `src/main/resources/mapper/`）

后端关键模式：
- REST 控制器返回 `Result<T>`，其中 `code`（1 = 成功，0 = 失败）、`msg`、`data`
- JWT Token 认证，通过 `JwtTokenInterceptor` 拦截；Token 通过 `token` 请求头传递
- MyBatis 配置 `map-underscore-to-camel-case: true`
- 分页使用 PageHelper
- CORS 配置在 `CorsConfig.java` 中

### 前端（`pdp-web/`）

- Vue 3 组合式 API，搭配 Pinia、Vue Router、Element Plus
- Vite 开发服务器运行在 5173 端口，将 `/api` 代理到 `http://localhost:8080`
- 配置自动导入 Vue、Vue Router、Pinia API 以及 Element Plus 组件
- Axios 实例位于 `src/utils/request.js`，带有请求/响应拦截器
  - 从 Pinia 用户状态中获取 `token` 并附加到请求头
  - 遇到 401 时：清除登录状态并重定向到 `/login`
  - 期望响应格式为 `{ code, msg, data }`
- 路由守卫：非公开路由在未登录时重定向到 `/login`
- 公开路由：`/login`、`/register`
- 所有需要认证的页面均在 `AppLayout.vue` 内渲染

## 常用命令

### 前端（`pdp-web/`）

```bash
cd pdp-web
npm install
npm run dev          # 启动 Vite 开发服务器（5173 端口）
npm run build        # 生产构建
npm run preview      # 预览生产构建产物
npm run lint         # 运行 oxlint + eslint --fix
npm run lint:oxlint  # 仅运行 oxlint
npm run lint:eslint  # 仅运行 eslint
npm run format       # 对 src/ 目录运行 prettier
```

### 后端（`pdp-service/`）

```bash
cd pdp-service
mvn compile          # 编译所有模块
mvn test             # 运行测试
mvn spring-boot:run -pl service   # 运行 service 模块
mvn clean package    # 构建 JAR 包
```

单独构建 service 模块：
```bash
cd pdp-service/service
mvn spring-boot:run
```

### 数据库

MySQL 数据库名为 `personal_dev_plan`，地址 `localhost:3306`。连接凭据和详细配置见 `pdp-service/service/src/main/resources/application.yml`。初始表结构在 `sql/sqlfile.sql` 中。

## 关键配置文件

- `pdp-web/vite.config.js` — Vite 配置（代理、自动导入、Element Plus Resolver）
- `pdp-web/eslint.config.js` — ESLint flat 配置（Vue、oxlint、Prettier）
- `pdp-web/.oxlintrc.json` — Oxlint 规则
- `pdp-service/pom.xml` — 父 POM，管理依赖版本
- `pdp-service/service/pom.xml` — service 模块 POM
- `pdp-service/service/src/main/resources/application.yml` — Spring Boot 配置（8080 端口、MySQL、MyBatis、PageHelper）
