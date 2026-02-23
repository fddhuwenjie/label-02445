# 学生社团管理系统 - 数据库设计文档

## 1. 需求分析

### 1.1 系统概述
学生社团管理系统是一个面向高校的社团管理平台，旨在为学生社团提供便捷的信息化管理服务。

### 1.2 用户角色

| 角色 | 说明 | 权限 |
|------|------|------|
| 系统管理员 | 管理整个系统 | 用户管理、社团审核、系统配置 |
| 社团负责人 | 管理社团事务 | 社团信息管理、成员审核、活动发布 |
| 普通学生 | 参与社团活动 | 浏览社团、申请加入、活动报名 |

### 1.3 功能需求

#### 用户管理
- 用户注册、登录
- 个人信息管理
- 密码修改

#### 社团管理
- 创建社团申请
- 社团信息编辑
- 社团审核（管理员）
- 社团解散

#### 成员管理
- 申请加入社团
- 成员审核
- 成员移除
- 退出社团

#### 活动管理
- 发布活动
- 活动报名
- 活动管理

#### 公告管理
- 发布公告
- 公告管理

### 1.4 源数据表

| 数据项 | 数据类型 | 长度 | 说明 | 约束 |
|--------|----------|------|------|------|
| 用户ID | BIGINT | - | 用户唯一标识 | 主键、自增 |
| 用户名 | VARCHAR | 50 | 登录用户名 | 唯一、非空 |
| 密码 | VARCHAR | 255 | BCrypt加密存储 | 非空 |
| 真实姓名 | VARCHAR | 50 | 用户真实姓名 | - |
| 学号 | VARCHAR | 20 | 学生学号 | 唯一 |
| 邮箱 | VARCHAR | 100 | 电子邮箱 | - |
| 手机号 | VARCHAR | 20 | 联系电话 | - |
| 角色 | VARCHAR | 20 | 用户角色 | 非空 |
| 头像 | VARCHAR | 255 | 头像URL | - |
| 状态 | TINYINT | - | 账号状态 | 非空 |
| 社团ID | BIGINT | - | 社团唯一标识 | 主键、自增 |
| 社团名称 | VARCHAR | 100 | 社团名称 | 非空 |
| 社团简介 | TEXT | - | 社团介绍 | - |
| 社团Logo | VARCHAR | 255 | Logo URL | - |
| 社团类别 | VARCHAR | 50 | 社团分类 | - |
| 活动ID | BIGINT | - | 活动唯一标识 | 主键、自增 |
| 活动标题 | VARCHAR | 200 | 活动名称 | 非空 |
| 活动描述 | TEXT | - | 活动详情 | - |
| 活动地点 | VARCHAR | 200 | 举办地点 | - |
| 开始时间 | DATETIME | - | 活动开始时间 | 非空 |
| 结束时间 | DATETIME | - | 活动结束时间 | 非空 |

---

## 2. 概念设计（ER模型）

### 2.1 实体识别

1. **用户（User）**
   - 属性：用户ID、用户名、密码、真实姓名、学号、邮箱、手机号、角色、头像、状态

2. **社团（Club）**
   - 属性：社团ID、名称、简介、Logo、类别、状态、成员数量

3. **活动（Activity）**
   - 属性：活动ID、标题、描述、地点、开始时间、结束时间、最大人数、当前人数、状态

4. **公告（Announcement）**
   - 属性：公告ID、标题、内容、状态

### 2.2 关系识别

1. **用户 - 创建 - 社团**（1:N）
   - 一个用户可以创建多个社团
   - 一个社团只有一个创建者

2. **用户 - 负责 - 社团**（1:N）
   - 一个用户可以负责多个社团
   - 一个社团只有一个负责人

3. **用户 - 加入 - 社团**（M:N）
   - 一个用户可以加入多个社团
   - 一个社团可以有多个成员
   - 通过成员关系表实现

4. **社团 - 举办 - 活动**（1:N）
   - 一个社团可以举办多个活动
   - 一个活动只属于一个社团

5. **用户 - 报名 - 活动**（M:N）
   - 一个用户可以报名多个活动
   - 一个活动可以有多个报名者
   - 通过报名表实现

6. **用户 - 发布 - 公告**（1:N）
   - 一个用户可以发布多个公告

7. **社团 - 拥有 - 公告**（1:N）
   - 一个社团可以有多个公告
   - 公告可以不属于任何社团（系统公告）

### 2.3 ER图

```
┌─────────────┐         创建          ┌─────────────┐
│             │─────────────────────▶│             │
│    用户     │         负责          │    社团     │
│   (User)    │─────────────────────▶│   (Club)    │
│             │                       │             │
└─────────────┘                       └─────────────┘
      │ │                                   │
      │ │                                   │
      │ │  加入(M:N)                        │ 举办(1:N)
      │ │  ┌─────────────┐                  │
      │ └──│   成员关系   │──────────────────┘
      │    │(Membership) │
      │    └─────────────┘
      │
      │    报名(M:N)      ┌─────────────┐
      │    ┌─────────────┐│             │
      └────│   报名表    ││    活动     │
           │(Registration)│(Activity)  │
           └─────────────┘│             │
                          └─────────────┘
      │
      │    发布(1:N)      ┌─────────────┐
      └──────────────────▶│    公告     │
                          │(Announcement)│
                          └─────────────┘
```

