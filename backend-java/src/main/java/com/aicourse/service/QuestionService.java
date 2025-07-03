package com.aicourse.service;

import com.aicourse.entity.Course;
import com.aicourse.entity.Question;
import com.aicourse.entity.User;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.QuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Page<Question> getQuestions(Pageable pageable, String questionType, 
                                      String difficulty, Long courseId, User currentUser) {
        
        Specification<Question> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 权限过滤
            if (currentUser.getRole().name().equals("STUDENT")) {
                // 学生只能看到已发布的题目（这里简化处理）
                // 在实际项目中可能需要更复杂的权限逻辑
            } else if (currentUser.getRole().name().equals("TEACHER")) {
                // 教师只能看到自己创建的题目
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), currentUser.getId()));
            }

            // 过滤条件
            if (questionType != null && !questionType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("type"), questionType));
            }
            if (difficulty != null && !difficulty.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }
            if (courseId != null) {
                predicates.add(criteriaBuilder.equal(root.get("courseId"), courseId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return questionRepository.findAll(spec, pageable);
    }

    public Question createQuestion(Map<String, Object> questionData, User currentUser) {
        try {
            Question question = new Question();
            question.setType((String) questionData.get("type"));
            question.setContent((String) questionData.get("content"));
            
            // 处理选项（JSON）
            if (questionData.get("options") != null) {
                String optionsJson = objectMapper.writeValueAsString(questionData.get("options"));
                question.setOptions(optionsJson);
            }
            
            // 处理正确答案（JSON）
            if (questionData.get("correct_answers") != null) {
                String answersJson = objectMapper.writeValueAsString(questionData.get("correct_answers"));
                question.setCorrectAnswers(answersJson);
            }
            
            question.setDifficulty((String) questionData.getOrDefault("difficulty", "medium"));
            question.setScore((Integer) questionData.getOrDefault("score", 10));
            
            // 处理知识点（JSON）
            if (questionData.get("knowledge_points") != null) {
                String knowledgePointsJson = objectMapper.writeValueAsString(questionData.get("knowledge_points"));
                question.setKnowledgePoints(knowledgePointsJson);
            }
            
            question.setExplanation((String) questionData.get("explanation"));
            question.setCourseId((Long) questionData.get("course_id"));
            question.setCreatedBy(currentUser.getId());
            question.setCreatedAt(LocalDateTime.now());
            question.setUsageCount(0);
            question.setCorrectRate(0.0);

            return questionRepository.save(question);

        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON数据处理失败: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("创建题目失败: " + e.getMessage());
        }
    }

    public Map<String, Object> getQuestionDetail(Long questionId, User currentUser) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("id", question.getId());
            result.put("type", question.getType());
            result.put("content", question.getContent());
            result.put("difficulty", question.getDifficulty());
            result.put("score", question.getScore());
            result.put("usage_count", question.getUsageCount());
            result.put("correct_rate", question.getCorrectRate());
            result.put("created_at", question.getCreatedAt());

            // 解析选项
            if (question.getOptions() != null) {
                List<String> options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {});
                result.put("options", options);
            }

            // 解析知识点
            if (question.getKnowledgePoints() != null) {
                List<String> knowledgePoints = objectMapper.readValue(question.getKnowledgePoints(), new TypeReference<List<String>>() {});
                result.put("knowledge_points", knowledgePoints);
            }

            // 权限控制 - 只有教师和管理员能看到正确答案和解析
            if (!currentUser.getRole().name().equals("STUDENT")) {
                if (question.getCorrectAnswers() != null) {
                    List<String> correctAnswers = objectMapper.readValue(question.getCorrectAnswers(), new TypeReference<List<String>>() {});
                    result.put("correct_answers", correctAnswers);
                }
                result.put("explanation", question.getExplanation());
            }

            return result;

        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON数据解析失败: " + e.getMessage());
        }
    }

    public Question updateQuestion(Long questionId, Map<String, Object> questionData, User currentUser) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));

        // 权限检查
        if (currentUser.getRole().name().equals("TEACHER") && 
            !question.getCreatedBy().equals(currentUser.getId())) {
            throw new BusinessException("无权修改此题目");
        }

        try {
            // 更新基本信息
            if (questionData.get("type") != null) {
                question.setType((String) questionData.get("type"));
            }
            if (questionData.get("content") != null) {
                question.setContent((String) questionData.get("content"));
            }
            if (questionData.get("difficulty") != null) {
                question.setDifficulty((String) questionData.get("difficulty"));
            }
            if (questionData.get("score") != null) {
                question.setScore((Integer) questionData.get("score"));
            }
            if (questionData.get("explanation") != null) {
                question.setExplanation((String) questionData.get("explanation"));
            }

            // 更新JSON字段
            if (questionData.get("options") != null) {
                String optionsJson = objectMapper.writeValueAsString(questionData.get("options"));
                question.setOptions(optionsJson);
            }
            if (questionData.get("correct_answers") != null) {
                String answersJson = objectMapper.writeValueAsString(questionData.get("correct_answers"));
                question.setCorrectAnswers(answersJson);
            }
            if (questionData.get("knowledge_points") != null) {
                String knowledgePointsJson = objectMapper.writeValueAsString(questionData.get("knowledge_points"));
                question.setKnowledgePoints(knowledgePointsJson);
            }

            question.setUpdatedAt(LocalDateTime.now());
            return questionRepository.save(question);

        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON数据处理失败: " + e.getMessage());
        }
    }

    public void deleteQuestion(Long questionId, User currentUser) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));

        // 权限检查
        if (currentUser.getRole().name().equals("TEACHER") && 
            !question.getCreatedBy().equals(currentUser.getId())) {
            throw new BusinessException("无权删除此题目");
        }

        // 检查题目是否正在被使用（这里可以添加更多检查逻辑）
        if (question.getUsageCount() > 0) {
            throw new BusinessException("题目正在被使用，无法删除");
        }

        questionRepository.delete(question);
    }

    public Map<String, Object> getQuestionStatistics(User currentUser) {
        try {
            // 基础查询
            List<Question> questions;
            if (currentUser.getRole().name().equals("TEACHER")) {
                questions = questionRepository.findByCreatedBy(currentUser.getId());
            } else {
                questions = questionRepository.findAll();
            }

            int totalQuestions = questions.size();

            // 按类型统计
            Map<String, Long> typeStats = questions.stream()
                    .collect(Collectors.groupingBy(Question::getType, Collectors.counting()));

            // 按难度统计
            Map<String, Long> difficultyStats = questions.stream()
                    .collect(Collectors.groupingBy(Question::getDifficulty, Collectors.counting()));

            // 按课程统计
            List<Map<String, Object>> courseDistribution = new ArrayList<>();
            Map<Long, Long> courseCounts = questions.stream()
                    .filter(q -> q.getCourseId() != null)
                    .collect(Collectors.groupingBy(Question::getCourseId, Collectors.counting()));

            for (Map.Entry<Long, Long> entry : courseCounts.entrySet()) {
                Course course = courseRepository.findById(entry.getKey()).orElse(null);
                if (course != null) {
                    Map<String, Object> courseData = new HashMap<>();
                    courseData.put("course_name", course.getName());
                    courseData.put("question_count", entry.getValue());
                    courseDistribution.add(courseData);
                }
            }

            // 计算平均难度
            double avgDifficulty = 0.0;
            if (totalQuestions > 0) {
                Map<String, Integer> difficultyValues = Map.of("easy", 1, "medium", 2, "hard", 3);
                int totalDifficultyScore = questions.stream()
                        .mapToInt(q -> difficultyValues.getOrDefault(q.getDifficulty(), 2))
                        .sum();
                avgDifficulty = Math.round((double) totalDifficultyScore / totalQuestions * 100.0) / 100.0;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total_questions", totalQuestions);
            result.put("by_type", Map.of(
                "single_choice", typeStats.getOrDefault("single", 0L),
                "multiple_choice", typeStats.getOrDefault("multiple", 0L),
                "essay", typeStats.getOrDefault("essay", 0L),
                "fill_blank", typeStats.getOrDefault("fill_blank", 0L)
            ));
            result.put("by_difficulty", Map.of(
                "easy", difficultyStats.getOrDefault("easy", 0L),
                "medium", difficultyStats.getOrDefault("medium", 0L),
                "hard", difficultyStats.getOrDefault("hard", 0L)
            ));
            result.put("avg_difficulty", avgDifficulty);
            result.put("course_distribution", courseDistribution);
            result.put("categories", courseCounts.size());

            return result;

        } catch (Exception e) {
            throw new BusinessException("获取题目统计失败: " + e.getMessage());
        }
    }
} 