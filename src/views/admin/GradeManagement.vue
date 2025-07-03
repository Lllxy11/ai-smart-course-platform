<template>
  <div class="grade-management">
    <div class="page-header">
      <h1>成绩管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="success" @click="exportGrades">
          <el-icon><Download /></el-icon>
          导出成绩
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalGrades }}</div>
            <div class="stat-label">总成绩数</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageScore }}</div>
            <div class="stat-label">平均分</div>
          </div>
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.passRate }}%</div>
            <div class="stat-label">通过率</div>
          </div>
          <el-icon class="stat-icon"><SuccessFilled /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.excellentRate }}%</div>
            <div class="stat-label">优秀率</div>
          </div>
          <el-icon class="stat-icon"><Star /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 200px">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.title"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生">
          <el-input
            v-model="searchForm.studentKeyword"
            placeholder="输入学生姓名或学号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="成绩范围">
          <el-select v-model="searchForm.scoreRange" placeholder="选择成绩范围" clearable>
            <el-option label="优秀 (90-100)" value="excellent" />
            <el-option label="良好 (80-89)" value="good" />
            <el-option label="中等 (70-79)" value="medium" />
            <el-option label="及格 (60-69)" value="pass" />
            <el-option label="不及格 (0-59)" value="fail" />
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

    <!-- 成绩列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>成绩列表</span>
        </div>
      </template>
      
      <el-table :data="grades" v-loading="loading" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNumber" label="学号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="taskTitle" label="任务/考试" min-width="150" />
        <el-table-column prop="score" label="得分" width="80" align="center">
          <template #default="{ row }">
            <span :class="getScoreClass(row.score)">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="percentage" label="百分比" width="100" align="center">
          <template #default="{ row }">
            {{ ((row.score / row.totalScore) * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.percentage)">
              {{ getScoreLevel(row.percentage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewGradeDetail(row)">
              详情
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

    <!-- 成绩详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="成绩详情" width="600px">
      <div v-if="selectedGrade" class="grade-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生姓名">{{ selectedGrade.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ selectedGrade.studentNumber }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ selectedGrade.courseName }}</el-descriptions-item>
          <el-descriptions-item label="任务/考试">{{ selectedGrade.taskTitle }}</el-descriptions-item>
          <el-descriptions-item label="得分">
            <span :class="getScoreClass(selectedGrade.score)">
              {{ selectedGrade.score }} / {{ selectedGrade.totalScore }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="百分比">
            {{ ((selectedGrade.score / selectedGrade.totalScore) * 100).toFixed(1) }}%
          </el-descriptions-item>
          <el-descriptions-item label="等级">
            <el-tag :type="getLevelTagType(selectedGrade.percentage)">
              {{ getScoreLevel(selectedGrade.percentage) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatTime(selectedGrade.submittedAt) }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="selectedGrade.feedback" style="margin-top: 20px;">
          <h4>教师反馈</h4>
          <p>{{ selectedGrade.feedback }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Download,
  Search,
  Document,
  TrendCharts,
  SuccessFilled,
  Star
} from '@element-plus/icons-vue'
import { getGradeList, getGradeStatistics } from '@/api/grade'
import { getCourseList } from '@/api/course'

// 响应式数据
const loading = ref(false)
const grades = ref([])
const courses = ref([])
const selectedGrade = ref(null)
const showDetailDialog = ref(false)

// 统计数据
const stats = reactive({
  totalGrades: 0,
  averageScore: 0,
  passRate: 0,
  excellentRate: 0
})

// 搜索表单
const searchForm = reactive({
  courseId: '',
  studentKeyword: '',
  scoreRange: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 生命周期
onMounted(() => {
  loadGrades()
  loadCourses()
  loadStats()
})

// 方法
const loadGrades = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      courseId: searchForm.courseId,
      studentKeyword: searchForm.studentKeyword,
      scoreRange: searchForm.scoreRange
    }
    
    const response = await getGradeList(params)
    grades.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    console.error('加载成绩列表失败:', error)
    ElMessage.error('加载成绩列表失败')
    throw error
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    const response = await getCourseList({ size: 1000 })
    courses.value = response.data.content || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
    throw error
  }
}

const loadStats = async () => {
  try {
    const response = await getGradeStatistics()
    if (response.data) {
      Object.assign(stats, response.data)
    }
  } catch (error) {
    console.error('加载成绩统计失败:', error)
    ElMessage.error('加载成绩统计失败')
    throw error
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadGrades()
}

const resetSearch = () => {
  searchForm.courseId = ''
  searchForm.studentKeyword = ''
  searchForm.scoreRange = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadGrades()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadGrades()
}

const viewGradeDetail = (grade) => {
  selectedGrade.value = grade
  showDetailDialog.value = true
}

const refreshData = () => {
  loadGrades()
  loadStats()
}

const exportGrades = () => {
  ElMessage.info('导出功能开发中...')
}

// 工具方法
const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-medium'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const getScoreLevel = (percentage) => {
  if (percentage >= 90) return '优秀'
  if (percentage >= 80) return '良好'
  if (percentage >= 70) return '中等'
  if (percentage >= 60) return '及格'
  return '不及格'
}

const getLevelTagType = (percentage) => {
  if (percentage >= 90) return 'success'
  if (percentage >= 80) return 'primary'
  if (percentage >= 70) return 'warning'
  if (percentage >= 60) return 'info'
  return 'danger'
}

const formatTime = (time) => {
  if (!time) return '未提交'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.grade-management {
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

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.grade-detail {
  padding: 20px 0;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-medium {
  color: #e6a23c;
  font-weight: bold;
}

.score-pass {
  color: #909399;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}
</style> 