import request from './request'

// 创建作业（教师）
export const createTask = (data) => {
  return request.post('/tasks', data)
}

// 获取作业列表
export const getTasks = (params) => {
  return request.get('/tasks', { params })
}

// 获取作业详情
export const getTaskById = (taskId) => {
  return request.get(`/tasks/${taskId}`)
} 