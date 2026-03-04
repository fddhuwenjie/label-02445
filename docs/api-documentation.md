# 学生社团管理系统 - API文档

## 基础信息

- 基础URL（本地开发）: `http://localhost:8080/api`
- 基础URL（Docker 部署）: `http://localhost:8084/api`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer <token>`

## 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 401 | 未登录/Token过期 |
| 500 | 服务器错误 |

---

## 1. 认证模块

### 1.1 用户登录

**POST** `/auth/login`

请求体：
```json
{
  "username": "admin",
  "password": "admin123"
}
```

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "ADMIN"
    }
  }
}
```

### 1.2 用户注册

**POST** `/auth/register`

请求体：
```json
{
  "username": "newuser",
  "password": "password123",
  "realName": "新用户",
  "studentId": "2024001001",
  "email": "newuser@school.edu",
  "phone": "13800138000"
}
```

---

## 2. 用户模块

### 2.1 获取当前用户信息

**GET** `/users/info`

### 2.2 更新用户信息

**PUT** `/users/info`

请求体：
```json
{
  "realName": "新姓名",
  "email": "newemail@school.edu",
  "phone": "13900139000"
}
```

### 2.3 修改密码

**PUT** `/users/password`

请求体：
```json
{
  "oldPassword": "oldpass",
  "newPassword": "newpass"
}
```

### 2.4 用户列表（管理员）

**GET** `/users/list`

参数：
- page: 页码（默认1）
- size: 每页数量（默认10）
- keyword: 搜索关键词
- role: 角色筛选

### 2.5 更新用户状态（管理员）

**PUT** `/users/{id}/status?status=0`

---

## 3. 社团模块

### 3.1 创建社团

**POST** `/clubs`

请求体：
```json
{
  "name": "新社团",
  "description": "社团简介",
  "category": "学术科技",
  "logo": "https://example.com/logo.png"
}
```

### 3.2 更新社团

**PUT** `/clubs`

请求体：
```json
{
  "id": 1,
  "name": "更新后的名称",
  "description": "更新后的简介"
}
```

### 3.3 删除社团

**DELETE** `/clubs/{id}`

### 3.4 获取社团详情

**GET** `/clubs/detail/{id}`

### 3.5 社团列表

**GET** `/clubs/list`

参数：
- page: 页码
- size: 每页数量
- keyword: 搜索关键词
- status: 状态筛选

### 3.6 审核社团（管理员）

**PUT** `/clubs/{id}/audit?status=1`

### 3.7 获取社团类别

**GET** `/clubs/categories`

### 3.8 获取我管理的社团

**GET** `/clubs/my`

---

## 4. 成员模块

### 4.1 申请加入社团

**POST** `/memberships/apply/{clubId}`

### 4.2 审核申请

**PUT** `/memberships/{id}/audit?status=1`

### 4.3 退出社团

**POST** `/memberships/quit/{clubId}`

### 4.4 移除成员

**DELETE** `/memberships/{id}`

### 4.5 获取社团成员列表

**GET** `/memberships/club/{clubId}`

参数：
- page: 页码
- size: 每页数量
- status: 状态筛选

### 4.6 获取我加入的社团

**GET** `/memberships/my`

### 4.7 检查是否是成员

**GET** `/memberships/check/{clubId}`

---

## 5. 活动模块

### 5.1 创建活动

**POST** `/activities`

请求体：
```json
{
  "clubId": 1,
  "title": "活动标题",
  "description": "活动描述",
  "location": "活动地点",
  "startTime": "2024-12-01T14:00:00",
  "endTime": "2024-12-01T17:00:00",
  "maxParticipants": 50
}
```

### 5.2 更新活动

**PUT** `/activities`

### 5.3 删除活动

**DELETE** `/activities/{id}`

### 5.4 获取活动详情

**GET** `/activities/detail/{id}`

### 5.5 活动列表

**GET** `/activities/list`

参数：
- page: 页码
- size: 每页数量
- clubId: 社团ID筛选
- keyword: 搜索关键词

### 5.6 报名活动

**POST** `/activities/{id}/register`

### 5.7 取消报名

**POST** `/activities/{id}/cancel`

### 5.8 更新活动状态

**PUT** `/activities/{id}/status?status=2`

---

## 6. 公告模块

### 6.1 发布公告

**POST** `/announcements`

请求体：
```json
{
  "title": "公告标题",
  "content": "公告内容",
  "clubId": 1
}
```

### 6.2 更新公告

**PUT** `/announcements`

### 6.3 删除公告

**DELETE** `/announcements/{id}`

### 6.4 公告列表

**GET** `/announcements/list`

参数：
- page: 页码
- size: 每页数量
- clubId: 社团ID筛选

---

## 7. 统计模块

### 7.1 获取统计概览

**GET** `/statistics/overview`

响应：
```json
{
  "code": 200,
  "data": {
    "totalUsers": 100,
    "studentCount": 95,
    "totalClubs": 20,
    "activeClubs": 18,
    "pendingClubs": 2,
    "totalActivities": 50,
    "ongoingActivities": 10,
    "pendingApplications": 5
  }
}
```
