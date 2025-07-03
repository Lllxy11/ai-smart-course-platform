<template>
  <div class="learning-path">
    <div class="page-header">
      <h1>学习路径</h1>
      <div class="header-actions">
        <el-button @click="generatePath">
          <el-icon><MagicStick /></el-icon>
          AI推荐路径
        </el-button>
        <el-button @click="refreshPaths">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 学习统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ learningStats.totalPaths }}</div>
            <div class="stat-label">学习路径</div>
          </div>
          <el-icon class="stat-icon"><Guide /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ learningStats.completedPaths }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ learningStats.inProgressPaths }}</div>
            <div class="stat-label">进行中</div>
          </div>
          <el-icon class="stat-icon"><Clock /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ learningStats.totalProgress }}%</div>
            <div class="stat-label">总进度</div>
          </div>
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="搜索路径">
          <el-input
            v-model="searchForm.keyword"
            placeholder="路径名称或描述"
            @keyup.enter="handleSearch"
            style="width: 250px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="学科领域">
          <el-select v-model="searchForm.subject" placeholder="选择学科" clearable>
            <el-option
              v-for="subject in subjects"
              :key="subject.value"
              :label="subject.label"
              :value="subject.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="searchForm.difficulty" placeholder="选择难度" clearable>
            <el-option label="入门" value="BEGINNER" />
            <el-option label="初级" value="ELEMENTARY" />
            <el-option label="中级" value="INTERMEDIATE" />
            <el-option label="高级" value="ADVANCED" />
            <el-option label="专家" value="EXPERT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 推荐路径 -->
    <el-card class="recommendation-card" v-if="recommendedPaths.length > 0">
      <template #header>
        <div class="card-header">
          <span>AI推荐路径</span>
          <el-tag type="success" size="small">智能推荐</el-tag>
        </div>
      </template>
      <div class="recommendation-list">
        <div 
          v-for="path in recommendedPaths" 
          :key="path.id" 
          class="recommendation-item"
          @click="viewPath(path)"
        >
          <div class="recommendation-content">
            <h3 class="recommendation-title">{{ path.title }}</h3>
            <p class="recommendation-description">{{ path.description }}</p>
            <div class="recommendation-meta">
              <el-tag size="small">{{ path.subject }}</el-tag>
              <el-tag :type="getDifficultyTagType(path.difficulty)" size="small">
                {{ getDifficultyText(path.difficulty) }}
              </el-tag>
              <span class="recommendation-duration">预计 {{ path.estimatedDuration }} 小时</span>
            </div>
          </div>
          <div class="recommendation-actions">
            <el-button type="primary" size="small" @click.stop="startPath(path)">
              开始学习
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 学习路径列表 -->
    <div class="paths-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="path in learningPaths" :key="path.id">
          <el-card class="path-card" shadow="hover" @click="viewPath(path)">
            <div class="path-header">
              <h3 class="path-title">{{ path.title }}</h3>
              <el-tag :type="getStatusTagType(path.status)" size="small">
                {{ getStatusText(path.status) }}
              </el-tag>
            </div>
            
            <div class="path-content">
              <div class="path-description">
                {{ path.description }}
              </div>
              
              <div class="path-meta">
                <div class="meta-item">
                  <el-icon><Reading /></el-icon>
                  <span>{{ path.subject }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ getDifficultyText(path.difficulty) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Timer /></el-icon>
                  <span>{{ path.estimatedDuration }} 小时</span>
                </div>
                <div class="meta-item">
                  <el-icon><Document /></el-icon>
                  <span>{{ path.nodeCount }} 个节点</span>
                </div>
              </div>

              <!-- 进度条 -->
              <div class="progress-section">
                <div class="progress-info">
                  <span class="progress-text">学习进度</span>
                  <span class="progress-percentage">{{ path.progress }}%</span>
                </div>
                <el-progress 
                  :percentage="path.progress" 
                  :stroke-width="8"
                  :show-text="false"
                />
              </div>

              <!-- 知识点预览 -->
              <div class="knowledge-preview">
                <div class="knowledge-title">主要知识点：</div>
                <div class="knowledge-tags">
                  <el-tag
                    v-for="(point, index) in path.keyPoints.slice(0, 3)"
                    :key="index"
                    size="small"
                    style="margin-right: 5px; margin-bottom: 5px;"
                  >
                    {{ point }}
                  </el-tag>
                  <span v-if="path.keyPoints.length > 3" class="more-points">
                    +{{ path.keyPoints.length - 3 }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="path-actions" @click.stop>
              <el-button 
                v-if="path.status === 'NOT_STARTED'" 
                type="primary" 
                size="small"
                @click="startPath(path)"
              >
                开始学习
              </el-button>
              <el-button 
                v-else-if="path.status === 'IN_PROGRESS'" 
                type="warning" 
                size="small"
                @click="continuePath(path)"
              >
                继续学习
              </el-button>
              <el-button 
                v-else 
                type="success" 
                size="small"
                @click="reviewPath(path)"
              >
                复习回顾
              </el-button>
              <el-button size="small" @click="viewPath(path)">
                查看详情
              </el-button>
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

    <!-- 路径详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="学习路径详情" width="1000px">
      <div v-if="selectedPath" class="path-detail">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="路径名称" :span="2">
            {{ selectedPath.title }}
          </el-descriptions-item>
          <el-descriptions-item label="学科领域">
            {{ selectedPath.subject }}
          </el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="getDifficultyTagType(selectedPath.difficulty)">
              {{ getDifficultyText(selectedPath.difficulty) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预计时长">
            {{ selectedPath.estimatedDuration }} 小时
          </el-descriptions-item>
          <el-descriptions-item label="节点数量">
            {{ selectedPath.nodeCount }} 个
          </el-descriptions-item>
          <el-descriptions-item label="学习进度">
            {{ selectedPath.progress }}%
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedPath.status)">
              {{ getStatusText(selectedPath.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="路径描述" :span="2">
            {{ selectedPath.description }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 学习路径图 -->
        <div class="path-graph" v-if="selectedPath.nodes">
          <h3>学习路径图</h3>
          <div class="graph-container">
            <div 
              v-for="(node, index) in selectedPath.nodes" 
              :key="node.id" 
              class="path-node"
              :class="{
                'completed': node.status === 'COMPLETED',
                'current': node.status === 'IN_PROGRESS',
                'locked': node.status === 'LOCKED'
              }"
            >
              <div class="node-content">
                <div class="node-number">{{ index + 1 }}</div>
                <div class="node-info">
                  <div class="node-title">{{ node.title }}</div>
                  <div class="node-type">{{ getNodeTypeText(node.type) }}</div>
                  <div class="node-duration">{{ node.duration }}分钟</div>
                </div>
                <div class="node-status">
                  <el-icon v-if="node.status === 'COMPLETED'" class="status-icon completed">
                    <CircleCheck />
                  </el-icon>
                  <el-icon v-else-if="node.status === 'IN_PROGRESS'" class="status-icon current">
                    <Clock />
                  </el-icon>
                  <el-icon v-else class="status-icon locked">
                    <Lock />
                  </el-icon>
                </div>
              </div>
              
              <!-- 连接线 -->
              <div v-if="index < selectedPath.nodes.length - 1" class="node-connector"></div>
            </div>
          </div>
        </div>

        <!-- 学习统计 -->
        <div class="learning-statistics" v-if="pathStatistics">
          <h3>学习统计</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="已完成节点" :value="pathStatistics.completedNodes" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总学习时长" :value="pathStatistics.totalStudyTime" suffix="分钟" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="平均得分" :value="pathStatistics.averageScore" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="掌握度" :value="pathStatistics.masteryLevel" suffix="%" />
            </el-col>
          </el-row>
        </div>

        <!-- 知识点掌握情况 -->
        <div class="knowledge-mastery" v-if="selectedPath.keyPoints">
          <h3>知识点掌握情况</h3>
          <div class="mastery-list">
            <div 
              v-for="(point, index) in selectedPath.keyPoints" 
              :key="index"
              class="mastery-item"
            >
              <span class="point-name">{{ point }}</span>
              <el-progress 
                :percentage="getMasteryPercentage(point)" 
                :stroke-width="6"
                :color="getMasteryColor(getMasteryPercentage(point))"
              />
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button 
          v-if="selectedPath && selectedPath.status !== 'COMPLETED'" 
          type="primary" 
          @click="startOrContinuePath(selectedPath)"
        >
          {{ selectedPath.status === 'NOT_STARTED' ? '开始学习' : '继续学习' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- AI路径生成对话框 -->
    <el-dialog v-model="showGenerateDialog" title="AI智能推荐学习路径" width="600px">
      <el-form :model="generateForm" label-width="120px">
        <el-form-item label="学习目标">
          <el-input
            v-model="generateForm.goal"
            type="textarea"
            :rows="3"
            placeholder="请描述您的学习目标，例如：想要掌握Python Web开发"
          />
        </el-form-item>
        <el-form-item label="当前水平">
          <el-select v-model="generateForm.currentLevel" placeholder="选择当前水平">
            <el-option label="零基础" value="BEGINNER" />
            <el-option label="有一定基础" value="ELEMENTARY" />
            <el-option label="中等水平" value="INTERMEDIATE" />
            <el-option label="较高水平" value="ADVANCED" />
          </el-select>
        </el-form-item>
        <el-form-item label="可用时间">
          <el-select v-model="generateForm.availableTime" placeholder="选择可用时间">
            <el-option label="每天1小时以内" value="1" />
            <el-option label="每天1-2小时" value="2" />
            <el-option label="每天2-4小时" value="4" />
            <el-option label="每天4小时以上" value="8" />
          </el-select>
        </el-form-item>
        <el-form-item label="学习偏好">
          <el-checkbox-group v-model="generateForm.preferences">
            <el-checkbox label="理论学习">理论学习</el-checkbox>
            <el-checkbox label="实践练习">实践练习</el-checkbox>
            <el-checkbox label="项目实战">项目实战</el-checkbox>
            <el-checkbox label="视频教程">视频教程</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showGenerateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleGeneratePath" :loading="generating">
          生成路径
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  Refresh,
  Search,
  Guide,
  Checked,
  Clock,
  TrendCharts,
  Reading,
  Star,
  Timer,
  Document,
  CircleCheck,
  Lock
} from '@element-plus/icons-vue'
import { getLearningPaths, getRecommendedPaths, generateLearningPath, startLearningPath } from '@/api/learningPath'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const generating = ref(false)
const learningPaths = ref([])
const recommendedPaths = ref([])
const learningStats = ref({
  totalPaths: 0,
  completedPaths: 0,
  inProgressPaths: 0,
  totalProgress: 0
})

// 对话框控制
const showDetailDialog = ref(false)
const showGenerateDialog = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  subject: '',
  difficulty: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 9,
  total: 0
})

// AI生成表单
const generateForm = reactive({
  goal: '',
  currentLevel: '',
  availableTime: '',
  preferences: []
})

// 选中的路径
const selectedPath = ref(null)
const pathStatistics = ref(null)

// 学科列表
const subjects = [
  { label: 'Python编程', value: 'python' },
  { label: 'Web前端', value: 'frontend' },
  { label: '数据科学', value: 'data_science' },
  { label: '机器学习', value: 'machine_learning' },
  { label: '数据库', value: 'database' }
]

// 生命周期
onMounted(() => {
  loadLearningPaths()
  loadRecommendedPaths()
})

// 方法
const loadLearningPaths = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      subject: searchForm.subject,
      difficulty: searchForm.difficulty,
      status: searchForm.status,
      studentId: authStore.user.id
    }
    
    const response = await getLearningPaths(params)
    learningPaths.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 计算统计数据
    learningStats.value = {
      totalPaths: learningPaths.value.length,
      completedPaths: learningPaths.value.filter(p => p.status === 'COMPLETED').length,
      inProgressPaths: learningPaths.value.filter(p => p.status === 'IN_PROGRESS').length,
      totalProgress: learningPaths.value.length > 0 ? 
        (learningPaths.value.reduce((sum, p) => sum + (p.progress || 0), 0) / learningPaths.value.length).toFixed(0) : 0
    }
  } catch (error) {
    ElMessage.error('加载学习路径失败')
    console.error('Failed to load learning paths:', error)
  } finally {
    loading.value = false
  }
}

const loadRecommendedPaths = async () => {
  try {
    const response = await getRecommendedPaths({ studentId: authStore.user.id })
    recommendedPaths.value = response.data || []
  } catch (error) {
    console.error('加载推荐路径失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadLearningPaths()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.subject = ''
  searchForm.difficulty = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadLearningPaths()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadLearningPaths()
}

const generatePath = () => {
  showGenerateDialog.value = true
}

const refreshPaths = () => {
  loadLearningPaths()
  loadRecommendedPaths()
}

const handleGeneratePath = async () => {
  if (!generateForm.goal) {
    ElMessage.warning('请输入学习目标')
    return
  }
  
  generating.value = true
  try {
    await generateLearningPath(generateForm)
    ElMessage.success('AI路径生成成功！')
    showGenerateDialog.value = false
    loadLearningPaths()
    loadRecommendedPaths()
  } catch (error) {
    ElMessage.error('生成学习路径失败')
  } finally {
    generating.value = false
  }
}

const startPath = async (path) => {
  try {
    await startLearningPath(path.id)
    ElMessage.success('开始学习路径')
    router.push(`/student/learning-path/${path.id}/study`)
  } catch (error) {
    ElMessage.error('开始学习失败')
  }
}

const continuePath = (path) => {
  router.push(`/student/learning-path/${path.id}/study`)
}

const reviewPath = (path) => {
  router.push(`/student/learning-path/${path.id}/review`)
}

const viewPath = async (path) => {
  selectedPath.value = path
  showDetailDialog.value = true
  
  try {
    const response = await getLearningPathDetail(path.id)
    if (response.data) {
      pathStatistics.value = {
        completedNodes: response.data.completedNodes || Math.floor(path.nodeCount * path.progress / 100),
        totalStudyTime: response.data.totalStudyTime || Math.floor(path.estimatedDuration * path.progress / 100 * 60),
        averageScore: response.data.averageScore || 0,
        masteryLevel: response.data.masteryLevel || path.progress
      }
    }
  } catch (error) {
    console.error('加载学习路径详情失败:', error)
    ElMessage.error('加载学习路径详情失败')
    throw error
  }
}

const startOrContinuePath = (path) => {
  showDetailDialog.value = false
  if (path.status === 'NOT_STARTED') {
    startPath(path)
  } else {
    continuePath(path)
  }
}

// 工具方法
const getStatusText = (status) => {
  const statusMap = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    NOT_STARTED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success'
  }
  return typeMap[status] || ''
}

const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    BEGINNER: '入门',
    ELEMENTARY: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级',
    EXPERT: '专家'
  }
  return difficultyMap[difficulty] || difficulty
}

const getDifficultyTagType = (difficulty) => {
  const typeMap = {
    BEGINNER: 'success',
    ELEMENTARY: 'primary',
    INTERMEDIATE: 'warning',
    ADVANCED: 'danger',
    EXPERT: 'info'
  }
  return typeMap[difficulty] || ''
}

const getNodeTypeText = (type) => {
  const typeMap = {
    VIDEO: '视频',
    READING: '阅读',
    EXERCISE: '练习',
    PROJECT: '项目',
    QUIZ: '测验'
  }
  return typeMap[type] || type
}

const getMasteryPercentage = (point) => {
  // 这里应该从API获取真实的掌握度数据
  // 暂时返回基于知识点名称的计算值
  const hash = point.split('').reduce((a, b) => {
    a = ((a << 5) - a) + b.charCodeAt(0)
    return a & a
  }, 0)
  return Math.abs(hash) % 100
}

const getMasteryColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.learning-path {
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

.recommendation-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommendation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.recommendation-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.recommendation-content {
  flex: 1;
}

.recommendation-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.recommendation-description {
  margin: 0 0 10px 0;
  color: #606266;
  line-height: 1.5;
}

.recommendation-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.recommendation-duration {
  color: #909399;
  font-size: 14px;
}

.recommendation-actions {
  margin-left: 20px;
}

.paths-grid {
  margin-bottom: 20px;
}

.path-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
  cursor: pointer;
  height: 450px;
}

.path-card:hover {
  transform: translateY(-5px);
}

.path-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.path-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 10px;
}

