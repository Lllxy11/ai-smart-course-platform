<template>
  <div class="student-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">学习仪表板</h1>
      <p class="page-description">欢迎回来，{{ userName }}！查看您的学习进度和最新动态</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stats-cards">
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon courses">
              <el-icon :size="32"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.enrolledCourses }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon tasks">
              <el-icon :size="32"><DocumentCopy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.completedTasks }}</div>
              <div class="stat-label">已完成任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon progress">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.studyHours }}小时</div>
              <div class="stat-label">学习时长</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon score">
              <el-icon :size="32"><Trophy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalScore }}</div>
              <div class="stat-label">总分数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="dashboard-content">
      <!-- 左侧内容 -->
      <el-col :xs="24" :lg="16">
        <!-- 学习进度图表 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>学习进度趋势</h3>
              <el-select v-model="chartPeriod" size="small" style="width: 100px" @change="onChartPeriodChange">
                <el-option label="7天" value="week" />
                <el-option label="30天" value="month" />
                <el-option label="90天" value="quarter" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="learningChartOption" style="height: 300px" />
          </div>
        </el-card>

        <!-- 课程列表 -->
        <el-card class="course-list-card">
          <template #header>
            <div class="card-header">
              <h3>我的课程</h3>
              <el-button type="primary" size="small" @click="$router.push('/student/courses')">
                查看全部
              </el-button>
            </div>
          </template>
          <div class="course-list">
            <div
              v-for="course in courses"
              :key="course.id"
              class="course-item"
              @click="viewCourse(course.id)"
            >
              <div class="course-cover">
                <img :src="course.coverImage" :alt="course.name" />
              </div>
              <div class="course-info">
                <h4 class="course-title">{{ course.name || course.title }}</h4>
                <p class="course-teacher">{{ course.teacherName || course.instructor }}</p>
                <div class="course-progress">
                  <el-progress
                    :percentage="course.progress || 0"
                    :stroke-width="6"
                    :show-text="false"
                  />
                  <span class="progress-text">{{ course.progress || 0 }}%</span>
                </div>
              </div>
              <div class="course-actions">
                <el-button type="primary" size="small">继续学习</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :xs="24" :lg="8">
                <!-- 任务管理功能已删除 -->

        <!-- 最近成绩 -->
        <el-card class="grade-card">
          <template #header>
            <div class="card-header">
              <h3>最近成绩</h3>
              <el-button type="text" size="small" @click="$router.push('/student/grades')">查看详情</el-button>
            </div>
          </template>
          <div class="grade-list">
            <div
              v-for="grade in recentGrades"
              :key="grade.id"
              class="grade-item"
            >
              <div class="grade-info">
                <h4 class="grade-title">{{ grade.task_title || grade.taskName }}</h4>
                <p class="grade-course">{{ grade.course_name || grade.courseName }}</p>
              </div>
              <div class="grade-score">
                <span class="score" :class="getScoreClass(grade.score)">
                  {{ grade.score || 0 }}
                </span>
                <span class="total">/{{ grade.max_score || grade.totalScore || 100 }}</span>
              </div>
            </div>
            
            <el-empty v-if="recentGrades.length === 0" description="暂无成绩记录" :image-size="80" />
          </div>
        </el-card>

        <!-- 学习建议 -->
        <el-card class="suggestion-card">
          <template #header>
            <div class="card-header">
              <h3>AI学习建议</h3>
              <el-icon color="#409eff"><ChatLineRound /></el-icon>
            </div>
          </template>
          <div class="suggestion-content">
            <div class="suggestion-item">
              <el-icon color="#67c23a"><Check /></el-icon>
              <span>建议加强《数据结构》中链表部分的练习</span>
            </div>
            <div class="suggestion-item">
              <el-icon color="#e6a23c"><Warning /></el-icon>
              <span>《算法导论》作业即将到期，请及时完成</span>
            </div>
            <div class="suggestion-item">
              <el-icon color="#409eff"><InfoFilled /></el-icon>
              <span>推荐学习《计算机网络》相关课程</span>
            </div>
          </div>
          <div class="suggestion-footer">
            <el-button type="primary" size="small" @click="$router.push('/student/ai-assistant')">
              获取更多建议
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import {
  Reading, DocumentCopy, Trophy, Clock,
  Plus, Refresh, MoreFilled, Check, Warning, InfoFilled, ChatLineRound, TrendCharts
} from '@element-plus/icons-vue'

