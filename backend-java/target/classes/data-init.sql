-- 知识点初始化数据
-- 为课程1（Java编程基础）创建知识点

-- 删除现有数据（如果存在）
DELETE FROM knowledge_point_relations WHERE source_id IN (SELECT id FROM knowledge_points WHERE course_id = 1);
DELETE FROM knowledge_points WHERE course_id = 1;

-- 重置自增ID
ALTER TABLE knowledge_points AUTO_INCREMENT = 1;
ALTER TABLE knowledge_point_relations AUTO_INCREMENT = 1;

-- 插入Java编程基础课程的知识点
INSERT INTO knowledge_points (name, description, course_id, parent_id, difficulty_level, importance, estimated_time, point_type, order_index) VALUES
-- 基础概念知识点
('Java简介', 'Java语言的历史、特点和应用领域介绍', 1, NULL, 1, 0.9, 30, 'concept', 1),
('JVM虚拟机', 'Java虚拟机的工作原理和内存结构', 1, NULL, 3, 0.8, 45, 'concept', 2),
('开发环境搭建', '安装JDK、IDE和配置开发环境', 1, NULL, 1, 0.7, 60, 'skill', 3),

-- 基础语法知识点
('数据类型', 'Java的基本数据类型和引用数据类型', 1, NULL, 2, 0.9, 40, 'concept', 4),
('变量与常量', '变量的声明、初始化和作用域', 1, NULL, 2, 0.8, 35, 'concept', 5),
('运算符', '算术、比较、逻辑和位运算符的使用', 1, NULL, 2, 0.7, 30, 'concept', 6),

-- 控制结构知识点
('条件语句', 'if-else、switch-case语句的使用', 1, NULL, 2, 0.8, 35, 'skill', 7),
('循环语句', 'for、while、do-while循环的使用', 1, NULL, 2, 0.8, 40, 'skill', 8),
('分支控制', 'break、continue、return语句', 1, NULL, 2, 0.6, 25, 'skill', 9),

-- 面向对象知识点
('类与对象', '类的定义、对象的创建和使用', 1, NULL, 3, 0.9, 50, 'concept', 10),
('封装', '访问修饰符、getter/setter方法', 1, NULL, 3, 0.8, 40, 'concept', 11),
('继承', '类的继承、super关键字、方法重写', 1, NULL, 4, 0.9, 55, 'concept', 12),
('多态', '方法重载、动态绑定、接口实现', 1, NULL, 4, 0.8, 50, 'concept', 13),

-- 高级特性知识点
('异常处理', 'try-catch-finally、throws关键字', 1, NULL, 3, 0.7, 45, 'skill', 14),
('集合框架', 'List、Set、Map接口和常用实现类', 1, NULL, 4, 0.8, 60, 'application', 15),
('泛型', '泛型类、泛型方法、通配符', 1, NULL, 4, 0.6, 50, 'application', 16),
('I/O流', '文件读写、字节流、字符流', 1, NULL, 4, 0.7, 55, 'application', 17),

-- 实践应用知识点
('单元测试', 'JUnit框架的使用和测试用例编写', 1, NULL, 3, 0.6, 40, 'application', 18),
('项目实践', '综合项目开发和代码规范', 1, NULL, 5, 0.9, 120, 'application', 19);

-- 插入知识点关系（前置依赖关系）
INSERT INTO knowledge_point_relations (source_id, target_id, relation_type, strength, description) VALUES
-- 基础依赖
(1, 3, 'prerequisite', 0.8, 'Java简介是环境搭建的前提'),
(3, 4, 'prerequisite', 0.9, '环境搭建后才能学习数据类型'),
(4, 5, 'prerequisite', 0.9, '理解数据类型后学习变量'),
(5, 6, 'prerequisite', 0.8, '变量概念是运算符使用的基础'),

-- 控制结构依赖
(6, 7, 'prerequisite', 0.8, '运算符是条件语句的基础'),
(7, 8, 'prerequisite', 0.7, '条件语句是循环语句的前提'),
(8, 9, 'prerequisite', 0.6, '循环语句是分支控制的基础'),

-- 面向对象依赖链
(5, 10, 'prerequisite', 0.8, '变量概念是类与对象的基础'),
(10, 11, 'prerequisite', 0.9, '类与对象是封装的前提'),
(11, 12, 'prerequisite', 0.9, '封装是继承的基础'),
(12, 13, 'prerequisite', 0.8, '继承是多态的前提'),

-- 高级特性依赖
(7, 14, 'prerequisite', 0.7, '条件语句是异常处理的基础'),
(10, 15, 'prerequisite', 0.8, '类与对象是集合框架的前提'),
(15, 16, 'prerequisite', 0.7, '集合框架是泛型学习的基础'),
(4, 17, 'prerequisite', 0.6, '数据类型是I/O流的基础'),

-- 实践应用依赖
(10, 18, 'prerequisite', 0.7, '类与对象是单元测试的基础'),
(13, 19, 'prerequisite', 0.9, '多态是项目实践的重要基础'),
(15, 19, 'prerequisite', 0.8, '集合框架在项目中广泛使用'),
(14, 19, 'prerequisite', 0.7, '异常处理在项目中必不可少'),

-- 关联关系
(2, 4, 'related', 0.6, 'JVM虚拟机与数据类型相关'),
(2, 17, 'related', 0.5, 'JVM虚拟机与I/O流有关联'),
(16, 15, 'related', 0.8, '泛型与集合框架密切相关');

