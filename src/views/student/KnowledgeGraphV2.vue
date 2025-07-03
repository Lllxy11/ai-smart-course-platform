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
        <el-button @click="toggleLayout">
          <el-icon><Grid /></el-icon>
          切换布局
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
                :label="course.name || course.title || `课程${course.id}`"
                :value="course.id"
              >
                <span style="float: left">{{ course.name || course.title || `课程${course.id}` }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ course.teacher || '' }}</span>
              </el-option>
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
                <el-tooltip content="显示/隐藏标签">
                  <el-button size="small" circle @click="toggleLabels">
                    <el-icon><View /></el-icon>
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
              <div class="stat-number performance">{{ graphStats.queryTime || 0 }}ms</div>
              <div class="stat-label">查询耗时</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ currentLayout }}</div>
              <div class="stat-label">当前布局</div>
            </div>
          </div>
        </el-card>

        <!-- 图例说明 -->
        <el-card class="legend-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>图例说明</span>
              <el-icon><InfoFilled /></el-icon>
            </div>
          </template>
          <div class="legend-items">
            <div class="legend-item">
              <div class="legend-color concept"></div>
              <span>概念知识点</span>
            </div>
            <div class="legend-item">
              <div class="legend-color skill"></div>
              <span>技能知识点</span>
            </div>
            <div class="legend-item">
              <div class="legend-color application"></div>
              <span>应用知识点</span>
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
                :percentage="Math.round((selectedNode.importance || 0) * 100)" 
                :stroke-width="8"
                :color="getImportanceColor(selectedNode.importance)"
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
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  ZoomIn,
  ZoomOut,
  FullScreen,
  TrendCharts,
  Grid,
  View,
  InfoFilled
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
let resizeObserver = null

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

// 布局相关
const currentLayout = ref('力导向')
const layoutType = ref('force')
const showLabels = ref(true)

// 生命周期
onMounted(async () => {
  await loadCourses()
  await loadKnowledgeGraph()
  await initGraph()
  setupResizeObserver()
})

