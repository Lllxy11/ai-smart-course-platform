import request from './request'

// 学生提交作业（支持文件上传）
export function submitAssignment({ taskId, content, file }) {
  const formData = new FormData()
  formData.append('taskId', taskId)
  if (content) formData.append('content', content)
  if (file) formData.append('file', file)
  return request.post('/submissions', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 查询自己某作业的所有提交
export function getMySubmissions(taskId) {
  return request.get('/submissions/mine', { params: { taskId } })
}

// 教师查询某作业所有学生的提交
export function getAllSubmissions(taskId) {
  return request.get('/submissions', { params: { taskId } })
}

// 教师批改作业
export function gradeSubmission(id, { score, feedback }) {
  return request.put(`/submissions/${id}/grade`, { score, feedback })
} 