import request from './request'

// 用户登录
export const login = (data) => {
  return request.post('/auth/login', data)
}

// 用户注册
export const register = (data) => {
  return request.post('/auth/register', data)
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return request.get('/auth/me')
}

// 更新用户信息
export const updateCurrentUser = (data) => {
  return request.put('/auth/me', data)
}

// 修改密码
export const changePassword = (data) => {
  return request.post('/auth/change-password', data)
}

// 用户登出
export const logout = () => {
  return request.post('/auth/logout')
}

// 退出指定设备
export const logoutDevice = (deviceId) => {
  return request.post('/auth/logout-device', { deviceId })
}

// 获取登录设备列表
export const getLoginDevices = () => {
  return request.get('/auth/login-devices')
}

// 健康检查
export const healthCheck = () => {
  return request.get('/auth/health')
}

// 默认导出所有认证相关的API
export default {
  login,
  register,
  getCurrentUser,
  updateCurrentUser,
  changePassword,
  logout,
  logoutDevice,
  getLoginDevices,
  healthCheck
} 