package com.aicourse.service;

import com.aicourse.entity.Resource;
import com.aicourse.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceService {
    @Autowired
    private ResourceRepository resourceRepository;

    public Resource saveResource(MultipartFile file, Long courseId, Long uploaderId, String description, Boolean visibleToAll) throws IOException {
        String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadsDir);
        if (!dir.exists()) dir.mkdirs();
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        Resource resource = new Resource();
        resource.setFileName(file.getOriginalFilename());
        resource.setFileType(file.getContentType());
        resource.setUrl("/uploads/" + fileName);
        resource.setCourseId(courseId);
        resource.setUploaderId(uploaderId);
        resource.setUploadTime(LocalDateTime.now());
        resource.setDescription(description);
        resource.setVisibleToAll(visibleToAll != null ? visibleToAll : true);
        return resourceRepository.save(resource);
    }

    public List<Resource> getResourcesByCourse(Long courseId) {
        return resourceRepository.findByCourseId(courseId);
    }

    public List<Resource> getAllPublicResources() {
        return resourceRepository.findByVisibleToAllTrue();
    }

    public Resource getResource(Long id) {
        return resourceRepository.findById(id).orElse(null);
    }
} 