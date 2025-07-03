<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-description">管理您的个人信息和账户设置</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：个人信息 -->
      <el-col :xs="24" :lg="16">
        <el-card class="profile-card">
          <template #header>
            <h3>基本信息</h3>
          </template>
          
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            size="large"
          >
            <!-- 头像上传 -->
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-upload
                  class="avatar-uploader"
                  action="/api/upload/avatar"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="handleAvatarSuccess"
                >
                  <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="avatar-tips">
                  <p>点击上传头像</p>
                  <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>

            <el-form-item label="姓名" prop="fullName">
              <el-input v-model="profileForm.fullName" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" />
            </el-form-item>

            <el-form-item label="角色">
              <el-tag :type="getRoleType(profileForm.role)">
                {{ getRoleText(profileForm.role) }}
              </el-tag>
            </el-form-item>

            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下自己..."
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">
                保存修改
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="password-card">
          <template #header>
            <h3>修改密码</h3>
          </template>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            size="large"
          >
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                show-password
                placeholder="请输入当前密码"
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="changingPassword" @click="changePassword">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：统计信息 -->
      <el-col :xs="24" :lg="8">
        <!-- 账户统计 -->
        <el-card class="stats-card">
          <template #header>
            <h3>账户统计</h3>
          </template>
          
          <div class="stats-list">
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon color="#409eff"><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">注册时间</div>
                <div class="stat-value">{{ userStats.registerDate }}</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon color="#67c23a"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">最后登录</div>
                <div class="stat-value">{{ userStats.lastLogin }}</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon color="#e6a23c"><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">学习时长</div>
                <div class="stat-value">{{ userStats.studyHours }}小时</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon color="#f56c6c"><Trophy /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">获得成就</div>
                <div class="stat-value">{{ userStats.achievements }}个</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 学习成就 -->
        <el-card class="achievements-card">
          <template #header>
            <h3>最近成就</h3>
          </template>
          
          <div class="achievements-list">
            <div
              v-for="achievement in achievements"
              :key="achievement.id"
              class="achievement-item"
            >
              <div class="achievement-icon">
                <el-icon :color="achievement.color" :size="24">
                  <component :is="achievement.icon" />
                </el-icon>
              </div>
              <div class="achievement-info">
                <h4>{{ achievement.title }}</h4>
                <p>{{ achievement.description }}</p>
                <span class="achievement-date">{{ achievement.date }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 安全设置 -->
        <el-card class="security-card">
          <template #header>
            <h3>安全设置</h3>
          </template>
          
          <div class="security-list">
            <div class="security-item">
              <div class="security-info">
                <h4>两步验证</h4>
                <p>为您的账户添加额外的安全保护</p>
              </div>
              <el-switch v-model="securitySettings.twoFactor" />
            </div>
            
            <div class="security-item">
              <div class="security-info">
                <h4>登录通知</h4>
                <p>当有新设备登录时接收通知</p>
              </div>
              <el-switch v-model="securitySettings.loginNotification" />
            </div>
            
            <div class="security-item">
              <div class="security-info">
                <h4>自动登出</h4>
                <p>长时间无操作自动退出登录</p>
              </div>
              <el-switch v-model="securitySettings.autoLogout" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  Plus, Calendar, Clock, Reading, Trophy, Star, Medal, Gift
} from '@element-plus/icons-vue'

const authStore = useAuthStore()

// 响应式数据
const saving = ref(false)
const changingPassword = ref(false)
const profileFormRef = ref()
const passwordFormRef = ref()

// 个人信息表单
const profileForm = reactive({
  username: '',
  fullName: '',
  email: '',
  phone: '',
  role: '',
  bio: '',
  avatarUrl: ''
})

// 密码修改表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户统计
const userStats = ref({
  registerDate: '2023-09-15',
  lastLogin: '2小时前',
  studyHours: 156,
  achievements: 12
})

