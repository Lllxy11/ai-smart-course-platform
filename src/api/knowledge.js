import request from './request'

// 获取知识图谱数据 - 核心API
export const getKnowledgeGraph = (params) => {
  return request.get('/knowledge-points/graph', { params })
}

// 分页获取知识点列表
export const getKnowledgePoints = (params) => {
  return request.get('/knowledge-points', { params })
}

// 获取知识点树形结构
export const getKnowledgeTree = (courseId) => {
  return request.get('/knowledge-points/tree', { 
    params: { courseId } 
  })
}

// 获取知识点统计
export const getKnowledgeStatistics = (params) => {
  return request.get('/knowledge-points/statistics', { params })
}

// 创建知识点
export const createKnowledgePoint = (data) => {
  return request.post('/knowledge-points', data)
}

// 更新知识点
export const updateKnowledgePoint = (id, data) => {
  return request.put(`/knowledge-points/${id}`, data)
}

// 删除知识点
export const deleteKnowledgePoint = (id) => {
  return request.delete(`/knowledge-points/${id}`)
}

// 获取知识点详情
export const getKnowledgePointById = (id) => {
  return request.get(`/knowledge-points/${id}`)
}

// 获取知识点关系
export const getKnowledgePointRelations = (pointId) => {
  return request.get(`/knowledge-points/${pointId}/relations`)
}

// 创建知识点关系
export const createKnowledgePointRelation = (data) => {
  return request.post('/knowledge-points/relations', data)
}

// 删除知识点关系
export const deleteKnowledgePointRelation = (id) => {
  return request.delete(`/knowledge-points/relations/${id}`)
} 