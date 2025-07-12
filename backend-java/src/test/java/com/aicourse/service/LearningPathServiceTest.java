package com.aicourse.service;

import com.aicourse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LearningPathService 单元测试")
class LearningPathServiceTest {

    private LearningPathService learningPathService;
    private User testUser;
    private Long testUserId = 1L;
    private Long testCourseId = 101L;
    private Long testKnowledgePointId = 201L;

    @BeforeEach
    void setUp() {
        // 由于没有依赖注入，直接 new 即可
        learningPathService = new LearningPathService();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("test-student");
    }

    @Nested
    @DisplayName("内部数据类测试")
    class InnerClassTests {

        @Test
        @DisplayName("LearningNode - 构造函数应正确初始化")
        void learningNode_constructor_shouldInitializeCorrectly() {
            List<Long> prerequisites = Arrays.asList(1L, 2L);
            LearningPathService.LearningNode node = new LearningPathService.LearningNode(
                10L, "节点1", "concept", 3.0, 60, prerequisites, 0.8
            );

            assertEquals(10L, node.getId());
            assertEquals("节点1", node.getName());
            assertEquals(60, node.getEstimatedTime());
            assertEquals(2, node.getPrerequisites().size());
            assertEquals(0.8, node.getMasteryThreshold()); // 默认值
        }

        @Test
        @DisplayName("LearningPath - 应能正确计算总时间")
        void learningPath_shouldCalculateTotalTimeCorrectly() {
            LearningPathService.LearningNode node1 = new LearningPathService.LearningNode();
            node1.setEstimatedTime(30);
            LearningPathService.LearningNode node2 = new LearningPathService.LearningNode();
            node2.setEstimatedTime(45);

            LearningPathService.LearningPath path = new LearningPathService.LearningPath(
                "path-1", "测试路径", "描述", Arrays.asList(node1, node2)
            );

            assertEquals(75, path.getTotalTime());
        }

        @Test
        @DisplayName("LearningPath - setNodes 应能更新总时间")
        void learningPath_setNodes_shouldUpdateTotalTime() {
            LearningPathService.LearningPath path = new LearningPathService.LearningPath();
            path.setNodes(List.of());
            assertEquals(0, path.getTotalTime());

            LearningPathService.LearningNode node1 = new LearningPathService.LearningNode();
            node1.setEstimatedTime(100);
            path.setNodes(List.of(node1));

            assertEquals(100, path.getTotalTime());
        }
    }

    @Nested
    @DisplayName("API方法测试")
    class ApiMethodTests {

        @Test
        @DisplayName("generatePersonalizedPath (Integer) - 应返回默认路径")
        void generatePersonalizedPath_withIntegerList_shouldReturnDefaultPath() {
            List<Integer> targetPoints = Arrays.asList(101, 102);
            List<Map<String, Object>> result = learningPathService.generatePersonalizedPath(testUserId, targetPoints, 30, List.of("goal1"));

            assertNotNull(result);
            assertEquals(1, result.size());
            Map<String, Object> path = result.get(0);
            assertEquals("default_path", path.get("path_id"));
            assertEquals(2, path.get("node_count"));
        }

        @Test
        @DisplayName("generatePersonalizedPath (Long) - 应返回默认路径")
        void generatePersonalizedPath_withLongList_shouldReturnDefaultPath() {
            List<Long> targetPoints = Arrays.asList(101L, 102L, 103L);
            List<Map<String, Object>> result = learningPathService.generatePersonalizedPath(testUserId, targetPoints);

            assertNotNull(result);
            assertEquals(1, result.size());
            Map<String, Object> path = result.get(0);
            assertEquals("推荐学习路径", path.get("path_name"));
            assertEquals(3, path.get("node_count"));
            assertEquals(180, path.get("total_time")); // 60 * 3
        }

        @Test
        @DisplayName("getPathDetail - 应返回模拟的路径详情")
        void getPathDetail_shouldReturnMockedDetails() {
            String pathId = "some-path";
            Map<String, Object> result = learningPathService.getPathDetail(pathId, testUser);

            assertNotNull(result);
            assertEquals(pathId, result.get("path_id"));
            assertEquals("详细学习路径", result.get("name"));
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) result.get("nodes");
            assertNotNull(nodes);
            assertEquals(5, nodes.size());
        }

        @Test
        @DisplayName("updateProgress - 调用时不应抛出异常")
        void updateProgress_shouldNotThrowException() {
            // 对于只打印日志的void方法，测试其能正常执行不报错即可
            assertDoesNotThrow(() -> {
                learningPathService.updateProgress("path-1", 1, 0.5, testUserId);
            });
        }

