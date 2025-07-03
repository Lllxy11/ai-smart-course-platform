<template>
  <div class="teacher-courses">
    <div class="page-header">
      <h1>我的课程</h1>
      <div class="header-actions">
        <el-button type="primary" @click="goToCreateCourse" size="large">
          <el-icon><Plus /></el-icon>
          创建课程
        </el-button>
        <el-button @click="forceRefresh" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 课程统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ courseStats.totalCourses }}</div>
            <div class="stat-label">我的课程</div>
          </div>
          <el-icon class="stat-icon"><Reading /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ courseStats.publishedCourses }}</div>
            <div class="stat-label">已发布</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ courseStats.totalStudents }}</div>
            <div class="stat-label">总学生数</div>
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

    <!-- 课程列表 -->
    <div class="courses-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="course in courses" :key="course.id">
          <el-card 
            class="course-card"
            :class="{ 'highlighted': course.id == highlightCourseId }"
            shadow="hover"
            :ref="course.id == highlightCourseId ? 'highlightedCard' : null"
          >
            <div class="course-cover">
              <el-image
                :src="course.coverImage"
                fit="cover"
                style="width: 100%; height: 180px;"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="course-status">
                <el-tag :type="getStatusTagType(course.status)" size="small">
                  {{ getStatusText(course.status) }}
                </el-tag>
              </div>
            </div>
            
            <div class="course-content">
              <h3 class="course-title">{{ course.name || course.title }}</h3>
              <p class="course-description">{{ course.description }}</p>
              
              <div class="course-meta">
                <div class="meta-item">
                  <el-icon><User /></el-icon>
                  <span>{{ course.studentCount || 0 }} 学生</span>
                </div>
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ course.semester || '2024春' }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Trophy /></el-icon>
                  <span>{{ course.credits || 0 }} 学分</span>
                </div>
              </div>
              
              <div class="course-actions">
                <el-button size="small" @click="viewCourse(course)">查看详情</el-button>
                <el-button size="small" type="primary" @click="editCourse(course)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteCourseConfirm(course)">删除</el-button>
                <el-button size="small" type="success" @click="openTaskDialog(course)">布置作业</el-button>
                <el-button size="small" type="info" @click="goToCourseDetail(course)">进入课程</el-button>
              </div>
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

    <!-- 编辑课程对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑课程"
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
              <el-input 
                v-model="courseForm.name" 
                @input="courseForm.title = courseForm.name"
                placeholder="请输入课程名称" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-input v-model="courseForm.category" placeholder="请输入课程分类" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="课程状态" prop="status">
              <el-select v-model="courseForm.status" placeholder="选择状态">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="已归档" value="ARCHIVED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学分" prop="credits">
              <el-input-number
                v-model="courseForm.credits"
                :min="0.5"
                :max="10"
                :step="0.5"
                placeholder="学分"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学期" prop="semester">
              <el-select v-model="courseForm.semester" placeholder="选择学期">
                <el-option label="2024春" value="2024春" />
                <el-option label="2024夏" value="2024夏" />
                <el-option label="2024秋" value="2024秋" />
                <el-option label="2024冬" value="2024冬" />
                <el-option label="2025春" value="2025春" />
              </el-select>
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
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateCourse" :loading="saveLoading">
          更新
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
                {{ selectedCourse.name || selectedCourse.title }}
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
                {{ selectedCourse.studentCount || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="学期">
                {{ selectedCourse.semester || '2024春' }}
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

    <!-- 布置作业对话框 -->
    <el-dialog v-model="showTaskDialog" title="布置新作业" width="500px">
      <el-form :model="taskForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="taskForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="taskForm.description" type="textarea" placeholder="请输入作业描述" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="taskForm.startDate" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="taskForm.dueDate" type="datetime" placeholder="选择截止时间" />
        </el-form-item>
        <el-form-item label="满分">
          <el-input v-model="taskForm.maxScore" type="number" placeholder="请输入满分" />
        </el-form-item>
        <el-form-item label="要求">
          <el-input v-model="taskForm.requirements" type="textarea" placeholder="作业要求（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="submitTask">提交</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  Reading,
  Checked,
  UserFilled,
  Star,
  Picture,
  User,
  Calendar,
  Trophy
} from '@element-plus/icons-vue'
import { getCourseList, createCourse, updateCourse, deleteCourse, getCourseById, getCourseAnalytics } from '@/api/course'
import { useAuthStore } from '@/stores/auth'
import { useRouter, useRoute } from 'vue-router'
import { createTask } from '@/api/task'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

// 高亮课程ID（从路由参数获取）
const highlightCourseId = ref(null)

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const courses = ref([])
const courseStats = ref({
  totalCourses: 0,
  publishedCourses: 0,
  totalStudents: 0,
  averageRating: 0
})

// 对话框控制
const showEditDialog = ref(false)
const showDetailDialog = ref(false)
const showTaskDialog = ref(false)

// 表单引用
const courseFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 9,
  total: 0
})

