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
- `需求文档-个人发展规划网站-2026-04-22.md` — 需求文档

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

### 分层规范（DTO / VO / Entity）

`pojo` 模块下已划分 `entity`、`dto`、`vo` 三个包，严禁在 Controller 层直接复用 Entity：

| 类型 | 用途 | 约束 |
|------|------|------|
| **Entity** | 数据库实体（如 `Course`、`User`） | 与表一一对应，含审计字段 `createdAt`/`updatedAt`、关联字段 `userId` |
| **DTO** | 接收请求参数 | 字段严格对接口文档，**不含**数据库自生成或后端注入字段（如 `id`、`userId`、`createdAt`）；加 `jakarta.validation` 注解做声明式校验 |
| **VO** | 返回响应数据 | 过滤敏感/无用字段（如 `userId`），可包含计算字段 |

**转换原则**：Service 层负责 DTO → Entity、Entity → VO 的转换，使用 `BeanUtils.copyProperties`（service 模块有 Spring 依赖）。GET 查询参数超过 2 个时，封装为 DTO 并用 `@ModelAttribute` 接收。

### Swagger / Knife4j 规范

- 项目使用 **Knife4j 4.4.0**（OpenAPI 3 + SpringDoc），适配 Spring Boot 3.x / Jakarta EE
- **不可使用**旧版 Springfox 的 `Docket` 方式（不支持 `jakarta` 包名）
- 全局 Token 配置在 `Knife4jConfig.java` 中，通过 `OpenAPI` Bean 设置 `SecurityScheme`（type = APIKEY, in = HEADER）
- Controller 层使用 `@Tag`、`@Operation`、`@Parameter` 注解生成接口文档
- **注意**：`@Schema` 注解来自 `swagger-annotations`，pojo 模块**未引入**该依赖。若需在 DTO/VO 上加 Swagger 字段说明，需先在 `pojo/pom.xml` 中添加依赖；目前 DTO/VO 保持无注解，文档说明集中在 Controller 层
- 文档地址：`http://localhost:8080/doc.html`

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

---

## 开发前检查清单

每次开始编写或修改代码前，必须按以下顺序核对文档，避免字段类型、接口格式、业务逻辑错误：

1. **读需求文档**（`需求文档-个人发展规划网站-*.md`）—— 明确功能目标和业务规则。
2. **读数据库设计文档**（`数据库设计.md`）—— 确认表结构、字段类型、约束、关联关系，特别注意 `JSON`、数组、枚举等非标量类型。
3. **读接口文档**（`接口文档.md`）—— 确认接口路径、请求方法、请求参数、响应格式，严格按照请求示例设计 DTO/VO。
4. **确认思路后再动手**—— 如果文档之间有矛盾或描述不清，**先问用户，不要自行推断**。

> **教训**：经历模块 `attachments` 字段，接口文档已明确为对象数组 `[{name, url}]`，数据库设计为 `JSON`，但因未仔细核对文档，DTO 里写成了 `String`，导致前端传数组时反序列化失败。

---

## 踩坑记录

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| `favicon.ico` 404 导致 ERROR 日志刷屏 | `NoResourceFoundException` 被 `GlobalExceptionHandler` 的 `@ExceptionHandler(Exception.class)` 兜底捕获，打印 error 日志 | 新增专门的 `@ExceptionHandler(NoResourceFoundException.class)`，返回 404 且不打印日志 |
| pojo 模块使用 `@Schema` 编译失败 | pojo 模块没有引入 `swagger-annotations` 依赖 | 去掉 DTO/VO 上的 `@Schema` 注解，或给 pojo 模块加依赖 |
| `Result<Void>` 与 `Result.success("msg")` 类型冲突 | `Result` 类没有单参数的 `success(String msg)` 方法 | POST/PUT/DELETE 返回 `Result<Object>`，使用 `Result.success("msg", null)` |

## 开发进度

### 后端