onUnmounted(() => {
  if (graphInstance) {
    graphInstance.dispose()
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
  window.removeEventListener('resize', handleResize)
})

// 方法
const loadCourses = async () => {
  try {
    console.log('知识图谱-开始加载课程列表')
    // 学生端应该获取所有已发布的课程，使用正确的参数
    const params = {
      page: 0, // 从第0页开始
      size: 100, // 获取足够多的课程
      status: 'PUBLISHED' // 只获取已发布的课程
    }
    console.log('知识图谱-请求参数:', params)
    const response = await getCourses(params)
    console.log('知识图谱-课程API响应:', response.data)
    
    // 处理不同的响应格式，与学生端课程页面保持一致
    let courseList = []
    if (response.data) {
      if (Array.isArray(response.data)) {
        courseList = response.data
      } else if (response.data.content && Array.isArray(response.data.content)) {
        courseList = response.data.content
      } else if (response.data.courses && Array.isArray(response.data.courses)) {
        courseList = response.data.courses
      }
    }
    
    // 打印原始课程数据结构以便调试
    console.log('知识图谱-原始课程数据:', courseList)
    if (courseList.length > 0) {
      console.log('知识图谱-第一个课程对象的所有字段:', Object.keys(courseList[0]))
      console.log('知识图谱-第一个课程的详细信息:', courseList[0])
    }
    
    // 处理课程数据字段映射，确保与学生端课程页面一致
    courses.value = courseList.map(course => {
      // 尝试多种可能的名称字段
      const displayName = course.name || course.title || course.courseName || 
                         course.course_name || `课程${course.id}` || '未命名课程'
      
      console.log(`知识图谱-课程${course.id}的名称处理:`, {
        originalName: course.name,
        originalTitle: course.title,
        originalCourseName: course.courseName,
        finalDisplayName: displayName,
        allFields: Object.keys(course)
      })
      
      return {
        ...course,
        name: displayName,
        title: displayName,
        teacher: course.teacherName || course.teacher || course.instructor || '未知教师',
        coverImage: course.coverImage || course.image || '/placeholder-course.jpg'
      }
    })
    
    console.log('知识图谱-处理后的课程数据:', courses.value)
    
    // 默认选择第一个课程，但只选择已发布的课程
    const availableCourses = courses.value.filter(course => 
      course.status === 'PUBLISHED' || course.status === 'ONGOING'
    )
    
    if (availableCourses.length > 0 && !filterForm.courseId) {
      filterForm.courseId = availableCourses[0].id
      console.log('知识图谱-自动选择课程ID:', filterForm.courseId)
    } else if (availableCourses.length === 0) {
      console.log('知识图谱-没有可用的课程')
      ElMessage.info('暂无可用的课程知识图谱')
    }
    
    // 只显示已发布的课程供选择
    courses.value = availableCourses
    
  } catch (error) {
    console.error('知识图谱-加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败: ' + (error.message || '网络错误'))
  }
}

const loadKnowledgeGraph = async () => {
  if (!filterForm.courseId) {
    console.warn('No course selected, cannot load knowledge graph')
    ElMessage.warning('请先选择课程')
    return
  }
  
  loading.value = true
  try {
    const params = {
      courseId: filterForm.courseId,
      pointType: filterForm.pointType || undefined
    }
    
    console.log('Loading knowledge graph with params:', params)
    const response = await getKnowledgeGraph(params)
    const data = response.data
    
    console.log('Knowledge graph data received:', data)
    
    // 如果课程列表为空但知识图谱有数据，说明课程存在，补充课程信息
    if (courses.value.length === 0 && data && data.courseId) {
      console.log('知识图谱-从图谱数据补充课程信息')
      
      // 根据课程ID和知识点内容智能推断课程名称
      let courseName = data.courseName
      if (!courseName) {
        if (data.courseId === 1) {
          // 检查知识点是否包含Java相关内容
          const hasJavaContent = data.nodes && data.nodes.some(node => 
            node.name && (node.name.includes('Java') || node.name.includes('类与对象') || node.name.includes('继承'))
          )
          courseName = hasJavaContent ? 'Java编程基础' : `课程${data.courseId}`
        } else {
          courseName = `课程${data.courseId}`
        }
      }
      
      const courseFromGraph = {
        id: data.courseId,
        name: courseName,
        title: courseName,
        status: 'PUBLISHED',
        teacher: data.teacherName || '教师',
        nodeCount: data.totalNodes || 0
      }
      courses.value = [courseFromGraph]
      console.log('知识图谱-补充的课程信息:', courses.value)
      
      // 强制更新选择的课程ID
      if (filterForm.courseId !== data.courseId) {
        filterForm.courseId = data.courseId
        console.log('知识图谱-更新选中的课程ID:', filterForm.courseId)
      }
    }
    
    // 检查是否有数据
    if (!data || (!data.nodes && !data.links)) {
      console.warn('No knowledge graph data received')
      ElMessage.info('该课程暂无知识图谱数据，请联系教师添加知识点')
      
      // 显示空状态
      graphData.value = {
        nodes: [],
        links: []
      }
      graphStats.value = {
        totalNodes: 0,
        totalLinks: 0,
        queryTime: data?.queryTime || 0
      }
    } else {
      graphData.value = {
        nodes: data.nodes || [],
        links: data.links || []
      }
      
      graphStats.value = {
        totalNodes: data.totalNodes || data.nodes?.length || 0,
        totalLinks: data.totalLinks || data.links?.length || 0,
        queryTime: data.queryTime || 0
      }
      
      ElMessage.success(`成功加载 ${graphStats.value.totalNodes} 个知识点`)
    }
    
    console.log('Graph stats:', graphStats.value)
    console.log('Graph data:', graphData.value)
    
    if (graphInstance) {
      updateGraphData()
    }
    
  } catch (error) {
    console.error('Failed to load knowledge graph:', error)
    if (error.response?.status === 404) {
      ElMessage.warning('课程知识图谱数据不存在，请联系教师创建')
    } else {
      ElMessage.error('加载知识图谱失败: ' + (error.message || '网络错误'))
    }
    
    // 设置空状态
    graphData.value = { nodes: [], links: [] }
    graphStats.value = { totalNodes: 0, totalLinks: 0, queryTime: 0 }
    
    if (graphInstance) {
      updateGraphData()
    }
  } finally {
    loading.value = false
  }
}

const initGraph = async () => {
  await nextTick()
  if (!graphContainer.value) {
    console.error('Graph container not found')
    return
  }
  
  console.log('Initializing graph...')
  console.log('Graph container:', graphContainer.value)
  console.log('Container dimensions:', {
    width: graphContainer.value.offsetWidth,
    height: graphContainer.value.offsetHeight
  })
  
  graphInstance = echarts.init(graphContainer.value, null, {
    renderer: 'canvas',
    useDirtyRect: false
  })
  
  console.log('Graph instance created:', graphInstance)
  
  const option = {
    backgroundColor: '#fafafa',
    title: {
      text: '知识图谱',
      subtext: '点击节点查看详情，拖拽可移动节点',
      top: 10,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333'
      },
      subtextStyle: {
        fontSize: 12,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0,0,0,0.8)',
      borderColor: '#777',
      borderWidth: 1,
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      formatter: function (params) {
        if (params.dataType === 'node') {
          const typeMap = {
            concept: '概念',
            skill: '技能',
            application: '应用'
          }
          const typeText = typeMap[params.data.type] || '未知'
          
          return `<div style="padding: 8px;">
                    <div style="font-weight: bold; margin-bottom: 4px;">${params.data.name}</div>
                    <div>类型: ${typeText}</div>
                    <div>难度: ${params.data.difficulty}/5</div>
                    <div>重要程度: ${Math.round((params.data.importance || 0) * 100)}%</div>
                    ${params.data.estimatedTime ? `<div>预估时长: ${params.data.estimatedTime}分钟</div>` : ''}
                  </div>`
        } else {
          return `<div style="padding: 8px;">
                    <div style="font-weight: bold;">${params.data.type || '前置'} 关系</div>
                    <div>强度: ${((params.data.strength || 1) * 100).toFixed(0)}%</div>
                  </div>`
        }
      }
    },
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [{
      type: 'graph',
      layout: layoutType.value,
      data: [],
      links: [],
      categories: [
        { 
          name: '概念', 
          itemStyle: { 
            color: '#5470c6',
            borderColor: '#3b5998',
            borderWidth: 2
          } 
        },
        { 
          name: '技能', 
          itemStyle: { 
            color: '#91cc75',
            borderColor: '#6ba442',
            borderWidth: 2
          } 
        },
        { 
          name: '应用', 
          itemStyle: { 
            color: '#fac858',
            borderColor: '#d4954e',
            borderWidth: 2
          } 
        }
      ],
      roam: true,
      zoom: 0.8,
      focusNodeAdjacency: true,
      draggable: true,
      symbol: 'circle',
      symbolSize: function(value, params) {
        return Math.max(25, Math.min(60, (params.data.importance || 0.5) * 80))
      },
      force: {
        initLayout: 'circular',
        repulsion: [100, 400],
        gravity: 0.1,
        edgeLength: [100, 300],
        layoutAnimation: true,
        friction: 0.6
      },
      label: {
        show: showLabels.value,
        position: 'bottom',
        fontSize: 11,
        fontWeight: 'bold',
        color: '#333',
        backgroundColor: 'rgba(255,255,255,0.8)',
        borderColor: '#ddd',
        borderWidth: 1,
        borderRadius: 3,
        padding: [2, 6],
        formatter: function(params) {
          const name = params.data.name
          return name.length > 6 ? name.substring(0, 6) + '...' : name
        }
      },
      edgeLabel: {
        show: false,
        fontSize: 10,
        color: '#666'
      },
      lineStyle: {
        color: '#aaa',
        width: function(params) {
          return Math.max(1, (params.data.strength || 1) * 3)
        },
        curveness: 0.1,
        opacity: 0.7
      },
      emphasis: {
        focus: 'adjacency',
        scale: 1.2,
        label: {
          show: true,
          fontSize: 12,
          fontWeight: 'bold'
        },
        lineStyle: {
          width: 4,
          opacity: 1
        }
      },
      select: {
        itemStyle: {
          borderColor: '#ff4757',
          borderWidth: 3
        }
      }
    }]
  }
  
  graphInstance.setOption(option)
  console.log('Initial option set for graph')
  updateGraphData()
  console.log('Graph initialization completed')
  
  // 添加点击事件
  graphInstance.on('click', function (params) {
    if (params.dataType === 'node') {
      selectedNode.value = params.data
      
      // 高亮选中节点
      graphInstance.dispatchAction({
        type: 'select',
        dataIndex: params.dataIndex
      })
    }
  })
  
  // 添加鼠标悬停事件
  graphInstance.on('mouseover', function (params) {
    if (params.dataType === 'node') {
      graphInstance.dispatchAction({
        type: 'highlight',
        dataIndex: params.dataIndex
      })
    }
  })
  
  graphInstance.on('mouseout', function (params) {
    if (params.dataType === 'node') {
      graphInstance.dispatchAction({
        type: 'downplay',
        dataIndex: params.dataIndex
      })
    }
  })
}

