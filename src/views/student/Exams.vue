<template>
  <div class="student-exams">
    <div class="page-header">
      <h1>我的考试</h1>
      <div class="header-actions">
        <el-button @click="loadExams">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 考试统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ examStats.totalExams }}</div>
            <div class="stat-label">总考试数</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ examStats.completedExams }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ examStats.pendingExams }}</div>
            <div class="stat-label">待参加</div>
          </div>
          <el-icon class="stat-icon"><Clock /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ examStats.averageScore }}</div>
            <div class="stat-label">平均分</div>
          </div>
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="搜索考试">
          <el-input
            v-model="searchForm.keyword"
            placeholder="考试标题"
            @keyup.enter="handleSearch"
            style="width: 250px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable filterable>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.title"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 考试列表 -->
    <div class="exams-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="exam in exams" :key="exam.id">
          <el-card class="exam-card" shadow="hover">
            <div class="exam-header">
              <h3 class="exam-title">{{ exam.title }}</h3>
              <el-tag :type="getStatusTagType(exam.status)" size="small">
                {{ getStatusText(exam.status) }}
              </el-tag>
            </div>
            
            <div class="exam-content">
              <div class="exam-info">
                <div class="info-item">
                  <el-icon><Reading /></el-icon>
                  <span>{{ exam.course?.title }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Timer /></el-icon>
                  <span>{{ exam.duration }} 分钟</span>
                </div>
                <div class="info-item">
                  <el-icon><Star /></el-icon>
                  <span>总分: {{ exam.totalScore }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatTime(exam.startTime) }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span>截止: {{ formatTime(exam.endTime) }}</span>
                </div>
              </div>

              <!-- 考试结果 -->
              <div class="exam-result" v-if="exam.myResult">
                <div class="result-score">
                  <span class="score">{{ exam.myResult.score }}</span>
                  <span class="total">/{{ exam.totalScore }}</span>
                </div>
                <div class="result-info">
                  <div>用时: {{ exam.myResult.duration }} 分钟</div>
                  <div>提交时间: {{ formatTime(exam.myResult.submitTime) }}</div>
                </div>
              </div>

              <!-- 考试描述 -->
              <div class="exam-description">
                {{ exam.description }}
              </div>
            </div>
            
            <div class="exam-actions">
              <el-button 
                v-if="canStartExam(exam)" 
                type="primary" 
                @click="startExam(exam)"
                :loading="startingExam === exam.id"
              >
                开始考试
              </el-button>
              <el-button 
                v-if="canContinueExam(exam)" 
                type="warning" 
                @click="continueExam(exam)"
              >
                继续考试
              </el-button>
              <el-button 
                v-if="canViewResult(exam)" 
                @click="viewResult(exam)"
              >
                查看结果
              </el-button>
              <el-button 
                size="small" 
                @click="viewExamDetail(exam)"
              >
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[9, 18, 36]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 考试详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="考试详情" width="800px">
      <div v-if="selectedExam" class="exam-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="考试标题" :span="2">
            {{ selectedExam.title }}
          </el-descriptions-item>
          <el-descriptions-item label="所属课程">
            {{ selectedExam.course?.title }}
          </el-descriptions-item>
          <el-descriptions-item label="考试状态">
            <el-tag :type="getStatusTagType(selectedExam.status)">
              {{ getStatusText(selectedExam.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="考试时长">
            {{ selectedExam.duration }} 分钟
          </el-descriptions-item>
          <el-descriptions-item label="总分">
            {{ selectedExam.totalScore }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatTime(selectedExam.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatTime(selectedExam.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="题目数量">
            {{ selectedExam.questionCount }}
          </el-descriptions-item>
          <el-descriptions-item label="允许重考">
            {{ selectedExam.allowRetake ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="考试描述" :span="2">
            {{ selectedExam.description }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 考试结果详情 -->
        <div class="exam-result-detail" v-if="selectedExam.myResult">
          <h3>我的考试结果</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="得分" :value="selectedExam.myResult.score" :suffix="`/${selectedExam.totalScore}`" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="用时" :value="selectedExam.myResult.duration" suffix="分钟" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="正确率" :value="selectedExam.myResult.accuracy" suffix="%" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="排名" :value="selectedExam.myResult.rank" :suffix="`/${selectedExam.myResult.totalParticipants}`" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <!-- 考试结果对话框 -->
    <el-dialog v-model="showResultDialog" title="考试结果" width="900px">
      <div v-if="examResult" class="exam-result-content">
        <div class="result-summary">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ examResult.score }}</div>
                <div class="summary-label">总分</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ examResult.accuracy }}%</div>
                <div class="summary-label">正确率</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ examResult.duration }}分钟</div>
                <div class="summary-label">用时</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ examResult.rank }}</div>
                <div class="summary-label">排名</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 题目详情 -->
        <div class="questions-review">
          <h3>答题详情</h3>
          <div v-for="(question, index) in examResult.questions" :key="question.id" class="question-item">
            <div class="question-header">
              <span class="question-number">第{{ index + 1 }}题</span>
              <el-tag :type="question.isCorrect ? 'success' : 'danger'" size="small">
                {{ question.isCorrect ? '正确' : '错误' }}
              </el-tag>
              <span class="question-score">{{ question.score }}/{{ question.maxScore }}分</span>
            </div>
            <div class="question-content">
              <div class="question-text">{{ question.content }}</div>
              <div class="question-answer">
                <div class="my-answer">
                  <strong>我的答案:</strong> {{ question.myAnswer }}
                </div>
                <div class="correct-answer" v-if="!question.isCorrect">
                  <strong>正确答案:</strong> {{ question.correctAnswer }}
                </div>
                <div class="answer-analysis" v-if="question.analysis">
                  <strong>解析:</strong> {{ question.analysis }}
                </div>
              </div>
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
  Refresh,
  Search,
  Document,
  Checked,
  Clock,
  TrendCharts,
  Reading,
  Timer,
  Star,
  Calendar
} from '@element-plus/icons-vue'
import { getExamList, startExam as startExamApi, getExamResult } from '@/api/exam'
import { getCourseList } from '@/api/course'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const startingExam = ref(null)
const exams = ref([])
const courses = ref([])
const examStats = ref({
  totalExams: 0,
  completedExams: 0,
  pendingExams: 0,
  averageScore: 0
})

// 对话框控制
const showDetailDialog = ref(false)
const showResultDialog = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  courseId: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 9,
  total: 0
})

