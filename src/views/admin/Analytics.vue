<template>
  <div class="admin-analytics">
    <div class="page-header">
      <h1>数据分析</h1>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-button type="primary" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 总体概览 -->
    <el-card class="overview-card">
      <template #header>
        <span>系统概览</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-number">{{ overviewData.totalUsers }}</div>
              <div class="overview-label">总用户数</div>
              <div class="overview-trend" :class="overviewData.userGrowth >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="overviewData.userGrowth >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(overviewData.userGrowth) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-icon course-icon">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-number">{{ overviewData.totalCourses }}</div>
              <div class="overview-label">总课程数</div>
              <div class="overview-trend" :class="overviewData.courseGrowth >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="overviewData.courseGrowth >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(overviewData.courseGrowth) }}%
              </div>
            </div>
          </div>
        </el-col>
        <!-- 任务统计已删除 -->
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-icon activity-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-number">{{ overviewData.totalActivities }}</div>
              <div class="overview-label">总活动数</div>
              <div class="overview-trend" :class="overviewData.activityGrowth >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="overviewData.activityGrowth >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(overviewData.activityGrowth) }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 用户增长趋势 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div ref="userGrowthChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 课程分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>课程分布</span>
          </template>
          <div ref="courseDistributionChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 学习活动统计 -->
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>学习活动统计</span>
          </template>
          <div ref="learningActivityChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 用户角色分布 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>用户角色分布</span>
          </template>
          <div ref="userRoleChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-row :gutter="20" class="tables-row">
      <!-- 热门课程 -->
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <span>热门课程排行</span>
          </template>
          <el-table :data="popularCourses" size="small">
            <el-table-column prop="rank" label="排名" width="60" />
            <el-table-column prop="title" label="课程名称" show-overflow-tooltip />
            <el-table-column prop="enrollmentCount" label="选课人数" width="100" />
            <el-table-column prop="completionRate" label="完成率" width="100">
              <template #default="{ row }">
                {{ row.completionRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="80">
              <template #default="{ row }">
                <el-rate
                  :model-value="row.rating"
                  disabled
                  size="small"
                  show-score
                  text-color="#ff9900"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 活跃用户 -->
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <span>活跃用户排行</span>
          </template>
          <el-table :data="activeUsers" size="small">
            <el-table-column prop="rank" label="排名" width="60" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="role" label="角色" width="80">
              <template #default="{ row }">
                <el-tag :type="getRoleTagType(row.role)" size="small">
                  {{ getRoleText(row.role) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="activityScore" label="活跃度" width="80" />
            <el-table-column prop="lastActiveTime" label="最后活跃" width="120">
              <template #default="{ row }">
                {{ formatTime(row.lastActiveTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统性能指标 -->
    <el-card class="performance-card">
      <template #header>
        <span>系统性能指标</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="平均响应时间" :value="performanceData.avgResponseTime" suffix="ms" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="系统可用性" :value="performanceData.availability" suffix="%" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="并发用户数" :value="performanceData.concurrentUsers" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="存储使用率" :value="performanceData.storageUsage" suffix="%" />
        </el-col>
      </el-row>
    </el-card>

    <!-- AI使用统计 -->
    <el-card class="ai-stats-card">
      <template #header>
        <span>AI功能使用统计</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="ai-stat-item">
            <div class="ai-stat-title">AI对话次数</div>
            <div class="ai-stat-number">{{ aiStats.chatCount }}</div>
            <div class="ai-stat-trend">
              <span>较昨日 </span>
              <span :class="aiStats.chatGrowth >= 0 ? 'positive' : 'negative'">
                {{ aiStats.chatGrowth >= 0 ? '+' : '' }}{{ aiStats.chatGrowth }}%
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="ai-stat-item">
            <div class="ai-stat-title">题目生成次数</div>
            <div class="ai-stat-number">{{ aiStats.questionGenCount }}</div>
            <div class="ai-stat-trend">
              <span>较昨日 </span>
              <span :class="aiStats.questionGrowth >= 0 ? 'positive' : 'negative'">
                {{ aiStats.questionGrowth >= 0 ? '+' : '' }}{{ aiStats.questionGrowth }}%
              </span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="ai-stat-item">
            <div class="ai-stat-title">作业分析次数</div>
            <div class="ai-stat-number">{{ aiStats.analysisCount }}</div>
            <div class="ai-stat-trend">
              <span>较昨日 </span>
              <span :class="aiStats.analysisGrowth >= 0 ? 'positive' : 'negative'">
                {{ aiStats.analysisGrowth >= 0 ? '+' : '' }}{{ aiStats.analysisGrowth }}%
              </span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Download,
  User,
  Reading,
  Document,
  TrendCharts,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardData } from '@/api/analytics'

// 响应式数据
const dateRange = ref([])
const loading = ref(false)

// 图表引用
const userGrowthChart = ref()
const courseDistributionChart = ref()
const learningActivityChart = ref()
const userRoleChart = ref()

// 数据
const overviewData = ref({
  totalUsers: 0,
  totalCourses: 0,
  totalActivities: 0,
  userGrowth: 0,
  courseGrowth: 0,
  activityGrowth: 0
})

const popularCourses = ref([])
const activeUsers = ref([])

const performanceData = ref({
  avgResponseTime: 0,
  availability: 0,
  concurrentUsers: 0,
  storageUsage: 0
})

const aiStats = ref({
  chatCount: 0,
  questionGenCount: 0,
  analysisCount: 0,
  chatGrowth: 0,
  questionGrowth: 0,
  analysisGrowth: 0
})

// 图表实例
let userGrowthChartInstance = null
let courseDistributionChartInstance = null
let learningActivityChartInstance = null
let userRoleChartInstance = null

// 生命周期
onMounted(() => {
  // 设置默认日期范围（最近30天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 30)
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  loadAnalyticsData()
  nextTick(() => {
    initCharts()
  })
})

// 方法
const loadAnalyticsData = async () => {
  loading.value = true
  try {
    const params = {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }
    
    const response = await getDashboardData(params)
    const data = response.data
    
    // 更新概览数据
    overviewData.value = {
      totalUsers: data.totalUsers || 1256,
      totalCourses: data.totalCourses || 89,
      totalActivities: data.totalActivities || 1567,
      userGrowth: data.userGrowth || 12.5,
      courseGrowth: data.courseGrowth || 8.3,
      activityGrowth: data.activityGrowth || 22.1
    }
    
    // 更新热门课程
    popularCourses.value = data.popularCourses || [
      { rank: 1, title: 'Python基础编程', enrollmentCount: 256, completionRate: 85, rating: 4.8 },
      { rank: 2, title: '数据结构与算法', enrollmentCount: 198, completionRate: 78, rating: 4.6 },
      { rank: 3, title: 'Web前端开发', enrollmentCount: 189, completionRate: 82, rating: 4.7 },
      { rank: 4, title: '机器学习入门', enrollmentCount: 167, completionRate: 73, rating: 4.5 },
      { rank: 5, title: '数据库设计', enrollmentCount: 145, completionRate: 80, rating: 4.4 }
    ]
    
    // 更新活跃用户
    activeUsers.value = data.activeUsers || [
      { rank: 1, username: 'student001', realName: '张三', role: 'STUDENT', activityScore: 98, lastActiveTime: new Date() },
      { rank: 2, username: 'teacher001', realName: '李老师', role: 'TEACHER', activityScore: 95, lastActiveTime: new Date() },
      { rank: 3, username: 'student002', realName: '王五', role: 'STUDENT', activityScore: 92, lastActiveTime: new Date() },
      { rank: 4, username: 'student003', realName: '赵六', role: 'STUDENT', activityScore: 89, lastActiveTime: new Date() },
      { rank: 5, username: 'teacher002', realName: '陈老师', role: 'TEACHER', activityScore: 87, lastActiveTime: new Date() }
    ]
    
    // 更新性能数据
    performanceData.value = {
      avgResponseTime: data.avgResponseTime || 156,
      availability: data.availability || 99.8,
      concurrentUsers: data.concurrentUsers || 89,
      storageUsage: data.storageUsage || 67
    }
    
    // 更新AI统计
    aiStats.value = {
      chatCount: data.aiStats?.chatCount || 1234,
      questionGenCount: data.aiStats?.questionGenCount || 567,
      analysisCount: data.aiStats?.analysisCount || 890,
      chatGrowth: data.aiStats?.chatGrowth || 15.2,
      questionGrowth: data.aiStats?.questionGrowth || 8.7,
      analysisGrowth: data.aiStats?.analysisGrowth || 12.3
    }
    
    // 更新图表
    updateCharts(data)
    
  } catch (error) {
    console.error('加载分析数据失败:', error)
    ElMessage.error('加载分析数据失败')
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  // 初始化用户增长趋势图
  userGrowthChartInstance = echarts.init(userGrowthChart.value)
  
  // 初始化课程分布图
  courseDistributionChartInstance = echarts.init(courseDistributionChart.value)
  
  // 初始化学习活动统计图
  learningActivityChartInstance = echarts.init(learningActivityChart.value)
  
  // 初始化用户角色分布图
  userRoleChartInstance = echarts.init(userRoleChart.value)
  
  // 响应式处理
  window.addEventListener('resize', () => {
    userGrowthChartInstance?.resize()
    courseDistributionChartInstance?.resize()
    learningActivityChartInstance?.resize()
    userRoleChartInstance?.resize()
  })
}

const updateCharts = (data) => {
  // 用户增长趋势图
  const userGrowthOption = {
    title: {
      text: '最近30天用户增长',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.userGrowthDates || generateDateRange(30)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '新增用户',
      type: 'line',
      data: data.userGrowthData || generateRandomData(30, 10, 50),
      smooth: true,
      itemStyle: {
        color: '#409EFF'
      }
    }]
  }
  userGrowthChartInstance.setOption(userGrowthOption)
  
  // 课程分布图
  const courseDistributionOption = {
    title: {
      text: '课程分类分布',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item'
    },
    series: [{
      name: '课程数量',
      type: 'pie',
      radius: '60%',
      data: data.courseDistribution || [
        { value: 25, name: '编程语言' },
        { value: 18, name: '数据科学' },
        { value: 15, name: 'Web开发' },
        { value: 12, name: '移动开发' },
        { value: 10, name: '人工智能' },
        { value: 9, name: '其他' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  courseDistributionChartInstance.setOption(courseDistributionOption)
  
  // 学习活动统计图
  const learningActivityOption = {
    title: {
      text: '学习活动统计',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['任务提交', '视频观看', '讨论参与', '考试参加']
    },
    xAxis: {
      type: 'category',
      data: data.activityDates || generateDateRange(7)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '任务提交',
        type: 'bar',
        data: data.taskSubmissions || generateRandomData(7, 20, 80)
      },
      {
        name: '视频观看',
        type: 'bar',
        data: data.videoWatches || generateRandomData(7, 30, 120)
      },
      {
        name: '讨论参与',
        type: 'bar',
        data: data.discussions || generateRandomData(7, 10, 40)
      },
      {
        name: '考试参加',
        type: 'bar',
        data: data.examParticipations || generateRandomData(7, 5, 25)
      }
    ]
  }
  learningActivityChartInstance.setOption(learningActivityOption)
  
  // 用户角色分布图
  const userRoleOption = {
    title: {
      text: '用户角色分布',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item'
    },
    series: [{
      name: '用户数量',
      type: 'doughnut',
      radius: ['40%', '70%'],
      data: data.userRoleDistribution || [
        { value: 1089, name: '学生' },
        { value: 156, name: '教师' },
        { value: 11, name: '管理员' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      label: {
        show: true,
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  }
  userRoleChartInstance.setOption(userRoleOption)
}

const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    loadAnalyticsData()
  }
}

const exportReport = () => {
  ElMessage.info('报告导出功能开发中...')
}

// 工具方法
const generateDateRange = (days) => {
  const dates = []
  const today = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(today.getDate() - i)
    dates.push(date.toISOString().split('T')[0])
  }
  return dates
}

const generateRandomData = (length, min, max) => {
  return Array.from({ length }, () => Math.floor(Math.random() * (max - min + 1)) + min)
}

const getRoleText = (role) => {
  const roleMap = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return roleMap[role] || role
}

const getRoleTagType = (role) => {
  const typeMap = {
    STUDENT: '',
    TEACHER: 'success',
    ADMIN: 'danger'
  }
  return typeMap[role] || ''
}

const formatTime = (time) => {
  if (!time) return '未知'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-analytics {
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
  align-items: center;
}

.overview-card {
  margin-bottom: 20px;
}

.overview-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.overview-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin-right: 15px;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.course-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.task-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.activity-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.overview-content {
  flex: 1;
}

.overview-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.overview-label {
  color: #606266;
  font-size: 14px;
  margin-bottom: 5px;
}

.overview-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: bold;
}

.overview-trend.positive {
  color: #67c23a;
}

.overview-trend.negative {
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-container {
  height: 320px;
}

.tables-row {
  margin-bottom: 20px;
}

.table-card {
  height: 400px;
}

.performance-card {
  margin-bottom: 20px;
}

.ai-stats-card {
  margin-bottom: 20px;
}

.ai-stat-item {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.ai-stat-title {
  font-size: 14px;
  margin-bottom: 10px;
  opacity: 0.9;
}

.ai-stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}

.ai-stat-trend {
  font-size: 12px;
  opacity: 0.8;
}

.ai-stat-trend .positive {
  color: #67c23a;
}

.ai-stat-trend .negative {
  color: #f56c6c;
}
</style> 