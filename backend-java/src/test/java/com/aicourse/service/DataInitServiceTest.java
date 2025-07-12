package com.aicourse.service;

import com.aicourse.entity.KnowledgePoint;
import com.aicourse.entity.KnowledgePointRelation;
import com.aicourse.repository.KnowledgePointRepository;
import com.aicourse.repository.KnowledgePointRelationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitServiceTest {

    @Mock
    private KnowledgePointRepository knowledgePointRepository;

    @Mock
    private KnowledgePointRelationRepository relationRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private DataInitService dataInitService;

    @Test
    @DisplayName("Run - 当数据已存在时，应跳过初始化")
    void run_whenDataExists_shouldSkipInitialization() throws Exception {
        // Arrange
        // 模拟知识点已存在
        when(knowledgePointRepository.countByCourseId(1L)).thenReturn(10L);

        // Act
        dataInitService.run(applicationArguments);

        // Assert
        // 验证 countByCourseId 被调用了一次
        verify(knowledgePointRepository, times(1)).countByCourseId(1L);

        // 验证 save 方法从未被调用，因为数据已存在
        verify(knowledgePointRepository, never()).save(any(KnowledgePoint.class));
        verify(relationRepository, never()).save(any(KnowledgePointRelation.class));
    }

    @Test
    @DisplayName("Run - 当数据不存在时，应初始化知识点和关系")
    void run_whenNoDataExists_shouldInitializeData() throws Exception {
        // Arrange
        // 模拟知识点不存在
        when(knowledgePointRepository.countByCourseId(1L)).thenReturn(0L);

        // 模拟 `initializeKnowledgeRelations` 中获取知识点的调用
        // 创建足够多的模拟知识点以通过 `allPoints.size() < 19` 检查
        List<KnowledgePoint> mockPoints = IntStream.range(1, 21)
                .mapToObj(i -> {
                    KnowledgePoint kp = new KnowledgePoint();
                    kp.setId((long) i);
                    return kp;
                })
                .collect(Collectors.toList());
        when(knowledgePointRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(mockPoints);

        // Act
        dataInitService.run(applicationArguments);

        // Assert
        // 验证 countByCourseId 被调用了一次
        verify(knowledgePointRepository, times(1)).countByCourseId(1L);

        // 验证 `initializeKnowledgePoints` 中的 19 次 save 调用
        verify(knowledgePointRepository, times(19)).save(any(KnowledgePoint.class));

        // 验证 `initializeKnowledgeRelations` 中的 19 次 save 调用
        verify(relationRepository, times(19)).save(any(KnowledgePointRelation.class));

        // 验证获取知识点列表的调用
        verify(knowledgePointRepository, times(1)).findByCourseIdOrderByOrderIndexAsc(1L);
    }
    
    @Test
    @DisplayName("Run - 当知识点不足时，关系初始化应跳过")
    void run_whenNotEnoughKnowledgePoints_shouldSkipRelationInitialization() throws Exception {
        // Arrange
        // 模拟知识点不存在
        when(knowledgePointRepository.countByCourseId(1L)).thenReturn(0L);

        // 模拟返回的知识点列表数量不足
        List<KnowledgePoint> mockPoints = IntStream.range(1, 5) // Only 4 points
                .mapToObj(i -> {
                    KnowledgePoint kp = new KnowledgePoint();
                    kp.setId((long) i);
                    return kp;
                })
                .collect(Collectors.toList());
        when(knowledgePointRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(mockPoints);
        
        // Act
        dataInitService.run(applicationArguments);
        
        // Assert
        // 验证知识点仍然被创建
        verify(knowledgePointRepository, times(19)).save(any(KnowledgePoint.class));
        
        // 验证关系没有被创建，因为知识点数量不足
        verify(relationRepository, never()).save(any(KnowledgePointRelation.class));
    }
}