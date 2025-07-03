<template>
  <div class="knowledge-graph">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>知识图谱</h1>
      <div class="header-actions">
        <el-button @click="refreshGraph" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新图谱
        </el-button>
        <el-button @click="resetView">
          <el-icon><FullScreen /></el-icon>
          重置视图
        </el-button>
      </div>
    </div>

    <!-- 筛选控制面板 -->
    <el-card class="filter-panel" shadow="never">
      <div class="filter-content">
        <el-form :model="filterForm" inline>
          <el-form-item label="课程：">
            <el-select 
              v-model="filterForm.courseId" 
              placeholder="选择课程" 
              @change="handleCourseChange"
              style="width: 180px"
            >
              <el-option label="全部课程" :value="null" />
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="知识点类型：">
            <el-select 
              v-model="filterForm.pointType" 
              placeholder="选择类型" 
              @change="handleTypeChange"
              style="width: 120px"
            >
              <el-option label="全部" value="" />
              <el-option label="概念" value="concept" />
              <el-option label="技能" value="skill" />
              <el-option label="应用" value="application" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="loadKnowledgeGraph">应用筛选</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 主内容区域 -->
    <el-row :gutter="20" class="main-content">
      <!-- 图谱可视化区域 -->
      <el-col :span="18">
        <el-card class="graph-container" shadow="never">
          <template #header>
            <div class="graph-header">
              <span>知识图谱可视化</span>
              <div class="graph-tools">
                <el-tooltip content="放大">
                  <el-button size="small" circle @click="zoomIn">
                    <el-icon><ZoomIn /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="缩小">
                  <el-button size="small" circle @click="zoomOut">
                    <el-icon><ZoomOut /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="适应屏幕">
                  <el-button size="small" circle @click="fitView">
                    <el-icon><FullScreen /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </template>
          
          <div 
            ref="graphContainer" 
            class="graph-canvas" 
            v-loading="loading"
            element-loading-text="正在加载知识图谱..."
          >
            <!-- ECharts图表将在这里渲染 -->
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧信息面板 -->
      <el-col :span="6">
        <!-- 学习统计 -->
        <el-card class="stats-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>统计信息</span>
              <el-icon><TrendCharts /></el-icon>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">{{ graphStats.totalNodes || 0 }}</div>
              <div class="stat-label">知识点总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ graphStats.totalLinks || 0 }}</div>
              <div class="stat-label">连接数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ graphStats.queryTime || 0 }}ms</div>
              <div class="stat-label">查询耗时</div>
            </div>
          </div>
        </el-card>

        <!-- 知识点详情 -->
        <el-card v-if="selectedNode" class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ selectedNode.name }}</span>
              <el-tag 
                :type="getTypeTagColor(selectedNode.type)" 
                size="small"
              >
                {{ getTypeText(selectedNode.type) }}
              </el-tag>
            </div>
          </template>
            
          <div class="node-detail">
            <div class="detail-item">
              <label>难度：</label>
              <el-rate 
                :model-value="selectedNode.difficulty || 1" 
                :max="5" 
                disabled 
                size="small"
              />
            </div>
            <div class="detail-item">
              <label>重要程度：</label>
              <el-progress 
                :percentage="(selectedNode.importance || 0) * 100" 
                :stroke-width="8"
              />
            </div>
            <div class="detail-item">
              <label>预估时长：</label>
              <span>{{ selectedNode.estimatedTime || 0 }} 分钟</span>
            </div>
            <div class="detail-item" v-if="selectedNode.description">
              <label>描述：</label>
              <p class="description-text">{{ selectedNode.description }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  ZoomIn,
  ZoomOut,
  FullScreen,
  TrendCharts
} from '@element-plus/icons-vue'
import { 
  getKnowledgeGraph, 
  getKnowledgeStatistics
} from '@/api/knowledge'
import { getCourses } from '@/api/course'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const graphContainer = ref()
let graphInstance = null

// 筛选表单
const filterForm = reactive({
  courseId: 1, // 默认选择第一个课程
  pointType: ''
})

// 课程列表
const courses = ref([])

// 图谱数据
const graphData = ref({
  nodes: [],
  links: []
})

// 统计数据
const graphStats = ref({
  totalNodes: 0,
  totalLinks: 0,
  queryTime: 0
})

// 选中的节点
const selectedNode = ref(null)

// 生命周期
onMounted(async () => {
  await loadCourses()
  await loadKnowledgeGraph()
  initGraph()
})

// 方法
const loadCourses = async () => {
  try {
    const response = await getCourses()
    courses.value = response.data.courses || []
  } catch (error) {
    console.error('Failed to load courses:', error)
  }
}

