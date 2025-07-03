package com.aicourse.service;

import com.aicourse.entity.KnowledgePoint;
import com.aicourse.entity.KnowledgePointRelation;
import com.aicourse.repository.KnowledgePointRepository;
import com.aicourse.repository.KnowledgePointRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据初始化服务 - 为演示创建知识点数据
 */
@Service
public class DataInitService implements ApplicationRunner {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private KnowledgePointRelationRepository relationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 检查是否已有知识点数据
        long existingCount = knowledgePointRepository.countByCourseId(1L);
        if (existingCount > 0) {
            System.out.println("知识点数据已存在，跳过初始化。当前数量: " + existingCount);
            return;
        }

        System.out.println("开始初始化知识点数据...");
        initializeKnowledgePoints();
        initializeKnowledgeRelations();
        System.out.println("知识点数据初始化完成！");
    }

    private void initializeKnowledgePoints() {
        // 创建Java编程基础课程的知识点
        createKnowledgePoint("Java简介", "Java语言的历史、特点和应用领域介绍", 1L, null, 1, 0.9, 30, "concept", 1);
        createKnowledgePoint("JVM虚拟机", "Java虚拟机的工作原理和内存结构", 1L, null, 3, 0.8, 45, "concept", 2);
        createKnowledgePoint("开发环境搭建", "安装JDK、IDE和配置开发环境", 1L, null, 1, 0.7, 60, "skill", 3);

        // 基础语法知识点
        createKnowledgePoint("数据类型", "Java的基本数据类型和引用数据类型", 1L, null, 2, 0.9, 40, "concept", 4);
        createKnowledgePoint("变量与常量", "变量的声明、初始化和作用域", 1L, null, 2, 0.8, 35, "concept", 5);
        createKnowledgePoint("运算符", "算术、比较、逻辑和位运算符的使用", 1L, null, 2, 0.7, 30, "concept", 6);

        // 控制结构知识点
        createKnowledgePoint("条件语句", "if-else、switch-case语句的使用", 1L, null, 2, 0.8, 35, "skill", 7);
        createKnowledgePoint("循环语句", "for、while、do-while循环的使用", 1L, null, 2, 0.8, 40, "skill", 8);
        createKnowledgePoint("分支控制", "break、continue、return语句", 1L, null, 2, 0.6, 25, "skill", 9);

        // 面向对象知识点
        createKnowledgePoint("类与对象", "类的定义、对象的创建和使用", 1L, null, 3, 0.9, 50, "concept", 10);
        createKnowledgePoint("封装", "访问修饰符、getter/setter方法", 1L, null, 3, 0.8, 40, "concept", 11);
        createKnowledgePoint("继承", "类的继承、super关键字、方法重写", 1L, null, 4, 0.9, 55, "concept", 12);
        createKnowledgePoint("多态", "方法重载、动态绑定、接口实现", 1L, null, 4, 0.8, 50, "concept", 13);

        // 高级特性知识点
        createKnowledgePoint("异常处理", "try-catch-finally、throws关键字", 1L, null, 3, 0.7, 45, "skill", 14);
        createKnowledgePoint("集合框架", "List、Set、Map接口和常用实现类", 1L, null, 4, 0.8, 60, "application", 15);
        createKnowledgePoint("泛型", "泛型类、泛型方法、通配符", 1L, null, 4, 0.6, 50, "application", 16);
        createKnowledgePoint("I/O流", "文件读写、字节流、字符流", 1L, null, 4, 0.7, 55, "application", 17);

        // 实践应用知识点
        createKnowledgePoint("单元测试", "JUnit框架的使用和测试用例编写", 1L, null, 3, 0.6, 40, "application", 18);
        createKnowledgePoint("项目实践", "综合项目开发和代码规范", 1L, null, 5, 0.9, 120, "application", 19);
    }

    private void createKnowledgePoint(String name, String description, Long courseId, Long parentId,
                                    Integer difficultyLevel, Double importance, Integer estimatedTime,
                                    String pointType, Integer orderIndex) {
        KnowledgePoint point = new KnowledgePoint();
        point.setName(name);
        point.setDescription(description);
        point.setCourseId(courseId);
        point.setParentId(parentId);
        point.setDifficultyLevel(difficultyLevel);
        point.setImportance(importance);
        point.setEstimatedTime(estimatedTime);
        point.setPointType(pointType);
        point.setOrderIndex(orderIndex);
        knowledgePointRepository.save(point);
    }

    private void initializeKnowledgeRelations() {
        // 获取所有已保存的知识点，用于创建关系
        List<KnowledgePoint> allPoints = knowledgePointRepository.findByCourseIdOrderByOrderIndexAsc(1L);
        
        if (allPoints.size() < 19) {
            System.out.println("警告：知识点数量不足，无法创建完整的关系网络");
            return;
        }

        // 创建知识点关系（使用数据库ID而不是硬编码ID）
        createRelation(allPoints.get(0).getId(), allPoints.get(2).getId(), "prerequisite", 0.8, "Java简介是环境搭建的前提");
        createRelation(allPoints.get(2).getId(), allPoints.get(3).getId(), "prerequisite", 0.9, "环境搭建后才能学习数据类型");
        createRelation(allPoints.get(3).getId(), allPoints.get(4).getId(), "prerequisite", 0.9, "理解数据类型后学习变量");
        createRelation(allPoints.get(4).getId(), allPoints.get(5).getId(), "prerequisite", 0.8, "变量概念是运算符使用的基础");

        // 控制结构依赖
        createRelation(allPoints.get(5).getId(), allPoints.get(6).getId(), "prerequisite", 0.8, "运算符是条件语句的基础");
        createRelation(allPoints.get(6).getId(), allPoints.get(7).getId(), "prerequisite", 0.7, "条件语句是循环语句的前提");
        createRelation(allPoints.get(7).getId(), allPoints.get(8).getId(), "prerequisite", 0.6, "循环语句是分支控制的基础");

        // 面向对象依赖链
        createRelation(allPoints.get(4).getId(), allPoints.get(9).getId(), "prerequisite", 0.8, "变量概念是类与对象的基础");
        createRelation(allPoints.get(9).getId(), allPoints.get(10).getId(), "prerequisite", 0.9, "类与对象是封装的前提");
        createRelation(allPoints.get(10).getId(), allPoints.get(11).getId(), "prerequisite", 0.9, "封装是继承的基础");
        createRelation(allPoints.get(11).getId(), allPoints.get(12).getId(), "prerequisite", 0.8, "继承是多态的前提");

        // 高级特性依赖
        createRelation(allPoints.get(6).getId(), allPoints.get(13).getId(), "prerequisite", 0.7, "条件语句是异常处理的基础");
        createRelation(allPoints.get(9).getId(), allPoints.get(14).getId(), "prerequisite", 0.8, "类与对象是集合框架的前提");
        createRelation(allPoints.get(14).getId(), allPoints.get(15).getId(), "prerequisite", 0.7, "集合框架是泛型学习的基础");
        createRelation(allPoints.get(3).getId(), allPoints.get(16).getId(), "prerequisite", 0.6, "数据类型是I/O流的基础");

        // 实践应用依赖
        createRelation(allPoints.get(9).getId(), allPoints.get(17).getId(), "prerequisite", 0.7, "类与对象是单元测试的基础");
        createRelation(allPoints.get(12).getId(), allPoints.get(18).getId(), "prerequisite", 0.9, "多态是项目实践的重要基础");
        createRelation(allPoints.get(14).getId(), allPoints.get(18).getId(), "prerequisite", 0.8, "集合框架在项目中广泛使用");
        createRelation(allPoints.get(13).getId(), allPoints.get(18).getId(), "prerequisite", 0.7, "异常处理在项目中必不可少");

        // 关联关系
        createRelation(allPoints.get(1).getId(), allPoints.get(3).getId(), "related", 0.6, "JVM虚拟机与数据类型相关");
        createRelation(allPoints.get(1).getId(), allPoints.get(16).getId(), "related", 0.5, "JVM虚拟机与I/O流有关联");
        createRelation(allPoints.get(15).getId(), allPoints.get(14).getId(), "related", 0.8, "泛型与集合框架密切相关");
    }

    private void createRelation(Long sourceId, Long targetId, String relationType, Double strength, String description) {
        KnowledgePointRelation relation = new KnowledgePointRelation();
        relation.setSourceId(sourceId);
        relation.setTargetId(targetId);
        relation.setRelationType(relationType);
        relation.setStrength(strength);
        // Note: description字段在实体中不存在，暂时忽略
        relationRepository.save(relation);
    }
} 