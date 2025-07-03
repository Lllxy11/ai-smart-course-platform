import request from './request'

// 获取用户列表
export const getUsers = (params) => {
  return request.get('/users', { params })
}

// 获取用户列表（别名）
export const getUserList = (params) => {
  return request.get('/users', { params })
}

// 获取用户详情
export const getUserById = (id) => {
  return request.get(`/users/${id}`)
}

// 创建用户
export const createUser = (data) => {
  return request.post('/users', data)
}

// 更新用户信息
export const updateUser = (id, data) => {
  return request.put(`/users/${id}`, data)
}

// 删除用户
export const deleteUser = (id) => {
  return request.delete(`/users/${id}`)
}

// 批量删除用户
export const batchDeleteUsers = (userIds) => {
  return request.delete('/users/batch', {
    data: { userIds }
  })
}

// 重置用户密码
export const resetUserPassword = (id, newPassword = '123456') => {
  return request.post(`/users/${id}/reset-password`, {
    newPassword
  })
}

// 启用/禁用用户 (已弃用，请使用 activateUser/deactivateUser)
// export const toggleUserStatus = (id, enabled) => {
//   return request.put(`/users/${id}/status`, {
//     enabled
//   })
// }

// 获取用户角色列表 (暂未实现)
// export const getUserRoles = () => {
//   return request.get('/users/roles')
// }

// 更新用户角色 (暂未实现)
// export const updateUserRole = (id, role) => {
//   return request.put(`/users/${id}/role`, {
//     role
//   })
// }

// 获取用户权限 (暂未实现)
// export const getUserPermissions = (id) => {
//   return request.get(`/users/${id}/permissions`)
// }

// 更新用户权限 (暂未实现)
// export const updateUserPermissions = (id, permissions) => {
//   return request.put(`/users/${id}/permissions`, {
//     permissions
//   })
// }

// 获取用户学习记录 (暂未实现)
// export const getUserLearningRecords = (id, params) => {
//   return request.get(`/users/${id}/learning-records`, { params })
// }

// 获取用户课程进度 (暂未实现)
// export const getUserCourseProgress = (id, params) => {
//   return request.get(`/users/${id}/course-progress`, { params })
// }

// 搜索用户 (暂未实现)
// export const searchUsers = (keyword, params) => {
//   return request.get('/users/search', {
//     params: { keyword, ...params }
//   })
// }

// 导出用户数据 (暂未实现)
// export const exportUsers = (params) => {
//   return request.get('/users/export', {
//     params,
//     responseType: 'blob'
//   })
// }

// 批量导入用户 (暂未实现)
// export const importUsers = (file) => {
//   const formData = new FormData()
//   formData.append('file', file)
//   return request.post('/users/import', formData, {
//     headers: {
//       'Content-Type': 'multipart/form-data'
//     }
//   })
// }

// 获取用户统计信息
export const getUserStats = () => {
  return request.get('/users/statistics')
}

// 获取用户统计信息（别名）
export const getUserStatistics = () => {
  return request.get('/users/statistics')
}

// 激活用户
export const activateUser = (id) => {
  return request.post(`/users/${id}/activate`)
}

// 禁用用户
export const deactivateUser = (id) => {
  return request.post(`/users/${id}/deactivate`)
}

// 批量激活用户
export const batchActivateUsers = (userIds) => {
  return request.post('/users/batch/activate', { userIds })
}

// 批量禁用用户
export const batchDeactivateUsers = (userIds) => {
  return request.post('/users/batch/deactivate', { userIds })
}

// 发送用户通知 (暂未实现)
// export const sendUserNotification = (id, data) => {
//   return request.post(`/users/${id}/notifications`, data)
// }

// 批量发送通知 (暂未实现)
// export const batchSendNotification = (userIds, data) => {
//   return request.post('/users/batch-notification', {
//     userIds,
//     ...data
//   })
// } 