import request from './request'

// 记录视频学习事件
export const recordVideoEvent = (data) => {
  return request.post('/video-learning/record-event', data)
}

// 获取视频分析
export const getVideoAnalytics = (taskId, params) => {
  return request.get(`/video-learning/analytics/${taskId}`, { params })
}

// 获取视频热图
export const getVideoHeatmap = (taskId, params) => {
  return request.get(`/video-learning/heatmap/${taskId}`, { params })
}

// 获取学习质量报告
export const getQualityReport = (taskId, params) => {
  return request.get(`/video-learning/quality-report/${taskId}`, { params })
}

// 获取视频学习进度
export const getVideoProgress = (userId, params) => {
  return request.get(`/video-learning/progress/${userId}`, { params })
}

// 获取视频学习统计
export const getVideoStatistics = (params) => {
  return request.get('/video-learning/statistics', { params })
} 