-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS student_club DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE student_club;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    student_id VARCHAR(20) UNIQUE COMMENT '学号',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色: ADMIN-管理员, LEADER-社团负责人, STUDENT-学生',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_student_id (student_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 社团表
CREATE TABLE IF NOT EXISTS t_club (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '社团ID',
    name VARCHAR(100) NOT NULL COMMENT '社团名称',
    description TEXT COMMENT '社团简介',
    logo VARCHAR(255) COMMENT '社团Logo',
    category VARCHAR(50) COMMENT '社团类别',
    founder_id BIGINT COMMENT '创建人ID',
    leader_id BIGINT COMMENT '负责人ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-正常, 2-已解散',
    member_count INT NOT NULL DEFAULT 0 COMMENT '成员数量',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_leader_id (leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团表';

-- 社团成员表
CREATE TABLE IF NOT EXISTS t_membership (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    club_id BIGINT NOT NULL COMMENT '社团ID',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '社团角色: LEADER-负责人, ADMIN-管理员, MEMBER-普通成员',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已通过, 2-已拒绝, 3-已退出',
    joined_at DATETIME COMMENT '加入时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_club (user_id, club_id),
    INDEX idx_user_id (user_id),
    INDEX idx_club_id (club_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团成员表';

-- 活动表
CREATE TABLE IF NOT EXISTS t_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    club_id BIGINT NOT NULL COMMENT '社团ID',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    location VARCHAR(200) COMMENT '活动地点',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    max_participants INT DEFAULT 0 COMMENT '最大参与人数, 0表示不限',
    current_participants INT DEFAULT 0 COMMENT '当前报名人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已取消, 1-报名中, 2-进行中, 3-已结束',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_club_id (club_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 活动报名表
CREATE TABLE IF NOT EXISTS t_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已取消, 1-已报名, 2-已签到',
    registered_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    INDEX idx_activity_id (activity_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 公告表
CREATE TABLE IF NOT EXISTS t_announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    club_id BIGINT COMMENT '社团ID, NULL表示系统公告',
    publisher_id BIGINT NOT NULL COMMENT '发布者ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_club_id (club_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';
