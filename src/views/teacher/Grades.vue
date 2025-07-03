<template>
  <div class="teacher-grades">
    <div class="page-header">
      <h1>成绩管理</h1>
      <div class="header-actions">
        <el-button type="success" @click="exportGrades">
          <el-icon><Download /></el-icon>
          导出成绩
        </el-button>
        <el-button @click="loadGrades">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 成绩统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ gradeStats.totalSubmissions }}</div>
            <div class="stat-label">总提交数</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ gradeStats.gradedSubmissions }}</div>
            <div class="stat-label">已批改</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ gradeStats.pendingGrading }}</div>
            <div class="stat-label">待批改</div>
          </div>
          <el-icon class="stat-icon"><Clock /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ gradeStats.averageScore }}</div>
            <div class="stat-label">平均分</div>
          </div>
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.title"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务/考试">
                  <!-- 任务选择器已删除 -->
        </el-form-item>
        <el-form-item label="批改状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="待批改" value="PENDING" />
            <el-option label="已批改" value="GRADED" />
            <el-option label="已返回" value="RETURNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生">
          <el-input
            v-model="searchForm.studentName"
            placeholder="学生姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作 -->
    <el-card class="batch-card" v-if="selectedGrades.length > 0">
      <div class="batch-actions">
        <span>已选择 {{ selectedGrades.length }} 项</span>
        <div class="actions">
          <el-button size="small" @click="batchGrade">批量批改</el-button>
          <el-button size="small" @click="batchReturn">批量返回</el-button>
          <el-button size="small" type="success" @click="batchExport">批量导出</el-button>
        </div>
      </div>
    </el-card>

    <!-- 成绩列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>成绩列表</span>
          <div class="view-toggle">
            <el-radio-group v-model="viewMode" @change="handleViewChange">
              <el-radio-button label="list">列表视图</el-radio-button>
              <el-radio-button label="grid">卡片视图</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'">
        <el-table
          :data="grades"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="student" label="学生" width="120">
            <template #default="{ row }">
              <div class="student-info">
                <el-avatar :size="30" :src="row.student?.avatar">
                  {{ row.student?.realName?.charAt(0) || row.student?.username?.charAt(0) }}
                </el-avatar>
                <span>{{ row.student?.realName || row.student?.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="task" label="任务/考试" width="200">
            <template #default="{ row }">
              <div class="task-info">
                <div class="task-title">{{ row.task?.title }}</div>
                <div class="task-course">{{ row.task?.course?.title }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" size="small">
                {{ getTypeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="120">
            <template #default="{ row }">
              <div v-if="row.status === 'GRADED'" class="score-display">
                <span :class="getScoreClass(row.score, row.maxScore)">
                  {{ row.score }}/{{ row.maxScore }}
                </span>
              </div>
              <el-tag v-else type="warning" size="small">待批改</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submissionTime" label="提交时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.submissionTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="gradingTime" label="批改时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.gradingTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewSubmission(row)">查看</el-button>
              <el-button
                size="small"
                type="primary"
                @click="gradeSubmission(row)"
                v-if="row.status === 'PENDING'"
              >
                批改
              </el-button>
              <el-button
                size="small"
                @click="editGrade(row)"
                v-if="row.status === 'GRADED'"
              >
                修改
              </el-button>
              <el-button
                size="small"
                type="success"
                @click="returnGrade(row)"
                v-if="row.status === 'GRADED'"
              >
                返回
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 卡片视图 -->
      <div v-else class="grades-grid">
        <el-row :gutter="20">
          <el-col :span="8" v-for="grade in grades" :key="grade.id">
            <el-card class="grade-card" shadow="hover">
              <div class="grade-header">
                <div class="student-info">
                  <el-avatar :size="40" :src="grade.student?.avatar">
                    {{ grade.student?.realName?.charAt(0) || grade.student?.username?.charAt(0) }}
                  </el-avatar>
                  <div class="student-details">
                    <div class="student-name">{{ grade.student?.realName || grade.student?.username }}</div>
                    <div class="student-id">{{ grade.student?.username }}</div>
                  </div>
                </div>
                <el-tag :type="getStatusTagType(grade.status)" size="small">
                  {{ getStatusText(grade.status) }}
                </el-tag>
              </div>
              
              <div class="grade-content">
                <div class="task-title">{{ grade.task?.title }}</div>
                <div class="task-course">{{ grade.task?.course?.title }}</div>
                
                <div class="grade-score" v-if="grade.status === 'GRADED'">
                  <span :class="getScoreClass(grade.score, grade.maxScore)">
                    {{ grade.score }}/{{ grade.maxScore }}
                  </span>
                </div>
                
                <div class="grade-meta">
                  <div>提交: {{ formatTime(grade.submissionTime) }}</div>
                  <div v-if="grade.gradingTime">批改: {{ formatTime(grade.gradingTime) }}</div>
                </div>
              </div>
              
              <div class="grade-actions">
                <el-button size="small" @click="viewSubmission(grade)">查看</el-button>
                <el-button
                  size="small"
                  type="primary"
                  @click="gradeSubmission(grade)"
                  v-if="grade.status === 'PENDING'"
                >
                  批改
                </el-button>
                <el-button
                  size="small"
                  @click="editGrade(grade)"
                  v-if="grade.status === 'GRADED'"
                >
                  修改
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 分页 -->
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

    <!-- 批改对话框 -->
    <el-dialog
      v-model="showGradingDialog"
      title="批改作业"
      width="900px"
      @close="resetGradingForm"
    >
      <div v-if="selectedSubmission" class="grading-content">
        <!-- 提交信息 -->
        <div class="submission-info">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="学生">
              {{ selectedSubmission.student?.realName || selectedSubmission.student?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="任务">
              {{ selectedSubmission.task?.title }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ formatTime(selectedSubmission.submissionTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 提交内容 -->
        <div class="submission-content">
          <h3>提交内容</h3>
          <div class="content-display">
            {{ selectedSubmission.content }}
          </div>
          
          <!-- 附件 -->
          <div v-if="selectedSubmission.attachments && selectedSubmission.attachments.length > 0" class="attachments">
            <h4>附件</h4>
            <el-list>
              <el-list-item v-for="attachment in selectedSubmission.attachments" :key="attachment.id">
                <el-link :href="attachment.url" target="_blank">
                  <el-icon><Document /></el-icon>
                  {{ attachment.name }}
                </el-link>
              </el-list-item>
            </el-list>
          </div>
        </div>

        <!-- 批改表单 -->
        <div class="grading-form">
          <h3>批改</h3>
          <el-form :model="gradingForm" :rules="gradingRules" ref="gradingFormRef" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="得分" prop="score">
                  <el-input-number
                    v-model="gradingForm.score"
                    :min="0"
                    :max="selectedSubmission.maxScore"
                    :precision="1"
                    style="width: 100%"
                  />
                  <span class="score-hint">满分: {{ selectedSubmission.maxScore }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="是否及格">
                  <el-switch
                    v-model="gradingForm.isPassed"
                    active-text="及格"
                    inactive-text="不及格"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="评语" prop="feedback">
              <el-input
                v-model="gradingForm.feedback"
                type="textarea"
                :rows="4"
                placeholder="请输入评语和建议"
              />
            </el-form-item>
            
            <el-form-item label="改进建议">
              <el-input
                v-model="gradingForm.suggestions"
                type="textarea"
                :rows="3"
                placeholder="请输入改进建议"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showGradingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitGrading" :loading="gradingLoading">
          提交批改
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看提交对话框 -->
    <el-dialog v-model="showViewDialog" title="查看提交" width="800px">
      <div v-if="selectedSubmission" class="view-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">
            {{ selectedSubmission.student?.realName || selectedSubmission.student?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="任务">
            {{ selectedSubmission.task?.title }}
          </el-descriptions-item>
          <el-descriptions-item label="得分" v-if="selectedSubmission.status === 'GRADED'">
            <span :class="getScoreClass(selectedSubmission.score, selectedSubmission.maxScore)">
              {{ selectedSubmission.score }}/{{ selectedSubmission.maxScore }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedSubmission.status)">
              {{ getStatusText(selectedSubmission.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatTime(selectedSubmission.submissionTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="批改时间" v-if="selectedSubmission.gradingTime">
            {{ formatTime(selectedSubmission.gradingTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="content-section">
          <h3>提交内容</h3>
          <div class="content-display">
            {{ selectedSubmission.content }}
          </div>
        </div>

        <div v-if="selectedSubmission.feedback" class="feedback-section">
          <h3>批改反馈</h3>
          <div class="feedback-content">
            {{ selectedSubmission.feedback }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download,
  Refresh,
  Document,
  Checked,
  Clock,
  TrendCharts
} from '@element-plus/icons-vue'
import { getGradeList, gradeSubmission as gradeSubmissionApi, updateGrade } from '@/api/grade'
import { getCourseList } from '@/api/course'
// Task API 已删除
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const gradingLoading = ref(false)
const grades = ref([])
const selectedGrades = ref([])
const courses = ref([])
// 任务数据已删除
const viewMode = ref('list')
const gradeStats = ref({
  totalSubmissions: 0,
  gradedSubmissions: 0,
  pendingGrading: 0,
  averageScore: 0
})

// 对话框控制
const showGradingDialog = ref(false)
const showViewDialog = ref(false)

// 表单引用
const gradingFormRef = ref()

// 搜索表单
const searchForm = reactive({
  courseId: '',
  status: '',
  studentName: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 选中的提交
const selectedSubmission = ref(null)

// 批改表单
const gradingForm = reactive({
  score: 0,
  feedback: '',
  suggestions: '',
  isPassed: false
})

// 批改表单验证规则
const gradingRules = {
  score: [
    { required: true, message: '请输入得分', trigger: 'blur' }
  ],
  feedback: [
    { required: true, message: '请输入评语', trigger: 'blur' },
    { min: 10, message: '评语至少10个字符', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadGrades()
  loadCourses()
})

// 方法
const loadGrades = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      courseId: searchForm.courseId,
      status: searchForm.status,
      teacherId: authStore.user.id
    }
    
    const response = await getGradeList(params)
    grades.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 计算统计数据
    gradeStats.value = {
      totalSubmissions: grades.value.length,
      gradedSubmissions: grades.value.filter(g => g.status === 'GRADED').length,
      pendingGrading: grades.value.filter(g => g.status === 'PENDING').length,
      averageScore: grades.value.length > 0 ? 
        (grades.value.filter(g => g.score).reduce((sum, g) => sum + g.score, 0) / 
         grades.value.filter(g => g.score).length || 0).toFixed(1) : 0
    }
  } catch (error) {
    ElMessage.error('加载成绩列表失败')
    console.error('Failed to load grades:', error)
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    const response = await getCourseList({ teacherId: authStore.user.id, size: 1000 })
    courses.value = response.data.content || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
    throw error
  }
}

// loadTasks方法已删除（任务管理功能已删除）

const handleSearch = () => {
  pagination.page = 1
  loadGrades()
}

const resetSearch = () => {
  searchForm.courseId = ''
  searchForm.status = ''
  searchForm.studentName = ''
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

const handleSelectionChange = (selection) => {
  selectedGrades.value = selection
}

const handleViewChange = () => {
  // 视图切换时可以做一些处理
}

const viewSubmission = (grade) => {
  selectedSubmission.value = grade
  showViewDialog.value = true
}

const gradeSubmission = (grade) => {
  selectedSubmission.value = grade
  gradingForm.score = 0
  gradingForm.feedback = ''
  gradingForm.suggestions = ''
  gradingForm.isPassed = false
  showGradingDialog.value = true
}

const editGrade = (grade) => {
  selectedSubmission.value = grade
  gradingForm.score = grade.score
  gradingForm.feedback = grade.feedback || ''
  gradingForm.suggestions = grade.suggestions || ''
  gradingForm.isPassed = grade.isPassed || false
  showGradingDialog.value = true
}

const submitGrading = async () => {
  if (!gradingFormRef.value) return
  
  try {
    await gradingFormRef.value.validate()
    gradingLoading.value = true
    
    ElMessage.success('批改提交成功')
    showGradingDialog.value = false
    resetGradingForm()
    loadGrades()
  } catch (error) {
    ElMessage.error('批改提交失败')
  } finally {
    gradingLoading.value = false
  }
}

const resetGradingForm = () => {
  gradingForm.score = 0
  gradingForm.feedback = ''
  gradingForm.suggestions = ''
  gradingForm.isPassed = false
  gradingFormRef.value?.resetFields()
}

const returnGrade = async (grade) => {
  try {
    await ElMessageBox.confirm(
      '确定要将此成绩返回给学生吗？',
      '返回成绩',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    ElMessage.success('成绩已返回')
    loadGrades()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('返回成绩失败')
    }
  }
}

const batchGrade = () => {
  ElMessage.info('批量批改功能开发中...')
}

const batchReturn = () => {
  ElMessage.info('批量返回功能开发中...')
}

const batchExport = () => {
  ElMessage.info('批量导出功能开发中...')
}

const exportGrades = () => {
  ElMessage.info('导出功能开发中...')
}

// 工具方法
const getTypeText = (type) => {
  const typeMap = {
    HOMEWORK: '作业',
    EXAM: '考试',
    PROJECT: '项目',
    DISCUSSION: '讨论'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    HOMEWORK: '',
    EXAM: 'success',
    PROJECT: 'warning',
    DISCUSSION: 'info'
  }
  return typeMap[type] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待批改',
    GRADED: '已批改',
    RETURNED: '已返回'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    GRADED: 'success',
    RETURNED: 'info'
  }
  return typeMap[status] || ''
}

const getScoreClass = (score, maxScore) => {
  const percentage = (score / maxScore) * 100
  if (percentage >= 90) return 'excellent'
  if (percentage >= 80) return 'good'
  if (percentage >= 60) return 'pass'
  return 'fail'
}

const formatTime = (time) => {
  if (!time) return '未设置'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.teacher-grades {
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

.batch-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.actions {
  display: flex;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-info .task-title {
  font-weight: bold;
  color: #303133;
}

.task-info .task-course {
  font-size: 12px;
  color: #909399;
}

.score-display {
  font-weight: bold;
}

.excellent {
  color: #67c23a;
}

.good {
  color: #409eff;
}

.pass {
  color: #e6a23c;
}

.fail {
  color: #f56c6c;
}

.grades-grid {
  margin-bottom: 20px;
}

.grade-card {
  margin-bottom: 20px;
  height: 280px;
}

.grade-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.student-details {
  margin-left: 10px;
}

.student-name {
  font-weight: bold;
  color: #303133;
}

.student-id {
  font-size: 12px;
  color: #909399;
}

.grade-content {
  margin-bottom: 15px;
}

.task-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.task-course {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.grade-score {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.grade-meta {
  font-size: 12px;
  color: #909399;
}

.grade-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.grading-content {
  padding: 20px 0;
}

.submission-info {
  margin-bottom: 20px;
}

.submission-content {
  margin-bottom: 30px;
}

.submission-content h3 {
  margin-bottom: 15px;
  color: #303133;
}

.content-display {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.attachments h4 {
  margin-bottom: 10px;
  color: #303133;
}

.grading-form h3 {
  margin-bottom: 15px;
  color: #303133;
}

.score-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.view-content {
  padding: 20px 0;
}

.content-section {
  margin-top: 20px;
}

.content-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.feedback-section {
  margin-top: 20px;
}

.feedback-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.feedback-content {
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 4px;
  line-height: 1.6;
}
</style> 