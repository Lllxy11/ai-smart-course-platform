import request from './request'

// 获取题目列表
export const getQuestions = (params) => {
  return request.get('/questions', { params })
}

// 获取题目列表（别名）
export const getQuestionList = (params) => {
  return request.get('/questions', { params })
}

// 创建题目（教师、管理员）
export const createQuestion = (data) => {
  return request.post('/questions', data)
}

// 获取题目详情
export const getQuestionById = (questionId) => {
  return request.get(`/questions/${questionId}`)
}

// 更新题目
export const updateQuestion = (questionId, data) => {
  return request.put(`/questions/${questionId}`, data)
}

// 删除题目
export const deleteQuestion = (questionId) => {
  return request.delete(`/questions/${questionId}`)
}

// 获取题目统计
export const getQuestionStatistics = (params) => {
  return request.get('/questions/statistics', { params })
} 