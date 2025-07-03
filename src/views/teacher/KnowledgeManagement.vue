<template>
  <div class="knowledge-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>知识点管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          添加知识点
        </el-button>
        <el-button @click="loadKnowledgePoints" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选控制面板 -->
    <el-card class="filter-panel" shadow="never">
      <el-form :model="filterForm" inline>
        <el-form-item label="课程：">
          <el-select 
            v-model="filterForm.courseId" 
            placeholder="选择课程" 
            @change="handleCourseChange"
            style="width: 200px"
          >
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name || course.title"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="知识点类型：">
          <el-select 
            v-model="filterForm.pointType" 
            placeholder="选择类型" 
            @change="loadKnowledgePoints"
            style="width: 120px"
            clearable
          >
            <el-option label="概念" value="concept" />
            <el-option label="技能" value="skill" />
            <el-option label="应用" value="application" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadKnowledgePoints">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主内容区域 -->
    <el-row :gutter="20">
      <!-- 左侧：知识点列表 -->
      <el-col :span="16">
        <el-card class="knowledge-list-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>知识点列表 ({{ knowledgePoints.length }})</span>
              <el-tag :type="getStatusTagType(filterForm.pointType)" v-if="filterForm.pointType">
                {{ getTypeText(filterForm.pointType) }}
              </el-tag>
            </div>
          </template>
          
          <div v-loading="loading" class="knowledge-list">
            <div 
              v-for="point in knowledgePoints" 
              :key="point.id"
              class="knowledge-item"
              :class="{ 'selected': selectedPoint?.id === point.id }"
              @click="selectKnowledgePoint(point)"
            >
              <div class="knowledge-content">
                <div class="knowledge-header">
                  <h4 class="knowledge-title">{{ point.name }}</h4>
                  <div class="knowledge-badges">
                    <el-tag :type="getStatusTagType(point.type)" size="small">
                      {{ getTypeText(point.type) }}
                    </el-tag>
                    <el-tag type="info" size="small">
                      难度: {{ point.difficulty }}/5
                    </el-tag>
                  </div>
                </div>
                <p class="knowledge-description">{{ point.description }}</p>
                <div class="knowledge-meta">
                  <span class="meta-item">
                    <el-icon><Clock /></el-icon>
                    {{ point.estimatedTime || 0 }}分钟
                  </span>
                  <span class="meta-item">
                    <el-icon><Star /></el-icon>
                    重要程度: {{ Math.round((point.importance || 0) * 100) }}%
                  </span>
                </div>
              </div>
              <div class="knowledge-actions">
                <el-button size="small" @click.stop="editKnowledgePoint(point)">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="handleDeleteKnowledgePoint(point)">删除</el-button>
              </div>
            </div>
            
            <el-empty v-if="knowledgePoints.length === 0 && !loading" 
                     description="暂无知识点" 
                     :image-size="80">
              <el-button type="primary" @click="showCreateDialog = true">
                添加第一个知识点
              </el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧：知识点详情/关系 -->
      <el-col :span="8">
        <el-card v-if="selectedPoint" class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ selectedPoint.name }}</span>
              <el-tag :type="getStatusTagType(selectedPoint.type)" size="small">
                {{ getTypeText(selectedPoint.type) }}
              </el-tag>
            </div>
          </template>
          
          <div class="point-detail">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-item">
                <label>描述：</label>
                <p>{{ selectedPoint.description || '暂无描述' }}</p>
              </div>
              <div class="detail-item">
                <label>难度等级：</label>
                <el-rate :model-value="selectedPoint.difficulty || 1" :max="5" disabled size="small" />
              </div>
              <div class="detail-item">
                <label>重要程度：</label>
                <el-progress 
                  :percentage="Math.round((selectedPoint.importance || 0) * 100)" 
                  :stroke-width="8"
                />
              </div>
              <div class="detail-item">
                <label>预估学时：</label>
                <span>{{ selectedPoint.estimatedTime || 0 }} 分钟</span>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>前置知识点</h4>
              <div v-if="prerequisites.length > 0" class="relation-list">
                <div v-for="prereq in prerequisites" :key="prereq.id" class="relation-item">
                  <span>{{ prereq.name }}</span>
                  <el-tag size="small" type="warning">前置</el-tag>
                </div>
              </div>
              <el-empty v-else description="无前置知识点" :image-size="60" />
            </div>
            
            <div class="detail-section">
              <h4>后续知识点</h4>
              <div v-if="subsequents.length > 0" class="relation-list">
                <div v-for="subseq in subsequents" :key="subseq.id" class="relation-item">
                  <span>{{ subseq.name }}</span>
                  <el-tag size="small" type="success">后续</el-tag>
                </div>
              </div>
              <el-empty v-else description="无后续知识点" :image-size="60" />
            </div>
          </div>
        </el-card>
        
        <!-- 统计信息卡片 -->
        <el-card class="stats-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>统计信息</span>
              <el-icon><TrendCharts /></el-icon>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalPoints }}</div>
              <div class="stat-label">知识点总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.conceptCount }}</div>
              <div class="stat-label">概念类型</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.skillCount }}</div>
              <div class="stat-label">技能类型</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.applicationCount }}</div>
              <div class="stat-label">应用类型</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑知识点对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingPoint ? '编辑知识点' : '创建知识点'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        :model="pointForm"
        :rules="pointRules"
        ref="pointFormRef"
        label-width="100px"
      >
        <el-form-item label="知识点名称" prop="name">
          <el-input v-model="pointForm.name" placeholder="请输入知识点名称" />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="pointForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入知识点描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="pointType">
              <el-select v-model="pointForm.pointType" placeholder="选择类型">
                <el-option label="概念" value="concept" />
                <el-option label="技能" value="skill" />
                <el-option label="应用" value="application" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-rate v-model="pointForm.difficultyLevel" :max="5" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="重要程度" prop="importance">
              <el-slider
                v-model="pointForm.importance"
                :min="0"
                :max="1"
                :step="0.1"
                :format-tooltip="formatImportance"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预估时长" prop="estimatedTime">
              <el-input-number
                v-model="pointForm.estimatedTime"
                :min="5"
                :max="480"
                :step="5"
              />
              <span style="margin-left: 10px;">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSavePoint" :loading="saveLoading">
          {{ editingPoint ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Clock,
  Star,
  TrendCharts
} from '@element-plus/icons-vue'
import { 
  getKnowledgePoints, 
  getKnowledgeStatistics, 
  createKnowledgePoint, 
  updateKnowledgePoint, 
  deleteKnowledgePoint,
  getKnowledgePointRelations 
} from '@/api/knowledge'
import { getCourses, getCourseList } from '@/api/course'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const showCreateDialog = ref(false)
const courses = ref([])
const knowledgePoints = ref([])
const selectedPoint = ref(null)
const editingPoint = ref(null)

// 筛选表单
const filterForm = reactive({
  courseId: null,
  pointType: ''
})

// 知识点表单
const pointForm = reactive({
  name: '',
  description: '',
  pointType: 'concept',
  difficultyLevel: 1,
  importance: 0.5,
  estimatedTime: 30
})

// 表单引用
const pointFormRef = ref()

// 统计数据
const stats = ref({
  totalPoints: 0,
  conceptCount: 0,
  skillCount: 0,
  applicationCount: 0
})

// 关系数据
const prerequisites = ref([])
const subsequents = ref([])

// 表单验证规则
const pointRules = {
  name: [
    { required: true, message: '请输入知识点名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  pointType: [
    { required: true, message: '请选择知识点类型', trigger: 'change' }
  ]
}

// 计算属性
const currentCourse = computed(() => {
  return courses.value.find(c => c.id === filterForm.courseId)
})

// 生命周期
onMounted(() => {
  loadCourses()
})

// 方法
const loadCourses = async () => {
  try {
    // 使用与教师端课程管理页面相同的API和参数
    const params = {
      page: 0,
      size: 100, // 加载更多课程以供选择
      teacherId: authStore.user?.id // 直接通过后端过滤
    }
    
    console.log('知识点管理-加载课程列表，参数：', params)
    const response = await getCourseList(params)
    console.log('知识点管理-课程列表响应：', response.data)
    
    if (response.data && response.data.content) {
      courses.value = response.data.content
      console.log('知识点管理-处理后的课程数据：', courses.value)
    } else {
      courses.value = []
      console.log('知识点管理-没有课程数据')
    }
    
    // 默认选择第一个课程
    if (courses.value.length > 0 && !filterForm.courseId) {
      filterForm.courseId = courses.value[0].id
      console.log('知识点管理-自动选择课程ID：', filterForm.courseId)
      await loadKnowledgePoints()
    } else if (courses.value.length === 0) {
      ElMessage.warning('您还没有创建任何课程，请先去课程管理页面创建课程')
    }
  } catch (error) {
    console.error('知识点管理-加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败: ' + (error.message || '未知错误'))
  }
}

const loadKnowledgePoints = async () => {
  if (!filterForm.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }
  
  loading.value = true
  try {
    const params = {
      courseId: filterForm.courseId,
      pointType: filterForm.pointType || undefined
    }
    
    const response = await getKnowledgePoints(params)
    knowledgePoints.value = response.data.data || []
    
    // 加载统计数据
    await loadStatistics()
    
    ElMessage.success(`加载了 ${knowledgePoints.value.length} 个知识点`)
  } catch (error) {
    console.error('Failed to load knowledge points:', error)
    ElMessage.error('加载知识点失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getKnowledgeStatistics({ courseId: filterForm.courseId })
    stats.value = response.data
  } catch (error) {
    console.error('Failed to load statistics:', error)
  }
}

const handleCourseChange = () => {
  selectedPoint.value = null
  prerequisites.value = []
  subsequents.value = []
  loadKnowledgePoints()
}

const selectKnowledgePoint = (point) => {
  selectedPoint.value = point
  // TODO: 加载关系数据
  loadRelations(point.id)
}

const loadRelations = async (pointId) => {
  try {
    const response = await getKnowledgePointRelations(pointId)
    prerequisites.value = response.data.prerequisites || []
    subsequents.value = response.data.subsequents || []
  } catch (error) {
    console.error('Failed to load relations:', error)
    prerequisites.value = []
    subsequents.value = []
  }
}

const editKnowledgePoint = (point) => {
  editingPoint.value = point
  Object.assign(pointForm, {
    name: point.name,
    description: point.description,
    pointType: point.type,
    difficultyLevel: point.difficulty || 1,
    importance: point.importance || 0.5,
    estimatedTime: point.estimatedTime || 30
  })
  showCreateDialog.value = true
}

const handleSavePoint = async () => {
  if (!pointFormRef.value) return
  
  try {
    await pointFormRef.value.validate()
    saveLoading.value = true
    
    const pointData = {
      name: pointForm.name,
      description: pointForm.description,
      courseId: filterForm.courseId,
      pointType: pointForm.pointType,
      difficultyLevel: pointForm.difficultyLevel,
      importance: pointForm.importance,
      estimatedTime: pointForm.estimatedTime
    }
    
    console.log('前端发送的数据:', pointData)
    console.log('各字段类型:', {
      name: typeof pointData.name,
      description: typeof pointData.description,
      courseId: typeof pointData.courseId,
      pointType: typeof pointData.pointType,
      difficultyLevel: typeof pointData.difficultyLevel,
      importance: typeof pointData.importance,
      estimatedTime: typeof pointData.estimatedTime
    })
    
    if (editingPoint.value) {
      await updateKnowledgePoint(editingPoint.value.id, pointData)
      ElMessage.success('知识点更新成功')
    } else {
      await createKnowledgePoint(pointData)
      ElMessage.success('知识点创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    await loadKnowledgePoints()
  } catch (error) {
    console.error('Save failed:', error)
    ElMessage.error('保存失败: ' + (error.response?.data?.error || error.message))
  } finally {
    saveLoading.value = false
  }
}

const handleDeleteKnowledgePoint = async (point) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除知识点 "${point.name}" 吗？此操作不可恢复！`,
      '删除知识点',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteKnowledgePoint(point.id)
    ElMessage.success('知识点删除成功')
    
    // 如果删除的是当前选中的知识点，清空选择
    if (selectedPoint.value?.id === point.id) {
      selectedPoint.value = null
      prerequisites.value = []
      subsequents.value = []
    }
    
    await loadKnowledgePoints()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
      ElMessage.error('删除失败: ' + (error.response?.data?.error || error.message))
    }
  }
}

const resetForm = () => {
  editingPoint.value = null
  Object.assign(pointForm, {
    name: '',
    description: '',
    pointType: 'concept',
    difficultyLevel: 1,
    importance: 0.5,
    estimatedTime: 30
  })
  pointFormRef.value?.resetFields()
}

const resetFilters = () => {
  filterForm.pointType = ''
  loadKnowledgePoints()
}

// 工具方法
const getTypeText = (type) => {
  const typeMap = {
    concept: '概念',
    skill: '技能',
    application: '应用'
  }
  return typeMap[type] || type
}

const getStatusTagType = (type) => {
  const typeMap = {
    concept: 'primary',
    skill: 'success',
    application: 'warning'
  }
  return typeMap[type] || ''
}

const formatImportance = (value) => {
  return `${Math.round(value * 100)}%`
}
</script>

<style scoped>
.knowledge-management {
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

.filter-panel {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.knowledge-list {
  max-height: 600px;
  overflow-y: auto;
}

.knowledge-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.knowledge-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.knowledge-item.selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.knowledge-content {
  flex: 1;
}

.knowledge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.knowledge-title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.knowledge-badges {
  display: flex;
  gap: 5px;
}

.knowledge-description {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.knowledge-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.knowledge-actions {
  display: flex;
  gap: 8px;
}

.detail-card {
  margin-bottom: 20px;
}

.point-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 5px;
}

.detail-item {
  margin-bottom: 10px;
}

.detail-item label {
  font-weight: bold;
  color: #606266;
}

.detail-item p {
  margin: 5px 0 0 0;
  color: #303133;
}

.relation-list {
  max-height: 150px;
  overflow-y: auto;
}

.relation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 5px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 12px;
}
</style> 