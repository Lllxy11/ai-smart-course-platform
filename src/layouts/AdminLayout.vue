<template>
  <div class="admin-layout">
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
          <el-menu-item index="/admin/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>仪表板</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/courses">
            <el-icon><Reading /></el-icon>
            <template #title>课程管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/analytics">
            <el-icon><TrendCharts /></el-icon>
            <template #title>数据分析</template>
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
            <!-- 系统状态 -->
            <el-tooltip content="系统状态" placement="bottom">
              <el-badge :is-dot="systemStatus.hasAlert" type="warning">
                <el-button type="text" :icon="Monitor" @click="showSystemStatus" />
              </el-badge>
            </el-tooltip>



            <!-- 用户菜单 -->
            <el-dropdown @command="handleUserCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userAvatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="username">{{ userName }}</span>
                <el-tag type="danger" size="small">管理员</el-tag>
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



    <!-- 系统状态抽屉 -->
    <el-drawer
      v-model="systemStatusDrawer"
      title="系统状态"
      :size="400"
      direction="rtl"
    >
      <div class="system-status">
        <div class="status-item">
          <div class="status-label">服务器状态</div>
          <el-tag :type="systemStatus.server === 'online' ? 'success' : 'danger'">
            {{ systemStatus.server === 'online' ? '在线' : '离线' }}
          </el-tag>
        </div>
        <div class="status-item">
          <div class="status-label">数据库连接</div>
          <el-tag :type="systemStatus.database === 'connected' ? 'success' : 'danger'">
            {{ systemStatus.database === 'connected' ? '已连接' : '断开' }}
          </el-tag>
        </div>
        <div class="status-item">
          <div class="status-label">在线用户</div>
          <span class="status-value">{{ systemStatus.onlineUsers }}</span>
        </div>
        <div class="status-item">
          <div class="status-label">CPU使用率</div>
          <el-progress :percentage="systemStatus.cpuUsage" :stroke-width="8" />
        </div>
        <div class="status-item">
          <div class="status-label">内存使用率</div>
          <el-progress :percentage="systemStatus.memoryUsage" :stroke-width="8" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  School, Monitor, User, Reading, TrendCharts,
  Expand, Fold, ArrowDown, Setting, SwitchButton,
  Warning, InfoFilled, SuccessFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式状态
const isCollapse = ref(false)
const systemStatusDrawer = ref(false)

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

// 系统状态
const systemStatus = ref({
  server: 'online',
  database: 'connected',
  onlineUsers: 128,
  cpuUsage: 45,
  memoryUsage: 68,
  hasAlert: false
})



// 方法
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}



const showSystemStatus = () => {
  systemStatusDrawer.value = true
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
.admin-layout {
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



.system-status {
  .status-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .status-label {
      font-size: 14px;
      color: #606266;
      font-weight: 500;
    }
    
    .status-value {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
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
    
    .header-right .user-info {
      .username {
        display: none;
      }
    }
  }
  
  .main-content {
    padding: 16px;
  }
}
</style> 