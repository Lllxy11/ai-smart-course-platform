import request from './request'

// 获取班级列表
export const getClasses = (params) => {
  return request.get('/classes', { params })
}

// 获取班级详情
export const getClassById = (classId) => {
  return request.get(`/classes/${classId}`)
}

// 创建班级
export const createClass = (data) => {
  return request.post('/classes', data)
}

// 更新班级
export const updateClass = (classId, data) => {
  return request.put(`/classes/${classId}`, data)
}

// 删除班级
export const deleteClass = (classId) => {
  return request.delete(`/classes/${classId}`)
}

// 添加学生到班级
export const addStudentToClass = (classId, studentId) => {
  return request.post(`/classes/${classId}/students/${studentId}`)
}

// 从班级中移除学生
export const removeStudentFromClass = (classId, studentId) => {
  return request.delete(`/classes/${classId}/students/${studentId}`)
} 