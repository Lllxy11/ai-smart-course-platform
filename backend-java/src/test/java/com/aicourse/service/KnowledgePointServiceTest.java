package com.aicourse.service;

import com.aicourse.entity.KnowledgePoint;
import com.aicourse.entity.KnowledgePointRelation;
import com.aicourse.entity.User;
import com.aicourse.repository.KnowledgePointRepository;
import com.aicourse.repository.KnowledgePointRelationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// 导入必要的类
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KnowledgePointService 单元测试")
class KnowledgePointServiceTest {

    @Mock
    private KnowledgePointRepository knowledgePointRepository;

    @Mock
    private KnowledgePointRelationRepository relationRepository;

    @InjectMocks
    private KnowledgePointService knowledgePointService;

    private User testUser;
    private Long courseId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        courseId = 101L;
    }

    @Test
    @DisplayName("getKnowledgeGraph - 当存在知识点和关系时，应返回完整的图谱数据")
    void getKnowledgeGraph_whenDataExists_shouldReturnGraphData() {
        // Arrange
        KnowledgePoint kp1 = createKnowledgePoint(1L, "Java基础", "concept");
        KnowledgePoint kp2 = createKnowledgePoint(2L, "面向对象", "concept");
        List<KnowledgePoint> points = Arrays.asList(kp1, kp2);

        KnowledgePointRelation relation = new KnowledgePointRelation();
        relation.setId(1L);
        relation.setSourceId(1L);
        relation.setTargetId(2L);
        relation.setRelationType("prerequisite");
        relation.setStrength(0.8);
        List<KnowledgePointRelation> relations = Collections.singletonList(relation);

        List<Long> nodeIds = Arrays.asList(1L, 2L);

        when(knowledgePointRepository.findByCourseIdAndPointTypeOrderByOrderIndexAsc(courseId, "concept")).thenReturn(points);
        when(relationRepository.findRelationsByNodeIds(nodeIds)).thenReturn(relations);

        // Act
        Map<String, Object> result = knowledgePointService.getKnowledgeGraph(courseId, "concept", testUser);

        // Assert
        assertNotNull(result);
        assertEquals(courseId, result.get("courseId"));
        assertEquals(2, result.get("totalNodes"));
        assertEquals(1, result.get("totalLinks"));

        List<Map<String, Object>> nodes = (List<Map<String, Object>>) result.get("nodes");
        assertEquals(2, nodes.size());
        assertEquals(1L, nodes.get(0).get("id"));
        assertEquals("Java基础", nodes.get(0).get("name"));

        List<Map<String, Object>> links = (List<Map<String, Object>>) result.get("links");
        assertEquals(1, links.size());
        assertEquals(1L, links.get(0).get("source"));
        assertEquals(2L, links.get(0).get("target"));

        verify(knowledgePointRepository).findByCourseIdAndPointTypeOrderByOrderIndexAsc(courseId, "concept");
        verify(relationRepository).findRelationsByNodeIds(nodeIds);
    }

    @Test
    @DisplayName("getKnowledgeGraph - 当知识点不存在时，应返回空的图谱数据")
    void getKnowledgeGraph_whenNoPoints_shouldReturnEmptyGraph() {
        // Arrange
        when(knowledgePointRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = knowledgePointService.getKnowledgeGraph(courseId, null, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.get("totalNodes"));
        assertEquals(0, result.get("totalLinks"));
        assertTrue(((List<?>) result.get("nodes")).isEmpty());
        assertTrue(((List<?>) result.get("links")).isEmpty());

        verify(relationRepository, never()).findRelationsByNodeIds(anyList());
    }

    @Test
    @DisplayName("getKnowledgePointsPaged - 应返回分页后的知识点列表")
    void getKnowledgePointsPaged_shouldReturnPagedData() {
        // Arrange
        int page = 0, size = 5;
        long total = 25L;
        List<KnowledgePoint> points = Arrays.asList(
            createKnowledgePoint(1L, "Point 1", "skill"),
            createKnowledgePoint(2L, "Point 2", "skill")
        );
        when(knowledgePointRepository.findByCourseIdWithPagination(courseId, size, page * size)).thenReturn(points);
        when(knowledgePointRepository.countByCourseId(courseId)).thenReturn(total);

        // Act
        Map<String, Object> result = knowledgePointService.getKnowledgePointsPaged(courseId, page, size);

        // Assert
        assertEquals(points.size(), ((List<?>) result.get("data")).size());
        assertEquals(total, result.get("total"));
        assertEquals(page, result.get("page"));
        assertEquals(size, result.get("size"));
        assertEquals(5L, result.get("totalPages"));
    }

    @Test
    @DisplayName("getKnowledgeTree - 应返回树形结构的知识点")
    void getKnowledgeTree_shouldReturnTreeStructure() {
        // Arrange
        KnowledgePoint root = createKnowledgePoint(1L, "Root", "concept");
        KnowledgePoint child = createKnowledgePoint(2L, "Child", "skill");
        child.setParentId(1L);

        when(knowledgePointRepository.findByParentIdIsNullAndCourseIdOrderByOrderIndexAsc(courseId))
                .thenReturn(Collections.singletonList(root));
        when(knowledgePointRepository.findByParentIdOrderByOrderIndexAsc(root.getId()))
                .thenReturn(Collections.singletonList(child));
        when(knowledgePointRepository.findByParentIdOrderByOrderIndexAsc(child.getId()))
                .thenReturn(Collections.emptyList());

        // Act
        List<Map<String, Object>> tree = knowledgePointService.getKnowledgeTree(courseId);

        // Assert
        assertNotNull(tree);
        assertEquals(1, tree.size());
        Map<String, Object> rootNode = tree.get(0);
        assertEquals("Root", rootNode.get("name"));
        assertTrue(rootNode.containsKey("children"));
        List<Map<String, Object>> children = (List<Map<String, Object>>) rootNode.get("children");
        assertEquals(1, children.size());
        assertEquals("Child", children.get(0).get("name"));
        assertFalse(children.get(0).containsKey("children"));
    }

    @Test
    @DisplayName("getStatistics - 当提供 courseId 时，应返回特定课程的统计信息")
    void getStatistics_withCourseId_shouldReturnCourseStats() {
        // Arrange
        when(knowledgePointRepository.countByCourseId(courseId)).thenReturn(10L);
        when(knowledgePointRepository.countByCourseIdAndPointType(courseId, "concept")).thenReturn(5L);
        when(knowledgePointRepository.countByCourseIdAndPointType(courseId, "skill")).thenReturn(3L);
        when(knowledgePointRepository.countByCourseIdAndPointType(courseId, "application")).thenReturn(2L);

        // Act
        Map<String, Object> stats = knowledgePointService.getStatistics(courseId);

        // Assert
        assertEquals(10L, stats.get("totalPoints"));
        assertEquals(5L, stats.get("conceptCount"));
        assertEquals(3L, stats.get("skillCount"));
        assertEquals(2L, stats.get("applicationCount"));
    }

    @Test
    @DisplayName("createKnowledgePoint - 应成功创建并返回新的知识点")
    void createKnowledgePoint_shouldCreateAndReturnNewPoint() {
        // Arrange
        Map<String, Object> pointData = new HashMap<>();
        pointData.put("name", "New Point");
        pointData.put("description", "A new point");
        pointData.put("courseId", courseId);
        pointData.put("pointType", "concept");
        pointData.put("difficultyLevel", 3);

        KnowledgePoint savedPoint = createKnowledgePoint(100L, "New Point", "concept");

        when(knowledgePointRepository.countByCourseId(courseId)).thenReturn(10L);
        when(knowledgePointRepository.save(any(KnowledgePoint.class))).thenReturn(savedPoint);

        // Act
        Map<String, Object> result = knowledgePointService.createKnowledgePoint(pointData, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.get("id"));
        assertEquals("New Point", result.get("name"));

        ArgumentCaptor<KnowledgePoint> pointCaptor = ArgumentCaptor.forClass(KnowledgePoint.class);
        verify(knowledgePointRepository).save(pointCaptor.capture());

        KnowledgePoint capturedPoint = pointCaptor.getValue();
        assertEquals("New Point", capturedPoint.getName());
        assertEquals(courseId, capturedPoint.getCourseId());
        assertEquals(11, capturedPoint.getOrderIndex());
    }

    @Test
    @DisplayName("updateKnowledgePoint - 当知识点存在时，应更新并返回")
    void updateKnowledgePoint_whenPointExists_shouldUpdateAndReturn() {
        // Arrange
        Long pointId = 1L;
        Map<String, Object> pointData = new HashMap<>();
        pointData.put("name", "Updated Name");
        pointData.put("description", "Updated Description");
        pointData.put("pointType", "skill");

        KnowledgePoint existingPoint = createKnowledgePoint(pointId, "Original Name", "concept");
        KnowledgePoint updatedPoint = createKnowledgePoint(pointId, "Updated Name", "skill");

        when(knowledgePointRepository.findById(pointId)).thenReturn(Optional.of(existingPoint));
        when(knowledgePointRepository.save(any(KnowledgePoint.class))).thenReturn(updatedPoint);

        // Act
        Map<String, Object> result = knowledgePointService.updateKnowledgePoint(pointId, pointData, testUser);

        // Assert
        assertEquals("Updated Name", result.get("name"));
        ArgumentCaptor<KnowledgePoint> captor = ArgumentCaptor.forClass(KnowledgePoint.class);
        verify(knowledgePointRepository).save(captor.capture());
        assertEquals("Updated Name", captor.getValue().getName());
        assertEquals("Updated Description", captor.getValue().getDescription());
        assertEquals("skill", captor.getValue().getPointType());
    }

    @Test
    @DisplayName("updateKnowledgePoint - 当知识点不存在时，应抛出异常")
    void updateKnowledgePoint_whenPointNotExist_shouldThrowException() {
        // Arrange
        Long nonExistentId = 999L;
        when(knowledgePointRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            knowledgePointService.updateKnowledgePoint(nonExistentId, new HashMap<>(), testUser);
        });
        assertEquals("知识点不存在", exception.getMessage());
    }

    @Test
    @DisplayName("deleteKnowledgePoint - 当知识点存在时，应删除知识点及其关系")
    void deleteKnowledgePoint_whenPointExists_shouldDeletePointAndRelations() {
        // Arrange
        Long pointId = 1L;
        KnowledgePoint pointToDelete = createKnowledgePoint(pointId, "To Delete", "concept");

        KnowledgePointRelation relation = new KnowledgePointRelation();
        relation.setId(1L);
        relation.setSourceId(pointId);
        relation.setTargetId(2L);
        relation.setRelationType("prerequisite");
        relation.setStrength(1.0);
        List<KnowledgePointRelation> relationsToDelete = Collections.singletonList(relation);

        when(knowledgePointRepository.findById(pointId)).thenReturn(Optional.of(pointToDelete));
        when(relationRepository.findRelationsByNodeIds(List.of(pointId))).thenReturn(relationsToDelete);

        // Act
        knowledgePointService.deleteKnowledgePoint(pointId, testUser);

        // Assert
        verify(relationRepository).deleteAll(relationsToDelete);
        verify(knowledgePointRepository).delete(pointToDelete);
    }
    
    @Test
    @DisplayName("getKnowledgePointById - 当知识点存在时，应返回其详情")
    void getKnowledgePointById_whenPointExists_shouldReturnDetails() {
        // Arrange
        Long pointId = 1L;
        KnowledgePoint point = createKnowledgePoint(pointId, "Detail Point", "application");
        when(knowledgePointRepository.findById(pointId)).thenReturn(Optional.of(point));

        // Act
        Map<String, Object> result = knowledgePointService.getKnowledgePointById(pointId);
        
        // Assert
        assertNotNull(result);
        assertEquals(pointId, result.get("id"));
        assertEquals("Detail Point", result.get("name"));
        assertEquals("application", result.get("type"));
    }

    // ========== 辅助方法 ==========

    /**
     * 创建一个知识点实体用于测试
     */
    private KnowledgePoint createKnowledgePoint(Long id, String name, String type) {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setId(id);
        kp.setCourseId(courseId);
        kp.setName(name);
        kp.setPointType(type);
        kp.setDescription("Description for " + name);
        kp.setDifficultyLevel(3);
        kp.setImportance(0.7);
        kp.setEstimatedTime(60);
        kp.setCreatedAt(LocalDateTime.now()); // <--- 已修正
        return kp;
    }
}