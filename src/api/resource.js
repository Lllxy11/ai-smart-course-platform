import request from './request'

// 上传课件
export function uploadResource({ file, courseId, description, visibleToAll }) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('courseId', courseId)
  if (description) formData.append('description', description)
  if (visibleToAll !== undefined) formData.append('visibleToAll', visibleToAll)
  return request.post('/resources/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取课件列表
export function getResourceList(courseId) {
  return request.get('/resources', { params: { courseId } })
}

// 下载课件
export function downloadResource(id) {
  return request.get(`/resources/download/${id}`, { responseType: 'blob' })
} 