// 成就列表
const achievements = ref([
  {
    id: 1,
    title: '学习达人',
    description: '连续学习7天',
    date: '2024-01-15',
    icon: 'Star',
    color: '#f39c12'
  },
  {
    id: 2,
    title: '优秀学员',
    description: '完成5门课程',
    date: '2024-01-10',
    icon: 'Medal',
    color: '#e74c3c'
  },
  {
    id: 3,
    title: '积极参与',
    description: '提交20个作业',
    date: '2024-01-05',
    icon: 'Gift',
    color: '#9b59b6'
  }
])

// 安全设置
const securitySettings = reactive({
  twoFactor: false,
  loginNotification: true,
  autoLogout: true
})

// 表单验证规则
const profileRules = {
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const currentUser = computed(() => authStore.user)

// 方法
const loadUserData = () => {
  const user = currentUser.value
  if (user) {
    Object.assign(profileForm, {
      username: user.username || '',
      fullName: user.fullName || '',
      email: user.email || '',
      phone: user.phone || '',
      role: user.role || '',
      bio: user.bio || '',
      avatarUrl: user.avatarUrl || ''
    })
  }
}

const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    saving.value = true

    // 调用API保存用户信息
    await authStore.updateUserInfo({
      fullName: profileForm.fullName,
      email: profileForm.email,
      phone: profileForm.phone,
      bio: profileForm.bio,
      avatarUrl: profileForm.avatarUrl
    })

    ElMessage.success('个人信息保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    // 调用API修改密码
    await authStore.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功')
    resetPasswordForm()
  } catch (error) {
    ElMessage.error('密码修改失败，请检查当前密码是否正确')
  } finally {
    changingPassword.value = false
  }
}

const resetForm = () => {
  loadUserData()
  ElMessage.info('表单已重置')
}

const resetPasswordForm = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const beforeAvatarUpload = (file) => {
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

const handleAvatarSuccess = (response) => {
  profileForm.avatarUrl = response.data.url
  ElMessage.success('头像上传成功')
}

const getRoleType = (role) => {
  const typeMap = {
    ADMIN: 'danger',
    TEACHER: 'warning',
    STUDENT: 'success'
  }
  return typeMap[role] || 'info'
}

const getRoleText = (role) => {
  const textMap = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return textMap[role] || '未知'
}

// 生命周期
onMounted(() => {
  loadUserData()
})
</script>

<style lang="scss" scoped>
.profile-page {
  .page-header {
    margin-bottom: 24px;
  }
}

.profile-card,
.password-card,
.stats-card,
.achievements-card,
.security-card {
  margin-bottom: 24px;
  
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .avatar-uploader {
    .avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      object-fit: cover;
    }
    
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 50%;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: border-color 0.3s;
      
      &:hover {
        border-color: #409eff;
      }
    }
    
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 80px;
      height: 80px;
      text-align: center;
      line-height: 80px;
    }
  }
  
  .avatar-tips {
    p {
      margin: 4px 0;
      font-size: 12px;
      color: #606266;
      
      &:first-child {
        font-weight: 500;
        color: #303133;
      }
    }
  }
}

.stats-list {
  .stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .stat-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background-color: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .stat-info {
      flex: 1;
      
      .stat-label {
        font-size: 14px;
        color: #606266;
        margin-bottom: 4px;
      }
      
      .stat-value {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
  }
}

.achievements-list {
  .achievement-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .achievement-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background-color: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    
    .achievement-info {
      flex: 1;
      
      h4 {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: #303133;
      }
      
      p {
        margin: 0 0 4px 0;
        font-size: 12px;
        color: #606266;
        line-height: 1.4;
      }
      
      .achievement-date {
        font-size: 11px;
        color: #909399;
      }
    }
  }
}

.security-list {
  .security-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .security-info {
      flex: 1;
      
      h4 {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: #303133;
      }
      
      p {
        margin: 0;
        font-size: 12px;
        color: #606266;
        line-height: 1.4;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .avatar-upload {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-list .stat-item {
    padding: 12px 0;
  }
}
</style> 