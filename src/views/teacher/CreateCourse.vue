<template>
  <div class="create-course">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" size="large">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>创建新课程</h1>
      </div>
    </div>

    <el-card class="create-form">
      <el-form
        :model="courseForm"
        :rules="courseRules"
        ref="courseFormRef"
        label-width="120px"
        size="large"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input 
                v-model="courseForm.name"
                placeholder="请输入课程名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-select v-model="courseForm.category" placeholder="选择课程分类" style="width: 100%">
                <el-option label="计算机科学" value="计算机科学" />
                <el-option label="数学" value="数学" />
                <el-option label="物理" value="物理" />
                <el-option label="化学" value="化学" />
                <el-option label="生物" value="生物" />
                <el-option label="外语" value="外语" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="学期" prop="semester">
              <el-select v-model="courseForm.semester" placeholder="选择学期" style="width: 100%">
                <el-option label="2024春" value="2024春" />
                <el-option label="2024夏" value="2024夏" />
                <el-option label="2024秋" value="2024秋" />
                <el-option label="2024冬" value="2024冬" />
                <el-option label="2025春" value="2025春" />
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
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="课程状态" prop="status">
              <el-select v-model="courseForm.status" placeholder="选择状态" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button @click="goBack" size="large">取消</el-button>
          <el-button type="primary" @click="handleCreateCourse" :loading="creating" size="large">
            创建课程
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { createCourse } from '@/api/course'

const authStore = useAuthStore()
const router = useRouter()

const creating = ref(false)
const courseFormRef = ref()

const courseForm = reactive({
  name: '',
  category: '',
  description: '',
  semester: '2024春',
  credits: 3.0,
  status: 'DRAFT'
})

const courseRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '课程名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' }
  ],
  semester: [
    { required: true, message: '请选择学期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择课程状态', trigger: 'change' }
  ]
}

const handleCreateCourse = async () => {
  if (!courseFormRef.value) return
  
  try {
    await courseFormRef.value.validate()
    
    // 检查用户是否已登录
    if (!authStore.user || !authStore.user.id) {
      ElMessage.error('用户信息不存在，请重新登录')
      return
    }
    
    creating.value = true
    console.log('开始创建课程，用户信息:', authStore.user)
    
    // 准备课程数据，严格按照后端DTO要求
    const courseData = {
      name: courseForm.name.trim(),
      description: courseForm.description?.trim() || '',
      category: courseForm.category || '',
      semester: courseForm.semester,
      credits: Number(courseForm.credits),
      status: courseForm.status,
      teacherId: Number(authStore.user.id), // 确保是数字类型
      year: new Date().getFullYear(),
      difficulty: 'intermediate',
      price: 0.0,
      maxStudents: 50,
      code: null, // 让后端自动生成
      tags: [],
      startDate: null,
      endDate: null,
      coverImage: '',
      syllabusUrl: ''
    }
    
    console.log('准备发送的课程数据:', courseData)
    
    // 调用API创建课程
    const response = await createCourse(courseData)
    console.log('创建课程成功响应:', response)
    
    ElMessage.success('课程创建成功！')
    
    // 延迟跳转，确保用户看到成功消息
    setTimeout(() => {
      router.push('/teacher/courses')
    }, 1000)
    
  } catch (error) {
    console.error('创建课程失败:', error)
    
    // 详细的错误处理
    if (error.response?.status === 400) {
      const errorData = error.response.data
      if (errorData.message) {
        ElMessage.error(`创建失败: ${errorData.message}`)
      } else if (errorData.errors) {
        // 处理字段验证错误
        const errorMessages = []
        Object.keys(errorData.errors).forEach(field => {
          const fieldErrors = errorData.errors[field]
          if (Array.isArray(fieldErrors)) {
            fieldErrors.forEach(msg => errorMessages.push(`${field}: ${msg}`))
          } else {
            errorMessages.push(`${field}: ${fieldErrors}`)
          }
        })
        ElMessage.error(`验证失败: ${errorMessages.join('; ')}`)
      } else {
        ElMessage.error('请求数据格式错误，请检查输入信息')
      }
    } else if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      authStore.logout()
      router.push('/login')
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限创建课程')
    } else if (error.response?.status === 500) {
      ElMessage.error('服务器内部错误，请稍后重试')
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('创建课程失败，请重试')
    }
  } finally {
    creating.value = false
  }
}

const goBack = () => {
  router.push('/teacher/courses')
}
</script>

<style scoped>
.create-course {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
}

.create-form {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.form-actions {
  margin-top: 32px;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
  margin: 0 8px;
}
</style> 