        @Test
        @DisplayName("getAnalytics - 应返回模拟的分析数据")
        void getAnalytics_shouldReturnMockedAnalytics() {
            Map<String, Object> result = learningPathService.getAnalytics(testUserId);
            assertNotNull(result);
            assertEquals(testUserId, result.get("user_id"));
            assertEquals(3, result.get("total_paths_taken"));
            Map<String, Object> efficiency = (Map<String, Object>) result.get("learning_efficiency");
            assertEquals("improving", efficiency.get("trend"));
        }

        @Test
        @DisplayName("getStudyScheduleRecommendation - 应返回模拟的日程推荐")
        void getStudyScheduleRecommendation_shouldReturnMockedSchedule() {
            Map<String, Object> result = learningPathService.getStudyScheduleRecommendation(testUserId, "path-1", "2024-12-31");
            assertNotNull(result);
            assertTrue(result.containsKey("daily_schedule"));
            assertTrue(result.containsKey("milestones"));
            List<Map<String, Object>> dailySchedule = (List<Map<String, Object>>) result.get("daily_schedule");
            assertEquals(7, dailySchedule.size());
        }

        @Test
        @DisplayName("analyzeLearningGaps - 应返回模拟的学习差距分析")
        void analyzeLearningGaps_shouldReturnMockedGaps() {
            Map<String, Object> result = learningPathService.analyzeLearningGaps(testUserId, testCourseId);
            assertNotNull(result);
            assertEquals(1, result.get("total_gaps"));
            List<Map<String, Object>> gaps = (List<Map<String, Object>>) result.get("knowledge_gaps");
            assertEquals(0.3, gaps.get(0).get("gap_size"));
        }
    }
    
    @Nested
    @DisplayName("新增API方法测试")
    class NewApiMethodTests {
        
        @Test
        @DisplayName("getLearningPaths - 应返回成功的路径列表")
        void getLearningPaths_shouldReturnSuccessfulPathList() {
            Map<String, Object> result = learningPathService.getLearningPaths(testCourseId, testUserId, testUser);

            assertTrue((Boolean) result.get("success"));
            List<Map<String, Object>> paths = (List<Map<String, Object>>) result.get("paths");
            assertNotNull(paths);
            assertEquals(3, paths.size());
            assertEquals(3, result.get("total"));
        }
        
        @Test
        @DisplayName("getKnowledgePointLearningPath - 应返回成功的知识点路径")
        void getKnowledgePointLearningPath_shouldReturnSuccessfulKpPath() {
            Map<String, Object> result = learningPathService.getKnowledgePointLearningPath(testKnowledgePointId, testUserId, testUser);
            
            assertTrue((Boolean) result.get("success"));
            Map<String, Object> path = (Map<String, Object>) result.get("path");
            assertNotNull(path);
            assertEquals("kp_path_" + testKnowledgePointId, path.get("id"));
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) path.get("nodes");
            assertEquals(4, nodes.size()); // 1 target + 3 related
            assertEquals("target", nodes.get(0).get("type"));
        }

        @Test
        @DisplayName("getRecommendedLearningPath - 应返回成功的推荐路径")
        void getRecommendedLearningPath_shouldReturnSuccessfulRecommendedPath() {
            Map<String, Object> result = learningPathService.getRecommendedLearningPath(testCourseId, testUserId, testUser);

            assertTrue((Boolean) result.get("success"));
            Map<String, Object> path = (Map<String, Object>) result.get("path");
            assertNotNull(path);
            assertEquals("recommended_path", path.get("id"));
            assertEquals(85.5, path.get("matchScore"));
        }

        @Test
        @DisplayName("startLearningPath - 应返回成功的开始状态")
        void startLearningPath_shouldReturnSuccessfulStartStatus() {
            Long pathId = 99L;
            Map<String, Object> result = learningPathService.startLearningPath(pathId, testUser);

            assertTrue((Boolean) result.get("success"));
            assertEquals("学习路径已开始", result.get("message"));
            assertEquals(pathId, result.get("pathId"));
            assertEquals("ACTIVE", result.get("status"));
        }

        @Test
        @DisplayName("completeLearningPathNode - 应返回成功的完成状态")
        void completeLearningPathNode_shouldReturnSuccessfulCompletionStatus() {
            Long pathId = 99L;
            Long nodeId = 10L;
            Map<String, Object> result = learningPathService.completeLearningPathNode(pathId, nodeId, testUser);

            assertTrue((Boolean) result.get("success"));
            assertEquals("学习节点已完成", result.get("message"));
            assertEquals(nodeId, result.get("nodeId"));
            assertEquals(100, result.get("progress"));
            assertEquals(nodeId + 1, result.get("nextNodeId"));
        }
    }
}