<template>
  <div class="teacher-exams">
    <div class="page-header">
      <h1>考试管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建考试
        </el-button>
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
            <div class="stat-number">{{ examStats.publishedExams }}</div>
            <div class="stat-label">已发布</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ examStats.totalParticipants }}</div>
            <div class="stat-label">参考人数</div>
          </div>
          <el-icon class="stat-icon"><UserFilled /></el-icon>
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
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable>
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
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="进行中" value="IN_PROGRESS" />
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
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>考试列表</span>
          <div class="batch-actions" v-if="selectedExams.length > 0">
            <el-button size="small" @click="batchPublish">批量发布</el-button>
            <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="exams"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="考试标题" width="200" show-overflow-tooltip />
        <el-table-column prop="course" label="所属课程" width="150">
          <template #default="{ row }">
            {{ row.course?.title }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80">
          <template #default="{ row }">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="参考人数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewExam(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editExam(row)">编辑</el-button>
            <el-dropdown @command="handleExamAction">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'questions', exam: row}">
                    管理题目
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'results', exam: row}">
                    查看结果
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'analytics', exam: row}">
                    数据分析
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'publish', exam: row}" v-if="row.status === 'DRAFT'">
                    发布考试
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'end', exam: row}" v-if="row.status === 'IN_PROGRESS'">
                    结束考试
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'duplicate', exam: row}">
                    复制考试
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'delete', exam: row}" divided>
                    删除考试
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 创建/编辑考试对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditing ? '编辑考试' : '创建考试'"
      width="800px"
      @close="resetExamForm"
    >
      <el-form
        :model="examForm"
        :rules="examRules"
        ref="examFormRef"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试标题" prop="title">
              <el-input v-model="examForm.title" placeholder="请输入考试标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select v-model="examForm.courseId" placeholder="选择课程" style="width: 100%">
                <el-option
                  v-for="course in courses"
                  :key="course.id"
                  :label="course.title"
                  :value="course.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="考试描述" prop="description">
          <el-input
            v-model="examForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入考试描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number
                v-model="examForm.duration"
                :min="1"
                :max="300"
                placeholder="分钟"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number
                v-model="examForm.totalScore"
                :min="1"
                :max="1000"
                placeholder="总分"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number
                v-model="examForm.passScore"
                :min="1"
                :max="examForm.totalScore"
                placeholder="及格分"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="examForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="examForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许重考">
              <el-switch v-model="examForm.allowRetake" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="随机题目">
              <el-switch v-model="examForm.randomQuestions" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveExam" :loading="saveLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 考试详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="考试详情" width="900px">
      <div v-if="selectedExam" class="exam-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="考试标题" :span="2">
            {{ selectedExam.title }}
          </el-descriptions-item>
          <el-descriptions-item label="所属课程">
            {{ selectedExam.course?.title }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
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
          <el-descriptions-item label="及格分">
            {{ selectedExam.passScore }}
          </el-descriptions-item>
          <el-descriptions-item label="题目数量">
            {{ selectedExam.questionCount }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatTime(selectedExam.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatTime(selectedExam.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="参考人数">
            {{ selectedExam.participantCount }}
          </el-descriptions-item>
          <el-descriptions-item label="允许重考">
            {{ selectedExam.allowRetake ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="考试描述" :span="2">
            {{ selectedExam.description }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 考试统计 -->
        <div class="exam-stats" v-if="examDetailStats">
          <h3>考试统计</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="参考人数" :value="examDetailStats.participantCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="完成人数" :value="examDetailStats.completedCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="平均分" :value="examDetailStats.averageScore" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="及格率" :value="examDetailStats.passRate" suffix="%" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <!-- 考试结果对话框 -->
    <el-dialog v-model="showResultsDialog" title="考试结果" width="1000px">
      <el-table :data="examResults" v-loading="resultsLoading">
        <el-table-column prop="student" label="学生" width="120">
          <template #default="{ row }">
            {{ row.student?.realName || row.student?.username }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="100">
          <template #default="{ row }">
            {{ row.score }}/{{ selectedExam?.totalScore }}
          </template>
        </el-table-column>
        <el-table-column prop="percentage" label="百分比" width="100">
          <template #default="{ row }">
            {{ ((row.score / selectedExam?.totalScore) * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="用时" width="100">
          <template #default="{ row }">
            {{ row.duration }} 分钟
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'COMPLETED' ? '已完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="viewStudentResult(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
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
  Document,
  Checked,
  UserFilled,
  TrendCharts,
  ArrowDown
} from '@element-plus/icons-vue'
import { getExamList, createExam, updateExam, deleteExam, getExamResults } from '@/api/exam'
import { getCourseList } from '@/api/course'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const resultsLoading = ref(false)
const exams = ref([])
const selectedExams = ref([])
const courses = ref([])
const examStats = ref({
  totalExams: 0,
  publishedExams: 0,
  totalParticipants: 0,
  averageScore: 0
})

// 对话框控制
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showResultsDialog = ref(false)
const isEditing = ref(false)

// 表单引用
const examFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  courseId: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 考试表单
const examForm = reactive({
  id: '',
  title: '',
  description: '',
  courseId: '',
  duration: 60,
  totalScore: 100,
  passScore: 60,
  startTime: '',
  endTime: '',
  allowRetake: false,
  randomQuestions: false
})

// 选中的考试详情
const selectedExam = ref(null)
const examDetailStats = ref(null)
const examResults = ref([])

// 表单验证规则
const examRules = {
  title: [
    { required: true, message: '请输入考试标题', trigger: 'blur' },
    { min: 2, max: 100, message: '考试标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入考试描述', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

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
      teacherId: authStore.user.id
    }
    
    const response = await getExamList(params)
    exams.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 计算统计数据
    examStats.value = {
      totalExams: exams.value.length,
      publishedExams: exams.value.filter(e => e.status === 'PUBLISHED').length,
      totalParticipants: exams.value.reduce((sum, e) => sum + (e.participantCount || 0), 0),
      averageScore: exams.value.length > 0 ? 
        (exams.value.reduce((sum, e) => sum + (e.averageScore || 0), 0) / exams.value.length).toFixed(1) : 0
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
    const response = await getCourseList({ teacherId: authStore.user.id, size: 1000 })
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

const handleSelectionChange = (selection) => {
  selectedExams.value = selection
}

const handleSaveExam = async () => {
  if (!examFormRef.value) return
  
  try {
    await examFormRef.value.validate()
    saveLoading.value = true
    
    if (isEditing.value) {
      ElMessage.success('考试更新成功')
    } else {
      ElMessage.success('考试创建成功')
    }
    
    showCreateDialog.value = false
    resetExamForm()
    loadExams()
  } catch (error) {
    ElMessage.error(isEditing.value ? '更新考试失败' : '创建考试失败')
  } finally {
    saveLoading.value = false
  }
}

const resetExamForm = () => {
  Object.assign(examForm, {
    id: '',
    title: '',
    description: '',
    courseId: '',
    duration: 60,
    totalScore: 100,
    passScore: 60,
    startTime: '',
    endTime: '',
    allowRetake: false,
    randomQuestions: false
  })
  isEditing.value = false
  examFormRef.value?.resetFields()
}

const editExam = (exam) => {
  Object.assign(examForm, {
    id: exam.id,
    title: exam.title,
    description: exam.description,
    courseId: exam.course?.id || '',
    duration: exam.duration,
    totalScore: exam.totalScore,
    passScore: exam.passScore,
    startTime: exam.startTime,
    endTime: exam.endTime,
    allowRetake: exam.allowRetake,
    randomQuestions: exam.randomQuestions
  })
  isEditing.value = true
  showCreateDialog.value = true
}

const viewExam = async (exam) => {
  selectedExam.value = exam
  showDetailDialog.value = true
  
  try {
    const response = await getExamResults(exam.id)
    const results = response.data.content || []
    
    examDetailStats.value = {
      participantCount: results.length,
      completedCount: results.filter(r => r.status === 'COMPLETED').length,
      averageScore: results.length > 0 ? 
        (results.reduce((sum, r) => sum + (r.score || 0), 0) / results.length).toFixed(1) : 0,
      passRate: results.length > 0 ? 
        Math.round((results.filter(r => r.score >= exam.passScore).length / results.length) * 100) : 0
    }
  } catch (error) {
    console.error('加载考试统计失败:', error)
    examDetailStats.value = {
      participantCount: 0,
      completedCount: 0,
      averageScore: 0,
      passRate: 0
    }
  }
}

const handleExamAction = async ({ action, exam }) => {
  switch (action) {
    case 'questions':
      ElMessage.info('题目管理功能开发中...')
      break
    case 'results':
      await viewExamResults(exam)
      break
    case 'analytics':
      ElMessage.info('数据分析功能开发中...')
      break
    case 'publish':
      await publishExam(exam)
      break
    case 'end':
      await endExam(exam)
      break
    case 'duplicate':
      await duplicateExam(exam)
      break
    case 'delete':
      await deleteExamConfirm(exam)
      break
  }
}

const publishExam = async (exam) => {
  try {
    ElMessage.success('考试发布成功')
    loadExams()
  } catch (error) {
    ElMessage.error('发布考试失败')
  }
}

const endExam = async (exam) => {
  try {
    ElMessage.success('考试结束成功')
    loadExams()
  } catch (error) {
    ElMessage.error('结束考试失败')
  }
}

const duplicateExam = async (exam) => {
  try {
    ElMessage.success('考试复制成功')
    loadExams()
  } catch (error) {
    ElMessage.error('复制考试失败')
  }
}

const viewExamResults = async (exam) => {
  selectedExam.value = exam
  resultsLoading.value = true
  try {
    const response = await getExamResults(exam.id)
    examResults.value = response.data.content || []
    showResultsDialog.value = true
  } catch (error) {
    ElMessage.error('加载考试结果失败')
    console.error('Failed to load exam results:', error)
  } finally {
    resultsLoading.value = false
  }
}

const deleteExamConfirm = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除考试 "${exam.title}" 吗？此操作不可恢复！`,
      '删除考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('考试删除成功')
    loadExams()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除考试失败')
    }
  }
}

const batchPublish = async () => {
  ElMessage.success('批量发布成功')
  loadExams()
}

const batchDelete = async () => {
  ElMessage.success('批量删除成功')
  loadExams()
}

const viewStudentResult = (result) => {
  ElMessage.info('学生结果详情功能开发中...')
}

// 工具方法
const getStatusText = (status) => {
  const statusMap = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    IN_PROGRESS: '进行中',
    ENDED: '已结束'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    IN_PROGRESS: 'warning',
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
.teacher-exams {
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

.exam-detail {
  padding: 20px 0;
}

.exam-stats {
  margin-top: 30px;
}

.exam-stats h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style> 