-- ========================================
-- 成绩数据初始化（模拟数据）
-- ========================================

-- 删除现有的成绩相关数据
DELETE FROM submissions;
DELETE FROM tasks WHERE course_id = 1;

-- 插入模拟任务数据
INSERT INTO tasks (id, title, description, task_type, course_id, creator_id, due_date, max_score, status, is_published, submission_format, requirements) VALUES
(1, 'Java基础语法练习', 'Java数据类型、变量、运算符的综合练习题', 'assignment', 1, 2, '2024-12-01 23:59:59', 100.0, 'active', true, 'code', '完成10道Java基础编程题'),
(2, 'Java面向对象编程项目', '设计并实现一个简单的学生管理系统', 'project', 1, 2, '2024-12-15 23:59:59', 100.0, 'active', true, 'code', '使用面向对象思想，实现增删改查功能'),
(3, 'Java期中考试', 'Java前半学期知识点综合测试', 'exam', 1, 2, '2024-11-15 10:00:00', 100.0, 'active', true, 'online', '闭卷考试，时长120分钟'),
(4, 'Java异常处理作业', '异常处理机制的应用和实践', 'assignment', 1, 2, '2024-11-30 23:59:59', 100.0, 'active', true, 'code', '处理文件读取、网络请求等异常场景'),
(5, 'Java集合框架编程', 'List、Set、Map的使用和性能分析', 'assignment', 1, 2, '2024-12-10 23:59:59', 100.0, 'active', true, 'code', '实现数据结构操作和算法优化'),
(6, 'Java讨论：最佳实践', '分享Java开发中的最佳实践和设计模式', 'discussion', 1, 2, '2024-12-05 23:59:59', 20.0, 'active', true, 'text', '参与讨论并分享经验，至少200字');

-- 插入模拟成绩数据（提交记录）
-- 学生ID为5（124同学）的成绩记录
INSERT INTO submissions (id, task_id, student_id, content, status, submitted_at, graded_at, score, max_score, percentage, feedback, grader_id, attempt_number, is_late, time_spent) VALUES
-- Java基础语法练习
(1, 1, 5, '完成了所有10道基础编程题，代码规范，逻辑清晰', 'GRADED', '2024-11-20 16:30:00', '2024-11-21 10:15:00', 88.0, 100.0, 88.0, '代码实现正确，注释详细，但部分变量命名可以更规范。建议多练习代码风格。', 2, 1, false, 180),

-- Java面向对象编程项目
(2, 2, 5, '实现了学生管理系统，包含Student类、StudentManager类等', 'GRADED', '2024-12-10 20:45:00', '2024-12-11 14:20:00', 92.5, 100.0, 92.5, '项目结构清晰，面向对象设计良好，功能完整。界面设计简洁美观，异常处理得当。', 2, 1, false, 300),

-- Java期中考试
(3, 3, 5, '在线考试提交', 'GRADED', '2024-11-15 11:45:00', '2024-11-15 17:30:00', 85.0, 100.0, 85.0, '基础概念掌握扎实，编程题完成度较高，但在复杂算法题上还需加强练习。', 2, 1, false, 120),

-- Java异常处理作业
(4, 4, 5, '完成异常处理练习，包含自定义异常类和异常链处理', 'GRADED', '2024-11-28 19:20:00', '2024-11-29 09:40:00', 90.0, 100.0, 90.0, '异常处理机制理解透彻，代码健壮性好，自定义异常设计合理。', 2, 1, false, 150),

-- Java集合框架编程
(5, 5, 5, '实现了多种集合操作，包含性能测试和比较分析', 'GRADED', '2024-12-08 21:15:00', '2024-12-09 11:00:00', 94.0, 100.0, 94.0, '集合框架掌握优秀，性能分析深入，代码优化到位。展现了很好的算法思维。', 2, 1, false, 240),

-- Java讨论：最佳实践
(6, 6, 5, '分享了单例模式、工厂模式在实际开发中的应用，以及代码优化经验...', 'GRADED', '2024-12-03 15:30:00', '2024-12-04 08:45:00', 18.0, 20.0, 90.0, '分享内容有价值，结合实际案例，表达清楚。积极参与讨论互动。', 2, 1, false, 45);

-- 为了让成绩更丰富，再添加一些其他学生的成绩（假设有其他学生）
-- 可以添加更多学生的记录让数据更真实
INSERT INTO submissions (task_id, student_id, content, status, submitted_at, graded_at, score, max_score, percentage, feedback, grader_id, attempt_number, is_late, time_spent) VALUES
-- 另一个学生的部分成绩
(1, 6, '完成基础编程题，有几处小错误', 'GRADED', '2024-11-21 22:30:00', '2024-11-22 09:15:00', 76.0, 100.0, 76.0, '基本功能实现，但有逻辑错误需要改正。建议多调试代码。', 2, 1, true, 120),
(3, 6, '期中考试提交', 'GRADED', '2024-11-15 11:55:00', '2024-11-15 17:45:00', 78.0, 100.0, 78.0, '基础知识掌握一般，需要加强练习。', 2, 1, false, 115),
(2, 6, '学生管理系统实现', 'GRADED', '2024-12-14 23:30:00', '2024-12-15 16:20:00', 82.0, 100.0, 82.0, '功能基本实现，但代码结构需要优化。', 2, 1, true, 280); 