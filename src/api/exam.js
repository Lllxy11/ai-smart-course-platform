import request from './request'

// 获取考试列表
export const getExams = (params) => {
  return request.get('/exams', { params })
}

// 获取考试列表（别名）
export const getExamList = (params) => {
  return request.get('/exams', { params })
}

// 获取考试详情
export const getExamById = (id) => {
  return request.get(`/exams/${id}`)
}

// 创建考试
export const createExam = (data) => {
  return request.post('/exams', data)
}

// 更新考试
export const updateExam = (id, data) => {
  return request.put(`/exams/${id}`, data)
}

// 删除考试
export const deleteExam = (id) => {
  return request.delete(`/exams/${id}`)
}

// 开始考试
export const startExam = (examId) => {
  return request.post(`/exams/${examId}/start`)
}

// 提交考试答案
export const submitExamAnswer = (examId, data) => {
  return request.post(`/exams/${examId}/submit`, data)
}

// 获取考试结果
export const getExamResult = (examId, sessionId) => {
  return request.get(`/exams/${examId}/result/${sessionId}`)
}

// 获取考试结果列表
export const getExamResults = (examId) => {
  return request.get(`/exams/${examId}/results`)
}

// 获取考试统计
export const getExamStatistics = (examId) => {
  return request.get(`/exams/${examId}/statistics`)
} 