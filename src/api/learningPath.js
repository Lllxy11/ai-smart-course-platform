import request from './request'

// 获取学习路径
export const getLearningPaths = (params) => {
  return request.get('/learning-paths', { params })
}

// 获取特定知识点的学习路径
export const getKnowledgePointLearningPath = (knowledgePointId, params) => {
  return request.get(`/learning-paths/knowledge-point/${knowledgePointId}`, { params })
}

// 获取推荐学习路径
export const getRecommendedPaths = (params) => {
  return request.get('/learning-paths/recommended', { params })
}

// 开始学习路径
export const startLearningPath = (pathId) => {
  return request.post(`/learning-paths/${pathId}/start`)
}

// 完成学习路径节点
export const completeLearningPathNode = (pathId, nodeId) => {
  return request.post(`/learning-paths/${pathId}/nodes/${nodeId}/complete`)
}

// 生成学习路径
export const generateLearningPath = (data) => {
  return request.post('/learning-path/generate', data)
}

// 获取学习路径详情
export const getLearningPathDetail = (pathId) => {
  return request.get(`/learning-path/path/${pathId}`)
} 