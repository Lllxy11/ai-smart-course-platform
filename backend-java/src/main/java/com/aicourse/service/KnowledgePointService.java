package com.aicourse.service;

import com.aicourse.entity.KnowledgePoint;
import com.aicourse.entity.KnowledgePointRelation;
import com.aicourse.entity.User;
import com.aicourse.repository.KnowledgePointRepository;
import com.aicourse.repository.KnowledgePointRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识点服务 - 高性能版本
 */
@Service
@Transactional(readOnly = true)
public class KnowledgePointService {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private KnowledgePointRelationRepository relationRepository;

    /**
     * 获取知识图谱数据 - 核心优化方法
     */
    public Map<String, Object> getKnowledgeGraph(Long courseId, String pointType, User currentUser) {
        long startTime = System.currentTimeMillis();
        
        // 1. 批量查询知识点（单次查询）
        List<KnowledgePoint> knowledgePoints = getKnowledgePointsBatch(courseId, pointType);
        
        if (knowledgePoints.isEmpty()) {
            return createEmptyResponse();
        }
        
        // 2. 批量查询关系（单次查询）
        List<Long> nodeIds = knowledgePoints.stream()
                .map(KnowledgePoint::getId)
                .collect(Collectors.toList());
        
        List<KnowledgePointRelation> relations = relationRepository.findRelationsByNodeIds(nodeIds);
        
        // 3. 转换为前端格式
        List<Map<String, Object>> nodes = convertToNodes(knowledgePoints);
        List<Map<String, Object>> links = convertToLinks(relations);
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        result.put("totalNodes", nodes.size());
        result.put("totalLinks", links.size());
        result.put("courseId", courseId);
        result.put("queryTime", endTime - startTime); // 性能监控
        
        return result;
    }

    /**
     * 分页获取知识点
     */
    public Map<String, Object> getKnowledgePointsPaged(Long courseId, int page, int size) {
        int offset = page * size;
        
        List<KnowledgePoint> points = knowledgePointRepository
                .findByCourseIdWithPagination(courseId, size, offset);
        
        long total = knowledgePointRepository.countByCourseId(courseId);
        
        List<Map<String, Object>> data = points.stream()
                .map(this::convertToBasicMap)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }

    /**
     * 获取知识点树形结构
     */
    public List<Map<String, Object>> getKnowledgeTree(Long courseId) {
        // 获取根节点
        List<KnowledgePoint> rootNodes = knowledgePointRepository
                .findByParentIdIsNullAndCourseIdOrderByOrderIndexAsc(courseId);
        
        return rootNodes.stream()
                .map(this::buildTreeNode)
                .collect(Collectors.toList());
    }

    /**
     * 获取知识点统计
     */
    public Map<String, Object> getStatistics(Long courseId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (courseId != null) {
            stats.put("totalPoints", knowledgePointRepository.countByCourseId(courseId));
            stats.put("conceptCount", knowledgePointRepository.countByCourseIdAndPointType(courseId, "concept"));
            stats.put("skillCount", knowledgePointRepository.countByCourseIdAndPointType(courseId, "skill"));
            stats.put("applicationCount", knowledgePointRepository.countByCourseIdAndPointType(courseId, "application"));
        } else {
            stats.put("totalPoints", knowledgePointRepository.count());
        }
        
        return stats;
    }

    // ========== 私有方法 ==========

    private List<KnowledgePoint> getKnowledgePointsBatch(Long courseId, String pointType) {
        if (courseId != null && pointType != null && !pointType.isEmpty()) {
            return knowledgePointRepository.findByCourseIdAndPointTypeOrderByOrderIndexAsc(courseId, pointType);
        } else if (courseId != null) {
            return knowledgePointRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        } else {
            return knowledgePointRepository.findAll();
        }
    }