| 模块 | 状态 | 说明 |
|------|------|------|
| 认证 (`/auth`) | ✅ 已完成 | `register` / `login` / `getCurrentUser` 3个接口 |
| 用户模块 (`/users`) | ✅ 已完成 | `getById` / `updateProfile` / `updatePassword` / `uploadAvatar` 4个接口 |
| 课程管理 (`/courses`) | ✅ 已完成 | 5 个接口（列表分页、详情、增删改），DTO/VO 模式 |
| 成就管理 (`/achievements`) | ✅ 已完成 | 5 个接口（列表分页、详情、增删改） |
| 经历管理 (`/experiences`) | ✅ 已完成 | 5 个接口（列表分页、详情、增删改） |
| 文件上传 (`/upload`) | ✅ 已完成 | 通用文件上传接口 |
| JWT 工具与拦截器 | ✅ 已完成 | `JwtUtil` + `JwtTokenInterceptor` + `ThreadLocalUtil` |
| 全局异常处理 | ✅ 已完成 | `BusinessException` + `NoResourceFoundException` 静默处理 |
| Knife4j 文档 | ✅ 已完成 | `Knife4jConfig` 配置全局 Token |
| 仪表盘 (`/dashboard`) | ✅ 已完成 | 1 个接口（聚合统计），复用 GPA/经历/成就/角色数据 |
| 角色管理 (`/roles`) | ✅ 已完成 | 5 个接口（列表分页、详情、增删改），DTO/VO 模式 |
| GPA 计算 (`/gpa`) | ✅ 已完成 | 2 个接口（计算、趋势），复用 Course 数据 |
| AI 分析 (`/ai-analysis`) | ✅ 已完成 | 4 个接口（生成、列表、详情、删除），集成 Spring AI 真实调用 |
| 系统设置 (`/settings`) | ✅ 已完成 | 2 个接口（查询、修改） |
| 导入导出 (`/data`) | ✅ 已完成 | 3 个接口（导入、导出、记录查询），集成 EasyExcel |

### 前端

| 页面 | 状态 | 说明 |
|------|------|------|
| 登录 (`/login`) | ✅ UI+API 完成 | 已对接 `/auth/login`，前后端联调通过 |
| 注册 (`/register`) | ✅ UI+API 完成 | 已对接 `/auth/register`，前后端联调通过 |
| 课程管理 (`/courses`) | ✅ UI+API 完成 | 增删改查弹窗齐全，已对接真实 API |
| 经历 (`/experiences`) | ✅ UI+API 完成 | 完整 CRUD + 附件上传，已对接真实 API |
| 成就 (`/achievements`) | ✅ UI+API 完成 | 完整 CRUD + 证书上传，已对接真实 API |
| 角色 (`/roles`) | ✅ UI+API 完成 | 完整 CRUD，已对接真实 API |
| 个人中心 (`/profile`) | ✅ UI+API 完成 | 资料修改、密码修改、头像上传，已对接真实 API |
| 仪表盘 (`/dashboard`) | ✅ UI+API 完成 | 真实 UI + API 调用，已对接真实 API |
| 成绩/GPA (`/grades`) | ✅ UI+API 完成 | GPA 计算、趋势图、成绩列表，已对接真实 API |
| 布局/路由/请求封装 | ✅ 完成 | `AppLayout`、`router`、`request.js`、`user.js` |
| AI 分析 (`/ai-analysis`) | ✅ UI+API 完成 | 分析报告生成 + 历史记录，已对接真实 API |
| 系统设置 (`/settings`) | ✅ UI+API 完成 | GPA 标准设置、绩点对照表、AI 分析维度，已对接真实 API |
| 导入导出 (`/data`) | ✅ UI+API 完成 | 导入/导出/记录查询，已对接真实 API |

### 联调状态

| 模块 | 状态 |
|------|------|
| 登录/注册 (`/auth`) | ✅ 前后端已联调 |
| 用户模块 (`/users`) | ✅ 前后端已联调 |
| 课程管理 (`/courses`) | ✅ 前后端已联调 |
| 经历管理 (`/experiences`) | ✅ 前后端已联调 |
| 成就管理 (`/achievements`) | ✅ 前后端已联调 |
| 文件上传 (`/upload`) | ✅ 前后端已联调 |
| 角色管理 (`/roles`) | ✅ 前后端已联调 |
| GPA 计算 (`/gpa`) | ✅ 前后端已联调 |
| AI 分析 (`/ai-analysis`) | ✅ 前后端已联调 |
| 系统设置 (`/settings`) | ✅ 前后端已联调 |
| 仪表盘 (`/dashboard`) | ✅ 前后端已联调 |
| 导入导出 (`/data`) | ✅ 前后端已联调 |
