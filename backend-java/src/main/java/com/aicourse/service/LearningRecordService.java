package com.aicourse.service;

import com.aicourse.entity.LearningRecord;
import com.aicourse.entity.KnowledgePoint;
import com.aicourse.entity.User;
import com.aicourse.repository.LearningRecordRepository;
import com.aicourse.repository.KnowledgePointRepository;
import com.aicourse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LearningRecordService {

    @Autowired
    private LearningRecordRepository learningRecordRepository;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建学习记录
     */
    public Map<String, Object> createLearningRecord(Map<String, Object> recordData, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LearningRecord record = new LearningRecord();
            
            // 设置知识点
            Long knowledgePointId = getLongValue(recordData, "knowledgePointId");
            if (knowledgePointId != null) {
                Optional<KnowledgePoint> knowledgePointOpt = knowledgePointRepository.findById(knowledgePointId);
                if (knowledgePointOpt.isPresent()) {
                    record.setKnowledgePoint(knowledgePointOpt.get());
                } else {
                    result.put("success", false);
                    result.put("message", "知识点不存在");
                    return result;
                }
            }

            // 设置学生
            Long studentId = getLongValue(recordData, "studentId");
            if (studentId != null) {
                Optional<User> studentOpt = userRepository.findById(studentId);
                if (studentOpt.isPresent()) {
                    record.setStudent(studentOpt.get());
                } else {
                    record.setStudent(currentUser);
                }
            } else {
                record.setStudent(currentUser);
            }

            // 设置学习行为类型
            String actionTypeStr = (String) recordData.get("actionType");
            if (actionTypeStr != null) {
                try {
                    LearningRecord.ActionType actionType = LearningRecord.ActionType.valueOf(actionTypeStr);
                    record.setActionType(actionType);
                } catch (IllegalArgumentException e) {
                    record.setActionType(LearningRecord.ActionType.VIEW);
                }
            } else {
                record.setActionType(LearningRecord.ActionType.VIEW);
            }

            // 设置学习时长
            Integer duration = getIntegerValue(recordData, "duration");
            record.setDuration(duration != null ? duration : 0);

            // 设置学习时间
            record.setLearningTime(LocalDateTime.now());

            // 设置完成状态
            Boolean completed = (Boolean) recordData.get("completed");
            record.setCompleted(completed != null ? completed : false);

            // 设置得分
            Integer score = getIntegerValue(recordData, "score");
            if (score != null) {
                record.setScore(score);
            }

            // 保存记录
            LearningRecord savedRecord = learningRecordRepository.save(record);

            result.put("success", true);
            result.put("message", "学习记录创建成功");
            result.put("record", convertToMap(savedRecord));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建学习记录失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取学生学习记录
     */
    public Map<String, Object> getStudentLearningRecords(Long studentId, Long knowledgePointId, 
                                                         String actionType, int skip, int limit, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<LearningRecord> records = new ArrayList<>();
            
            if (knowledgePointId != null && actionType != null) {
                // 按知识点和行为类型查询
                try {
                    LearningRecord.ActionType actionTypeEnum = LearningRecord.ActionType.valueOf(actionType);
                    records = learningRecordRepository.findByStudentIdAndKnowledgePointIdAndActionType(
                        studentId, knowledgePointId, actionTypeEnum);
                } catch (IllegalArgumentException e) {
                    records = learningRecordRepository.findByStudentIdAndKnowledgePointId(studentId, knowledgePointId);
                }
            } else if (knowledgePointId != null) {
                // 按知识点查询
                records = learningRecordRepository.findByStudentIdAndKnowledgePointId(studentId, knowledgePointId);
            } else if (actionType != null) {
                // 按行为类型查询
                try {
                    LearningRecord.ActionType actionTypeEnum = LearningRecord.ActionType.valueOf(actionType);
                    records = learningRecordRepository.findByStudentIdAndActionType(studentId, actionTypeEnum);
                } catch (IllegalArgumentException e) {
                    records = learningRecordRepository.findByStudentId(studentId);
                }
            } else {
                // 查询所有记录
                records = learningRecordRepository.findByStudentId(studentId);
            }

            // 应用分页
            int start = Math.min(skip, records.size());
            int end = Math.min(skip + limit, records.size());
            List<LearningRecord> pagedRecords = records.subList(start, end);

            List<Map<String, Object>> recordMaps = pagedRecords.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

            result.put("success", true);
            result.put("records", recordMaps);
            result.put("total", records.size());
            result.put("skip", skip);
            result.put("limit", limit);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取学习记录失败：" + e.getMessage());
            result.put("records", new ArrayList<>());
            result.put("total", 0);
        }

        return result;
    }

    /**
     * 获取知识点学习统计
     */
    public Map<String, Object> getKnowledgePointLearningStatistics(Long knowledgePointId, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<LearningRecord> records = learningRecordRepository.findByKnowledgePointId(knowledgePointId);
            
            // 统计不同行为类型的数量
            Map<String, Long> actionTypeCounts = records.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getActionType().name(),
                    Collectors.counting()
                ));

            // 统计总学习时长
            int totalDuration = records.stream()
                .mapToInt(LearningRecord::getDuration)
                .sum();

            // 统计学习学生数
            long studentCount = records.stream()
                .map(r -> r.getStudent().getId())
                .distinct()
                .count();

            // 统计完成率
            long completedCount = records.stream()
                .filter(LearningRecord::isCompleted)
                .count();
            double completionRate = records.size() > 0 ? (double) completedCount / records.size() * 100 : 0;

            // 统计平均得分
            OptionalDouble averageScore = records.stream()
                .filter(r -> r.getScore() != null)
                .mapToInt(LearningRecord::getScore)
                .average();

            result.put("success", true);
            result.put("statistics", Map.of(
                "totalRecords", records.size(),
                "actionTypeCounts", actionTypeCounts,
                "totalDuration", totalDuration,
                "studentCount", studentCount,
                "completionRate", Math.round(completionRate * 100.0) / 100.0,
                "averageScore", averageScore.isPresent() ? Math.round(averageScore.getAsDouble() * 100.0) / 100.0 : null
            ));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取学习统计失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 将学习记录转换为Map
     */
    private Map<String, Object> convertToMap(LearningRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("studentId", record.getStudent().getId());
        map.put("studentName", record.getStudent().getUsername());
        map.put("knowledgePointId", record.getKnowledgePoint().getId());
        map.put("knowledgePointName", record.getKnowledgePoint().getName());
        map.put("actionType", record.getActionType().name());
        map.put("duration", record.getDuration());
        map.put("learningTime", record.getLearningTime().toString());
        map.put("completed", record.isCompleted());
        map.put("score", record.getScore());
        return map;
    }

    /**
     * 安全获取Long值
     */
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 安全获取Integer值
     */
    private Integer getIntegerValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
} 