const loadKnowledgeGraph = async () => {
  loading.value = true
  try {
    const params = {
      courseId: filterForm.courseId,
      pointType: filterForm.pointType || undefined
    }
    
    const response = await getKnowledgeGraph(params)
    const data = response.data
    
    graphData.value = {
      nodes: data.nodes || [],
      links: data.links || []
    }
    
    graphStats.value = {
      totalNodes: data.totalNodes || 0,
      totalLinks: data.totalLinks || 0,
      queryTime: data.queryTime || 0
    }
    
    if (graphInstance) {
      updateGraphData()
    }
    
  } catch (error) {
    console.error('Failed to load knowledge graph:', error)
    ElMessage.error('加载知识图谱失败')
  } finally {
    loading.value = false
  }
}

const initGraph = async () => {
  await nextTick()
  if (!graphContainer.value) return
  
  graphInstance = echarts.init(graphContainer.value)
  
  const option = {
    title: {
      text: '知识图谱',
      subtext: '点击节点查看详情',
      top: 'top',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        if (params.dataType === 'node') {
          return `<strong>${params.data.name}</strong><br/>
                  类型: ${getTypeText(params.data.type)}<br/>
                  难度: ${params.data.difficulty}/5<br/>
                  重要程度: ${(params.data.importance * 100).toFixed(0)}%`
        } else {
          return `${params.data.type} 关系`
        }
      }
    },
    legend: [{
      data: ['概念', '技能', '应用'],
      top: 30
    }],
    series: [{
      type: 'graph',
      layout: 'force',
      data: [],
      links: [],
      categories: [
        { name: '概念', itemStyle: { color: '#5470c6' } },
        { name: '技能', itemStyle: { color: '#91cc75' } },
        { name: '应用', itemStyle: { color: '#fac858' } }
      ],
      roam: true,
      zoom: 1,
      focusNodeAdjacency: true,
      draggable: true,
      force: {
        repulsion: 1000,
        gravity: 0.1,
        edgeLength: 200,
        layoutAnimation: true
      },
      label: {
        show: true,
        position: 'right',
        fontSize: 12
      },
      lineStyle: {
        color: 'source',
        curveness: 0.1,
        width: 2
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 4
        }
      }
    }]
  }
  
  graphInstance.setOption(option)
  updateGraphData()
  
  // 添加点击事件
  graphInstance.on('click', function (params) {
    if (params.dataType === 'node') {
      selectedNode.value = params.data
    }
  })
  
  // 窗口大小变化时重新渲染
  window.addEventListener('resize', () => {
    if (graphInstance) {
      graphInstance.resize()
    }
  })
}

const updateGraphData = () => {
  if (!graphInstance) return
  
  const nodes = graphData.value.nodes.map(node => ({
    ...node,
    category: node.category || 0,
    symbolSize: node.symbolSize || 20,
    itemStyle: {
      color: getCategoryColor(node.category || 0)
    }
  }))
  
  const links = graphData.value.links.map(link => ({
    ...link,
    lineStyle: {
      width: (link.strength || 1) * 2
    }
  }))
  
  graphInstance.setOption({
    series: [{
      data: nodes,
      links: links
    }]
  })
}

const handleCourseChange = () => {
  loadKnowledgeGraph()
}

const handleTypeChange = () => {
  loadKnowledgeGraph()
}

const resetFilters = () => {
  filterForm.courseId = null
  filterForm.pointType = ''
  loadKnowledgeGraph()
}

const refreshGraph = () => {
  loadKnowledgeGraph()
}

const resetView = () => {
  if (graphInstance) {
    graphInstance.dispatchAction({
      type: 'restore'
    })
  }
}

const zoomIn = () => {
  if (graphInstance) {
    graphInstance.dispatchAction({
      type: 'dataZoom',
      zoom: 1.2
    })
  }
}

const zoomOut = () => {
  if (graphInstance) {
    graphInstance.dispatchAction({
      type: 'dataZoom',
      zoom: 0.8
    })
  }
}

const fitView = () => {
  if (graphInstance) {
    graphInstance.dispatchAction({
      type: 'restore'
    })
  }
}

// 工具函数
const getTypeText = (type) => {
  const typeMap = {
    concept: '概念',
    skill: '技能',
    application: '应用'
  }
  return typeMap[type] || '未知'
}

const getTypeTagColor = (type) => {
  const colorMap = {
    concept: 'primary',
    skill: 'success',
    application: 'warning'
  }
  return colorMap[type] || 'info'
}

const getCategoryColor = (category) => {
  const colors = ['#5470c6', '#91cc75', '#fac858']
  return colors[category] || colors[0]
}
</script>

<style scoped>
.knowledge-graph {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #333;
}

.filter-panel {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.main-content {
  margin-top: 20px;
}

.graph-container {
  height: 600px;
}

.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.graph-tools {
  display: flex;
  gap: 8px;
}

.graph-canvas {
  width: 100%;
  height: 520px;
}

.stats-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.detail-card {
  margin-bottom: 20px;
}

.node-detail {
  space-y: 12px;
}

.detail-item {
  margin-bottom: 12px;
}

.detail-item label {
  display: inline-block;
  width: 80px;
  font-weight: 500;
  color: #333;
}

.description-text {
  margin: 8px 0 0 0;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
}
</style> 