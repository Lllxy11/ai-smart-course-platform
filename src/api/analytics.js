import request from './request'

// 获取仪表板统计
export const getDashboardStats = (params) => {
  return request.get('/analytics/dashboard', { params })
}

// 获取仪表板数据（别名）
export const getDashboardData = (params) => {
  return request.get('/analytics/dashboard', { params })
}

// 获取教师分析数据
export const getTeacherAnalytics = (teacherId) => {
  return request.get(`/analytics/teacher/${teacherId}`)
}

// 获取用户仪表板统计
export const getUserDashboardStats = (userId, params) => {
  return request.get(`/analytics/user-dashboard/${userId}`, { params })
}

// 获取最近活动
export const getRecentActivities = (userId, params) => {
  return request.get(`/analytics/recent-activities/${userId}`, { params })
}

// 获取学习趋势
export const getLearningTrends = (userId, params) => {
  return request.get(`/analytics/learning-trends/${userId}`, { params })
}

// 获取知识掌握情况
export const getKnowledgeMastery = (userId, params) => {
  return request.get(`/analytics/knowledge-mastery/${userId}`, { params })
}

// 获取薄弱环节
export const getWeakPoints = (userId, params) => {
  return request.get(`/analytics/weak-points/${userId}`, { params })
}

// 获取学习建议
export const getLearningRecommendations = (userId, params) => {
  return request.get(`/analytics/learning-recommendations/${userId}`, { params })
}

// 获取进度统计
export const getProgressStats = (userId, params) => {
  return request.get(`/analytics/progress-stats/${userId}`, { params })
}

// 获取行为统计
export const getBehaviorStats = (userId, params) => {
  return request.get(`/analytics/behavior-stats/${userId}`, { params })
}

// 获取学习效率
export const getLearningEfficiency = (userId, params) => {
  return request.get(`/analytics/learning-efficiency/${userId}`, { params })
}

// 获取成绩分布
export const getGradeDistribution = (userId, params) => {
  return request.get(`/analytics/grade-distribution/${userId}`, { params })
}

// 获取科目表现
export const getSubjectPerformance = (userId, params) => {
  return request.get(`/analytics/subject-performance/${userId}`, { params })
}

// 获取成绩趋势
export const getGradeTrend = (userId, params) => {
  return request.get(`/analytics/grade-trend/${userId}`, { params })
}

// 获取时间分布
export const getTimeDistribution = (userId, params) => {
  return request.get(`/analytics/time-distribution/${userId}`, { params })
}

// 获取活动热图
export const getActivityHeatmap = (userId, params) => {
  return request.get(`/analytics/activity-heatmap/${userId}`, { params })
}

// 获取设备使用情况
export const getDeviceUsage = (userId, params) => {
  return request.get(`/analytics/device-usage/${userId}`, { params })
}

// 获取预测数据
export const getPredictionData = (userId, params) => {
  return request.get(`/analytics/prediction/${userId}`, { params })
}

// 获取课程分析（教师、管理员）
export const getCourseAnalytics = (courseId, params) => {
  return request.get(`/analytics/course-analytics/${courseId}`, { params })
}

// 获取班级表现（教师、管理员）
export const getClassPerformance = (classId, params) => {
  return request.get(`/analytics/class-performance/${classId}`, { params })
}

// 获取用户统计数据
export const getUserStatistics = (params) => {
  return request.get('/analytics/users', { params })
}

// 获取课程统计数据
export const getCourseStatistics = (params) => {
  return request.get('/analytics/courses', { params })
}

// 获取学习进度统计
export const getLearningProgress = (params) => {
  return request.get('/analytics/learning-progress', { params })
}

// 获取任务完成统计
export const getTaskCompletion = (params) => {
  return request.get('/analytics/task-completion', { params })
}

// 获取考试成绩统计
export const getExamScores = (params) => {
  return request.get('/analytics/exam-scores', { params })
}

// 获取平台使用统计
export const getPlatformUsage = (params) => {
  return request.get('/analytics/platform-usage', { params })
}

// 获取知识点掌握情况
export const getKnowledgePointMastery = (params) => {
  return request.get('/analytics/knowledge-mastery', { params })
}

// 获取学习路径分析
export const getLearningPathAnalysis = (params) => {
  return request.get('/analytics/learning-path', { params })
}

// 获取教师教学效果分析
export const getTeachingEffectiveness = (teacherId, params) => {
  return request.get(`/analytics/teaching-effectiveness/${teacherId}`, { params })
}

// 获取学生学习行为分析
export const getStudentBehavior = (studentId, params) => {
  return request.get(`/analytics/student-behavior/${studentId}`, { params })
}

// 获取课程热度排行
export const getCoursePopularity = (params) => {
  return request.get('/analytics/course-popularity', { params })
}

// 获取系统性能指标
export const getSystemMetrics = (params) => {
  return request.get('/analytics/system-metrics', { params })
}

// 生成学习报告
export const generateLearningReport = (studentId, params) => {
  return request.post(`/analytics/learning-report/${studentId}`, params)
}

// 生成教学报告
export const generateTeachingReport = (teacherId, params) => {
  return request.post(`/analytics/teaching-report/${teacherId}`, params)
}

// 导出数据分析报告
export const exportAnalyticsReport = (type, params) => {
  return request.get(`/analytics/export/${type}`, {
    params,
    responseType: 'blob'
  })
} 