import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import NProgress from 'nprogress'

// 创建axios实例
const request = axios.create({
  baseURL: '/api/v1',
  // timeout: 30000, // 删除全局超时，让AI请求有足够时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 开始进度条
    NProgress.start()
    
    // 添加认证token
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    return config
  },
  (error) => {
    NProgress.done()
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 结束进度条
    NProgress.done()
    
    // Spring Boot默认返回的成功响应没有错误code字段，直接返回数据
    // 只有在有明确错误状态码时才处理（数字类型的状态码）
    const { code, message } = response.data || {}
    
    // 只有当code是数字且不是成功状态码时才认为是错误
    if (typeof code === 'number' && code !== 200 && code !== 0) {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
    
    return response
  },
  (error) => {
    // 结束进度条
    NProgress.done()
    
    const { response } = error
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 401:
          // 未授权，清除token并跳转登录
          ElMessage.error('登录已过期，请重新登录')
          const authStore = useAuthStore()
          authStore.logout()
          router.push('/login')
          break
          
        case 403:
          ElMessage.error('权限不足')
          router.push('/403')
          break
          
        case 404:
          ElMessage.error('请求的资源不存在')
          break
          
        case 500:
          ElMessage.error('服务器内部错误')
          break
          
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      ElMessage.error(error.message || '未知错误')
    }
    
    return Promise.reject(error)
  }
)

// 封装常用请求方法
export const http = {
  get: (url, params = {}) => {
    return request.get(url, { params })
  },
  
  post: (url, data = {}) => {
    return request.post(url, data)
  },
  
  put: (url, data = {}) => {
    return request.put(url, data)
  },
  
  patch: (url, data = {}) => {
    return request.patch(url, data)
  },
  
  delete: (url, params = {}) => {
    return request.delete(url, { params })
  },
  
  upload: (url, formData) => {
    return request.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export default request 