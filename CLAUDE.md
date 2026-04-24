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
| 课程管理 (`/courses`) | ✅ 已完成 | 5 个接口（列表分页、详情、增删改），已改用 DTO/VO 模式 |
| JWT 工具与拦截器 | ✅ 已完成 | `JwtUtil` + `JwtTokenInterceptor` + `ThreadLocalUtil` |
| 全局异常处理 | ✅ 已完成 | `BusinessException` + `NoResourceFoundException` 静默处理 |
| Knife4j 文档 | ✅ 已完成 | `Knife4jConfig` 配置全局 Token |
| 登录/注册 (`/auth`) | ⏳ **未实现** | 仅有 `UserLoginDTO`、`UserRegisterDTO`、`UserVO`、`User` 实体，**无 Controller/Service/Mapper** |
| 用户模块 (`/users`) | ⏳ 未实现 | 无 |
| GPA 计算 (`/gpa`) | ⏳ 未实现 | 无 |
| 经历/成就/角色/AI/设置/导入导出 | ⏳ 未实现 | 数据库表和 Entity 类已有，但无业务代码 |

### 前端

| 页面 | 状态 | 说明 |
|------|------|------|
| 登录 (`/login`) | ✅ UI 完成 | 已对接 `/auth/login` API（**后端接口待实现**） |
| 注册 (`/register`) | ✅ UI 完成 | 已对接 `/auth/register` API（**后端接口待实现**） |
| 仪表盘 (`/dashboard`) | ✅ UI 完成 | 纯静态 mock 数据展示 |
| 课程管理 (`/courses`) | ✅ UI 完成 | 增删改查弹窗齐全，真实 API 请求已注释，当前用前端 mock |
| 布局/路由/请求封装 | ✅ 完成 | `AppLayout`、`router`、`request.js`、`user.js` |
| 成绩/GPA (`/grades`) | ⏳ 空占位 | 仅 `el-empty` |
| 经历 (`/experiences`) | ⏳ 空占位 | 仅 `el-empty` |
| 成就 (`/achievements`) | ⏳ 空占位 | 仅 `el-empty` |
| 角色 (`/roles`) | ⏳ 空占位 | 仅 `el-empty` |
| AI 分析 (`/ai-analysis`) | ⏳ 空占位 | 仅 `el-empty` |
| 导入导出 (`/data`) | ⏳ 空占位 | 仅 `el-empty` |
| 个人中心 (`/profile`) | ⏳ 空占位 | 仅 `el-empty` |
| 系统设置 (`/settings`) | ⏳ 空占位 | 仅 `el-empty` |

### 联调待办

`pdp-web/src/views/courses/index.vue` 中以下 API 调用已被注释，需后端接口完成后取消注释：
- `fetchList()` → `GET /courses`
- `handleAdd()` → `POST /courses`
- `handleUpdate()` → `PUT /courses`
- `handleDelete()` → `DELETE /courses/{id}`