// API imports
import { getUserDashboardStats, getRecentActivities, getLearningTrends } from '@/api/analytics'
import { getCourses } from '@/api/course'
import { getNotifications } from '@/api/notification'
import { getGrades } from '@/api/grade'

const router = useRouter()
const authStore = useAuthStore()

// 计算属性
const userName = computed(() => authStore.userName)
const userId = computed(() => authStore.userId)

// 响应式数据
const loading = ref(true)
const stats = ref({
  enrolledCourses: 0,
  completedTasks: 0,
  totalScore: 0,
  studyHours: 0
})

const courses = ref([])
const tasks = ref([])
const notifications = ref([])
const activities = ref([])
const recentGrades = ref([])

// 图表数据
const chartPeriod = ref('week')
const learningChartOption = ref({})

// 方法
const loadDashboardData = async () => {
  try {
    loading.value = true
    
    // 检查用户是否已登录
    if (!userId.value) {
      console.warn('用户未登录，无法加载数据')
      loading.value = false
      ElMessage.warning('请先登录以查看您的学习数据')
      return
    }
    
    // 逐步加载数据，显示具体错误信息
    console.log('开始加载用户数据，用户ID:', userId.value)
    
    try {
      // 先加载统计数据
      const dashboardStats = await getUserDashboardStats(userId.value)
      console.log('统计数据加载成功:', dashboardStats.data)
      
      if (dashboardStats.data) {
        stats.value = {
          enrolledCourses: dashboardStats.data.enrolled_courses || 0,
          completedTasks: Math.max(0, (dashboardStats.data.total_tasks || 0) - (dashboardStats.data.unfinished_tasks || 0)),
          totalScore: Math.round(dashboardStats.data.average_score || 0),
          studyHours: Math.round((dashboardStats.data.total_study_time || 0) / 60) // 转换为小时
        }
        console.log('统计数据已更新:', stats.value)
      }
    } catch (error) {
      console.error('加载统计数据失败:', error)
      ElMessage.warning('加载统计数据失败，请检查后端服务是否启动')
    }

    try {
      // 加载课程数据 - 学生只加载已选课程
      const coursesData = await getCourses({ page: 0, size: 6 })
      console.log('课程数据加载成功:', coursesData.data)
      
      if (coursesData.data && coursesData.data.content) {
        // 只显示已选的课程
        courses.value = coursesData.data.content.filter(course => course.enrolled).slice(0, 6)
      }
    } catch (error) {
      console.error('加载课程数据失败:', error)
    }

    // 任务管理功能已删除
    tasks.value = []

    try {
      // 加载成绩数据
      const gradesData = await getGrades({ studentId: userId.value, skip: 0, limit: 5 })
      console.log('成绩数据加载成功:', gradesData.data)
      
      if (gradesData.data && gradesData.data.grades) {
        recentGrades.value = gradesData.data.grades.slice(0, 5)
      }
    } catch (error) {
      console.error('加载成绩数据失败:', error)
    }

    try {
      // 加载学习趋势数据
      const periodDays = chartPeriod.value === 'week' ? 7 : chartPeriod.value === 'month' ? 30 : 90
      const trendsData = await getLearningTrends(userId.value, { 
        days: periodDays
      })
      console.log('学习趋势数据加载成功:', trendsData.data)
      
      if (trendsData.data) {
        updateLearningChart(trendsData.data)
      }
    } catch (error) {
      console.error('加载学习趋势数据失败:', error)
      // 如果加载失败，设置空的图表
      learningChartOption.value = {
        title: {
          text: '暂无学习数据',
          left: 'center',
          top: 'middle',
          textStyle: {
            color: '#999',
            fontSize: 14
          }
        }
      }
    }

  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败，请检查后端服务连接')
  } finally {
    loading.value = false
  }
}

