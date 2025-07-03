<template>
  <div class="question-management">
    <div class="page-header">
      <h1>题目管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建题目
        </el-button>
        <el-button type="success" @click="importQuestions">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button type="info" @click="exportQuestions">
          <el-icon><Download /></el-icon>
          导出题目
        </el-button>
      </div>
    </div>

    <!-- 题目统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ questionStats.totalQuestions }}</div>
            <div class="stat-label">总题目数</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ questionStats.singleChoice }}</div>
            <div class="stat-label">单选题</div>
          </div>
          <el-icon class="stat-icon"><Check /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ questionStats.multipleChoice }}</div>
            <div class="stat-label">多选题</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ questionStats.essay }}</div>
            <div class="stat-label">问答题</div>
          </div>
          <el-icon class="stat-icon"><Edit /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="搜索题目">
          <el-input
            v-model="searchForm.keyword"
            placeholder="题目内容或标签"
            @keyup.enter="handleSearch"
            style="width: 250px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="判断题" value="TRUE_FALSE" />
            <el-option label="填空题" value="FILL_BLANK" />
            <el-option label="问答题" value="ESSAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="searchForm.difficulty" placeholder="选择难度" clearable>
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属科目">
          <el-select v-model="searchForm.subject" placeholder="选择科目" clearable>
            <el-option
              v-for="subject in subjects"
              :key="subject.value"
              :label="subject.label"
              :value="subject.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建人">
          <el-select v-model="searchForm.creatorId" placeholder="选择创建人" clearable filterable>
            <el-option
              v-for="creator in creators"
              :key="creator.id"
              :label="creator.realName || creator.username"
              :value="creator.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 题目列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>题目列表</span>
          <div class="batch-actions" v-if="selectedQuestions.length > 0">
            <el-button size="small" @click="batchDelete">批量删除</el-button>
            <el-button size="small" @click="batchExport">批量导出</el-button>
            <el-button size="small" @click="batchSetDifficulty">批量设置难度</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="questions"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="question-content">
              <div class="question-text">{{ row.content }}</div>
              <div class="question-tags" v-if="row.tags && row.tags.length > 0">
                <el-tag
                  v-for="tag in row.tags"
                  :key="tag"
                  size="small"
                  style="margin-right: 5px;"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.difficulty)" size="small">
              {{ getDifficultyText(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" width="120" />
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="creator" label="创建人" width="120">
          <template #default="{ row }">
            {{ row.creator?.realName || row.creator?.username }}
          </template>
        </el-table-column>
        <el-table-column prop="usageCount" label="使用次数" width="100" />
        <el-table-column prop="correctRate" label="正确率" width="100">
          <template #default="{ row }">
            {{ row.correctRate !== null ? `${row.correctRate}%` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewQuestion(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editQuestion(row)">编辑</el-button>
            <el-dropdown @command="handleQuestionAction">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'duplicate', question: row}">
                    复制题目
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'statistics', question: row}">
                    答题统计
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'export', question: row}">
                    导出题目
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'delete', question: row}" divided>
                    删除题目
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

    <!-- 创建/编辑题目对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditing ? '编辑题目' : '创建题目'"
      width="900px"
      @close="resetQuestionForm"
    >
      <el-form
        :model="questionForm"
        :rules="questionRules"
        ref="questionFormRef"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题目类型" prop="type">
              <el-select v-model="questionForm.type" placeholder="选择类型" @change="handleTypeChange">
                <el-option label="单选题" value="SINGLE_CHOICE" />
                <el-option label="多选题" value="MULTIPLE_CHOICE" />
                <el-option label="判断题" value="TRUE_FALSE" />
                <el-option label="填空题" value="FILL_BLANK" />
                <el-option label="问答题" value="ESSAY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficulty">
              <el-select v-model="questionForm.difficulty" placeholder="选择难度">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属科目" prop="subject">
              <el-select v-model="questionForm.subject" placeholder="选择科目" filterable allow-create>
                <el-option
                  v-for="subject in subjects"
                  :key="subject.value"
                  :label="subject.label"
                  :value="subject.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分值" prop="score">
              <el-input-number
                v-model="questionForm.score"
                :min="1"
                :max="100"
                placeholder="分值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <!-- 选择题选项 -->
        <div v-if="isChoiceQuestion" class="options-section">
          <el-form-item label="选项设置">
            <div v-for="(option, index) in questionForm.options" :key="index" class="option-item">
              <el-row :gutter="10">
                <el-col :span="2">
                  <el-checkbox
                    v-if="questionForm.type === 'MULTIPLE_CHOICE'"
                    v-model="option.isCorrect"
                  >
                    {{ String.fromCharCode(65 + index) }}
                  </el-checkbox>
                  <el-radio
                    v-else
                    v-model="selectedCorrectOption"
                    :label="index"
                  >
                    {{ String.fromCharCode(65 + index) }}
                  </el-radio>
                </el-col>
                <el-col :span="20">
                  <el-input
                    v-model="option.content"
                    placeholder="请输入选项内容"
                  />
                </el-col>
                <el-col :span="2">
                  <el-button
                    size="small"
                    type="danger"
                    @click="removeOption(index)"
                    :disabled="questionForm.options.length <= 2"
                  >
                    删除
                  </el-button>
                </el-col>
              </el-row>
            </div>
            <el-button @click="addOption" :disabled="questionForm.options.length >= 6">
              添加选项
            </el-button>
          </el-form-item>
        </div>

        <!-- 判断题 -->
        <div v-if="questionForm.type === 'TRUE_FALSE'" class="true-false-section">
          <el-form-item label="正确答案" prop="correctAnswer">
            <el-radio-group v-model="questionForm.correctAnswer">
              <el-radio label="true">正确</el-radio>
              <el-radio label="false">错误</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <!-- 填空题 -->
        <div v-if="questionForm.type === 'FILL_BLANK'" class="fill-blank-section">
          <el-form-item label="参考答案" prop="referenceAnswer">
            <el-input
              v-model="questionForm.referenceAnswer"
              type="textarea"
              :rows="2"
              placeholder="请输入参考答案，多个答案用分号分隔"
            />
          </el-form-item>
        </div>

        <!-- 问答题 -->
        <div v-if="questionForm.type === 'ESSAY'" class="essay-section">
          <el-form-item label="参考答案" prop="referenceAnswer">
            <el-input
              v-model="questionForm.referenceAnswer"
              type="textarea"
              :rows="4"
              placeholder="请输入参考答案"
            />
          </el-form-item>
          <el-form-item label="评分标准">
            <el-input
              v-model="questionForm.gradingCriteria"
              type="textarea"
              :rows="3"
              placeholder="请输入评分标准"
            />
          </el-form-item>
        </div>

        <el-form-item label="解析">
          <el-input
            v-model="questionForm.explanation"
            type="textarea"
            :rows="3"
            placeholder="请输入题目解析（可选）"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-select
            v-model="questionForm.tags"
            multiple
            filterable
            allow-create
            placeholder="添加标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in commonTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveQuestion" :loading="saveLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="题目详情" width="800px">
      <div v-if="selectedQuestion" class="question-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目类型">
            <el-tag :type="getTypeTagType(selectedQuestion.type)">
              {{ getTypeText(selectedQuestion.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="getDifficultyTagType(selectedQuestion.difficulty)">
              {{ getDifficultyText(selectedQuestion.difficulty) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属科目">
            {{ selectedQuestion.subject }}
          </el-descriptions-item>
          <el-descriptions-item label="分值">
            {{ selectedQuestion.score }}
          </el-descriptions-item>
          <el-descriptions-item label="创建人">
            {{ selectedQuestion.creator?.realName || selectedQuestion.creator?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="使用次数">
            {{ selectedQuestion.usageCount }}
          </el-descriptions-item>
          <el-descriptions-item label="正确率">
            {{ selectedQuestion.correctRate !== null ? `${selectedQuestion.correctRate}%` : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(selectedQuestion.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="题目内容" :span="2">
            {{ selectedQuestion.content }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 选项显示 -->
        <div v-if="selectedQuestion.options && selectedQuestion.options.length > 0" class="options-display">
          <h3>选项</h3>
          <div v-for="(option, index) in selectedQuestion.options" :key="index" class="option-display">
            <span class="option-label" :class="{ correct: option.isCorrect }">
              {{ String.fromCharCode(65 + index) }}
            </span>
            <span class="option-content">{{ option.content }}</span>
            <el-tag v-if="option.isCorrect" type="success" size="small">正确答案</el-tag>
          </div>
        </div>

        <!-- 答案显示 -->
        <div v-if="selectedQuestion.correctAnswer || selectedQuestion.referenceAnswer" class="answer-display">
          <h3>答案</h3>
          <div class="answer-content">
            {{ selectedQuestion.correctAnswer || selectedQuestion.referenceAnswer }}
          </div>
        </div>

        <!-- 解析显示 -->
        <div v-if="selectedQuestion.explanation" class="explanation-display">
          <h3>解析</h3>
          <div class="explanation-content">
            {{ selectedQuestion.explanation }}
          </div>
        </div>

        <!-- 标签显示 -->
        <div v-if="selectedQuestion.tags && selectedQuestion.tags.length > 0" class="tags-display">
          <h3>标签</h3>
          <el-tag
            v-for="tag in selectedQuestion.tags"
            :key="tag"
            style="margin-right: 10px;"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- 批量设置难度对话框 -->
    <el-dialog v-model="showBatchDifficultyDialog" title="批量设置难度" width="400px">
      <el-form>
        <el-form-item label="难度等级">
          <el-select v-model="batchDifficulty" placeholder="选择难度" style="width: 100%">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchDifficultyDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchSetDifficulty">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Upload,
  Download,
  Search,
  Document,
  Check,
  Checked,
  Edit,
  ArrowDown
} from '@element-plus/icons-vue'
import { getQuestionList, createQuestion, updateQuestion, deleteQuestion } from '@/api/question'
import { getUserList } from '@/api/user'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const questions = ref([])
const selectedQuestions = ref([])
const creators = ref([])
const questionStats = ref({
  totalQuestions: 0,
  singleChoice: 0,
  multipleChoice: 0,
  essay: 0
})

// 对话框控制
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showBatchDifficultyDialog = ref(false)
const isEditing = ref(false)

// 表单引用
const questionFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: '',
  difficulty: '',
  subject: '',
  creatorId: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 题目表单
const questionForm = reactive({
  id: '',
  type: '',
  difficulty: '',
  subject: '',
  score: 5,
  content: '',
  options: [],
  correctAnswer: '',
  referenceAnswer: '',
  gradingCriteria: '',
  explanation: '',
  tags: []
})

// 选中的正确选项（单选题用）
const selectedCorrectOption = ref(0)

// 批量操作
const batchDifficulty = ref('')

// 选中的题目详情
const selectedQuestion = ref(null)

// 科目列表
const subjects = [
  { label: 'Python编程', value: 'python' },
  { label: '数据结构', value: 'data_structure' },
  { label: 'Web开发', value: 'web_development' },
  { label: '数据库', value: 'database' },
  { label: '算法', value: 'algorithm' }
]

// 常用标签
const commonTags = ['基础', '进阶', '重点', '难点', '易错', '必考']

// 计算属性
const isChoiceQuestion = computed(() => {
  return ['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(questionForm.type)
})

// 表单验证规则
const questionRules = {
  type: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ],
  subject: [
    { required: true, message: '请选择所属科目', trigger: 'change' }
  ],
  score: [
    { required: true, message: '请输入分值', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { min: 10, message: '题目内容至少10个字符', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadQuestions()
  loadCreators()
})

// 方法
const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      type: searchForm.type,
      difficulty: searchForm.difficulty,
      subject: searchForm.subject,
      creatorId: searchForm.creatorId
    }
    
    const response = await getQuestionList(params)
    questions.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 计算统计数据
    questionStats.value = {
      totalQuestions: questions.value.length,
      singleChoice: questions.value.filter(q => q.type === 'SINGLE_CHOICE').length,
      multipleChoice: questions.value.filter(q => q.type === 'MULTIPLE_CHOICE').length,
      essay: questions.value.filter(q => q.type === 'ESSAY').length
    }
  } catch (error) {
    ElMessage.error('加载题目列表失败')
    console.error('Failed to load questions:', error)
  } finally {
    loading.value = false
  }
}

const loadCreators = async () => {
  try {
    const response = await getUserList({ role: 'TEACHER', size: 1000 })
    creators.value = response.data.content || []
  } catch (error) {
    console.error('加载创建人列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadQuestions()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.difficulty = ''
  searchForm.subject = ''
  searchForm.creatorId = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadQuestions()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadQuestions()
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const handleTypeChange = () => {
  // 重置选项
  questionForm.options = []
  questionForm.correctAnswer = ''
  questionForm.referenceAnswer = ''
  selectedCorrectOption.value = 0
  
  // 根据题目类型初始化选项
  if (isChoiceQuestion.value) {
    questionForm.options = [
      { content: '', isCorrect: false },
      { content: '', isCorrect: false }
    ]
    if (questionForm.type === 'SINGLE_CHOICE') {
      questionForm.options[0].isCorrect = true
    }
  }
}

const addOption = () => {
  questionForm.options.push({ content: '', isCorrect: false })
}

const removeOption = (index) => {
  questionForm.options.splice(index, 1)
  if (selectedCorrectOption.value >= questionForm.options.length) {
    selectedCorrectOption.value = 0
  }
}

const handleSaveQuestion = async () => {
  if (!questionFormRef.value) return
  
  try {
    await questionFormRef.value.validate()
    
    // 处理单选题的正确答案
    if (questionForm.type === 'SINGLE_CHOICE') {
      questionForm.options.forEach((option, index) => {
        option.isCorrect = index === selectedCorrectOption.value
      })
    }
    
    saveLoading.value = true
    
    if (isEditing.value) {
      await updateQuestion(questionForm.id, questionForm)
      ElMessage.success('题目更新成功')
    } else {
      await createQuestion(questionForm)
      ElMessage.success('题目创建成功')
    }
    
    showCreateDialog.value = false
    resetQuestionForm()
    loadQuestions()
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error(isEditing.value ? '更新题目失败' : '创建题目失败')
    }
  } finally {
    saveLoading.value = false
  }
}

const resetQuestionForm = () => {
  Object.assign(questionForm, {
    id: '',
    type: '',
    difficulty: '',
    subject: '',
    score: 5,
    content: '',
    options: [],
    correctAnswer: '',
    referenceAnswer: '',
    gradingCriteria: '',
    explanation: '',
    tags: []
  })
  selectedCorrectOption.value = 0
  isEditing.value = false
  questionFormRef.value?.resetFields()
}

const editQuestion = (question) => {
  Object.assign(questionForm, {
    id: question.id,
    type: question.type,
    difficulty: question.difficulty,
    subject: question.subject,
    score: question.score,
    content: question.content,
    options: question.options ? [...question.options] : [],
    correctAnswer: question.correctAnswer || '',
    referenceAnswer: question.referenceAnswer || '',
    gradingCriteria: question.gradingCriteria || '',
    explanation: question.explanation || '',
    tags: question.tags ? [...question.tags] : []
  })
  
  // 设置单选题的正确选项
  if (question.type === 'SINGLE_CHOICE' && question.options) {
    const correctIndex = question.options.findIndex(option => option.isCorrect)
    selectedCorrectOption.value = correctIndex >= 0 ? correctIndex : 0
  }
  
  isEditing.value = true
  showCreateDialog.value = true
}

const viewQuestion = (question) => {
  selectedQuestion.value = question
  showDetailDialog.value = true
}

const handleQuestionAction = async ({ action, question }) => {
  switch (action) {
    case 'duplicate':
      await duplicateQuestion(question)
      break
    case 'statistics':
      ElMessage.info('答题统计功能开发中...')
      break
    case 'export':
      await exportSingleQuestion(question)
      break
    case 'delete':
      await deleteQuestionConfirm(question)
      break
  }
}

const duplicateQuestion = async (question) => {
  try {
    const newQuestion = {
      ...question,
      content: `${question.content} (副本)`
    }
    delete newQuestion.id
    await createQuestion(newQuestion)
    ElMessage.success('题目复制成功')
    loadQuestions()
  } catch (error) {
    ElMessage.error('复制题目失败')
  }
}

const exportSingleQuestion = (question) => {
  ElMessage.info('导出单个题目功能开发中...')
}

const deleteQuestionConfirm = async (question) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除题目 "${question.content.substring(0, 20)}..." 吗？此操作不可恢复！`,
      '删除题目',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteQuestion(question.id)
    ElMessage.success('题目删除成功')
    loadQuestions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除题目失败')
    }
  }
}

const batchDelete = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedQuestions.value.length} 个题目吗？此操作不可恢复！`,
      '批量删除题目',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const promises = selectedQuestions.value.map(question => deleteQuestion(question.id))
    await Promise.all(promises)
    ElMessage.success('批量删除成功')
    loadQuestions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const batchExport = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要导出的题目')
    return
  }
  ElMessage.info('批量导出功能开发中...')
}

const batchSetDifficulty = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要设置难度的题目')
    return
  }
  showBatchDifficultyDialog.value = true
}

const confirmBatchSetDifficulty = async () => {
  if (!batchDifficulty.value) {
    ElMessage.warning('请选择难度等级')
    return
  }
  
  try {
    const promises = selectedQuestions.value.map(question => 
      updateQuestion(question.id, { ...question, difficulty: batchDifficulty.value })
    )
    await Promise.all(promises)
    ElMessage.success('批量设置难度成功')
    showBatchDifficultyDialog.value = false
    batchDifficulty.value = ''
    loadQuestions()
  } catch (error) {
    ElMessage.error('批量设置难度失败')
  }
}

const importQuestions = () => {
  ElMessage.info('批量导入功能开发中...')
}

const exportQuestions = () => {
  ElMessage.info('导出题目功能开发中...')
}

// 工具方法
const getTypeText = (type) => {
  const typeMap = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    FILL_BLANK: '填空题',
    ESSAY: '问答题'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    SINGLE_CHOICE: '',
    MULTIPLE_CHOICE: 'success',
    TRUE_FALSE: 'info',
    FILL_BLANK: 'warning',
    ESSAY: 'danger'
  }
  return typeMap[type] || ''
}

const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return difficultyMap[difficulty] || difficulty
}

const getDifficultyTagType = (difficulty) => {
  const typeMap = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'danger'
  }
  return typeMap[difficulty] || ''
}

const formatTime = (time) => {
  if (!time) return '未设置'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.question-management {
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

.question-content {
  line-height: 1.5;
}

.question-text {
  margin-bottom: 8px;
  color: #303133;
}

.question-tags {
  margin-top: 5px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.options-section {
  margin: 20px 0;
}

.option-item {
  margin-bottom: 10px;
}

.question-detail {
  padding: 20px 0;
}

.options-display {
  margin-top: 20px;
}

.options-display h3 {
  margin-bottom: 15px;
  color: #303133;
}

.option-display {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.option-label {
  display: inline-block;
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  background-color: #e4e7ed;
  border-radius: 50%;
  margin-right: 15px;
  font-weight: bold;
}

.option-label.correct {
  background-color: #67c23a;
  color: white;
}

.option-content {
  flex: 1;
  margin-right: 15px;
}

.answer-display,
.explanation-display,
.tags-display {
  margin-top: 20px;
}

.answer-display h3,
.explanation-display h3,
.tags-display h3 {
  margin-bottom: 15px;
  color: #303133;
}

.answer-content,
.explanation-content {
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 4px;
  line-height: 1.6;
}
</style> 