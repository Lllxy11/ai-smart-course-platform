<template>
  <div class="course-management">
    <div class="page-header">
      <h1>课程管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建课程
        </el-button>
        <el-button type="success" @click="exportCourses">
          <el-icon><Download /></el-icon>
          导出课程
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="搜索课程">
          <el-input
            v-model="searchForm.keyword"
            placeholder="课程名称或描述"
            @keyup.enter="handleSearch"
            style="width: 250px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="授课教师">
          <el-select v-model="searchForm.teacherId" placeholder="选择教师" clearable filterable>
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.fullName || teacher.username"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ courseStats.totalCourses }}</div>
            <div class="stat-label">总课程数</div>
          </div>
          <el-icon class="stat-icon"><Reading /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ courseStats.publishedCourses }}</div>
            <div class="stat-label">已发布课程</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ courseStats.totalEnrollments }}</div>
            <div class="stat-label">总选课人数</div>
          </div>
          <el-icon class="stat-icon"><UserFilled /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ courseStats.averageRating }}</div>
            <div class="stat-label">平均评分</div>
          </div>
          <el-icon class="stat-icon"><Star /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课程列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>课程列表</span>
          <div class="batch-actions" v-if="selectedCourses.length > 0">
            <el-button size="small" @click="batchPublish">批量发布</el-button>
            <el-button size="small" @click="batchArchive">批量归档</el-button>
            <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="courses"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover"
              style="width: 60px; height: 40px; border-radius: 4px;"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="课程名称" width="200" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="category" label="课程分类" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="选课人数" width="100" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate
              :model-value="row.rating || 0"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewCourse(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editCourse(row)">编辑</el-button>
            <el-dropdown @command="handleCourseAction">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'publish', course: row}" v-if="row.status === 'DRAFT'">
                    发布课程
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'archive', course: row}" v-if="row.status === 'PUBLISHED'">
                    归档课程
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'duplicate', course: row}">
                    复制课程
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'students', course: row}">
                    查看学生
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'analytics', course: row}">
                    数据分析
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'delete', course: row}" divided>
                    删除课程
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

    <!-- 创建/编辑课程对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditing ? '编辑课程' : '创建课程'"
      width="800px"
      @close="resetCourseForm"
    >
      <el-form
        :model="courseForm"
        :rules="courseRules"
        ref="courseFormRef"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-input v-model="courseForm.category" placeholder="请输入课程分类" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="courseForm.teacherId" placeholder="选择授课教师" filterable>
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.fullName || teacher.username"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程状态" prop="status">
              <el-select v-model="courseForm.status" placeholder="选择状态">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="已归档" value="ARCHIVED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学分" prop="credits">
              <el-input-number
                v-model="courseForm.credits"
                :min="0"
                :max="10"
                :step="0.5"
                placeholder="学分"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverUpload"
            :before-upload="beforeCoverUpload"
          >
            <img v-if="courseForm.coverImage" :src="courseForm.coverImage" class="cover-preview" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCourse" :loading="saveLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 课程详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="课程详情" width="900px">
      <div v-if="selectedCourse" class="course-detail">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-image
              :src="selectedCourse.coverImage"
              fit="cover"
              style="width: 100%; height: 200px; border-radius: 8px;"
            >
              <template #error>
                <div class="image-placeholder-large">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </el-col>
          <el-col :span="16">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="课程名称" :span="2">
                {{ selectedCourse.name }}
              </el-descriptions-item>
              <el-descriptions-item label="授课教师">
                {{ selectedCourse.teacherName }}
              </el-descriptions-item>
              <el-descriptions-item label="课程分类">
                {{ selectedCourse.category }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusTagType(selectedCourse.status)">
                  {{ getStatusText(selectedCourse.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="学分">
                {{ selectedCourse.credits }}
              </el-descriptions-item>
              <el-descriptions-item label="选课人数">
                {{ selectedCourse.studentCount }}
              </el-descriptions-item>
              <el-descriptions-item label="评分">
                <el-rate
                  :model-value="selectedCourse.rating || 0"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </el-descriptions-item>
              <el-descriptions-item label="创建时间" :span="2">
                {{ formatTime(selectedCourse.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="课程描述" :span="2">
                {{ selectedCourse.description }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <!-- 课程统计 -->
        <div class="course-stats" v-if="courseDetailStats">
          <h3>课程数据</h3>
          <el-row :gutter="20">
            <!-- 任务统计已删除 -->
            <el-col :span="6">
              <el-statistic title="平均完成率" :value="courseDetailStats.averageProgress" suffix="%" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="活跃学生" :value="courseDetailStats.activeStudents" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <!-- 课程学生列表对话框 -->
    <el-dialog v-model="showStudentsDialog" title="课程学生" width="800px">
      <el-table :data="courseStudents" v-loading="studentsLoading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="enrollmentDate" label="选课时间">
          <template #default="{ row }">
            {{ formatTime(row.enrollmentDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="学习进度">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="viewStudentProgress(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Download,
  Search,
  Reading,
  Checked,
  UserFilled,
  Star,
  Picture,
  ArrowDown
} from '@element-plus/icons-vue'
import { getCourseList, createCourse, updateCourse, deleteCourse, getCourseById, getCourseStudents, getCourseAnalytics, getCourseStatistics } from '@/api/course'
import { getUserList } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const studentsLoading = ref(false)
const courses = ref([])
const selectedCourses = ref([])
const teachers = ref([])
const courseStats = ref({
  totalCourses: 0,
  publishedCourses: 0,
  totalEnrollments: 0,
  averageRating: 0
})

// 对话框控制
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showStudentsDialog = ref(false)
const isEditing = ref(false)

// 表单引用
const courseFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  teacherId: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 课程表单
const courseForm = reactive({
  id: '',
  name: '',
  description: '',
  category: '',
  teacherId: '',
  status: 'DRAFT',
  credits: 1,
  coverImage: ''
})

// 选中的课程详情
const selectedCourse = ref(null)
const courseDetailStats = ref(null)
const courseStudents = ref([])

// 上传配置
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/upload/course-cover`)
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${authStore.token}`
}))

// 表单验证规则
const courseRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '课程名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '课程描述长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请输入课程分类', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择授课教师', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择课程状态', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  loadCourses()
  loadTeachers()
  loadCourseStats()
})

// 方法
const loadCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      teacherId: searchForm.teacherId,
      status: searchForm.status
    }
    const response = await getCourseList(params)
    courses.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

const loadTeachers = async () => {
  try {
    const response = await getUserList({ role: 'TEACHER', size: 1000 })
    teachers.value = response.data.content || []
  } catch (error) {
    console.error('加载教师列表失败:', error)
    ElMessage.error('加载教师列表失败')
    throw error
  }
}

const loadCourseStats = async () => {
  try {
    const response = await getCourseStatistics()
    courseStats.value = response.data || {
      totalCourses: 0,
      publishedCourses: 0,
      totalEnrollments: 0,
      averageRating: 0
    }
  } catch (error) {
    console.error('加载课程统计失败:', error)
    ElMessage.error('加载课程统计失败')
    throw error
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadCourses()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.teacherId = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadCourses()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadCourses()
}

const handleSelectionChange = (selection) => {
  selectedCourses.value = selection
}

const handleSaveCourse = async () => {
  if (!courseFormRef.value) return
  
  try {
    await courseFormRef.value.validate()
    saveLoading.value = true
    
    if (isEditing.value) {
      await updateCourse(courseForm.id, courseForm)
      ElMessage.success('课程更新成功')
    } else {
      await createCourse(courseForm)
      ElMessage.success('课程创建成功')
    }
    
    showCreateDialog.value = false
    resetCourseForm()
    loadCourses()
    loadCourseStats()
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error(isEditing.value ? '更新课程失败' : '创建课程失败')
    }
  } finally {
    saveLoading.value = false
  }
}

const resetCourseForm = () => {
  Object.assign(courseForm, {
    id: '',
    name: '',
    description: '',
    category: '',
    teacherId: '',
    status: 'DRAFT',
    credits: 1,
    coverImage: ''
  })
  isEditing.value = false
  courseFormRef.value?.resetFields()
}

const editCourse = (course) => {
  Object.assign(courseForm, {
    id: course.id,
    name: course.name,
    description: course.description,
    category: course.category,
    teacherId: course.teacherId,
    status: course.status,
    credits: course.credits,
    coverImage: course.coverImage
  })
  isEditing.value = true
  showCreateDialog.value = true
}

const viewCourse = async (course) => {
  selectedCourse.value = course
  showDetailDialog.value = true
  
  // 加载课程详细统计
  try {
    const response = await getCourseAnalytics(course.id)
    courseDetailStats.value = response.data
  } catch (error) {
    console.error('加载课程统计失败:', error)
  }
}

const handleCourseAction = async ({ action, course }) => {
  switch (action) {
    case 'publish':
      await publishCourse(course)
      break
    case 'archive':
      await archiveCourse(course)
      break
    case 'duplicate':
      await duplicateCourse(course)
      break
    case 'students':
      await viewCourseStudents(course)
      break
    case 'analytics':
      await viewCourseAnalytics(course)
      break
    case 'delete':
      await deleteCourseConfirm(course)
      break
  }
}

const publishCourse = async (course) => {
  try {
    await updateCourse(course.id, { ...course, status: 'PUBLISHED' })
    ElMessage.success('课程发布成功')
    loadCourses()
  } catch (error) {
    ElMessage.error('发布课程失败')
  }
}

const archiveCourse = async (course) => {
  try {
    await updateCourse(course.id, { ...course, status: 'ARCHIVED' })
    ElMessage.success('课程归档成功')
    loadCourses()
  } catch (error) {
    ElMessage.error('归档课程失败')
  }
}

const duplicateCourse = async (course) => {
  try {
    const newCourse = {
      ...course,
      name: `${course.name} (副本)`,
      status: 'DRAFT'
    }
    delete newCourse.id
    await createCourse(newCourse)
    ElMessage.success('课程复制成功')
    loadCourses()
  } catch (error) {
    ElMessage.error('复制课程失败')
  }
}

const viewCourseStudents = async (course) => {
  studentsLoading.value = true
  try {
    const response = await getCourseStudents(course.id)
    courseStudents.value = response.data || []
    showStudentsDialog.value = true
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    studentsLoading.value = false
  }
}

const viewCourseAnalytics = (course) => {
  ElMessage.info('课程分析功能开发中...')
}

const deleteCourseConfirm = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程 "${course.name}" 吗？此操作不可恢复！`,
      '删除课程',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteCourse(course.id)
    ElMessage.success('课程删除成功')
    loadCourses()
    loadCourseStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除课程失败')
    }
  }
}

const batchPublish = async () => {
  if (selectedCourses.value.length === 0) {
    ElMessage.warning('请选择要发布的课程')
    return
  }
  
  try {
    const promises = selectedCourses.value.map(course => 
      updateCourse(course.id, { ...course, status: 'PUBLISHED' })
    )
    await Promise.all(promises)
    ElMessage.success('批量发布成功')
    loadCourses()
  } catch (error) {
    ElMessage.error('批量发布失败')
  }
}

const batchArchive = async () => {
  if (selectedCourses.value.length === 0) {
    ElMessage.warning('请选择要归档的课程')
    return
  }
  
  try {
    const promises = selectedCourses.value.map(course => 
      updateCourse(course.id, { ...course, status: 'ARCHIVED' })
    )
    await Promise.all(promises)
    ElMessage.success('批量归档成功')
    loadCourses()
  } catch (error) {
    ElMessage.error('批量归档失败')
  }
}

const batchDelete = async () => {
  if (selectedCourses.value.length === 0) {
    ElMessage.warning('请选择要删除的课程')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCourses.value.length} 门课程吗？此操作不可恢复！`,
      '批量删除课程',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const promises = selectedCourses.value.map(course => deleteCourse(course.id))
    await Promise.all(promises)
    ElMessage.success('批量删除成功')
    loadCourses()
    loadCourseStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const exportCourses = () => {
  ElMessage.info('导出功能开发中...')
}

const viewStudentProgress = (student) => {
  ElMessage.info('学生进度详情功能开发中...')
}

// 上传相关方法
const handleCoverUpload = (response) => {
  if (response.success) {
    courseForm.coverImage = response.data.url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error('封面上传失败')
  }
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 工具方法
const getStatusText = (status) => {
  const statusMap = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ARCHIVED: '已归档'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    ARCHIVED: 'warning'
  }
  return typeMap[status] || ''
}

const formatTime = (time) => {
  if (!time) return '未设置'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.course-management {
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

.filter-card {
  margin-bottom: 20px;
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

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 40px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.image-placeholder-large {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 200px;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #909399;
  font-size: 48px;
}

.cover-preview {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 80px;
  text-align: center;
  line-height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.cover-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}

.course-detail {
  padding: 20px 0;
}

.course-stats {
  margin-top: 30px;
}

.course-stats h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style> 