// 选中的考试和结果
const selectedExam = ref(null)
const examResult = ref(null)

// 生命周期
onMounted(() => {
  loadExams()
  loadCourses()
})

// 方法
const loadExams = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      courseId: searchForm.courseId,
      status: searchForm.status,
      studentId: authStore.user.id
    }
    
    const response = await getExamList(params)
    exams.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 计算统计数据
    examStats.value = {
      totalExams: exams.value.length,
      completedExams: exams.value.filter(e => e.status === 'COMPLETED').length,
      pendingExams: exams.value.filter(e => e.status === 'NOT_STARTED' || e.status === 'IN_PROGRESS').length,
      averageScore: exams.value.length > 0 ? 
        (exams.value.filter(e => e.myResult).reduce((sum, e) => sum + (e.myResult.score || 0), 0) / 
         exams.value.filter(e => e.myResult).length || 0).toFixed(1) : 0
    }
  } catch (error) {
    ElMessage.error('加载考试列表失败')
    console.error('Failed to load exams:', error)
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    // 加载学生已选课程
    const response = await getCourseList({ studentId: authStore.user.id, size: 1000 })
    courses.value = response.data.content || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
    throw error
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadExams()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.courseId = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadExams()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadExams()
}

const canStartExam = (exam) => {
  const now = new Date()
  const startTime = new Date(exam.startTime)
  const endTime = new Date(exam.endTime)
  
  return exam.status === 'NOT_STARTED' && 
         now >= startTime && 
         now <= endTime && 
         !exam.myResult
}

const canContinueExam = (exam) => {
  return exam.status === 'IN_PROGRESS'
}

const canViewResult = (exam) => {
  return exam.myResult && exam.status === 'COMPLETED'
}

const startExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要开始考试 "${exam.title}" 吗？考试开始后不能暂停，请确保网络稳定。`,
      '开始考试',
      {
        confirmButtonText: '开始',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    startingExam.value = exam.id
    const response = await startExamApi(exam.id)
    
    // 跳转到考试页面
    router.push(`/student/exam/${exam.id}/take?sessionId=${response.data.sessionId}`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('开始考试失败')
    }
  } finally {
    startingExam.value = null
  }
}

const continueExam = (exam) => {
  // 跳转到考试页面继续答题
  router.push(`/student/exam/${exam.id}/take`)
}

const viewResult = async (exam) => {
  try {
    const response = await getExamResult(exam.id)
    examResult.value = response.data
    showResultDialog.value = true
  } catch (error) {
    ElMessage.error('加载考试结果失败')
    console.error('Failed to load exam result:', error)
  }
}

const viewExamDetail = (exam) => {
  selectedExam.value = exam
  showDetailDialog.value = true
}

// 工具方法
const getStatusText = (status) => {
  const statusMap = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    ENDED: '已结束'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    NOT_STARTED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    ENDED: 'danger'
  }
  return typeMap[status] || ''
}

const formatTime = (time) => {
  if (!time) return '未设置'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.student-exams {
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

.exams-grid {
  margin-bottom: 20px;
}

.exam-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
  height: 420px;
}

.exam-card:hover {
  transform: translateY(-5px);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.exam-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 10px;
}

.exam-content {
  margin-bottom: 15px;
}

.exam-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.exam-result {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.result-score {
  text-align: center;
  margin-bottom: 10px;
}

.score {
  font-size: 32px;
  font-weight: bold;
}

.total {
  font-size: 18px;
  opacity: 0.8;
}

.result-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  opacity: 0.9;
}

.exam-description {
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.exam-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.exam-detail {
  padding: 20px 0;
}

.exam-result-detail {
  margin-top: 30px;
}

.exam-result-detail h3 {
  margin-bottom: 15px;
  color: #303133;
}

.exam-result-content {
  padding: 20px 0;
}

.result-summary {
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
}

.summary-item {
  text-align: center;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.summary-label {
  color: #909399;
  font-size: 14px;
}

.questions-review h3 {
  margin-bottom: 20px;
  color: #303133;
}

.question-item {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f7fa;
}

.question-number {
  font-weight: bold;
  color: #303133;
}

.question-score {
  margin-left: auto;
  color: #909399;
  font-size: 14px;
}

.question-content {
  line-height: 1.6;
}

.question-text {
  margin-bottom: 15px;
  color: #303133;
}

.question-answer > div {
  margin-bottom: 10px;
}

.my-answer {
  color: #606266;
}

.correct-answer {
  color: #67c23a;
}

.answer-analysis {
  color: #909399;
  font-style: italic;
}
</style> 