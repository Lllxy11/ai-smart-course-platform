<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-image">
        <el-icon :size="120" color="#f56c6c">
          <CircleCloseFilled />
        </el-icon>
      </div>
      
      <div class="error-info">
        <h1 class="error-code">403</h1>
        <h2 class="error-title">权限不足</h2>
        <p class="error-description">
          抱歉，您没有权限访问此页面。
          <br>
          请联系管理员获取相应权限，或返回首页。
        </p>
        
        <div class="error-actions">
          <el-button type="primary" @click="goHome">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          <el-button @click="goBack">
            <el-icon><Back /></el-icon>
            返回上页
          </el-button>
          <el-button type="danger" plain @click="logout">
            <el-icon><SwitchButton /></el-icon>
            重新登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { CircleCloseFilled, HomeFilled, Back, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// 返回首页
const goHome = () => {
  if (authStore.isLoggedIn) {
    const defaultRoute = authStore.getDefaultRoute()
    router.push(defaultRoute)
  } else {
    router.push('/login')
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 重新登录
const logout = async () => {
  try {
    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    console.error('Logout error:', error)
    ElMessage.error('退出登录失败')
  }
}
</script>

<style lang="scss" scoped>
.error-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.error-content {
  text-align: center;
  max-width: 500px;
}

.error-image {
  margin-bottom: 32px;
}

.error-info {
  .error-code {
    font-size: 72px;
    font-weight: 700;
    color: #f56c6c;
    margin: 0 0 16px 0;
    line-height: 1;
  }

  .error-title {
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px 0;
  }

  .error-description {
    font-size: 16px;
    color: #606266;
    line-height: 1.6;
    margin: 0 0 32px 0;
  }
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;

  .el-button {
    padding: 12px 24px;
    font-size: 14px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .error-info {
    .error-code {
      font-size: 56px;
    }

    .error-title {
      font-size: 24px;
    }

    .error-description {
      font-size: 14px;
    }
  }

  .error-actions {
    flex-direction: column;
    align-items: center;

    .el-button {
      width: 200px;
    }
  }
}
</style> 