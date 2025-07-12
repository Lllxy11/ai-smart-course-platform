package com.aicourse.service;

import com.aicourse.entity.Resource;
import com.aicourse.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceService 单元测试")
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    // 我们需要一个 MultipartFile 的模拟对象
    @Mock
    private MultipartFile mockFile;

    private Long courseId = 1L;
    private Long uploaderId = 10L;
    private Resource sampleResource;

    @BeforeEach
    void setUp() {
        // 为模拟的 MultipartFile 设置默认行为
        when(mockFile.getOriginalFilename()).thenReturn("test-document.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");

        // 创建一个用于返回的示例 Resource 对象
        sampleResource = new Resource();
        sampleResource.setId(101L);
        sampleResource.setFileName("test-document.pdf");
        sampleResource.setCourseId(courseId);
        sampleResource.setUploaderId(uploaderId);
    }

    @Nested
    @DisplayName("saveResource 方法测试")
    class SaveResourceTests {

        @Test
        @DisplayName("成功保存资源应返回 Resource 对象并调用文件传输")
        void saveResource_success() throws IOException {
            // Arrange
            // 当 resourceRepository.save 被调用时，我们让它直接返回传入的 Resource 对象
            // 这样便于我们断言对象的所有属性都被正确设置了
            when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
                Resource resourceToSave = invocation.getArgument(0);
                resourceToSave.setId(99L); // 模拟数据库生成ID
                return resourceToSave;
            });
            
            String description = "A test document.";
            Boolean visibleToAll = true;

            // Act
            Resource savedResource = resourceService.saveResource(mockFile, courseId, uploaderId, description, visibleToAll);

            // Assert
            // 1. 验证 file.transferTo() 是否被调用，这是关键，我们不实际写入文件
            verify(mockFile, times(1)).transferTo(any(File.class));

            // 2. 使用 ArgumentCaptor 捕获传递给 repository.save 的对象
            ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
            verify(resourceRepository, times(1)).save(resourceCaptor.capture());
            Resource capturedResource = resourceCaptor.getValue();

            // 3. 断言被保存的 Resource 对象的属性是否正确
            assertNotNull(savedResource);
            assertEquals(99L, savedResource.getId()); // 验证返回的对象是经过模拟 save 后的
            
            assertEquals("test-document.pdf", capturedResource.getFileName());
            assertEquals("application/pdf", capturedResource.getFileType());
            assertEquals(courseId, capturedResource.getCourseId());
            assertEquals(uploaderId, capturedResource.getUploaderId());
            assertEquals(description, capturedResource.getDescription());
            assertTrue(capturedResource.getVisibleToAll());
            assertNotNull(capturedResource.getUploadTime());
            assertTrue(capturedResource.getUrl().startsWith("/uploads/"));
            assertTrue(capturedResource.getUrl().endsWith("_test-document.pdf"));
        }

        @Test
        @DisplayName("当 visibleToAll 为 null 时，应默认为 true")
        void saveResource_withNullVisibility_shouldDefaultToTrue() throws IOException {
             when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArgument(0));

             resourceService.saveResource(mockFile, courseId, uploaderId, "desc", null);

             ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
             verify(resourceRepository).save(captor.capture());
             assertTrue(captor.getValue().getVisibleToAll());
        }


        @Test
        @DisplayName("当文件传输失败时，应抛出 IOException 且不保存到数据库")
        void saveResource_whenTransferToFails_shouldThrowIOException() throws IOException {
            // Arrange
            // 模拟 file.transferTo() 抛出 IOException
            doThrow(new IOException("Disk is full")).when(mockFile).transferTo(any(File.class));
            
            // Act & Assert
            // 断言调用 service 方法时会抛出 IOException
            IOException exception = assertThrows(IOException.class, () -> {
                resourceService.saveResource(mockFile, courseId, uploaderId, "description", true);
            });

            assertEquals("Disk is full", exception.getMessage());

            // 最重要的是，验证在 IO 异常发生后，repository.save 方法根本没有被调用
            verify(resourceRepository, never()).save(any(Resource.class));
        }
    }

    @Nested
    @DisplayName("查询资源方法测试")
    class GetResourceTests {

        @Test
        @DisplayName("getResourcesByCourse 应返回指定课程的资源列表")
        void getResourcesByCourse_shouldReturnResourceList() {
            // Arrange
            List<Resource> resources = Collections.singletonList(sampleResource);
            when(resourceRepository.findByCourseId(courseId)).thenReturn(resources);

            // Act
            List<Resource> result = resourceService.getResourcesByCourse(courseId);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(sampleResource.getId(), result.get(0).getId());
            verify(resourceRepository, times(1)).findByCourseId(courseId);
        }

        @Test
        @DisplayName("getAllPublicResources 应返回所有可见的资源列表")
        void getAllPublicResources_shouldReturnPublicResourceList() {
            // Arrange
            List<Resource> resources = Collections.singletonList(sampleResource);
            when(resourceRepository.findByVisibleToAllTrue()).thenReturn(resources);

            // Act
            List<Resource> result = resourceService.getAllPublicResources();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(resourceRepository, times(1)).findByVisibleToAllTrue();
        }

        @Test
        @DisplayName("getResource 根据 ID 应返回对应的资源")
        void getResource_whenFound_shouldReturnResource() {
            // Arrange
            Long resourceId = 101L;
            when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(sampleResource));

            // Act
            Resource result = resourceService.getResource(resourceId);

            // Assert
            assertNotNull(result);
            assertEquals(resourceId, result.getId());
            verify(resourceRepository, times(1)).findById(resourceId);
        }

        @Test
        @DisplayName("getResource 当资源不存在时应返回 null")
        void getResource_whenNotFound_shouldReturnNull() {
            // Arrange
            Long nonExistentId = 404L;
            when(resourceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act
            Resource result = resourceService.getResource(nonExistentId);

            // Assert
            assertNull(result);
            verify(resourceRepository, times(1)).findById(nonExistentId);
        }
    }
}