const updateLearningChart = (data) => {
  if (!data || !data.daily_trends) {
    learningChartOption.value = {
      title: {
        text: '暂无学习数据',
        left: 'center',
        top: 'middle',
        textStyle: {
          color: '#999',
          fontSize: 14
        }
      }
    }
    return
  }

  const dailyTrends = data.daily_trends || []
  
  // 提取日期和数据
  const dates = dailyTrends.map(item => item.date)
  const studyTimes = dailyTrends.map(item => Math.round(item.study_time || 0))
  const completedTasks = dailyTrends.map(item => item.tasks_completed || 0)
  const viewedMaterials = dailyTrends.map(item => item.materials_viewed || 0)
  
  learningChartOption.value = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let result = params[0].axisValue + '<br/>'
        params.forEach(param => {
          const unit = param.seriesName === '学习时长' ? '分钟' : '个'
          result += `${param.marker}${param.seriesName}: ${param.value}${unit}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['学习时长', '完成任务', '查看材料']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '学习时长',
        type: 'line',
        data: studyTimes,
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        }
      },
      {
        name: '完成任务',
        type: 'line',
        data: completedTasks,
        smooth: true,
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '查看材料',
        type: 'line',
        data: viewedMaterials,
        smooth: true,
        itemStyle: { color: '#e6a23c' }
      }
    ]
  }
}

const refreshData = () => {
  loadDashboardData()
}

const viewCourse = (courseId) => {
  router.push(`/student/courses/${courseId}`)
}

// 任务相关方法已删除（任务管理功能已删除）

const getScoreClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 60) return 'average'
  return 'poor'
}

// 监听图表周期变化
const onChartPeriodChange = async () => {
  try {
    const periodDays = chartPeriod.value === 'week' ? 7 : chartPeriod.value === 'month' ? 30 : 90
    const trendsData = await getLearningTrends(userId.value, { 
      days: periodDays
    })
    if (trendsData.data) {
      updateLearningChart(trendsData.data)
    }
  } catch (error) {
    console.error('更新图表数据失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.student-dashboard {
  .page-header {
    margin-bottom: 24px;
  }
}

.stats-cards {
  margin-bottom: 24px;
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.courses {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }
        
        &.tasks {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }
        
        &.progress {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          color: white;
        }
        
        &.score {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          color: white;
        }
      }
      
      .stat-info {
        .stat-number {
          font-size: 28px;
          font-weight: 700;
          color: #303133;
          line-height: 1;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
}

.dashboard-content {
  .chart-card,
  .course-list-card,
  .task-card,
  .grade-card,
  .suggestion-card {
    margin-bottom: 24px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
      }
    }
  }
  
  .chart-container {
    padding: 16px 0;
  }
  
  .course-list {
    .course-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f8f9fa;
        transform: translateY(-2px);
      }
      
      &:last-child {
        border-bottom: none;
      }
      
      .course-cover {
        width: 80px;
        height: 60px;
        border-radius: 8px;
        overflow: hidden;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .course-info {
        flex: 1;
        
        .course-title {
          margin: 0 0 4px 0;
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
        
        .course-teacher {
          margin: 0 0 8px 0;
          font-size: 14px;
          color: #909399;
        }
        
        .course-progress {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .el-progress {
            flex: 1;
          }
          
          .progress-text {
            font-size: 12px;
            color: #606266;
            min-width: 35px;
          }
        }
      }
    }
  }
  
  .task-list {
    .task-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      &.urgent {
        .task-title {
          color: #f56c6c;
        }
      }
      
      .task-info {
        flex: 1;
        
        .task-title {
          margin: 0 0 4px 0;
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
        
        .task-course {
          margin: 0 0 8px 0;
          font-size: 12px;
          color: #909399;
        }
        
        .task-deadline {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #606266;
        }
      }
    }
  }
  
  .grade-list {
    .grade-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .grade-info {
        .grade-title {
          margin: 0 0 4px 0;
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
        
        .grade-course {
          margin: 0;
          font-size: 12px;
          color: #909399;
        }
      }
      
      .grade-score {
        text-align: right;
        
        .score {
          font-size: 18px;
          font-weight: 700;
          
          &.excellent {
            color: #67c23a;
          }
          
          &.good {
            color: #409eff;
          }
          
          &.average {
            color: #e6a23c;
          }
          
          &.poor {
            color: #f56c6c;
          }
        }
        
        .total {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
  
  .suggestion-content {
    .suggestion-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 12px;
      font-size: 14px;
      line-height: 1.5;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
  
  .suggestion-footer {
    margin-top: 16px;
    text-align: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .stats-cards {
    .stat-card .stat-content {
      flex-direction: column;
      text-align: center;
      gap: 8px;
      
      .stat-icon {
        width: 48px;
        height: 48px;
      }
      
      .stat-info .stat-number {
        font-size: 24px;
      }
    }
  }
  
  .course-list .course-item {
    flex-direction: column;
    align-items: flex-start;
    
    .course-cover {
      width: 100%;
      height: 120px;
    }
  }
}
</style> 