const updateGraphData = () => {
  if (!graphInstance) {
    console.warn('Graph instance not ready')
    return
  }
  
  console.log('Updating graph data...')
  console.log('Raw nodes:', graphData.value.nodes)
  console.log('Raw links:', graphData.value.links)
  
  const nodes = graphData.value.nodes.map(node => ({
    ...node,
    category: getCategoryIndex(node.type),
    symbolSize: calculateNodeSize(node.importance),
    draggable: true,
    itemStyle: {
      color: getCategoryColor(getCategoryIndex(node.type)),
      borderColor: getCategoryBorderColor(getCategoryIndex(node.type)),
      borderWidth: 2,
      shadowBlur: 5,
      shadowColor: 'rgba(0,0,0,0.2)'
    }
  }))
  
  const links = graphData.value.links.map(link => ({
    ...link,
    lineStyle: {
      width: Math.max(1, (link.strength || 1) * 3),
      color: '#aaa',
      opacity: 0.7
    }
  }))
  
  console.log('Processed nodes:', nodes)
  console.log('Processed links:', links)
  
  if (nodes.length === 0) {
    console.warn('No nodes to display')
    ElMessage.warning('没有找到知识点数据')
  }
  
  // 使用安全的方式更新图表数据
  graphInstance.setOption({
    series: [{
      type: 'graph',
      data: nodes,
      links: links,
      layout: layoutType.value,
      roam: true,
      focusNodeAdjacency: true,
      draggable: true,
      symbol: 'circle',
      label: {
        show: showLabels.value,
        position: 'bottom',
        fontSize: 11,
        fontWeight: 'bold',
        color: '#333',
        backgroundColor: 'rgba(255,255,255,0.8)',
        borderColor: '#ddd',
        borderWidth: 1,
        borderRadius: 3,
        padding: [2, 6],
        formatter: function(params) {
          const name = params.data.name
          return name && name.length > 6 ? name.substring(0, 6) + '...' : (name || '')
        }
      }
    }]
  })
  
  console.log('Graph data updated successfully')
}

