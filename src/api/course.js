import request from './request'

// 获取课程列表（分页、筛选）
export const getCourses = (params) => {
  return request.get('/courses', { params })
}

// 获取课程列表（别名，兼容性）
export const getCourseList = (params) => {
  return request.get('/courses', { params })
}

// 获取课程详情
export const getCourseById = (courseId) => {
  return request.get(`/courses/${courseId}`)
}

// 创建课程（教师、管理员）
export const createCourse = (data) => {
  return request.post('/courses', data)
}

// 更新课程信息
export const updateCourse = (courseId, data) => {
  return request.put(`/courses/${courseId}`, data)
}

// 删除课程
export const deleteCourse = (courseId) => {
  return request.delete(`/courses/${courseId}`)
}

// 学生选课
export const enrollCourse = (courseId) => {
  return request.post(`/courses/${courseId}/enroll`)
}

// 获取课程学生列表
export const getCourseStudents = (courseId, params) => {
  return request.get(`/courses/${courseId}/students`, { params })
}

// 获取课程学习进度
export const getCourseProgress = (courseId, params) => {
  return request.get(`/courses/${courseId}/progress`, { params })
}

// 获取课程统计
export const getCourseStatistics = (params) => {
  return request.get('/courses/statistics', { params })
}

// 获取课程分析数据
export const getCourseAnalytics = (courseId) => {
  return request.get(`/courses/${courseId}/analytics`)
}

// 获取课程资源
export const getCourseResources = (courseId, params) => {
  return request.get(`/courses/${courseId}/resources`, { params })
}

// 获取知识点相关资源
export const getKnowledgePointResources = (knowledgePointId) => {
  return request.get(`/knowledge-points/${knowledgePointId}/resources`)
}

// 记录资源访问
export const recordResourceAccess = (resourceId) => {
  return request.post(`/resources/${resourceId}/access`)
} 