// 课程表单
const courseForm = reactive({
  id: '',
  name: '', // 改为name，匹配后端API
  title: '', // 保留title用于显示
  description: '',
  category: '',
  status: 'DRAFT',
  semester: '2024春', // 添加学期字段
  credits: 1.0, // 明确为浮点数
  coverImage: ''
})

// 选中的课程详情
const selectedCourse = ref(null)
const courseDetailStats = ref(null)

// 上传配置
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/upload/course-cover`)
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${authStore.token}`
}))

// 表单验证规则 - 简化验证以提高性能
const courseRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 255, message: '课程名称长度在 2 到 255 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择课程状态', trigger: 'change' }
  ],
  semester: [
    { required: true, message: '请选择学期', trigger: 'change' }
  ]
}

// 表单引用
const taskForm = ref({
  title: '',
  description: '',
  startDate: '',
  dueDate: '',
  maxScore: 100,
  requirements: '',
  courseId: null
})
const currentCourse = ref(null)

// 生命周期
onMounted(() => {
  // 获取路由参数中的课程ID
  if (route.query.courseId) {
    highlightCourseId.value = route.query.courseId
    console.log('从仪表板跳转，高亮课程ID:', highlightCourseId.value)
  }
  
  loadCourses()
  
  // 设置定时刷新，每30秒刷新一次课程数据
  const refreshInterval = setInterval(() => {
    if (!loading.value && !saveLoading.value) {
      loadCourses()
    }
  }, 30000)
  
  // 组件卸载时清理定时器
  onUnmounted(() => {
    clearInterval(refreshInterval)
  })
})

