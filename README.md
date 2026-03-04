# 学生社团管理系统

## How to Run

### 使用 Docker Compose 运行（推荐）

```bash
# 克隆项目后，在根目录执行
docker-compose up --build -d

# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 手动运行

1. 确保已安装 JDK 21、Maven、Node.js 18+、MySQL 8.0
2. 创建数据库（系统会自动创建表结构）
3. 修改 `backend/src/main/resources/application.yml` 中的数据库配置
4. 运行后端：
   ```bash
   cd backend
   mvn spring-boot:run
   ```
5. 运行管理后台前端：
   ```bash
   cd frontend-admin
   npm install
   npm run dev
   ```

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| backend | 8084 | 后端 API 服务 |
| frontend-admin | 8085 | 管理后台前端 |
| mysql | 3306 | MySQL 数据库 |

## 访问地址

- 管理后台：http://localhost:8085
- 后端API：http://localhost:8084/api

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 社团负责人 | leader | leader123 |
| 学生 | student | student123 |

注：系统启动时会自动初始化这些测试账号，确保密码正确。

## 题目内容

### 项目课题
以学生社团管理系统为项目课题进行调研及设计开发，完成该系统的常规系统功能模块设计及数据库设计。

### 数据库设计要求

#### 1. 需求分析
理解具体问题场景下的用户需求，分析并详细列出需求"源数据"表。

**用户角色：**
- 系统管理员：管理整个系统，审核社团，管理用户
- 社团负责人：管理社团信息，审核成员申请，发布活动
- 普通学生：浏览社团，申请加入，参与活动

**功能需求：**
- 用户管理：注册、登录、个人信息管理
- 社团管理：创建、编辑、删除、审核社团
- 成员管理：申请加入、审核、退出社团
- 活动管理：发布、编辑、删除活动，活动报名

**源数据表：**
| 数据项 | 数据类型 | 说明 |
|--------|----------|------|
| 用户ID | 整数 | 用户唯一标识 |
| 用户名 | 字符串 | 登录用户名 |
| 密码 | 字符串 | 加密存储 |
| 姓名 | 字符串 | 真实姓名 |
| 学号 | 字符串 | 学生学号 |
| 角色 | 枚举 | 管理员/负责人/学生 |
| 社团ID | 整数 | 社团唯一标识 |
| 社团名称 | 字符串 | 社团名称 |
| 社团简介 | 文本 | 社团介绍 |
| 活动ID | 整数 | 活动唯一标识 |
| 活动名称 | 字符串 | 活动名称 |
| 活动时间 | 日期时间 | 活动举办时间 |
| 活动地点 | 字符串 | 活动地点 |

#### 2. 概念设计（ER模型）

**实体：**
- 用户（User）
- 社团（Club）
- 活动（Activity）
- 成员关系（Membership）
- 活动报名（Registration）

**关系：**
- 用户 - 创建 - 社团（1:N）
- 用户 - 加入 - 社团（M:N，通过成员关系）
- 社团 - 举办 - 活动（1:N）
- 用户 - 报名 - 活动（M:N，通过活动报名）

#### 3. 逻辑设计（关系模型）

**用户表（t_user）**
- id (PK)
- username (UNIQUE)
- password
- real_name
- student_id (UNIQUE)
- email
- phone
- role
- avatar
- status
- created_at
- updated_at

**社团表（t_club）**
- id (PK)
- name
- description
- logo
- category
- founder_id (FK -> t_user.id)
- leader_id (FK -> t_user.id)
- status
- member_count
- created_at
- updated_at

**成员表（t_membership）**
- id (PK)
- user_id (FK -> t_user.id)
- club_id (FK -> t_club.id)
- role
- status
- joined_at
- created_at

**活动表（t_activity）**
- id (PK)
- club_id (FK -> t_club.id)
- title
- description
- location
- start_time
- end_time
- max_participants
- current_participants
- status
- created_at
- updated_at

**活动报名表（t_registration）**
- id (PK)
- activity_id (FK -> t_activity.id)
- user_id (FK -> t_user.id)
- status
- registered_at

#### 4. 物理设计
- 数据库：MySQL 8.0
- 存储引擎：InnoDB
- 字符集：utf8mb4
- 索引设计：主键索引、外键索引、唯一索引

#### 5. 应用系统开发
- 后端：Spring Boot 2.7.18 + MyBatis Plus
- 前端：Vue 3 + Element Plus
- 开发工具：IntelliJ IDEA
- 服务器：Tomcat 9.x（Spring Boot 2.7 内嵌）

---

## 系统功能模块

### 1. 用户模块
- 用户注册/登录
- 个人信息管理
- 密码修改

### 2. 社团模块
- 社团列表展示
- 社团详情查看
- 创建社团申请
- 社团信息编辑
- 社团审核（管理员）

### 3. 成员模块
- 申请加入社团
- 成员审核（社团负责人）
- 成员列表管理
- 退出社团

### 4. 活动模块
- 活动发布
- 活动列表
- 活动报名
- 活动管理

### 5. 系统管理
- 用户管理
- 社团审核
- 数据统计

---

## 技术栈

- **后端**: Java 21, Spring Boot 2.7.18, MyBatis Plus, MySQL 8.0
- **前端**: Vue 3, Vite, Element Plus, Axios
- **容器化**: Docker, Docker Compose
- **其他**: JWT 认证, RESTful API

> **版本说明**：课题 Prompt 要求 OpenJDK 25，当前实现采用 Java 21 + Spring Boot 2.7.18，以兼容 Docker 构建及主流运行环境。Spring Boot 2.7 内嵌 Tomcat 9.x，使用 `javax.servlet.*`。

## 项目结构

```
student-club-management/
├── backend/                 # 后端服务
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend-admin/          # 管理后台
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml
├── .gitignore
└── README.md
```
