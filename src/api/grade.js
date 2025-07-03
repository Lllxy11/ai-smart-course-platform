import request from './request'

// 获取成绩列表
export const getGrades = (params) => {
  return request.get('/grades', { params })
}

// 获取成绩列表（别名，兼容性）
export const getGradeList = (params) => {
  return request.get('/grades', { params })
}

// 导出成绩
export const exportGrades = (params) => {
  return request.post('/grades/export', params, {
    responseType: 'blob'
  })
}

// 更新成绩
export const updateGrade = (submissionId, data) => {
  return request.put(`/grades/${submissionId}/grade`, data)
}

// 评分提交（别名）
export const gradeSubmission = (submissionId, data) => {
  return request.put(`/grades/${submissionId}/grade`, data)
}



// 获取成绩统计
export const getGradeStatistics = (params) => {
  return request.get('/grades/statistics', { params })
} 