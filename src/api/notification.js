import request from './request'

// 获取用户通知列表
export const getNotifications = (params) => {
  return request.get('/notifications', { params })
}

// 获取通知列表（别名）
export const getNotificationList = (params) => {
  return request.get('/notifications', { params })
}

// 获取通知摘要
export const getNotificationSummary = () => {
  return request.get('/notifications/summary')
}

// 发送通知（仅管理员）
export const sendNotification = (data) => {
  return request.post('/notifications/send', data)
}

// 创建通知（别名）
export const createNotification = (data) => {
  return request.post('/notifications', data)
}

// 删除通知
export const deleteNotification = (id) => {
  return request.delete(`/notifications/${id}`)
}

// 获取通知统计
export const getNotificationStatistics = () => {
  return request.get('/notifications/statistics')
}

// 标记所有通知为已读
export const markAllNotificationsAsRead = () => {
  return request.put('/notifications/mark-all-read')
}

// 标记通知为已读
export const markNotificationAsRead = (notificationId) => {
  return request.put(`/notifications/${notificationId}/read`)
}

// 获取通知分类列表
export const getNotificationCategories = () => {
  return request.get('/notifications/categories')
} 