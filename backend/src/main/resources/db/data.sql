-- 初始化数据
USE student_club;

-- 插入管理员账号 (密码: admin123)
-- BCrypt hash for 'admin123': $2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O
INSERT IGNORE INTO t_user (id, username, password, real_name, student_id, email, role, status) VALUES
(1, 'admin', '$2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O', '系统管理员', 'ADMIN001', 'admin@school.edu', 'ADMIN', 1);

-- 插入社团负责人账号 (密码: leader123)
-- BCrypt hash for 'leader123': $2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O
INSERT IGNORE INTO t_user (id, username, password, real_name, student_id, email, role, status) VALUES
(2, 'leader', '$2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O', '张三', '2021001001', 'leader@school.edu', 'STUDENT', 1);

-- 插入学生账号 (密码: student123)
-- BCrypt hash for 'student123': $2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O
INSERT IGNORE INTO t_user (id, username, password, real_name, student_id, email, role, status) VALUES
(3, 'student', '$2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O', '李四', '2021001002', 'student@school.edu', 'STUDENT', 1);

-- 插入更多测试学生
INSERT IGNORE INTO t_user (id, username, password, real_name, student_id, email, role, status) VALUES
(4, 'student2', '$2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O', '王五', '2021001003', 'student2@school.edu', 'STUDENT', 1),
(5, 'student3', '$2a$10$EqKcp1WFKVQISheBxkVJaeNzSIb.Z8YPHlJXhNFyuG8JzHEuWNe9O', '赵六', '2021001004', 'student3@school.edu', 'STUDENT', 1);

-- 插入示例社团
INSERT IGNORE INTO t_club (id, name, description, category, founder_id, leader_id, status, member_count) VALUES
(1, '计算机协会', '致力于计算机技术学习与交流，定期举办编程比赛和技术分享会', '学术科技', 2, 2, 1, 3),
(2, '篮球社', '热爱篮球运动的同学聚集地，每周组织训练和友谊赛', '体育运动', 2, 2, 1, 2),
(3, '音乐社', '音乐爱好者的天堂，涵盖声乐、器乐等多种形式', '文化艺术', 3, 3, 1, 1),
(4, '志愿者协会', '传递爱心，服务社会，组织各类公益活动', '公益服务', 2, 2, 0, 0);

-- 插入社团成员关系
INSERT IGNORE INTO t_membership (user_id, club_id, role, status, joined_at) VALUES
(2, 1, 'LEADER', 1, NOW()),
(3, 1, 'MEMBER', 1, NOW()),
(4, 1, 'MEMBER', 1, NOW()),
(2, 2, 'LEADER', 1, NOW()),
(5, 2, 'MEMBER', 1, NOW()),
(3, 3, 'LEADER', 1, NOW());

-- 插入示例活动
INSERT IGNORE INTO t_activity (id, club_id, title, description, location, start_time, end_time, max_participants, current_participants, status) VALUES
(1, 1, '编程马拉松大赛', '24小时编程挑战赛，展示你的编程实力！', '图书馆报告厅', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), 50, 3, 1),
(2, 1, 'Python入门讲座', '面向零基础同学的Python编程入门讲座', '教学楼A101', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 100, 5, 1),
(3, 2, '篮球友谊赛', '与隔壁学校篮球队的友谊赛', '体育馆', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 20, 8, 1),
(4, 3, '校园歌手大赛', '展示你的歌喉，赢取丰厚奖品', '大礼堂', DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY), 30, 12, 1);

-- 插入活动报名
INSERT IGNORE INTO t_registration (activity_id, user_id, status) VALUES
(1, 3, 1),
(1, 4, 1),
(1, 5, 1),
(2, 3, 1),
(2, 4, 1);

-- 插入公告
INSERT IGNORE INTO t_announcement (id, title, content, club_id, publisher_id, status) VALUES
(1, '欢迎加入学生社团管理系统', '本系统为学生社团提供便捷的管理服务，欢迎各位同学积极参与社团活动！', NULL, 1, 1),
(2, '计算机协会招新通知', '计算机协会2024年秋季招新正式开始，欢迎对编程感兴趣的同学加入！', 1, 2, 1),
(3, '篮球社训练通知', '本周六下午2点在体育馆进行常规训练，请社员准时参加。', 2, 2, 1);
