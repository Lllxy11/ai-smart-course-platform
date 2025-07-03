import request from './request'

// AI生成题目（教师、管理员）
export const generateQuestions = (data) => {
  return request.post('/ai/generate-questions', data)
}

// AI分析提交内容（教师、管理员）
export const analyzeSubmission = (data) => {
  return request.post('/ai/analyze-submission', data)
}

// AI推荐学习路径
export const recommendLearningPath = (data) => {
  return request.post('/ai/recommend-learning-path', data)
}

// AI生成课程内容（教师、管理员）
export const generateCourseContent = (data) => {
  return request.post('/ai/generate-course-content', data)
}

// AI生成内容摘要
export const generateSummary = (data) => {
  return request.post('/ai/generate-summary', data)
}

// AI检测抄袭（教师、管理员）
export const detectPlagiarism = (data) => {
  return request.post('/ai/detect-plagiarism', data)
}

// 与AI对话
export const chatWithAI = (data) => {
  return request.post('/ai/chat', data)
}

// 获取AI功能列表
export const getAIFeatures = () => {
  return request.get('/ai/features')
}

// 获取AI使用统计（仅管理员）
export const getAIUsageStats = (params) => {
  return request.get('/ai/usage-stats', { params })
} 