const setupResizeObserver = () => {
  if (graphContainer.value && window.ResizeObserver) {
    resizeObserver = new ResizeObserver(() => {
      if (graphInstance) {
        graphInstance.resize()
      }
    })
    resizeObserver.observe(graphContainer.value)
  }
  
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  if (graphInstance) {
    setTimeout(() => {
      graphInstance.resize()
    }, 100)
  }
}

const handleCourseChange = () => {
  selectedNode.value = null
  loadKnowledgeGraph()
}

const handleTypeChange = () => {
  selectedNode.value = null
  loadKnowledgeGraph()
}

const resetFilters = () => {
  filterForm.courseId = null
  filterForm.pointType = ''
  selectedNode.value = null
  loadKnowledgeGraph()
}

const refreshGraph = () => {
  selectedNode.value = null
  loadKnowledgeGraph()
}

const resetView = () => {
  if (graphInstance) {
    graphInstance.dispatchAction({
      type: 'restore'
    })
    selectedNode.value = null
  }
}

const zoomIn = () => {
  if (graphInstance) {
    const zoom = graphInstance.getOption().series[0].zoom || 1
    graphInstance.setOption({
      series: [{
        zoom: Math.min(zoom * 1.2, 3)
      }]
    })
  }
}

const zoomOut = () => {
  if (graphInstance) {
    const zoom = graphInstance.getOption().series[0].zoom || 1
    graphInstance.setOption({
      series: [{
        zoom: Math.max(zoom * 0.8, 0.3)
      }]
    })
  }
}

