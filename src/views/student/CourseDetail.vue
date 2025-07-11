<template>
  <div class="course-detail">
    <div class="page-header">
      <el-button @click="$router.go(-1)" type="text" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回课程列表
      </el-button>
      <div v-if="course.title" class="header-content">
        <h1>{{ course.title }}</h1>
        <div class="course-meta">
          <el-tag :type="getStatusColor(course.status)" size="large">
            {{ getStatusText(course.status) }}
          </el-tag>
          <span class="teacher-info">
            <el-icon><User /></el-icon>
            {{ course.teacher || '未知教师' }}
          </span>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="detail-content">
      <el-row :gutter="24">
        <!-- 左侧：课程信息 -->
        <el-col :lg="16" :md="24">
          <!-- 课程概览 -->
          <el-card class="course-overview-card">
            <template #header>
              <h3>课程概览</h3>
            </template>
            
            <div class="course-cover" v-if="course.coverImage">
              <img :src="course.coverImage" :alt="course.title" />
            </div>
            
            <div class="course-description">
              <h4>课程简介</h4>
              <p>{{ course.description || '暂无课程简介' }}</p>
            </div>

            <div class="course-objectives" v-if="course.objectives">
              <h4>学习目标</h4>
              <ul>
                <li v-for="(objective, index) in course.objectives" :key="index">
                  {{ objective }}
                </li>
              </ul>
            </div>

            <div class="course-requirements" v-if="course.requirements">
              <h4>课程要求</h4>
              <p>{{ course.requirements }}</p>
            </div>
          </el-card>

          <!-- 课程内容 -->
          <el-card class="course-content-card">
            <template #header>
              <h3>课程内容</h3>
            </template>
            
            <div class="lessons-list">
              <div v-for="(lesson, index) in course.lessons" :key="lesson.id" class="lesson-item">
                <div class="lesson-header">
                  <div class="lesson-info">
                    <span class="lesson-number">{{ index + 1 }}</span>
                    <h4 class="lesson-title">{{ lesson.title }}</h4>
                  </div>
                  <div class="lesson-status">
                    <el-tag 
                      :type="lesson.completed ? 'success' : 'info'" 
                      size="small"
                    >
                      {{ lesson.completed ? '已完成' : '未完成' }}
                    </el-tag>
                  </div>
                </div>
                
                <div class="lesson-content" v-if="lesson.description">
                  <p>{{ lesson.description }}</p>
                  <div v-if="lesson.keyPoints && lesson.keyPoints.length > 0" class="lesson-key-points">
                    <h5>学习要点：</h5>
                    <ul>
                      <li v-for="point in lesson.keyPoints" :key="point">
                        {{ point }}
                      </li>
                    </ul>
                  </div>
                </div>
                
                <div class="lesson-actions">
                  <el-button 
                    type="primary" 
                    size="small"
                    :disabled="!course.enrolled"
                    @click="startLesson(lesson)"
                  >
                    {{ lesson.completed ? '回顾' : '开始学习' }}
                  </el-button>
                  <span class="lesson-duration" v-if="lesson.duration">
                    <el-icon><Clock /></el-icon>
                    {{ lesson.duration }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：进度和操作 -->
        <el-col :lg="8" :md="24">
          <!-- 学习进度 -->
          <el-card class="progress-card">
            <template #header>
              <h3>学习进度</h3>
            </template>
            
            <div class="progress-overview">
              <div class="progress-circle">
                <el-progress 
                  type="circle" 
                  :percentage="course.progress || 0"
                  :width="120"
                  :stroke-width="8"
                />
              </div>
              
              <div class="progress-stats">
                <div class="stat-item">
                  <span class="stat-label">已完成课时</span>
                  <span class="stat-value">{{ course.completedLessons || 0 }}/{{ course.totalLessons || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">学习时长</span>
                  <span class="stat-value">{{ course.studyTime || '0小时' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">最后学习</span>
                  <span class="stat-value">{{ formatDate(course.lastStudy) }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 课程信息 -->
          <el-card class="info-card">
            <template #header>
              <h3>课程信息</h3>
            </template>
            
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="课程名称">
                {{ course.title }}
              </el-descriptions-item>
              <el-descriptions-item label="授课教师">
                {{ course.teacher }}
              </el-descriptions-item>
              <el-descriptions-item label="课程类别">
                {{ course.category }}
              </el-descriptions-item>
              <el-descriptions-item label="难度等级">
                <el-tag :type="getDifficultyColor(course.difficulty)" size="small">
                  {{ course.difficulty }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="课程时长">
                {{ course.duration }}
              </el-descriptions-item>
              <el-descriptions-item label="开课时间">
                {{ formatDate(course.startDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="结课时间">
                {{ formatDate(course.endDate) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 课件列表 -->
          <el-card class="resource-card" style="margin-top: 20px;">
            <template #header>
              <h3>课件/资料</h3>
            </template>
            <div v-if="isTeacher">
              <el-form :inline="true" @submit.prevent>
                <el-form-item label="选择文件">
                  <input type="file" @change="onResourceFileChange" ref="resourceFileInput" />
                </el-form-item>
                <el-form-item label="描述">
                  <el-input v-model="resourceUploadForm.description" placeholder="可选" style="width: 200px;" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="uploadResourceAction" :loading="uploadingResource">上传</el-button>
                </el-form-item>
              </el-form>
            </div>
            <el-table :data="resourceList" size="small" v-loading="resourceLoading">
              <el-table-column prop="fileName" label="文件名" />
              <el-table-column prop="fileType" label="类型" />
              <el-table-column prop="uploadTime" label="上传时间" />
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="downloadResource(row)">下载</el-button>
                  <el-button size="small" type="success" v-if="canPreview(row)" @click="previewResource(row)">预览</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <!-- 操作按钮 -->
          <el-card class="action-card">
            <template #header>
              <h3>操作</h3>
            </template>
            
            <div class="action-buttons">
              <el-button 
                v-if="!course.enrolled"
                type="primary" 
                size="large"
                @click="enrollCourse"
                :loading="enrolling"
                class="action-btn"
              >
                <el-icon><Plus /></el-icon>
                选择课程
              </el-button>
              
              <el-button 
                v-else-if="course.status !== 'completed'"
                type="primary" 
                size="large"
                @click="continueLearning"
                class="action-btn"
              >
                <el-icon><CaretRight /></el-icon>
                继续学习
              </el-button>
              
              <el-button 
                v-else
                type="success" 
                size="large"
                disabled
                class="action-btn"
              >
                <el-icon><Check /></el-icon>
                已完成
              </el-button>
              
              <el-button 
                size="large"
                @click="viewCertificate"
                :disabled="course.status !== 'completed'"
                class="action-btn"
              >
                <el-icon><Document /></el-icon>
                查看证书
              </el-button>
            </div>
          </el-card>

          <!-- 相关推荐 -->
          <el-card class="recommendation-card" v-if="relatedCourses.length > 0">
            <template #header>
              <h3>相关推荐</h3>
            </template>
            
            <div class="related-courses">
              <div 
                v-for="related in relatedCourses" 
                :key="related.id"
                class="related-item"
                @click="$router.push(`/student/courses/${related.id}`)"
              >
                <img :src="related.coverImage" :alt="related.title" />
                <div class="related-info">
                  <h5>{{ related.title }}</h5>
                  <p>{{ related.teacher }}</p>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 作业列表 -->
          <el-card class="course-tasks-card">
            <template #header>
              <h3>作业列表</h3>
            </template>
            <el-table :data="tasks">
              <el-table-column prop="title" label="作业标题" />
              <el-table-column prop="startDate" label="开始时间" />
              <el-table-column prop="dueDate" label="截止时间" />
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button v-if="isStudent" size="small" @click="openSubmitDialog(row)">提交作业</el-button>
                  <el-button v-if="isTeacher" size="small" @click="openSubmissionsDialog(row)">批改作业</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 学生提交作业对话框 -->
    <el-dialog v-model="showSubmitDialog" title="提交作业" width="500px">
      <el-form>
        <el-form-item label="作业内容">
          <el-input v-model="submitForm.content" type="textarea" />
        </el-form-item>
        <el-form-item label="上传文件">
          <input type="file" @change="onFileChange" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="submitAssignmentAction">提交</el-button>
      <h4>我的提交记录</h4>
      <el-table :data="mySubmissions">
        <el-table-column prop="attemptNumber" label="第几次提交" />
        <el-table-column prop="submittedAt" label="提交时间" />
        <el-table-column prop="score" label="分数" />
        <el-table-column prop="feedback" label="评语" />
        <el-table-column label="文件">
          <template #default="{ row }">
            <a v-if="row.fileUrls" :href="getFileUrl(row.fileUrls)" target="_blank">下载</a>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 教师批改作业对话框 -->
    <el-dialog v-model="showSubmissionsDialog" title="作业提交列表" width="700px">
      <el-table :data="allSubmissions">
        <el-table-column prop="studentId" label="学生ID" />
        <el-table-column prop="attemptNumber" label="第几次提交" />
        <el-table-column prop="submittedAt" label="提交时间" />
        <el-table-column prop="score" label="分数" />
        <el-table-column prop="feedback" label="评语" />
        <el-table-column label="文件">
          <template #default="{ row }">
            <a v-if="row.fileUrls" :href="getFileUrl(row.fileUrls)" target="_blank">下载</a>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="openGradeDialog(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="showGradeDialog" title="批改作业" width="400px">
        <el-form>
          <el-form-item label="分数">
            <el-input v-model="gradeForm.score" type="number" />
          </el-form-item>
          <el-form-item label="评语">
            <el-input v-model="gradeForm.feedback" type="textarea" />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="submitGrade">提交批改</el-button>
      </el-dialog>
    </el-dialog>

    <el-dialog v-model="showPreviewDialog" title="课件预览" width="60vw" @close="closePreviewDialog">
      <template #default>
        <div v-if="previewType === 'video'">
          <video :src="previewUrl" controls style="width:100%;max-height:70vh;" />
        </div>
        <div v-else-if="previewType === 'pdf'">
          <iframe :src="previewUrl" style="width:100%;height:70vh;" frameborder="0"></iframe>
        </div>
        <div v-else>
          <p>暂不支持该文件类型的在线预览，请下载后查看。</p>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, User, Clock, Plus, Check, 
  Document, CaretRight 
} from '@element-plus/icons-vue'
import { getCourseById, enrollCourse as enrollCourseAPI } from '@/api/course'
import { generateCourseContent } from '@/api/ai'
import { getTasks } from '@/api/task'
import { submitAssignment, getMySubmissions, getAllSubmissions, gradeSubmission } from '@/api/submission'
import { getResourceList, downloadResource as apiDownloadResource, uploadResource } from '@/api/resource'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(true)
const enrolling = ref(false)
const course = ref({})
const relatedCourses = ref([])
const isStudent = computed(() => authStore.user?.role === 'STUDENT')
const isTeacher = computed(() => authStore.user?.role === 'TEACHER' || authStore.user?.role === 'ADMIN')

const tasks = ref([])
const showSubmitDialog = ref(false)
const showSubmissionsDialog = ref(false)
const showGradeDialog = ref(false)
const submitForm = ref({ content: '', file: null, taskId: null })
const mySubmissions = ref([])
const allSubmissions = ref([])
const gradeForm = ref({ id: null, score: '', feedback: '' })
const resourceList = ref([])
const resourceLoading = ref(false)
const resourceUploadForm = ref({ file: null, description: '' })
const resourceFileInput = ref(null)
const uploadingResource = ref(false)
const showPreviewDialog = ref(false)
const previewUrl = ref('')
const previewType = ref('')

// 计算属性
const courseId = computed(() => route.params.courseId)

// 方法
const loadCourseDetail = async () => {
  try {
    loading.value = true
    const response = await getCourseById(courseId.value)
    
    if (response.data) {
      // 处理字段映射
      const courseData = response.data
      course.value = {
        ...courseData,
        title: courseData.name || courseData.title,
        teacher: courseData.teacherName || courseData.teacher,
        coverImage: courseData.coverImage || '/placeholder-course.jpg'
      }
      
      // 生成AI课程内容
      await generateAICourseContent()
    }
  } catch (error) {
    console.error('加载课程详情失败:', error)
    ElMessage.error('加载课程详情失败')
    router.go(-1)
  } finally {
    loading.value = false
  }
}

// 生成AI课程内容
const generateAICourseContent = async () => {
  try {
    console.log('开始生成AI课程内容...')
    const aiResponse = await generateCourseContent({
      courseName: course.value.title,
      courseDescription: course.value.description || '',
      difficulty: course.value.difficulty || 'intermediate'
    })
    
    if (aiResponse.data && aiResponse.data.success) {
      const aiContent = aiResponse.data.content
      console.log('AI生成内容成功:', aiContent)
      
      // 应用AI生成的内容
      if (aiContent.objectives) {
        course.value.objectives = aiContent.objectives
      }
      
      if (aiContent.requirements) {
        course.value.requirements = aiContent.requirements
      }
      
      if (aiContent.lessons && aiContent.lessons.length > 0) {
        // 随机设置部分课程为已完成（模拟学习进度）
        const lessonsWithProgress = aiContent.lessons.map((lesson, index) => ({
          ...lesson,
          completed: index < Math.floor(aiContent.lessons.length * 0.3) // 前30%课程标记为已完成
        }))
        
        course.value.lessons = lessonsWithProgress
        
        // 计算学习进度
        course.value.completedLessons = lessonsWithProgress.filter(l => l.completed).length
        course.value.totalLessons = lessonsWithProgress.length
        course.value.progress = Math.round((course.value.completedLessons / course.value.totalLessons) * 100)
      }
      
      // 设置其他生成的信息
      if (aiContent.totalDuration) {
        course.value.duration = aiContent.totalDuration
      }
      
      // 模拟学习时间和最后学习时间
      course.value.studyTime = course.value.completedLessons ? `${course.value.completedLessons}小时` : '0小时'
      course.value.lastStudy = course.value.completedLessons > 0 ? new Date() : null
      
      console.log('课程内容更新完成:', course.value)
      
    } else {
      console.warn('AI生成失败，使用备用内容')
      generateFallbackContent()
    }
  } catch (error) {
    console.error('AI生成课程内容失败:', error)
    console.log('使用备用课程内容')
    generateFallbackContent()
  }
}

// 备用课程内容生成
const generateFallbackContent = () => {
  console.log('生成备用课程内容...')
  
  const courseName = course.value.title || '课程'
  
  // 根据课程名称判断类型
  if (courseName.toLowerCase().includes('java') || courseName.includes('Java')) {
    course.value.objectives = [
      '掌握Java编程语言的基础语法和面向对象编程概念',
      '能够编写简单的Java应用程序',
      '理解Java的核心特性：封装、继承、多态',
      '学会使用常用的Java开发工具和框架'
    ]
    
    course.value.requirements = '建议具备基础的计算机操作能力，了解编程基本概念更佳'
    
    course.value.lessons = [
      {
        id: 1,
        title: 'Java基础入门',
        description: '学习Java语言基础语法，包括变量、数据类型、运算符等核心概念',
        duration: '60分钟',
        keyPoints: ['变量声明', '数据类型', '运算符', '控制结构'],
        completed: true
      },
      {
        id: 2,
        title: '面向对象编程',
        description: '深入理解类和对象的概念，掌握封装、继承、多态等OOP特性',
        duration: '90分钟',
        keyPoints: ['类和对象', '封装性', '继承机制', '多态性'],
        completed: true
      },
      {
        id: 3,
        title: '异常处理与调试',
        description: '学习Java异常处理机制，掌握程序调试技巧',
        duration: '45分钟',
        keyPoints: ['异常类型', 'try-catch语句', '调试技巧', '日志记录'],
        completed: false
      },
      {
        id: 4,
        title: '集合框架应用',
        description: '掌握Java集合框架的使用，包括List、Set、Map等常用集合',
        duration: '75分钟',
        keyPoints: ['ArrayList用法', 'HashMap操作', 'Set集合', 'Iterator遍历'],
        completed: false
      },
      {
        id: 5,
        title: '文件操作与IO流',
        description: '学习文件读写操作和输入输出流的使用',
        duration: '60分钟',
        keyPoints: ['文件读写', '字节流', '字符流', '缓冲流'],
        completed: false
      },
      {
        id: 6,
        title: '项目实战演练',
        description: '通过完整项目实践，综合运用所学Java知识',
        duration: '120分钟',
        keyPoints: ['项目设计', '代码实现', '测试调试', '代码优化'],
        completed: false
      }
    ]
    
  } else {
    // 通用课程内容
    course.value.objectives = [
      `理解${courseName}的核心概念和基本原理`,
      `掌握${courseName}的实际应用技能`,
      '能够独立解决相关领域的实际问题',
      '培养持续学习和自主探索的能力'
    ]
    
    course.value.requirements = '建议具备基础知识，有学习热情和时间投入'
    
    course.value.lessons = [
      {
        id: 1,
        title: '基础概念入门',
        description: `了解${courseName}的基本概念、发展历史和应用领域`,
        duration: '45分钟',
        keyPoints: ['基本概念', '发展历程', '应用场景', '学习方法'],
        completed: true
      },
      {
        id: 2,
        title: '核心理论学习',
        description: `深入学习${courseName}的核心理论和重要原理`,
        duration: '60分钟',
        keyPoints: ['理论基础', '核心原理', '重点概念', '案例分析'],
        completed: true
      },
      {
        id: 3,
        title: '实践技能训练',
        description: `通过实际操作练习，掌握${courseName}的基本技能`,
        duration: '75分钟',
        keyPoints: ['基础操作', '技能训练', '实践练习', '错误纠正'],
        completed: false
      },
      {
        id: 4,
        title: '进阶技能提升',
        description: `学习${courseName}的高级技能和优化方法`,
        duration: '90分钟',
        keyPoints: ['高级技能', '优化方法', '最佳实践', '性能提升'],
        completed: false
      },
      {
        id: 5,
        title: '综合应用案例',
        description: `通过真实案例学习${courseName}的综合应用`,
        duration: '80分钟',
        keyPoints: ['案例分析', '解决方案', '实施步骤', '效果评估'],
        completed: false
      },
      {
        id: 6,
        title: '项目实战与总结',
        description: '完成完整项目，总结学习成果和经验',
        duration: '120分钟',
        keyPoints: ['项目规划', '实施执行', '成果展示', '学习总结'],
        completed: false
      }
    ]
  }
  
  // 计算完成的课时数
  if (course.value.lessons) {
    course.value.completedLessons = course.value.lessons.filter(l => l.completed).length
    course.value.totalLessons = course.value.lessons.length
    course.value.progress = Math.round((course.value.completedLessons / course.value.totalLessons) * 100)
    
    // 设置学习时间
    const totalMinutes = course.value.lessons.reduce((sum, lesson) => {
      const duration = parseInt(lesson.duration.replace(/[^0-9]/g, ''))
      return sum + duration
    }, 0)
    course.value.duration = `${Math.round(totalMinutes / 60 * 10) / 10}小时`
    course.value.studyTime = course.value.completedLessons ? `${course.value.completedLessons}小时` : '0小时'
    course.value.lastStudy = course.value.completedLessons > 0 ? new Date() : null
  }
  
  console.log('备用内容生成完成:', course.value)
}

const enrollCourse = async () => {
  try {
    enrolling.value = true
    await enrollCourseAPI(courseId.value)
    
    ElMessage.success('选课成功！')
    course.value.enrolled = true
    course.value.status = 'enrolled'
  } catch (error) {
    ElMessage.error('选课失败，请重试')
  } finally {
    enrolling.value = false
  }
}

const continueLearning = () => {
  // 找到第一个未完成的课时
  const nextLesson = course.value.lessons?.find(lesson => !lesson.completed)
  if (nextLesson) {
    startLesson(nextLesson)
  } else {
    ElMessage.info('所有课时已完成')
  }
}

const startLesson = (lesson) => {
  // 这里可以跳转到具体的学习页面
  ElMessage.info(`开始学习：${lesson.title}`)
  // router.push(`/student/courses/${courseId.value}/lessons/${lesson.id}`)
}

const viewCertificate = () => {
  ElMessage.info('证书功能开发中...')
}

const getStatusColor = (status) => {
  const colorMap = {
    enrolled: 'primary',
    completed: 'success',
    available: 'info'
  }
  return colorMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    enrolled: '进行中',
    completed: '已完成',
    available: '未开始'
  }
  return statusMap[status] || '未知'
}

const getDifficultyColor = (difficulty) => {
  const colorMap = {
    '初级': 'success',
    '中级': 'warning', 
    '高级': 'danger'
  }
  return colorMap[difficulty] || 'info'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

async function loadTasks() {
  const res = await getTasks({ courseId: course.id })
  tasks.value = res.data.content || res.data
}

function openSubmitDialog(task) {
  submitForm.value = { content: '', file: null, taskId: task.id }
  showSubmitDialog.value = true
  loadMySubmissions(task.id)
}

function onFileChange(e) {
  submitForm.value.file = e.target.files[0]
}

async function submitAssignmentAction() {
  await submitAssignment(submitForm.value)
  ElMessage.success('提交成功')
  loadMySubmissions(submitForm.value.taskId)
}

async function loadMySubmissions(taskId) {
  const res = await getMySubmissions(taskId)
  mySubmissions.value = res.data
}

function getFileUrl(fileName) {
  return `/uploads/${fileName}`
}

function openSubmissionsDialog(task) {
  showSubmissionsDialog.value = true
  loadAllSubmissions(task.id)
}

async function loadAllSubmissions(taskId) {
  const res = await getAllSubmissions(taskId)
  allSubmissions.value = res.data
}

function openGradeDialog(row) {
  gradeForm.value = { id: row.id, score: row.score || '', feedback: row.feedback || '' }
  showGradeDialog.value = true
}

async function submitGrade() {
  await gradeSubmission(gradeForm.value.id, { score: gradeForm.value.score, feedback: gradeForm.value.feedback })
  ElMessage.success('批改成功')
  showGradeDialog.value = false
  loadAllSubmissions(submitForm.value.taskId)
}

const loadResourceList = async () => {
  resourceLoading.value = true
  try {
    const res = await getResourceList(courseId.value)
    resourceList.value = res.data || []
  } catch (e) {
    ElMessage.error('课件加载失败')
  } finally {
    resourceLoading.value = false
  }
}

const downloadResource = async (row) => {
  try {
    const res = await apiDownloadResource(row.id)
    const blob = new Blob([res.data], { type: row.fileType })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.fileName
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

const onResourceFileChange = (e) => {
  resourceUploadForm.value.file = e.target.files[0]
}

const uploadResourceAction = async () => {
  if (!resourceUploadForm.value.file) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploadingResource.value = true
  try {
    await uploadResource({
      file: resourceUploadForm.value.file,
      courseId: courseId.value,
      description: resourceUploadForm.value.description
    })
    ElMessage.success('上传成功')
    resourceUploadForm.value.file = null
    resourceUploadForm.value.description = ''
    if (resourceFileInput.value) resourceFileInput.value.value = ''
    loadResourceList()
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploadingResource.value = false
  }
}

const canPreview = (row) => {
  const type = (row.fileType || '').toLowerCase()
  return type.includes('video') || type === 'application/pdf' || row.fileName.endsWith('.pdf')
}

const previewResource = async (row) => {
  try {
    const res = await apiDownloadResource(row.id)
    const type = (row.fileType || '').toLowerCase()
    let url = ''
    if (type.includes('video')) {
      url = window.URL.createObjectURL(new Blob([res.data], { type: row.fileType }))
      previewType.value = 'video'
    } else if (type === 'application/pdf' || row.fileName.endsWith('.pdf')) {
      url = window.URL.createObjectURL(new Blob([res.data], { type: row.fileType }))
      previewType.value = 'pdf'
    } else {
      previewType.value = 'other'
    }
    previewUrl.value = url
    showPreviewDialog.value = true
  } catch (e) {
    ElMessage.error('预览失败')
  }
}

const closePreviewDialog = () => {
  if (previewUrl.value) {
    window.URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  previewType.value = ''
}

// 生命周期
onMounted(() => {
  loadCourseDetail()
  loadTasks()
  loadResourceList()
})
</script>

<style lang="scss" scoped>
.course-detail {
  padding: 24px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 24px;
  
  .back-btn {
    margin-bottom: 16px;
    font-size: 14px;
    
    .el-icon {
      margin-right: 4px;
    }
  }
  
  .header-content {
    h1 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 28px;
      font-weight: 600;
    }
    
    .course-meta {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .teacher-info {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #409eff;
        font-size: 14px;
      }
    }
  }
}

.detail-content {
  .course-overview-card, .course-content-card, 
  .progress-card, .info-card, .action-card, .recommendation-card {
    margin-bottom: 24px;
    
    .el-card__header {
      h3 {
        margin: 0;
        color: #303133;
        font-size: 18px;
        font-weight: 600;
      }
    }
  }
  
  .course-overview-card {
    .course-cover {
      margin-bottom: 24px;
      
      img {
        width: 100%;
        height: 200px;
        object-fit: cover;
        border-radius: 8px;
      }
    }
    
    .course-description, .course-objectives, .course-requirements {
      margin-bottom: 24px;
      
      h4 {
        margin: 0 0 12px 0;
        color: #303133;
        font-size: 16px;
        font-weight: 600;
      }
      
      p {
        line-height: 1.6;
        color: #606266;
        margin: 0;
      }
      
      ul {
        margin: 0;
        padding-left: 20px;
        color: #606266;
        
        li {
          margin-bottom: 8px;
          line-height: 1.5;
        }
      }
    }
  }
  
  .course-content-card {
    .lessons-list {
      .lesson-item {
        border: 1px solid #e4e7ed;
        border-radius: 8px;
        margin-bottom: 16px;
        padding: 16px;
        transition: all 0.3s;
        
        &:hover {
          border-color: #409eff;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
        }
        
        .lesson-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .lesson-info {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .lesson-number {
              display: flex;
              align-items: center;
              justify-content: center;
              width: 24px;
              height: 24px;
              background: #409eff;
              color: white;
              border-radius: 50%;
              font-size: 12px;
              font-weight: 600;
            }
            
            .lesson-title {
              margin: 0;
              color: #303133;
              font-size: 16px;
              font-weight: 600;
            }
          }
        }
        
        .lesson-content {
          margin-bottom: 12px;
          
          p {
            margin: 0;
            color: #606266;
            font-size: 14px;
            line-height: 1.5;
          }
          
          .lesson-key-points {
            margin-top: 12px;
            padding: 12px;
            background-color: #f8f9fa;
            border-radius: 6px;
            border-left: 3px solid #409eff;
            
            h5 {
              margin: 0 0 8px 0;
              color: #409eff;
              font-size: 13px;
              font-weight: 600;
            }
            
            ul {
              margin: 0;
              padding-left: 16px;
              
              li {
                margin-bottom: 4px;
                color: #606266;
                font-size: 12px;
                line-height: 1.4;
              }
            }
          }
        }
        
        .lesson-actions {
          display: flex;
          align-items: center;
          justify-content: space-between;
          
          .lesson-duration {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #909399;
            font-size: 12px;
          }
        }
      }
    }
  }
  
  .progress-card {
    .progress-overview {
      text-align: center;
      
      .progress-circle {
        margin-bottom: 24px;
      }
      
      .progress-stats {
        .stat-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .stat-label {
            color: #606266;
            font-size: 14px;
          }
          
          .stat-value {
            color: #303133;
            font-weight: 600;
            font-size: 14px;
          }
        }
      }
    }
  }
  
  .action-card {
    .action-buttons {
      .action-btn {
        width: 100%;
        margin-bottom: 12px;
        height: 48px;
        font-size: 16px;
        
        .el-icon {
          margin-right: 8px;
        }
      }
    }
  }
  
  .recommendation-card {
    .related-courses {
      .related-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: background-color 0.3s;
        margin-bottom: 12px;
        
        &:hover {
          background-color: #f5f7fa;
        }
        
        img {
          width: 60px;
          height: 45px;
          object-fit: cover;
          border-radius: 4px;
        }
        
        .related-info {
          flex: 1;
          
          h5 {
            margin: 0 0 4px 0;
            color: #303133;
            font-size: 14px;
            font-weight: 600;
          }
          
          p {
            margin: 0;
            color: #409eff;
            font-size: 12px;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .course-detail {
    padding: 12px;
  }
  
  .page-header {
    .header-content h1 {
      font-size: 24px;
    }
    
    .course-meta {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }
  
  .detail-content {
    .lesson-item {
      .lesson-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }
      
      .lesson-actions {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }
    }
  }
}
</style> 