### 2.4 设计说明

1. **消除冗余**：
   - 用户角色使用枚举值存储，而非单独建表
   - 社团类别使用字符串存储，便于扩展

2. **关系优化**：
   - 成员关系表包含角色字段，区分负责人、管理员、普通成员
   - 报名表包含状态字段，支持取消报名、签到等状态

---

## 3. 逻辑设计（关系模型）

### 3.1 关系模式

#### 用户表（t_user）
```
t_user(id, username, password, real_name, student_id, email, phone, role, avatar, status, deleted, created_at, updated_at)
主键：id
唯一键：username, student_id
```

#### 社团表（t_club）
```
t_club(id, name, description, logo, category, founder_id, leader_id, status, member_count, deleted, created_at, updated_at)
主键：id
外键：founder_id → t_user.id, leader_id → t_user.id
```

#### 成员表（t_membership）
```
t_membership(id, user_id, club_id, role, status, joined_at, created_at, updated_at)
主键：id
外键：user_id → t_user.id, club_id → t_club.id
唯一键：(user_id, club_id)
```

#### 活动表（t_activity）
```
t_activity(id, club_id, title, description, location, start_time, end_time, max_participants, current_participants, status, deleted, created_at, updated_at)
主键：id
外键：club_id → t_club.id
```

#### 报名表（t_registration）
```
t_registration(id, activity_id, user_id, status, registered_at, created_at)
主键：id
外键：activity_id → t_activity.id, user_id → t_user.id
唯一键：(activity_id, user_id)
```

#### 公告表（t_announcement）
```
t_announcement(id, title, content, club_id, publisher_id, status, deleted, created_at, updated_at)
主键：id
外键：club_id → t_club.id, publisher_id → t_user.id
```

### 3.2 规范化分析

所有表均满足第三范式（3NF）：
- 每个属性都是原子的（1NF）
- 非主属性完全依赖于主键（2NF）
- 非主属性不传递依赖于主键（3NF）

### 3.3 完整性约束

#### 实体完整性
- 所有表都有主键约束
- 主键使用自增策略

#### 参照完整性
- 外键约束确保数据一致性
- 使用逻辑删除避免级联删除问题

#### 用户定义完整性
- 用户名、学号唯一性约束
- 状态字段使用枚举值约束
- 时间字段非空约束

---

## 4. 物理设计

### 4.1 数据库选型
- DBMS：MySQL 8.0
- 存储引擎：InnoDB
- 字符集：utf8mb4
- 排序规则：utf8mb4_unicode_ci

### 4.2 索引设计

| 表名 | 索引名 | 索引列 | 索引类型 |
|------|--------|--------|----------|
| t_user | PRIMARY | id | 主键索引 |
| t_user | idx_username | username | 唯一索引 |
| t_user | idx_student_id | student_id | 唯一索引 |
| t_user | idx_role | role | 普通索引 |
| t_club | PRIMARY | id | 主键索引 |
| t_club | idx_name | name | 普通索引 |
| t_club | idx_status | status | 普通索引 |
| t_club | idx_leader_id | leader_id | 普通索引 |
| t_membership | PRIMARY | id | 主键索引 |
| t_membership | uk_user_club | (user_id, club_id) | 唯一索引 |
| t_activity | PRIMARY | id | 主键索引 |
| t_activity | idx_club_id | club_id | 普通索引 |
| t_activity | idx_start_time | start_time | 普通索引 |
| t_registration | PRIMARY | id | 主键索引 |
| t_registration | uk_activity_user | (activity_id, user_id) | 唯一索引 |

### 4.3 存储估算

假设：
- 用户数：10,000
- 社团数：100
- 活动数：1,000
- 成员关系：50,000
- 报名记录：20,000

预估存储空间：约 50MB

### 4.4 性能优化

1. **查询优化**
   - 使用覆盖索引减少回表
   - 分页查询使用游标分页

2. **写入优化**
   - 批量插入使用批处理
   - 更新操作使用乐观锁

3. **缓存策略**
   - 热点数据使用Redis缓存
   - 统计数据定时更新

---

## 5. 数据字典

### 5.1 用户角色枚举

| 值 | 说明 |
|----|------|
| ADMIN | 系统管理员 |
| LEADER | 社团负责人 |
| STUDENT | 普通学生 |

### 5.2 社团状态枚举

| 值 | 说明 |
|----|------|
| 0 | 待审核 |
| 1 | 正常 |
| 2 | 已解散 |

### 5.3 成员状态枚举

| 值 | 说明 |
|----|------|
| 0 | 待审核 |
| 1 | 已通过 |
| 2 | 已拒绝 |
| 3 | 已退出 |

### 5.4 活动状态枚举

| 值 | 说明 |
|----|------|
| 0 | 已取消 |
| 1 | 报名中 |
| 2 | 进行中 |
| 3 | 已结束 |

### 5.5 报名状态枚举

| 值 | 说明 |
|----|------|
| 0 | 已取消 |
| 1 | 已报名 |
| 2 | 已签到 |
