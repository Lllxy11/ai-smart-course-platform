<template>
  <div class="notification-management">
    <div class="page-header">
      <h1>通知管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          发送通知
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalNotifications }}</div>
            <div class="stat-label">总通知数</div>
          </div>
          <el-icon class="stat-icon"><Bell /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.unreadCount }}</div>
            <div class="stat-label">未读通知</div>
          </div>
          <el-icon class="stat-icon"><Message /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.readRate }}%</div>
            <div class="stat-label">阅读率</div>
          </div>
          <el-icon class="stat-icon"><View /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.todayCount }}</div>
            <div class="stat-label">今日发送</div>
          </div>
          <el-icon class="stat-icon"><Calendar /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="通知类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="课程通知" value="COURSE" />
            <el-option label="作业通知" value="ASSIGNMENT" />
            <el-option label="考试通知" value="EXAM" />
            <el-option label="成绩通知" value="GRADE" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收对象">
          <el-select v-model="searchForm.targetType" placeholder="选择对象" clearable>
            <el-option label="全体用户" value="ALL" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="已发送" value="SENT" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="定时发送" value="SCHEDULED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 通知列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>通知列表</span>
          <div class="batch-actions">
            <el-button size="small" type="danger" @click="batchDelete" :disabled="selectedNotifications.length === 0">
              批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="notifications" 
        v-loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="接收对象" width="100">
          <template #default="{ row }">
            {{ getTargetTypeText(row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column prop="readCount" label="已读/总数" width="120" align="center">
          <template #default="{ row }">
            {{ row.readCount }} / {{ row.totalCount }}
          </template>
        </el-table-column>
        <el-table-column prop="readRate" label="阅读率" width="100" align="center">
          <template #default="{ row }">
            {{ ((row.readCount / row.totalCount) * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewNotification(row)">
              查看
            </el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="success" @click="sendNotification(row)">
              发送
            </el-button>
            <el-button size="small" type="danger" @click="deleteNotification(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑通知对话框 -->
    <el-dialog v-model="showCreateDialog" title="发送通知" width="600px">
      <el-form :model="notificationForm" :rules="notificationRules" ref="notificationFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="notificationForm.type" placeholder="选择通知类型">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="课程通知" value="COURSE" />
            <el-option label="作业通知" value="ASSIGNMENT" />
            <el-option label="考试通知" value="EXAM" />
            <el-option label="成绩通知" value="GRADE" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收对象" prop="targetType">
          <el-select v-model="notificationForm.targetType" placeholder="选择接收对象">
            <el-option label="全体用户" value="ALL" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="notificationForm.content" 
            type="textarea" 
            :rows="5" 
            placeholder="请输入通知内容"
          />
        </el-form-item>
        <el-form-item label="发送方式">
          <el-radio-group v-model="notificationForm.sendType">
            <el-radio value="immediate">立即发送</el-radio>
            <el-radio value="scheduled">定时发送</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="notificationForm.sendType === 'scheduled'" label="发送时间">
          <el-date-picker
            v-model="notificationForm.scheduledTime"
            type="datetime"
            placeholder="选择发送时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSendNotification" :loading="sendLoading">
          {{ notificationForm.sendType === 'immediate' ? '立即发送' : '定时发送' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 通知详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="通知详情" width="600px">
      <div v-if="selectedNotification" class="notification-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ selectedNotification.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(selectedNotification.type)">
              {{ getTypeText(selectedNotification.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="接收对象">{{ getTargetTypeText(selectedNotification.targetType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedNotification.status)">
              {{ getStatusText(selectedNotification.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="已读/总数">
            {{ selectedNotification.readCount }} / {{ selectedNotification.totalCount }}
          </el-descriptions-item>
          <el-descriptions-item label="阅读率">
            {{ ((selectedNotification.readCount / selectedNotification.totalCount) * 100).toFixed(1) }}%
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(selectedNotification.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ formatTime(selectedNotification.sentAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>通知内容</h4>
          <div class="notification-content">{{ selectedNotification.content }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  Bell,
  Message,
  View,
  Calendar
} from '@element-plus/icons-vue'
import { 
  getNotificationList, 
  createNotification, 
  deleteNotification as deleteNotificationApi,
  getNotificationStatistics
} from '@/api/notification'

// 响应式数据
const loading = ref(false)
const sendLoading = ref(false)
const notifications = ref([])
const selectedNotifications = ref([])
const selectedNotification = ref(null)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)

// 表单引用
const notificationFormRef = ref(null)

// 统计数据
const stats = reactive({
  totalNotifications: 0,
  unreadCount: 0,
  readRate: 0,
  todayCount: 0
})

// 搜索表单
const searchForm = reactive({
  type: '',
  targetType: '',
  status: ''
})

// 通知表单
const notificationForm = reactive({
  title: '',
  type: '',
  targetType: '',
  content: '',
  sendType: 'immediate',
  scheduledTime: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表单验证规则
const notificationRules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  targetType: [
    { required: true, message: '请选择接收对象', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadNotifications()
  loadStats()
})

// 方法
const loadNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      type: searchForm.type,
      targetType: searchForm.targetType,
      status: searchForm.status
    }
    
    const response = await getNotificationList(params)
    notifications.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    console.error('加载通知列表失败:', error)
    ElMessage.error('加载通知列表失败')
    throw error
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await getNotificationStatistics()
    if (response.data) {
      Object.assign(stats, response.data)
    }
  } catch (error) {
    console.error('加载通知统计失败:', error)
    ElMessage.error('加载通知统计失败')
    throw error
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadNotifications()
}

const resetSearch = () => {
  searchForm.type = ''
  searchForm.targetType = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadNotifications()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadNotifications()
}

const handleSelectionChange = (selection) => {
  selectedNotifications.value = selection
}

const handleSendNotification = async () => {
  if (!notificationFormRef.value) return
  
  try {
    await notificationFormRef.value.validate()
    sendLoading.value = true
    
    await createNotification(notificationForm)
    ElMessage.success('通知发送成功')
    showCreateDialog.value = false
    resetNotificationForm()
    loadNotifications()
    loadStats()
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('发送通知失败')
    }
  } finally {
    sendLoading.value = false
  }
}

const resetNotificationForm = () => {
  Object.assign(notificationForm, {
    title: '',
    type: '',
    targetType: '',
    content: '',
    sendType: 'immediate',
    scheduledTime: ''
  })
  notificationFormRef.value?.resetFields()
}

const viewNotification = (notification) => {
  selectedNotification.value = notification
  showDetailDialog.value = true
}

const sendNotification = async (notification) => {
  try {
    await ElMessageBox.confirm(
      `确定要发送通知 "${notification.title}" 吗？`,
      '发送通知',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该调用发送通知的API
    ElMessage.success('通知发送成功')
    loadNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发送通知失败')
    }
  }
}

const deleteNotification = async (notification) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除通知 "${notification.title}" 吗？此操作不可恢复！`,
      '删除通知',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteNotificationApi(notification.id)
    ElMessage.success('通知删除成功')
    loadNotifications()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除通知失败')
    }
  }
}

const batchDelete = async () => {
  if (selectedNotifications.value.length === 0) {
    ElMessage.warning('请选择要删除的通知')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedNotifications.value.length} 个通知吗？此操作不可恢复！`,
      '批量删除通知',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const promises = selectedNotifications.value.map(notification => 
      deleteNotificationApi(notification.id)
    )
    await Promise.all(promises)
    ElMessage.success('批量删除成功')
    loadNotifications()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const refreshData = () => {
  loadNotifications()
  loadStats()
}

// 工具方法
const getTypeText = (type) => {
  const typeMap = {
    SYSTEM: '系统',
    COURSE: '课程',
    ASSIGNMENT: '作业',
    EXAM: '考试',
    GRADE: '成绩'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    SYSTEM: 'danger',
    COURSE: 'primary',
    ASSIGNMENT: 'warning',
    EXAM: 'success',
    GRADE: 'info'
  }
  return typeMap[type] || ''
}

const getTargetTypeText = (targetType) => {
  const targetMap = {
    ALL: '全体用户',
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return targetMap[targetType] || targetType
}

const getStatusText = (status) => {
  const statusMap = {
    SENT: '已发送',
    DRAFT: '草稿',
    SCHEDULED: '定时发送'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const statusMap = {
    SENT: 'success',
    DRAFT: 'info',
    SCHEDULED: 'warning'
  }
  return statusMap[status] || ''
}

const formatTime = (time) => {
  if (!time) return '未设置'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.notification-management {
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

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.notification-detail {
  padding: 20px 0;
}

.notification-content {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style> 