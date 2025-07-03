<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <h1 class="login-title">AI智慧课程平台</h1>
          <p class="login-subtitle">基于人工智能的在线教育平台</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>



          <el-form-item>
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="register-link">
              还没有账号？
              <el-link type="primary" @click="goToRegister">立即注册</el-link>
            </div>
          </el-form-item>
        </el-form>

        <!-- 演示账号 -->
        <div class="demo-accounts">
          <el-divider>演示账号</el-divider>
          <div class="demo-account-info">
            <el-text size="small" type="info">
              点击下方按钮可快速填入演示账号信息
            </el-text>
          </div>
          <div class="demo-account-list">
            <div 
              v-for="account in demoAccounts"
              :key="account.username"
              class="demo-account-item"
            >
              <el-button
                :type="account.type"
                size="small"
                @click="fillDemoAccount(account)"
              >
                {{ account.label }}
              </el-button>
              <el-tag 
                v-if="account.badge" 
                size="small" 
                type="warning" 
                class="account-badge"
              >
                {{ account.badge }}
              </el-tag>
            </div>
          </div>
          <div class="demo-account-details">
            <el-collapse>
              <el-collapse-item title="查看演示账号详情" name="demo-details">
                <div class="account-detail" v-for="account in demoAccounts" :key="account.username">
                  <el-tag :type="account.type" size="small">{{ account.label }}</el-tag>
                  <span class="account-info">
                    用户名: <code>{{ account.username }}</code> | 
                    密码: <code>{{ account.password }}</code>
                  </span>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 演示账号
const demoAccounts = [
  { 
    username: '123', 
    password: '123456', 
    label: '教师演示', 
    type: 'warning', 
    badge: '推荐',
    description: '可创建课程、管理学生'
  },
  { 
    username: '124', 
    password: '123456', 
    label: '学生演示', 
    type: 'success', 
    badge: false,
    description: '可参与课程学习、完成任务'
  }
]

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const result = await authStore.login({
      username: loginForm.username,
      password: loginForm.password
    })

    if (result.success) {
      ElMessage.success('登录成功')
      
      // 跳转到对应的仪表板
      const defaultRoute = authStore.getDefaultRoute()
      router.push(defaultRoute)
    }
  } catch (error) {
    console.error('Login error:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 填充演示账号
const fillDemoAccount = (account) => {
  loginForm.username = account.username
  loginForm.password = account.password
  ElMessage.info(`已填入${account.label}账号信息，点击登录即可体验`)
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .login-title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .login-subtitle {
    color: #909399;
    font-size: 14px;
    margin: 0;
  }
}

.login-form {
  .login-button {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
  }



  .register-link {
    text-align: center;
    width: 100%;
    color: #909399;
    font-size: 14px;
  }
}

.demo-accounts {
  margin-top: 24px;

  .demo-account-info {
    text-align: center;
    margin-bottom: 12px;
  }

  .demo-account-list {
    display: flex;
    gap: 8px;
    justify-content: center;
    flex-wrap: wrap;
    margin-bottom: 16px;

    .demo-account-item {
      position: relative;
      display: inline-flex;
      align-items: center;

      .account-badge {
        position: absolute;
        top: -8px;
        right: -8px;
        transform: scale(0.8);
        z-index: 1;
      }
    }
  }

  .demo-account-details {
    margin-top: 16px;

    .account-detail {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      padding: 8px;
      background-color: #f8f9fa;
      border-radius: 6px;

      .account-info {
        font-size: 12px;
        color: #666;

        code {
          background-color: #e9ecef;
          color: #495057;
          padding: 2px 4px;
          border-radius: 3px;
          font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          font-size: 11px;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 480px) {
  .login-card {
    padding: 24px;
  }

  .login-header .login-title {
    font-size: 24px;
  }
}
</style> 