    private List<Map<String, Object>> convertToNodes(List<KnowledgePoint> points) {
        return points.stream().map(point -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", point.getId());
            node.put("name", point.getName());
            node.put("description", point.getDescription());
            node.put("type", point.getPointType());
            node.put("difficulty", point.getDifficultyLevel());
            node.put("importance", point.getImportance());
            node.put("estimatedTime", point.getEstimatedTime());
            node.put("parentId", point.getParentId());
            
            // 添加节点样式属性
            node.put("category", getCategoryIndex(point.getPointType()));
            node.put("symbolSize", calculateNodeSize(point.getImportance()));
            
            return node;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertToLinks(List<KnowledgePointRelation> relations) {
        return relations.stream().map(relation -> {
            Map<String, Object> link = new HashMap<>();
            link.put("source", relation.getSourceId());
            link.put("target", relation.getTargetId());
            link.put("type", relation.getRelationType());
            link.put("strength", relation.getStrength());
            
            return link;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> convertToBasicMap(KnowledgePoint point) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", point.getId());
        map.put("name", point.getName());
        map.put("description", point.getDescription());
        map.put("type", point.getPointType());
        map.put("difficulty", point.getDifficultyLevel());
        map.put("importance", point.getImportance());
        map.put("estimatedTime", point.getEstimatedTime());
        map.put("createdAt", point.getCreatedAt());
        return map;
    }

    private Map<String, Object> buildTreeNode(KnowledgePoint point) {
        Map<String, Object> node = convertToBasicMap(point);
        
        // 获取子节点
        List<KnowledgePoint> children = knowledgePointRepository
                .findByParentIdOrderByOrderIndexAsc(point.getId());
        
        if (!children.isEmpty()) {
            node.put("children", children.stream()
                    .map(this::buildTreeNode)
                    .collect(Collectors.toList()));
        }
        
        return node;
    }

    private Map<String, Object> createEmptyResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", Collections.emptyList());
        result.put("links", Collections.emptyList());
        result.put("totalNodes", 0);
        result.put("totalLinks", 0);
        return result;
    }

    private int getCategoryIndex(String pointType) {
        switch (pointType) {
            case "concept": return 0;
            case "skill": return 1;
            case "application": return 2;
            default: return 0;
        }
    }

    private double calculateNodeSize(Double importance) {
        if (importance == null) return 20;
        return Math.max(15, Math.min(40, importance * 30));
    }

    /**
     * 创建知识点
     */
    @Transactional
    public Map<String, Object> createKnowledgePoint(Map<String, Object> pointData, User currentUser) {
        System.out.println("创建知识点，收到的数据: " + pointData);
        
        KnowledgePoint point = new KnowledgePoint();
        
        // 安全的数据提取和转换
        point.setName((String) pointData.get("name"));
        point.setDescription((String) pointData.get("description"));
        
        // courseId 处理
        Object courseIdObj = pointData.get("courseId");
        if (courseIdObj != null) {
            if (courseIdObj instanceof Number) {
                point.setCourseId(((Number) courseIdObj).longValue());
            } else {
                point.setCourseId(Long.parseLong(courseIdObj.toString()));
            }
        } else {
            throw new RuntimeException("课程ID不能为空");
        }
        
        point.setPointType((String) pointData.get("pointType"));
        
        // difficultyLevel 处理
        Object difficultyObj = pointData.get("difficultyLevel");
        if (difficultyObj instanceof Number) {
            point.setDifficultyLevel(((Number) difficultyObj).intValue());
        } else if (difficultyObj != null) {
            point.setDifficultyLevel(Integer.parseInt(difficultyObj.toString()));
        } else {
            point.setDifficultyLevel(1); // 默认值
        }
        
        // importance 处理
        Object importanceObj = pointData.get("importance");
        if (importanceObj instanceof Number) {
            point.setImportance(((Number) importanceObj).doubleValue());
        } else if (importanceObj != null) {
            point.setImportance(Double.parseDouble(importanceObj.toString()));
        } else {
            point.setImportance(0.5); // 默认值
        }
        
        // estimatedTime 处理
        Object timeObj = pointData.get("estimatedTime");
        if (timeObj instanceof Number) {
            point.setEstimatedTime(((Number) timeObj).intValue());
        } else if (timeObj != null) {
            point.setEstimatedTime(Integer.parseInt(timeObj.toString()));
        } else {
            point.setEstimatedTime(30); // 默认值
        }
        
        // parentId 处理
        Object parentIdObj = pointData.get("parentId");
        if (parentIdObj != null) {
            if (parentIdObj instanceof Number) {
                point.setParentId(((Number) parentIdObj).longValue());
            } else {
                point.setParentId(Long.parseLong(parentIdObj.toString()));
            }
        }
        
        // 设置排序索引
        long maxOrder = knowledgePointRepository.countByCourseId(point.getCourseId());
        point.setOrderIndex((int) maxOrder + 1);
        
        KnowledgePoint savedPoint = knowledgePointRepository.save(point);
        return convertToBasicMap(savedPoint);
    }

    /**
     * 更新知识点
     */
    @Transactional
    public Map<String, Object> updateKnowledgePoint(Long id, Map<String, Object> pointData, User currentUser) {
        KnowledgePoint point = knowledgePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识点不存在"));
        
        // 安全的数据更新
        point.setName((String) pointData.get("name"));
        point.setDescription((String) pointData.get("description"));
        point.setPointType((String) pointData.get("pointType"));
        
        // difficultyLevel 处理
        Object difficultyObj = pointData.get("difficultyLevel");
        if (difficultyObj instanceof Number) {
            point.setDifficultyLevel(((Number) difficultyObj).intValue());
        } else if (difficultyObj != null) {
            point.setDifficultyLevel(Integer.parseInt(difficultyObj.toString()));
        }
        
        // importance 处理
        Object importanceObj = pointData.get("importance");
        if (importanceObj instanceof Number) {
            point.setImportance(((Number) importanceObj).doubleValue());
        } else if (importanceObj != null) {
            point.setImportance(Double.parseDouble(importanceObj.toString()));
        }
        
        // estimatedTime 处理
        Object timeObj = pointData.get("estimatedTime");
        if (timeObj instanceof Number) {
            point.setEstimatedTime(((Number) timeObj).intValue());
        } else if (timeObj != null) {
            point.setEstimatedTime(Integer.parseInt(timeObj.toString()));
        }
        
        // parentId 处理
        Object parentIdObj = pointData.get("parentId");
        if (parentIdObj != null) {
            if (parentIdObj instanceof Number) {
                point.setParentId(((Number) parentIdObj).longValue());
            } else {
                point.setParentId(Long.parseLong(parentIdObj.toString()));
            }
        } else {
            point.setParentId(null);
        }
        
        KnowledgePoint savedPoint = knowledgePointRepository.save(point);
        return convertToBasicMap(savedPoint);
    }

    /**
     * 删除知识点
     */
    @Transactional
    public void deleteKnowledgePoint(Long id, User currentUser) {
        KnowledgePoint point = knowledgePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识点不存在"));
        
        // 删除相关的关系
        List<KnowledgePointRelation> relations = relationRepository.findRelationsByNodeIds(List.of(id));
        relationRepository.deleteAll(relations);
        
        // 删除知识点
        knowledgePointRepository.delete(point);
    }

    /**
     * 获取知识点详情
     */
    public Map<String, Object> getKnowledgePointById(Long id) {
        KnowledgePoint point = knowledgePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识点不存在"));
        
        return convertToBasicMap(point);
    }

    /**
     * 获取知识点关系
     */
    public Map<String, Object> getKnowledgePointRelations(Long pointId) {
        List<KnowledgePointRelation> relations = relationRepository.findRelationsByNodeIds(List.of(pointId));
        
        // 分离前置和后续关系
        List<KnowledgePointRelation> prerequisites = relations.stream()
                .filter(r -> r.getTargetId().equals(pointId) && "prerequisite".equals(r.getRelationType()))
                .collect(Collectors.toList());
        
        List<KnowledgePointRelation> subsequents = relations.stream()
                .filter(r -> r.getSourceId().equals(pointId) && "prerequisite".equals(r.getRelationType()))
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("prerequisites", prerequisites);
        result.put("subsequents", subsequents);
        
        return result;
    }
} 