const fitView = () => {
  if (graphInstance) {
    graphInstance.setOption({
      series: [{
        zoom: 0.8
      }]
    })
  }
}

const toggleLayout = () => {
  const layouts = [
    { type: 'force', name: '力导向' },
    { type: 'circular', name: '环形' },
    { type: 'none', name: '自由' }
  ]
  
  const currentIndex = layouts.findIndex(l => l.type === layoutType.value)
  const nextIndex = (currentIndex + 1) % layouts.length
  
  layoutType.value = layouts[nextIndex].type
  currentLayout.value = layouts[nextIndex].name
  
  updateGraphData()
  ElMessage.success(`已切换到${currentLayout.value}布局`)
}

const toggleLabels = () => {
  showLabels.value = !showLabels.value
  updateGraphData()
  ElMessage.success(showLabels.value ? '已显示标签' : '已隐藏标签')
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

const getCategoryIndex = (pointType) => {
  switch (pointType) {
    case 'concept': return 0
    case 'skill': return 1
    case 'application': return 2
    default: return 0
  }
}

const getCategoryColor = (category) => {
  const colors = ['#5470c6', '#91cc75', '#fac858']
  return colors[category] || colors[0]
}

const getCategoryBorderColor = (category) => {
  const colors = ['#3b5998', '#6ba442', '#d4954e']
  return colors[category] || colors[0]
}

const calculateNodeSize = (importance) => {
  if (importance == null) return 30
  return Math.max(25, Math.min(60, importance * 80))
}

const getImportanceColor = (importance) => {
  if (importance >= 0.8) return '#67c23a'
  if (importance >= 0.6) return '#e6a23c'
  if (importance >= 0.4) return '#f56c6c'
  return '#909399'
}
</script>

<style scoped>
.knowledge-graph {
  padding: 20px;
  background-color: #f5f7fa;
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
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-panel {
  margin-bottom: 20px;
  border-radius: 8px;
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
  height: 650px;
  border-radius: 8px;
  overflow: hidden;
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
  height: 580px;
  border-radius: 4px;
}

.stats-card, .legend-card, .detail-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.stat-number {
  font-size: 22px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-number.performance {
  color: #67c23a;
}

.stat-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid;
}

.legend-color.concept {
  background-color: #5470c6;
  border-color: #3b5998;
}

.legend-color.skill {
  background-color: #91cc75;
  border-color: #6ba442;
}

.legend-color.application {
  background-color: #fac858;
  border-color: #d4954e;
}

.detail-card {
  max-height: 400px;
  overflow-y: auto;
}

.node-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.description-text {
  margin: 0;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #555;
  border: 1px solid #dee2e6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content .el-col {
    width: 100% !important;
  }
  
  .graph-container {
    height: 500px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style> 