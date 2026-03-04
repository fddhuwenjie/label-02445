# 学生社团管理系统 - 部署指南

## 0. 部署形态说明

**默认部署方式**：项目默认打包为**可执行 JAR**（内嵌 Tomcat），通过 `java -jar` 或 `mvn spring-boot:run` 运行，**非** Tomcat 9 独立 WAR 部署。

若需部署到独立 Tomcat 9 服务器，需**手动修改 POM**（见第 5 节），非开箱即用。

---

## 1. 环境要求

### 1.1 开发环境
- JDK 21+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- IntelliJ IDEA（推荐）

### 1.2 生产环境
- Docker 20.10+
- Docker Compose 2.0+

---

## 2. 本地开发部署

### 2.1 数据库配置

1. 安装MySQL 8.0
2. 创建数据库（系统会自动创建表结构）：
```sql
CREATE DATABASE student_club DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 修改后端配置文件 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/student_club?...
    username: root
    password: 147369852
```

### 2.2 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后访问：http://localhost:8080

### 2.3 启动前端

```bash
cd frontend-admin
npm install
npm run dev
```

前端启动后访问：http://localhost:5173

---

## 3. Docker部署

### 3.1 一键启动

```bash
# 构建并启动所有服务（使用 docker compose，Docker 内置）
docker compose up --build -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down
```

> 若提示 `command not found: docker-compose`，请使用 `docker compose`（空格，Docker 20.10+ 内置）。

### 3.2 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库 |
| Backend | 8084 | 后端API |
| Frontend-Admin | 8085 | 管理后台 |

### 3.3 访问地址

- 管理后台：http://localhost:8085
- 后端API：http://localhost:8084/api

---

## 4. IntelliJ IDEA配置

### 4.1 导入项目

1. 打开IDEA，选择 `File -> Open`
2. 选择项目根目录
3. 等待Maven依赖下载完成

### 4.2 配置JDK

1. `File -> Project Structure -> Project`
2. 设置Project SDK为JDK 21

### 4.3 运行后端

1. 找到 `StudentClubApplication.java`
2. 右键选择 `Run 'StudentClubApplication'`

### 4.4 运行前端

1. 打开Terminal
2. 执行：
```bash
cd frontend-admin
npm install
npm run dev
```

---

## 5. Tomcat 9 WAR 部署（可选，需手动改 POM）

### 5.1 打包 WAR

当前默认打包为 JAR，需**手动修改** `backend/pom.xml`：

1. 将 `<packaging>jar</packaging>` 改为 `<packaging>war</packaging>`
2. 添加 `spring-boot-starter-tomcat` 的 `provided` 依赖（由外部 Tomcat 提供）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

3. 打包：
```bash
mvn clean package -DskipTests
```

### 5.2 部署到 Tomcat 9

1. 将生成的 `student-club-backend-1.0.0.war` 复制到 Tomcat 的 `webapps` 目录
2. 启动 Tomcat 9
3. 访问：http://localhost:8080/student-club-backend-1.0.0

---

## 6. 常见问题

### 6.1 数据库连接失败

检查：
- MySQL服务是否启动
- 用户名密码是否正确
- 数据库是否存在

### 6.2 端口被占用

```bash
# 查看端口占用
lsof -i :8080

# 杀死进程
kill -9 <PID>
```

### 6.3 Docker 构建失败

```bash
# 清理 Docker 缓存
docker system prune -a

# 重新构建
docker compose build --no-cache
```

### 6.4 前端 npm install 失败

项目已默认使用 npmmirror 源，国内外均可访问。若仍失败：

1. **改用官方源**（如 npmmirror 不可用）：在 `docker-compose.yml` 中修改：
   ```yaml
   args:
     NPM_REGISTRY: https://registry.npmjs.org/
   ```

2. **增加 Docker 内存**：Docker Desktop → Settings → Resources，将 Memory 调至 4GB 以上。

3. **清理后重试**：`docker compose build --no-cache frontend-admin`

---

## 7. 生产环境建议

### 7.1 安全配置

- 修改默认密码
- 配置 HTTPS
- 限制数据库访问 IP
- **JWT 密钥**：`application.yml` 中 `jwt.secret` 为硬编码，生产环境**不建议**。建议：
  - 使用环境变量：`JWT_SECRET` 或 `jwt.secret`
  - 或使用密钥管理服务（如 Vault）

### 7.2 配置说明

- **MyBatis Plus**：`application.yml` 中配置了 `mapper-locations: classpath:mapper/*.xml`，但项目使用注解方式，仓库**无** XML 文件，该配置为冗余，不影响运行。

### 7.3 性能优化

- 配置数据库连接池
- 启用 Redis 缓存
- 配置 Nginx 负载均衡

### 7.4 监控告警

- 配置日志收集
- 设置健康检查
- 配置告警通知
