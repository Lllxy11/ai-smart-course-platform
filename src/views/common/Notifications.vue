<template>
  <div class="notifications">
    <div class="page-header">
      <h1>通知中心</h1>
      <div class="header-actions">
        <el-button @click="markAllAsRead" :disabled="unreadCount === 0">
          <el-icon><Check /></el-icon>
          全部标记已读
        </el-button>
        <el-button @click="loadNotifications">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 通知统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalCount }}</div>
            <div class="stat-label">总通知数</div>
          </div>
          <el-icon class="stat-icon"><Bell /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ unreadCount }}</div>
            <div class="stat-label">未读通知</div>
          </div>
          <el-icon class="stat-icon"><Message /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ todayCount }}</div>
            <div class="stat-label">今日新增</div>
          </div>
          <el-icon class="stat-icon"><Calendar /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选标签 -->
    <el-card class="filter-card">
      <div class="filter-tabs">
        <el-radio-group v-model="activeFilter" @change="handleFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
          <el-radio-button label="read">已读</el-radio-button>
        </el-radio-group>
        
        <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="handleCategoryChange">
          <el-option
            v-for="category in categories"
            :key="category.value"
            :label="category.label"
            :value="category.value"
          />
        </el-select>
      </div>
    </el-card>

    <!-- 通知列表 -->
    <el-card class="notifications-card">
      <div class="notifications-list" v-loading="loading">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ 'unread': !notification.isRead }"
          @click="markAsRead(notification)"
        >
          <div class="notification-avatar">
            <el-avatar :size="40" :src="notification.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          
          <div class="notification-content">
            <div class="notification-header">
              <span class="notification-title">{{ notification.title }}</span>
              <el-tag :type="getCategoryTagType(notification.category)" size="small">
                {{ getCategoryText(notification.category) }}
              </el-tag>
            </div>
            
            <div class="notification-message">
              {{ notification.message }}
            </div>
            
            <div class="notification-meta">
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
              <span class="notification-sender" v-if="notification.sender">
                来自: {{ notification.sender.realName || notification.sender.username }}
              </span>
            </div>
          </div>
          
          <div class="notification-actions">
            <el-button
              v-if="!notification.isRead"
              size="small"
              type="primary"
              @click.stop="markAsRead(notification)"
            >
              标记已读
            </el-button>
            <el-button
              size="small"
              @click.stop="deleteNotification(notification)"
            >
              删除
            </el-button>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="通知详情" width="600px">
      <div v-if="selectedNotification" class="notification-detail">
        <div class="detail-header">
          <h3>{{ selectedNotification.title }}</h3>
          <el-tag :type="getCategoryTagType(selectedNotification.category)">
            {{ getCategoryText(selectedNotification.category) }}
          </el-tag>
        </div>
        
        <div class="detail-content">
          <p>{{ selectedNotification.message }}</p>
          
          <div class="detail-meta">
            <div class="meta-item">
              <strong>发送时间:</strong> {{ formatTime(selectedNotification.createdAt) }}
            </div>
            <div class="meta-item" v-if="selectedNotification.sender">
              <strong>发送人:</strong> {{ selectedNotification.sender.realName || selectedNotification.sender.username }}
            </div>
            <div class="meta-item" v-if="selectedNotification.relatedUrl">
              <strong>相关链接:</strong>
              <el-link :href="selectedNotification.relatedUrl" target="_blank">
                查看详情
              </el-link>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check,
  Refresh,
  Bell,
  Message,
  Calendar,
  User
} from '@element-plus/icons-vue'
import { getNotificationList, markNotificationAsRead, deleteNotification as deleteNotificationApi, markAllNotificationsAsRead } from '@/api/notification'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const notifications = ref([])
const activeFilter = ref('all')
const selectedCategory = ref('')

// 对话框控制
const showDetailDialog = ref(false)
const selectedNotification = ref(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 通知分类
const categories = [
  { label: '系统通知', value: 'SYSTEM' },
  { label: '课程通知', value: 'COURSE' },
  { label: '作业通知', value: 'ASSIGNMENT' },
  { label: '考试通知', value: 'EXAM' },
  { label: '成绩通知', value: 'GRADE' },
  { label: '消息通知', value: 'MESSAGE' }
]

// 计算属性
const totalCount = computed(() => notifications.value.length)
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const todayCount = computed(() => {
  const today = new Date().toDateString()
  return notifications.value.filter(n => new Date(n.createdAt).toDateString() === today).length
})

// 生命周期
onMounted(() => {
  loadNotifications()
})

// 方法
const loadNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      isRead: activeFilter.value === 'all' ? undefined : activeFilter.value === 'read',
      category: selectedCategory.value || undefined,
      userId: authStore.user.id
    }
    
    const response = await getNotificationList(params)
    notifications.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载通知失败')
    console.error('Failed to load notifications:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.page = 1
  loadNotifications()
}

const handleCategoryChange = () => {
  pagination.page = 1
  loadNotifications()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadNotifications()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadNotifications()
}

const markAsRead = async (notification) => {
  if (notification.isRead) {
    selectedNotification.value = notification
    showDetailDialog.value = true
    return
  }

  try {
    await markNotificationAsRead(notification.id)
    notification.isRead = true
    ElMessage.success('已标记为已读')
    
    // 显示详情
    selectedNotification.value = notification
    showDetailDialog.value = true
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

const markAllAsRead = async () => {
  if (unreadCount.value === 0) {
    ElMessage.info('没有未读通知')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要将所有 ${unreadCount.value} 条未读通知标记为已读吗？`,
      '批量标记已读',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await markAllNotificationsAsRead()
    notifications.value.forEach(n => n.isRead = true)
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量标记已读失败:', error)
      ElMessage.error('批量标记已读失败')
    }
  }
}

const deleteNotification = async (notification) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条通知吗？',
      '删除通知',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteNotificationApi(notification.id)
    const index = notifications.value.findIndex(n => n.id === notification.id)
    if (index > -1) {
      notifications.value.splice(index, 1)
      pagination.total--
    }
    ElMessage.success('通知已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
    }
  }
}

// 工具方法
const getCategoryText = (category) => {
  const categoryMap = {
    SYSTEM: '系统通知',
    COURSE: '课程通知',
    ASSIGNMENT: '作业通知',
    EXAM: '考试通知',
    GRADE: '成绩通知',
    MESSAGE: '消息通知'
  }
  return categoryMap[category] || category
}

const getCategoryTagType = (category) => {
  const typeMap = {
    SYSTEM: 'info',
    COURSE: 'success',
    ASSIGNMENT: 'warning',
    EXAM: 'danger',
    GRADE: 'primary',
    MESSAGE: ''
  }
  return typeMap[category] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const notificationTime = new Date(time)
  const diffMs = now - notificationTime
  const diffMins = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  
  return notificationTime.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.notifications {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  color: #409eff;
  opacity: 0.3;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notifications-card {
  margin-bottom: 20px;
}

.notifications-list {
  min-height: 400px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f8f9fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 4px solid #409eff;
}

.notification-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-weight: bold;
  color: #303133;
  font-size: 16px;
}

.notification-message {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.notification-time {
  font-size: 12px;
}

.notification-sender {
  font-size: 12px;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 15px;
  flex-shrink: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.notification-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.detail-header h3 {
  margin: 0;
  color: #303133;
}

.detail-content {
  line-height: 1.8;
}

.detail-content p {
  margin-bottom: 20px;
  color: #606266;
}

.detail-meta {
  border-top: 1px solid #f0f2f5;
  padding-top: 15px;
}

.meta-item {
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.meta-item strong {
  color: #303133;
}
</style> 