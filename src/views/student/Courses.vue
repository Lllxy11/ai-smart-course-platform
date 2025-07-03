<template>
  <div class="student-courses">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">我的课程</h1>
      <p class="page-description">管理您的课程学习进度和内容</p>
    </div>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="8" :md="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程..."
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :xs="12" :sm="4" :md="3">
          <el-select v-model="selectedStatus" placeholder="状态" clearable @change="handleFilterChange">
            <el-option label="全部课程" value="" />
            <el-option label="已选课程" value="enrolled" />
            <el-option label="可选课程" value="available" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="4" :md="3">
          <el-select v-model="selectedCategory" placeholder="分类">
            <el-option label="全部" value="" />
            <el-option label="计算机科学" value="computer-science" />
            <el-option label="数学" value="mathematics" />
            <el-option label="物理" value="physics" />
            <el-option label="工程" value="engineering" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="8" :md="12">
          <div class="view-controls">
            <el-button-group>
              <el-button
                :type="viewMode === 'grid' ? 'primary' : ''"
                :icon="Grid"
                @click="viewMode = 'grid'"
              />
              <el-button
                :type="viewMode === 'list' ? 'primary' : ''"
                :icon="List"
                @click="viewMode = 'list'"
              />
            </el-button-group>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 课程列表 -->
    <div v-loading="loading" class="courses-container">
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="grid-view">
        <el-row :gutter="24">
          <el-col
            v-for="course in filteredCourses"
            :key="course.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-card class="course-card" shadow="hover" @click="viewCourse(course.id)">
              <div class="course-cover">
                <img 
                  :src="course.coverImage" 
                  :alt="course.title"
                  @error="handleImageError"
                />
                <div class="course-status">
                  <el-tag :type="getStatusType(course.status)" size="small">
                    {{ getStatusText(course.status) }}
                  </el-tag>
                </div>
              </div>
              
              <div class="course-content">
                <h3 class="course-title">{{ course.title }}</h3>
                <p class="course-teacher">{{ course.teacher }}</p>
                <p class="course-description">{{ course.description }}</p>
                
                <div class="course-progress">
                  <div class="progress-info">
                    <span class="progress-label">学习进度</span>
                    <span class="progress-percent">{{ course.progress }}%</span>
                  </div>
                  <el-progress
                    :percentage="course.progress"
                    :stroke-width="6"
                    :show-text="false"
                  />
                </div>
                
                <div class="course-meta">
                  <div class="meta-item">
                    <el-icon><Clock /></el-icon>
                    <span>{{ course.duration }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon><User /></el-icon>
                    <span>{{ course.studentCount }}人</span>
                  </div>
                </div>
                
                <div class="course-actions">
                  <el-button
                    v-if="!course.enrolled"
                    type="primary"
                    size="small"
                    :loading="enrolling"
                    @click.stop="handleEnroll(course)"
                  >
                    加入课程
                  </el-button>
                  <el-button
                    v-else
                    type="success"
                    size="small"
                    @click.stop="viewCourse(course.id)"
                    :disabled="course.status === 'completed'"
                  >
                    {{ course.status === 'completed' ? '已完成' : '继续学习' }}
                  </el-button>
                  <el-tag
                    v-if="course.enrolled"
                    type="success"
                    size="small"
                    class="enrolled-tag"
                  >
                    已选课
                  </el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-table :data="filteredCourses" style="width: 100%">
          <el-table-column width="80">
            <template #default="{ row }">
              <div class="course-avatar">
                <img 
                  :src="row.coverImage" 
                  :alt="row.title"
                  @error="handleImageError"
                />
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="课程信息" min-width="200">
            <template #default="{ row }">
              <div class="course-info">
                <h4 class="course-title">{{ row.title }}</h4>
                <p class="course-teacher">{{ row.teacher }}</p>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="学习进度" width="150">
            <template #default="{ row }">
              <div class="progress-cell">
                <el-progress
                  :percentage="row.progress"
                  :stroke-width="8"
                  :show-text="false"
                />
                <span class="progress-text">{{ row.progress }}%</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="时长" width="100">
            <template #default="{ row }">
              <div class="duration-cell">
                <el-icon><Clock /></el-icon>
                <span>{{ row.duration }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="最后学习" width="120">
            <template #default="{ row }">
              <span class="last-study">{{ row.lastStudy }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  v-if="!row.enrolled"
                  type="primary"
                  size="small"
                  :loading="enrolling"
                  @click="handleEnroll(row)"
                >
                  加入课程
                </el-button>
                <el-button
                  v-else
                  type="success"
                  size="small"
                  @click="viewCourse(row.id)"
                  :disabled="row.status === 'completed'"
                >
                  {{ row.status === 'completed' ? '已完成' : '继续学习' }}
                </el-button>
                <el-tag
                  v-if="row.enrolled"
                  type="success"
                  size="small"
                  style="margin-left: 8px;"
                >
                  已选课
                </el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="filteredCourses.length === 0 && !loading"
        description="暂无课程数据"
        :image-size="120"
      >
        <el-button type="primary" @click="$router.push('/student/dashboard')">
          返回仪表板
        </el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="filteredCourses.length > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import {
  Search, Filter, Plus, Star, StarFilled, 
  Clock, User, TrendCharts, Grid, List
} from '@element-plus/icons-vue'

// API imports
import { getCourses, enrollCourse, getCourseProgress } from '@/api/course'

const router = useRouter()
const authStore = useAuthStore()

// 计算属性
const userId = computed(() => authStore.userId)

// 计算属性：过滤后的课程列表
const filteredCourses = computed(() => {
  if (!courses.value) return []
  
  let filtered = courses.value
  
  // 按状态筛选
  if (selectedStatus.value) {
    filtered = filtered.filter(course => {
      switch (selectedStatus.value) {
        case 'enrolled':
          return course.enrolled === true
        case 'available':
          return course.enrolled === false || course.enrolled === undefined
        case 'completed':
          return course.enrolled === true && course.progress >= 100
        default:
          return true
      }
    })
  }
  
  // 按分类筛选
  if (selectedCategory.value) {
    filtered = filtered.filter(course => course.category === selectedCategory.value)
  }
  
  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(course => 
      course.title?.toLowerCase().includes(keyword) ||
      course.name?.toLowerCase().includes(keyword) ||
      course.description?.toLowerCase().includes(keyword) ||
      course.teacher?.toLowerCase().includes(keyword)
    )
  }
  
  return filtered
})

// 响应式数据
const loading = ref(true)
const enrolling = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const viewMode = ref('grid')

const courses = ref([])
const categories = ref([
  { label: '全部', value: '' },
  { label: '计算机科学', value: 'computer-science' },
  { label: '数学', value: 'mathematics' },
  { label: '物理', value: 'physics' },
  { label: '工程', value: 'engineering' }
])

const statusOptions = ref([
  { label: '全部', value: '' },
  { label: '未选课', value: 'available' },
  { label: '进行中', value: 'enrolled' },
  { label: '已完成', value: 'completed' }
])

// 方法
const loadCourses = async () => {
  try {
    loading.value = true
    
    // 检查用户是否已登录
    if (!userId.value) {
      console.warn('用户未登录，跳过课程加载')
      loading.value = false
      return
    }
    
    const params = {
      page: currentPage.value - 1, // 后端通常从0开始
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      category: selectedCategory.value || undefined
      // 移除 status 和 student 参数，让学生看到所有已发布的课程
    }
    
    const response = await getCourses(params)
    
    if (response.data) {
      let courseList = response.data.content || response.data.courses || []
      
      // 处理课程数据字段映射
      courseList = courseList.map(course => ({
        ...course,
        title: course.name || course.title,
        teacher: course.teacherName || course.teacher,
        coverImage: course.coverImage || '/placeholder-course.jpg'
      }))
      
      courses.value = courseList
      total.value = response.data.totalElements || response.data.total || courseList.length
      
      // 为每个课程加载进度信息
      await loadCoursesProgress()
    }
    
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程失败，请稍后重试')
    throw error
  } finally {
    loading.value = false
  }
}

const loadCoursesProgress = async () => {
  try {
    const progressPromises = courses.value.map(async (course) => {
      if (course.enrolled) {
        try {
          const progressResponse = await getCourseProgress(course.id, {
            student: userId.value
          })
          if (progressResponse.data) {
            course.progress = progressResponse.data.progress || 0
            course.completedLessons = progressResponse.data.completedLessons || 0
            course.totalLessons = progressResponse.data.totalLessons || 0
          }
        } catch (error) {
          console.error(`加载课程 ${course.id} 进度失败:`, error)
          throw error
        }
      }
    })
    
    await Promise.all(progressPromises)
  } catch (error) {
    console.error('加载课程进度失败:', error)
    throw error
  }
}

const handleEnroll = async (course) => {
  if (course.enrolled) {
    viewCourse(course.id)
    return
  }
  
  try {
    enrolling.value = true
    
    await enrollCourse(course.id)
    
    ElMessage.success('选课成功！')
    course.enrolled = true
    course.progress = 0
    
    // 重新加载课程数据
    await loadCourses()
    
  } catch (error) {
    console.error('选课失败:', error)
    ElMessage.error('选课失败，请稍后重试')
  } finally {
    enrolling.value = false
  }
}

const viewCourse = (courseId) => {
  router.push(`/student/courses/${courseId}`)
}

const handleSearch = () => {
  currentPage.value = 1
  loadCourses()
}

const handleFilterChange = () => {
  currentPage.value = 1
  // 状态筛选在前端进行，不需要重新加载数据
  // loadCourses()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadCourses()
}

const getDifficultyType = (difficulty) => {
  const typeMap = {
    '初级': 'success',
    '中级': 'warning',
    '高级': 'danger'
  }
  return typeMap[difficulty] || 'info'
}

const getRatingStars = (rating) => {
  return Math.floor(rating)
}

const getStatusType = (status) => {
  const statusMap = {
    enrolled: 'primary',
    completed: 'success',
    available: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    enrolled: '进行中',
    completed: '已完成',
    available: '未开始'
  }
  return statusMap[status] || '未知'
}

const handleImageError = (event) => {
  // 设置默认占位图片
  event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmN2ZhIi8+CiAgPHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMTYiIGZpbGw9IiM5MDkzOTkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7or77nqIvlm77niYc8L3RleHQ+Cjwvc3ZnPg=='
}

// 监听器
watch(selectedCategory, () => {
  currentPage.value = 1
  loadCourses()
})

watch(selectedStatus, () => {
  currentPage.value = 1
  // 状态筛选在前端进行，不需要重新加载数据
})

// 生命周期
onMounted(() => {
  loadCourses()
})
</script>

<style lang="scss" scoped>
.student-courses {
  .page-header {
    margin-bottom: 24px;
  }
  
  .filter-card {
    margin-bottom: 24px;
    
    .view-controls {
      display: flex;
      justify-content: flex-end;
    }
  }
}

.courses-container {
  margin-bottom: 24px;
}

.grid-view {
  .course-card {
    height: 100%;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    }
    
    .course-cover {
      position: relative;
      height: 160px;
      overflow: hidden;
      border-radius: 8px 8px 0 0;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s;
      }
      
      &:hover img {
        transform: scale(1.05);
      }
      
      .course-status {
        position: absolute;
        top: 8px;
        right: 8px;
      }
    }
    
    .course-content {
      padding: 16px;
      
      .course-title {
        margin: 0 0 8px 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .course-teacher {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #409eff;
      }
      
      .course-description {
        margin: 0 0 16px 0;
        font-size: 13px;
        color: #606266;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .course-progress {
        margin-bottom: 16px;
        
        .progress-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          .progress-label {
            font-size: 12px;
            color: #909399;
          }
          
          .progress-percent {
            font-size: 12px;
            font-weight: 600;
            color: #303133;
          }
        }
      }
      
      .course-meta {
        display: flex;
        justify-content: space-between;
        margin-bottom: 16px;
        
        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #909399;
        }
      }
      
      .course-actions {
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 8px;
        
        .el-button {
          flex: 1;
          min-width: 0;
        }
        
        .enrolled-tag {
          margin-left: 8px;
          flex-shrink: 0;
        }
      }
    }
  }
}

.list-view {
  .course-avatar {
    width: 60px;
    height: 45px;
    border-radius: 4px;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .course-info {
    .course-title {
      margin: 0 0 4px 0;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
    
    .course-teacher {
      margin: 0;
      font-size: 12px;
      color: #409eff;
    }
  }
  
  .progress-cell {
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
  
  .duration-cell {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #606266;
  }
  
  .last-study {
    font-size: 12px;
    color: #909399;
  }
  
  .action-buttons {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

// 响应式设计
@media (max-width: 768px) {
  .filter-card {
    :deep(.el-row) {
      .el-col {
        margin-bottom: 12px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
    
    .view-controls {
      justify-content: center;
    }
  }
  
  .grid-view {
    .course-card .course-content {
      padding: 12px;
      
      .course-title {
        font-size: 14px;
      }
    }
  }
}
</style> 