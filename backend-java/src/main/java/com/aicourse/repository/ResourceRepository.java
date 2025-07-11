package com.aicourse.repository;

import com.aicourse.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByCourseId(Long courseId);
    List<Resource> findByVisibleToAllTrue();
} 