// 方法
const loadCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status,
      teacherId: authStore.user.id // 只加载当前教师的课程
    }
    
    console.log('开始加载课程列表，参数：', params)
    const response = await getCourseList(params)
    console.log('课程列表响应：', response.data)
    
    if (response.data && response.data.content) {
      courses.value = response.data.content.map(course => ({
        ...course,
        // 确保studentCount字段正确映射
        studentCount: course.studentCount || 0,
        // 兼容处理
        enrollmentCount: course.studentCount || 0
      }))
      pagination.total = response.data.totalElements || 0
      
      console.log('处理后的课程数据：', courses.value)
      
      // 计算统计数据
      courseStats.value = {
        totalCourses: courses.value.length,
        publishedCourses: courses.value.filter(c => c.status === 'PUBLISHED').length,
        totalStudents: courses.value.reduce((sum, c) => sum + (c.studentCount || 0), 0),
        averageRating: courses.value.length > 0 ? 
          (courses.value.reduce((sum, c) => sum + (c.rating || 0), 0) / courses.value.length).toFixed(1) : 0
      }
    } else {
      courses.value = []
      pagination.total = 0
      courseStats.value = {
        totalCourses: 0,
        publishedCourses: 0,
        totalStudents: 0,
        averageRating: 0
      }
    }
    
    console.log('统计数据：', courseStats.value)
    
    // 如果有高亮课程ID，滚动到对应课程
    if (highlightCourseId.value) {
      await nextTick()
      scrollToHighlightedCourse()
    }
    
  } catch (error) {
    console.error('加载课程列表失败：', error)
    ElMessage.error('加载课程列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const scrollToHighlightedCourse = () => {
  const highlightedCourseExists = courses.value.some(course => course.id == highlightCourseId.value)
  if (highlightedCourseExists) {
    // 查找高亮的课程卡片
    const highlightedElement = document.querySelector('.course-card.highlighted')
    if (highlightedElement) {
      // 平滑滚动到高亮的课程
      highlightedElement.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
      
      // 显示提示消息
      ElMessage.success('已定位到选中的课程')
      
      // 3秒后移除高亮效果
      setTimeout(() => {
        highlightCourseId.value = null
        // 清除URL中的query参数
        router.replace({ path: '/teacher/courses' })
      }, 3000)
    }
  } else if (highlightCourseId.value) {
    // 课程不存在，清除高亮
    ElMessage.warning('未找到指定的课程')
    highlightCourseId.value = null
    router.replace({ path: '/teacher/courses' })
  }
}

const forceRefresh = async () => {
  console.log('强制刷新课程列表')
  ElMessage.info('正在刷新课程数据...')
  await loadCourses()
  ElMessage.success('课程数据已更新')
}

const handleSearch = () => {
  pagination.page = 1
  loadCourses()
}

const resetSearch = () => {
  searchForm.keyword = ''
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

const handleUpdateCourse = async () => {
  if (!courseFormRef.value) return
  
  try {
    await courseFormRef.value.validate()
    saveLoading.value = true
    
    // 显示操作提示
    ElMessage.info('正在更新课程...')
    
    // 处理封面图片URL
    let coverImageUrl = courseForm.coverImage || ''
    if (coverImageUrl && !coverImageUrl.startsWith('http')) {
      // 如果是相对路径，转换为完整URL
      coverImageUrl = `${window.location.origin}${coverImageUrl}`
    }

    // 准备发送到后端的数据，匹配后端API字段
    const courseData = {
      name: courseForm.name,
      description: courseForm.description || '',
      category: courseForm.category || '',
      status: courseForm.status,
      semester: courseForm.semester || '2024春',
      credits: Number(courseForm.credits),
      coverImage: coverImageUrl,
      teacherId: authStore.user.id,
      year: new Date().getFullYear(),
      difficulty: 'intermediate',
      price: 0.0,
      maxStudents: 50
    }

    const courseResponse = await updateCourse(courseForm.id, courseData)
    ElMessage.success('课程更新成功')
    
    // 更新列表中的课程数据，避免重新加载
    const index = courses.value.findIndex(c => c.id === courseForm.id)
    if (index !== -1) {
      courses.value[index] = courseResponse.data
    }
    
    showEditDialog.value = false
    resetCourseForm()
  } catch (error) {
    console.error('更新课程失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.response?.data?.errors) {
      // 处理验证错误
      const errors = error.response.data.errors
      const errorMessages = Object.values(errors).flat()
      ElMessage.error(`验证失败: ${errorMessages.join(', ')}`)
    } else {
      ElMessage.error('更新课程失败')
    }
  } finally {
    saveLoading.value = false
  }
}

const resetCourseForm = () => {
  Object.assign(courseForm, {
    id: '',
    name: '',
    title: '',
    description: '',
    category: '',
    status: 'DRAFT',
    semester: '2024春',
    credits: 1.0,
    coverImage: ''
  })
  courseFormRef.value?.resetFields()
}

const editCourse = (course) => {
  Object.assign(courseForm, {
    id: course.id,
    name: course.title || course.name || '',
    title: course.title || course.name || '',
    description: course.description || '',
    category: course.category || '',
    status: course.status || 'DRAFT',
    semester: course.semester || '2024春',
    credits: Number(course.credits) || 1.0,
    coverImage: course.coverImage || ''
  })
  showEditDialog.value = true
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

const deleteCourseConfirm = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程 "${course.name || course.title}" 吗？此操作不可恢复！`,
      '删除课程',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteCourse(course.id)
    ElMessage.success('课程删除成功')
    
    // 直接从列表中移除，避免重新加载
    const index = courses.value.findIndex(c => c.id === course.id)
    if (index !== -1) {
      courses.value.splice(index, 1)
      pagination.total -= 1
      
      // 更新统计数据
      courseStats.value.totalCourses -= 1
      if (course.status === 'PUBLISHED') {
        courseStats.value.publishedCourses -= 1
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除课程失败')
    }
  }
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

const goToCreateCourse = () => {
  router.push('/teacher/courses/create')
}

function openTaskDialog(course) {
  currentCourse.value = course
  taskForm.value = {
    title: '',
    description: '',
    startDate: '',
    dueDate: '',
    maxScore: 100,
    requirements: '',
    courseId: course.id
  }
  showTaskDialog.value = true
}

async function submitTask() {
  try {
    await createTask(taskForm.value)
    ElMessage.success('作业布置成功！')
    showTaskDialog.value = false
  } catch (e) {
    ElMessage.error('作业布置失败')
  }
}

function goToCourseDetail(course) {
  router.push(`/teacher/courses/${course.id}`)
}
</script>

<style scoped>
.teacher-courses {
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

.courses-grid {
  margin-bottom: 20px;
}

.course-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
  height: 420px;
}

.course-card:hover {
  transform: translateY(-5px);
}

/* 高亮课程样式 */
.course-card.highlighted {
  border: 2px solid #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  animation: highlight-pulse 2s ease-in-out infinite;
}

@keyframes highlight-pulse {
  0% {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }
  50% {
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
  }
  100% {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }
}

.course-cover {
  position: relative;
}

.course-status {
  position: absolute;
  top: 10px;
  right: 10px;
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 180px;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 48px;
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

.course-content {
  padding: 15px;
}

.course-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-description {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 12px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.course-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
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