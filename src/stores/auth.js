import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const userId = computed(() => user.value?.id || null)
  const userRole = computed(() => user.value?.role || '')
  const userName = computed(() => user.value?.fullName || user.value?.username || '')
  const userAvatar = computed(() => user.value?.avatarUrl || '')

  // 获取默认路由
  const getDefaultRoute = () => {
    const role = userRole.value
    switch (role) {
      case 'ADMIN':
        return '/admin/dashboard'
      case 'TEACHER':
        return '/teacher/dashboard'
      case 'STUDENT':
        return '/student/dashboard'
      default:
        return '/login'
    }
  }

  // 登录
  const login = async (credentials) => {
    try {
      isLoading.value = true
      const response = await authApi.login(credentials)
      
      if (response.data.token) {
        token.value = response.data.token
        user.value = response.data.user
        
        // 持久化存储
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        
        return { success: true, user: user.value }
      } else {
        throw new Error('登录失败')
      }
    } catch (error) {
      console.error('Login error:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 注册
  const register = async (userData) => {
    try {
      isLoading.value = true
      const response = await authApi.register(userData)
      return { success: true, data: response.data }
    } catch (error) {
      console.error('Register error:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(user.value))
      return user.value
    } catch (error) {
      console.error('Get current user error:', error)
      logout()
      throw error
    }
  }

  // 更新用户信息
  const updateUserInfo = async (updates) => {
    try {
      const response = await authApi.updateUserInfo(updates)
      user.value = { ...user.value, ...response.data }
      localStorage.setItem('user', JSON.stringify(user.value))
      return user.value
    } catch (error) {
      console.error('Update user info error:', error)
      throw error
    }
  }

  // 修改密码
  const changePassword = async (passwordData) => {
    try {
      await authApi.changePassword(passwordData)
      return { success: true }
    } catch (error) {
      console.error('Change password error:', error)
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 清除本地状态
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  // 检查认证状态
  const checkAuth = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      token.value = storedToken
      try {
        user.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('Parse stored user error:', error)
        logout()
      }
    }
  }

  // 初始化时检查认证状态
  checkAuth()

  return {
    // 状态
    token,
    user,
    isLoading,
    
    // 计算属性
    isLoggedIn,
    userId,
    userRole,
    userName,
    userAvatar,
    
    // 方法
    login,
    register,
    logout,
    getCurrentUser,
    updateUserInfo,
    changePassword,
    getDefaultRoute,
    checkAuth
  }
}) 