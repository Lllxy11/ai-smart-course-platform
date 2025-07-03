<template>
  <div class="teacher-analytics">
    <div class="page-header">
      <h1>数据分析</h1>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>



    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-row" v-loading="loading">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalCourses }}</div>
            <div class="stat-label">我的课程</div>
          </div>
          <el-icon class="stat-icon"><Reading /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalStudents }}</div>
            <div class="stat-label">学生总数</div>
          </div>
          <el-icon class="stat-icon"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageScore }}</div>
            <div class="stat-label">平均成绩</div>
          </div>
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.completionRate }}%</div>
            <div class="stat-label">通过率</div>
          </div>
          <el-icon class="stat-icon"><SuccessFilled /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表分析 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>学生成绩分布</span>
          </template>
          <div ref="scoreChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>课程完成情况</span>
          </template>
          <div ref="completionChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>最近活动</span>
          </template>
          <el-table :data="recentActivities" style="width: 100%">
            <el-table-column prop="title" label="活动类型" width="150" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="time" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Reading, User, TrendCharts, SuccessFilled } from '@element-plus/icons-vue'
import { getTeacherAnalytics } from '@/api/analytics'
import { useAuthStore } from '@/stores/auth'
import * as echarts from 'echarts'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const stats = reactive({
  totalCourses: 0,
  totalStudents: 0,
  averageScore: 0,
  completionRate: 0
})

// 图表引用
const scoreChart = ref(null)
const completionChart = ref(null)

// 图表实例
let scoreChartInstance = null
let completionChartInstance = null

// 最近活动数据
const recentActivities = ref([])

// 原始分析数据
const analyticsData = ref(null)

// 生命周期
onMounted(() => {
  loadAnalytics()
  
  // 添加窗口调整事件监听
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  if (scoreChartInstance) {
    scoreChartInstance.dispose()
  }
  if (completionChartInstance) {
    completionChartInstance.dispose()
  }
})

// 处理窗口调整
const handleResize = () => {
  if (scoreChartInstance) {
    scoreChartInstance.resize()
  }
  if (completionChartInstance) {
    completionChartInstance.resize()
  }
}

// 方法
const loadAnalytics = async () => {
  loading.value = true
  try {
    if (!authStore.user?.id) {
      throw new Error('用户信息不完整，无法获取分析数据')
    }
    
    const response = await getTeacherAnalytics(authStore.user.id)
    
    if (response && response.data) {
      const data = response.data
      
      // 保存原始数据
      analyticsData.value = data
      
      // 映射后端返回的数据结构到前端期望的格式
      stats.totalCourses = data.course_stats?.total_courses || 0
      stats.totalStudents = data.course_stats?.total_students || 0
      stats.averageScore = data.grade_stats?.average_score || 0
      stats.completionRate = data.grade_stats?.pass_rate || 0
      
      // 处理最近活动数据
      recentActivities.value = data.recent_activities || []
      
      // 初始化图表
      await nextTick()
      initCharts()
      
      ElMessage.success('数据加载成功')
    } else {
      ElMessage.warning('未获取到分析数据')
    }
  } catch (error) {
    console.error('加载分析数据失败:', error)
    
    let errorMessage = '加载分析数据失败'
    if (error.response?.status === 403) {
      errorMessage = '无权限访问该数据'
    } else if (error.response?.status === 404) {
      errorMessage = '分析数据接口不存在'
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadAnalytics()
}

// 初始化图表
const initCharts = () => {
  initScoreChart()
  initCompletionChart()
}

// 初始化成绩分布图表
const initScoreChart = () => {
  if (!scoreChart.value) return
  
  // 销毁已存在的图表
  if (scoreChartInstance) {
    scoreChartInstance.dispose()
  }
  
  scoreChartInstance = echarts.init(scoreChart.value)
  
  const gradeDistribution = analyticsData.value?.grade_stats?.grade_distribution || {}
  
  const option = {
    title: {
      text: '成绩分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['A (90-100)', 'B (80-89)', 'C (70-79)', 'D (60-69)', 'F (<60)']
    },
    series: [
      {
        name: '成绩分布',
        type: 'pie',
        radius: '50%',
        data: [
          { value: gradeDistribution.A || 0, name: 'A (90-100)' },
          { value: gradeDistribution.B || 0, name: 'B (80-89)' },
          { value: gradeDistribution.C || 0, name: 'C (70-79)' },
          { value: gradeDistribution.D || 0, name: 'D (60-69)' },
          { value: gradeDistribution.F || 0, name: 'F (<60)' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  scoreChartInstance.setOption(option)
}

// 初始化完成情况图表
const initCompletionChart = () => {
  if (!completionChart.value) return
  
  // 销毁已存在的图表
  if (completionChartInstance) {
    completionChartInstance.dispose()
  }
  
  completionChartInstance = echarts.init(completionChart.value)
  
  const taskStats = analyticsData.value?.task_stats || {}
  
  const option = {
    title: {
      text: '任务完成统计',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['总任务数', '总提交数', '待批改', '已批改']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: [
          taskStats.total_tasks || 0,
          taskStats.total_submissions || 0,
          taskStats.pending_grading || 0,
          taskStats.graded_submissions || 0
        ],
        itemStyle: {
          color: function(params) {
            const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
            return colors[params.dataIndex]
          }
        }
      }
    ]
  }
  
  completionChartInstance.setOption(option)
}
</script>

<style scoped>
.teacher-analytics {
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

/* 图表容器样式 */
.el-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

/* 表格样式优化 */
.el-table {
  border-radius: 4px;
  overflow: hidden;
}

/* 响应式处理 */
@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 20px;
  }
  
  .stat-number {
    font-size: 24px !important;
  }
}
</style> 