.path-content {
  margin-bottom: 15px;
}

.path-description {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 15px;
  height: 48px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.path-meta {
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.progress-section {
  margin-bottom: 15px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 14px;
  color: #606266;
}

.progress-percentage {
  font-size: 14px;
  font-weight: bold;
  color: #409eff;
}

.knowledge-preview {
  margin-bottom: 15px;
}

.knowledge-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.knowledge-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.more-points {
  color: #909399;
  font-size: 12px;
}

.path-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.path-detail {
  padding: 20px 0;
}

.path-graph {
  margin-top: 30px;
}

.path-graph h3 {
  margin-bottom: 20px;
  color: #303133;
}

.graph-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.path-node {
  position: relative;
  display: flex;
  align-items: center;
  padding: 15px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.3s;
}

.path-node.completed {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.path-node.current {
  border-color: #e6a23c;
  background-color: #fdf6ec;
}

.path-node.locked {
  border-color: #c0c4cc;
  background-color: #f5f7fa;
  opacity: 0.6;
}

.node-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.node-number {
  width: 40px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  font-weight: bold;
  margin-right: 15px;
}

.node-info {
  flex: 1;
}

.node-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.node-type {
  font-size: 14px;
  color: #606266;
  margin-bottom: 3px;
}

.node-duration {
  font-size: 12px;
  color: #909399;
}

.node-status {
  margin-left: 15px;
}

.status-icon {
  font-size: 24px;
}

.status-icon.completed {
  color: #67c23a;
}

.status-icon.current {
  color: #e6a23c;
}

.status-icon.locked {
  color: #c0c4cc;
}

.node-connector {
  position: absolute;
  left: 34px;
  top: 100%;
  width: 2px;
  height: 15px;
  background-color: #ebeef5;
}

.learning-statistics {
  margin-top: 30px;
}

.learning-statistics h3 {
  margin-bottom: 20px;
  color: #303133;
}

.knowledge-mastery {
  margin-top: 30px;
}

.knowledge-mastery h3 {
  margin-bottom: 20px;
  color: #303133;
}

.mastery-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.mastery-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.point-name {
  width: 150px;
  font-size: 14px;
  color: #303133;
}
</style> 