<template>
  <div class="student-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
        <div class="logo">
          <el-icon v-if="isCollapse" :size="32" color="#409eff">
            <School />
          </el-icon>
          <template v-else>
            <el-icon :size="32" color="#409eff">
              <School />
            </el-icon>
            <span class="logo-text">AI课程平台</span>
          </template>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/student/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>仪表板</template>
          </el-menu-item>
          
          <el-menu-item index="/student/courses">
            <el-icon><Reading /></el-icon>
            <template #title>我的课程</template>
          </el-menu-item>
          

          
          <el-menu-item index="/student/knowledge-graph">
            <el-icon><Share /></el-icon>
            <template #title>知识图谱</template>
          </el-menu-item>
          
          <el-menu-item index="/student/ai-assistant">
            <el-icon><ChatLineRound /></el-icon>
            <template #title>AI学习助手</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主要内容区域 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-button
              type="text"
              :icon="isCollapse ? Expand : Fold"
              @click="toggleCollapse"
            />
            
            <!-- 面包屑导航 -->
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item
                v-for="item in breadcrumbs"
                :key="item.path"
                :to="item.path"
              >
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <!-- 用户菜单 -->
            <el-dropdown @command="handleUserCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userAvatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="username">{{ userName }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>


  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  School, Monitor, Reading, DocumentCopy, ChatLineRound, Share,
  Expand, Fold, User, ArrowDown, Setting, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式状态
const isCollapse = ref(false)

// 计算属性
const activeMenu = computed(() => route.path)
const userName = computed(() => authStore.userName)
const userAvatar = computed(() => authStore.userAvatar)

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    title: item.meta.title,
    path: item.path
  }))
})



// 方法
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}



const handleUserCommand = async (command) => {
  switch (command) {
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await authStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('退出登录失败')
        }
      }
      break
  }
}

// 监听路由变化，自动收起移动端侧边栏
watch(route, () => {
  if (window.innerWidth < 768) {
    isCollapse.value = true
  }
})
</script>

<style lang="scss" scoped>
.student-layout {
  height: 100vh;
  
  .el-container {
    height: 100%;
  }
}

.sidebar {
  background: #001529;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 0 16px;
    border-bottom: 1px solid #1f2937;
    
    .logo-text {
      color: white;
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
    }
  }
  
  .sidebar-menu {
    border: none;
    background: transparent;
    
    :deep(.el-menu-item) {
      color: rgba(255, 255, 255, 0.65);
      
      &:hover {
        background-color: #1890ff;
        color: white;
      }
      
      &.is-active {
        background-color: #1890ff;
        color: white;
      }
    }
  }
}

.header {
  background: white;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .breadcrumb {
      :deep(.el-breadcrumb__inner) {
        color: #606266;
        
        &.is-link {
          color: #409eff;
        }
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 8px;
      border-radius: 4px;
      transition: background-color 0.3s;
      
      &:hover {
        background-color: #f5f5f5;
      }
      
      .username {
        font-size: 14px;
        color: #606266;
      }
      
      .dropdown-icon {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.main-content {
  background: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}



// 页面切换动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

// 响应式设计
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
    
    .header-left .breadcrumb {
      display: none;
    }
  }
  
  .main-content {
    padding: 16px;
  }
}
</style> 