<template>
  <div class="user-management">
    <div class="page-header">
      <h1>用户管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建用户
        </el-button>
        <el-button type="success" @click="exportUsers">
          <el-icon><Download /></el-icon>
          导出用户
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="搜索用户">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名、邮箱或姓名"
            @keyup.enter="handleSearch"
            style="width: 250px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="选择角色" clearable>
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isActive" placeholder="选择状态" clearable>
            <el-option label="激活" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <el-icon class="stat-icon"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.activeUsers }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
          <el-icon class="stat-icon"><UserFilled /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.newUsersToday }}</div>
            <div class="stat-label">今日新增</div>
          </div>
          <el-icon class="stat-icon"><Plus /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.onlineUsers }}</div>
            <div class="stat-label">在线用户</div>
          </div>
          <el-icon class="stat-icon"><Connection /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>用户列表</span>
          <div class="batch-actions" v-if="selectedUsers.length > 0">
            <el-button size="small" @click="batchActivate">批量激活</el-button>
            <el-button size="small" @click="batchDeactivate">批量禁用</el-button>
            <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="users"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatarUrl" :size="40">
              {{ row.fullName?.charAt(0) || row.username.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="fullName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '激活' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastLogin) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewUser(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editUser(row)">编辑</el-button>
            <el-dropdown @command="handleUserAction">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'toggle', user: row}">
                    {{ row.isActive ? '禁用' : '激活' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'resetPassword', user: row}">
                    重置密码
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'delete', user: row}" divided>
                    删除用户
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

    <!-- 创建用户对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建用户"
      width="600px"
      @close="resetCreateForm"
    >
      <el-form
        :model="createForm"
        :rules="createRules"
        ref="createFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="fullName">
          <el-input v-model="createForm.fullName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="createForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="createForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateUser" :loading="createLoading">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑用户"
      width="600px"
      @close="resetEditForm"
    >
      <el-form
        :model="editForm"
        :rules="editRules"
        ref="editFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="fullName">
          <el-input v-model="editForm.fullName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-radio-group v-model="editForm.isActive">
            <el-radio :label="true">激活</el-radio>
            <el-radio :label="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateUser" :loading="updateLoading">
          更新
        </el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="用户详情"
      width="800px"
    >
      <div v-if="selectedUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="头像">
            <el-avatar :src="selectedUser.avatarUrl" :size="60">
              {{ selectedUser.fullName?.charAt(0) || selectedUser.username.charAt(0) }}
            </el-avatar>
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ selectedUser.username }}
          </el-descriptions-item>
          <el-descriptions-item label="真实姓名">
            {{ selectedUser.fullName || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ selectedUser.email }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleTagType(selectedUser.role)">
              {{ getRoleText(selectedUser.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.isActive ? 'success' : 'danger'">
              {{ selectedUser.isActive ? '激活' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(selectedUser.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="最后登录">
            {{ formatTime(selectedUser.lastLogin) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 登录设备信息 -->
        <div class="login-devices" v-if="loginDevices.length > 0">
          <h3>登录设备</h3>
          <el-table :data="loginDevices" size="small">
            <el-table-column prop="deviceInfo" label="设备信息" />
            <el-table-column prop="ipAddress" label="IP地址" />
            <el-table-column prop="location" label="位置" />
            <el-table-column prop="lastActiveTime" label="最后活跃时间">
              <template #default="{ row }">
                {{ formatTime(row.lastActiveTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                                        <el-button size="small" type="danger" @click="handleLogoutDevice(row.sessionId)">
                  强制下线
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
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
  User,
  UserFilled,
  Connection,
  ArrowDown
} from '@element-plus/icons-vue'
import { getUserList, getUserStatistics, createUser, updateUser, deleteUser, activateUser, deactivateUser, resetUserPassword, batchActivateUsers, batchDeactivateUsers, batchDeleteUsers } from '@/api/user'
import { getLoginDevices, logoutDevice } from '@/api/auth'

// 响应式数据
const loading = ref(false)
const createLoading = ref(false)
const updateLoading = ref(false)
const users = ref([])
const selectedUsers = ref([])
const userStats = ref({
  totalUsers: 0,
  activeUsers: 0,
  newUsersToday: 0,
  onlineUsers: 0
})

// 对话框控制
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showDetailDialog = ref(false)

// 表单引用
const createFormRef = ref()
const editFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  isActive: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 创建用户表单
const createForm = reactive({
  username: '',
  email: '',
  fullName: '',
  role: '',
  password: '',
  confirmPassword: ''
})

// 编辑用户表单
const editForm = reactive({
  id: '',
  username: '',
  email: '',
  fullName: '',
  role: '',
  isActive: true
})

// 选中的用户详情
const selectedUser = ref(null)
const loginDevices = ref([])

// 表单验证规则
const createRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== createForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const editRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  loadUsers()
  loadUserStats()
})

// 方法
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword,
      role: searchForm.role,
      isActive: searchForm.isActive
    }
    console.log('加载用户参数:', params)
    const response = await getUserList(params)
    users.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadUserStats = async () => {
  try {
    const response = await getUserStatistics()
    userStats.value = response.data
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.isActive = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadUsers()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadUsers()
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleCreateUser = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    createLoading.value = true
    
    await createUser(createForm)
    ElMessage.success('用户创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    loadUsers()
    loadUserStats()
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('创建用户失败')
    }
  } finally {
    createLoading.value = false
  }
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    username: '',
    email: '',
    fullName: '',
    role: '',
    password: '',
    confirmPassword: ''
  })
  createFormRef.value?.resetFields()
}

const editUser = (user) => {
  Object.assign(editForm, {
    id: user.id,
    username: user.username,
    email: user.email,
    fullName: user.fullName,
    role: user.role,
    isActive: user.isActive
  })
  showEditDialog.value = true
}

const handleUpdateUser = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    updateLoading.value = true
    
    const response = await updateUser(editForm.id, editForm)
    ElMessage.success('用户更新成功')
    
    // 更新本地数据
    const userIndex = users.value.findIndex(u => u.id === editForm.id)
    if (userIndex !== -1) {
      users.value[userIndex] = { ...users.value[userIndex], ...response.data }
    }
    
    showEditDialog.value = false
    resetEditForm()
    loadUsers()
    loadUserStats()
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('更新用户失败')
    }
  } finally {
    updateLoading.value = false
  }
}

const resetEditForm = () => {
  Object.assign(editForm, {
    id: '',
    username: '',
    email: '',
    fullName: '',
    role: '',
    isActive: true
  })
  editFormRef.value?.resetFields()
}

const viewUser = async (user) => {
  selectedUser.value = user
  showDetailDialog.value = true
  
  // 加载登录设备信息
  try {
    const response = await getLoginDevices(user.id)
    loginDevices.value = response.data
  } catch (error) {
    console.error('加载登录设备失败:', error)
  }
}

const handleUserAction = async ({ action, user }) => {
  switch (action) {
    case 'toggle':
      await toggleUserStatus(user)
      break
    case 'resetPassword':
      await resetPassword(user)
      break
    case 'delete':
      await deleteUserConfirm(user)
      break
  }
}

const toggleUserStatus = async (user) => {
  try {
    if (user.isActive) {
      await deactivateUser(user.id)
      ElMessage.success('用户已禁用')
      // 更新本地数据
      user.isActive = false
    } else {
      await activateUser(user.id)
      ElMessage.success('用户已激活')
      // 更新本地数据
      user.isActive = true
    }
    
    // 如果当前用户正在编辑中，也更新编辑表单数据
    if (selectedUser.value && selectedUser.value.id === user.id) {
      selectedUser.value.isActive = user.isActive
    }
    if (editForm.id === user.id) {
      editForm.isActive = user.isActive
    }
    
    loadUsers()
    loadUserStats()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 ${user.username} 的密码吗？`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await resetUserPassword(user.id)
    ElMessage.success('密码重置成功，新密码已发送到用户邮箱')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}

const deleteUserConfirm = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteUser(user.id)
    ElMessage.success('用户删除成功')
    loadUsers()
    loadUserStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
    }
  }
}

const batchActivate = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要激活的用户')
    return
  }
  
  try {
    const userIds = selectedUsers.value.map(user => user.id)
    await batchActivateUsers(userIds)
    ElMessage.success('批量激活成功')
    loadUsers()
    loadUserStats()
  } catch (error) {
    ElMessage.error('批量激活失败')
  }
}

const batchDeactivate = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要禁用的用户')
    return
  }
  
  try {
    const userIds = selectedUsers.value.map(user => user.id)
    await batchDeactivateUsers(userIds)
    ElMessage.success('批量禁用成功')
    loadUsers()
    loadUserStats()
  } catch (error) {
    ElMessage.error('批量禁用失败')
  }
}

const batchDelete = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复！`,
      '批量删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const userIds = selectedUsers.value.map(user => user.id)
    await batchDeleteUsers(userIds)
    ElMessage.success('批量删除成功')
    loadUsers()
    loadUserStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const exportUsers = () => {
  // 实现用户导出功能
  ElMessage.info('导出功能开发中...')
}

const handleLogoutDevice = async (sessionId) => {
  try {
    await logoutDevice(sessionId)
    ElMessage.success('设备已强制下线')
    // 重新加载设备列表
    if (selectedUser.value) {
      const response = await getLoginDevices(selectedUser.value.id)
      loginDevices.value = response.data
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 工具方法
const getRoleText = (role) => {
  const roleMap = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return roleMap[role] || role
}

const getRoleTagType = (role) => {
  const typeMap = {
    STUDENT: '',
    TEACHER: 'success',
    ADMIN: 'danger'
  }
  return typeMap[role] || ''
}

const formatTime = (time) => {
  if (!time) return '从未'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.user-management {
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

.user-detail {
  padding: 20px 0;
}

.login-devices {
  margin-top: 30px;
}

.login-devices h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style> 