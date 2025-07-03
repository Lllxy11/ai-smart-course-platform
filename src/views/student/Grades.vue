<template>
  <div class="student-grades">
    <div class="page-header">
      <h1>我的成绩</h1>
      <div class="header-actions">
        <el-dropdown @command="handleExportCommand" :disabled="grades.length === 0">
          <el-button type="primary" :disabled="grades.length === 0">
            <el-icon><Download /></el-icon>
            导出成绩 ({{ grades.length }}条)
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="current-csv">
                导出当前筛选结果 ({{ grades.length }}条) - CSV
              </el-dropdown-item>
              <el-dropdown-item command="all-csv">导出所有成绩 - CSV</el-dropdown-item>
              <el-dropdown-item command="current-excel">
                导出当前筛选结果 ({{ grades.length }}条) - Excel
              </el-dropdown-item>
              <el-dropdown-item command="all-excel">导出所有成绩 - Excel</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button @click="loadGrades">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 成绩概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ computedStats.totalGrades }}</div>
            <div class="stat-label">总成绩数</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ computedStats.averageScore }}</div>
            <div class="stat-label">平均分</div>
          </div>
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ computedStats.passRate }}%</div>
            <div class="stat-label">及格率</div>
          </div>
          <el-icon class="stat-icon"><Checked /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ computedStats.classRank }}</div>
            <div class="stat-label">班级排名</div>
          </div>
          <el-icon class="stat-icon"><Trophy /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成绩趋势图 -->
    <el-card class="chart-card">
      <template #header>
        <span>成绩趋势</span>
      </template>
      <div ref="gradeChart" class="chart-container"></div>
    </el-card>

    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.title"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="作业" value="HOMEWORK" />
            <el-option label="考试" value="EXAM" />
            <el-option label="项目" value="PROJECT" />
            <el-option label="讨论" value="DISCUSSION" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 成绩列表 -->
    <el-card class="table-card">
      <template #header>
        <span>成绩详情</span>
      </template>

      <el-table :data="grades" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="项目名称" width="200" show-overflow-tooltip />
        <el-table-column prop="course" label="课程" width="150">
          <template #default="{ row }">
            {{ row.course?.title }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.score, row.maxScore)">
              {{ row.score }}/{{ row.maxScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="percentage" label="百分比" width="100">
          <template #default="{ row }">
            <span :class="getScoreClass(row.score, row.maxScore)">
              {{ ((row.score / row.maxScore) * 100).toFixed(1) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="rank" label="排名" width="100">
          <template #default="{ row }">
            {{ row.rank }}/{{ row.totalStudents }}
          </template>
        </el-table-column>
        <el-table-column prop="submissionTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.submissionTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="gradedTime" label="批改时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.gradedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="viewGradeDetail(row)">查看详情</el-button>
            <el-button size="small" @click="viewFeedback(row)" v-if="row.feedback">反馈</el-button>
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

    <!-- 成绩详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="成绩详情" width="800px">
      <div v-if="selectedGrade" class="grade-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目名称" :span="2">
            {{ selectedGrade.title }}
          </el-descriptions-item>
          <el-descriptions-item label="课程">
            {{ selectedGrade.course?.title }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(selectedGrade.type)">
              {{ getTypeText(selectedGrade.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="得分">
            <span :class="getScoreClass(selectedGrade.score, selectedGrade.maxScore)">
              {{ selectedGrade.score }}/{{ selectedGrade.maxScore }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="百分比">
            <span :class="getScoreClass(selectedGrade.score, selectedGrade.maxScore)">
              {{ ((selectedGrade.score / selectedGrade.maxScore) * 100).toFixed(1) }}%
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="班级排名">
            {{ selectedGrade.rank }}/{{ selectedGrade.totalStudents }}
          </el-descriptions-item>
          <el-descriptions-item label="平均分">
            {{ selectedGrade.classAverage }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatTime(selectedGrade.submissionTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="批改时间">
            {{ formatTime(selectedGrade.gradedTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="批改老师">
            {{ selectedGrade.grader?.realName || selectedGrade.grader?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="是否及格">
            <el-tag :type="selectedGrade.isPassed ? 'success' : 'danger'">
              {{ selectedGrade.isPassed ? '及格' : '不及格' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 评语和反馈 -->
        <div class="grade-feedback" v-if="selectedGrade.feedback">
          <h3>老师评语</h3>
          <div class="feedback-content">
            {{ selectedGrade.feedback }}
          </div>
        </div>

        <!-- 错题分析 -->
        <div class="mistake-analysis" v-if="selectedGrade.mistakes && selectedGrade.mistakes.length > 0">
          <h3>错题分析</h3>
          <div v-for="(mistake, index) in selectedGrade.mistakes" :key="index" class="mistake-item">
            <div class="mistake-question">
              <strong>题目:</strong> {{ mistake.question }}
            </div>
            <div class="mistake-answer">
              <strong>我的答案:</strong> {{ mistake.myAnswer }}
            </div>
            <div class="correct-answer">
              <strong>正确答案:</strong> {{ mistake.correctAnswer }}
            </div>
            <div class="mistake-explanation" v-if="mistake.explanation">
              <strong>解析:</strong> {{ mistake.explanation }}
            </div>
          </div>
        </div>

        <!-- 改进建议 -->
        <div class="improvement-suggestions" v-if="selectedGrade.suggestions">
          <h3>改进建议</h3>
          <ul>
            <li v-for="(suggestion, index) in selectedGrade.suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>

    <!-- 反馈对话框 -->
    <el-dialog v-model="showFeedbackDialog" title="老师反馈" width="600px">
      <div v-if="selectedGrade" class="feedback-dialog">
        <div class="feedback-header">
          <h3>{{ selectedGrade.title }}</h3>
          <div class="feedback-score">
            得分: {{ selectedGrade.score }}/{{ selectedGrade.maxScore }}
          </div>
        </div>
        <div class="feedback-content">
          {{ selectedGrade.feedback }}
        </div>
        <div class="feedback-time">
          批改时间: {{ formatTime(selectedGrade.gradedTime) }}
        </div>
      </div>
    </el-dialog>

    <!-- 学科成绩分析 -->
    <el-row :gutter="20" class="analysis-row">
      <el-col :span="12">
        <el-card class="analysis-card">
          <template #header>
            <span>各科成绩分布</span>
          </template>
          <div ref="subjectChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="analysis-card">
          <template #header>
            <span>成绩类型分析</span>
          </template>
          <div ref="typeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/request'
import {
  Download,
  Refresh,
  Document,
  TrendCharts,
  Checked,
  Trophy,
  ArrowDown
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { getGradeList, exportGrades as exportGradesAPI } from '@/api/grade'
import { getCourseList } from '@/api/course'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const error = ref(null)
const grades = ref([])
const courses = ref([])

// 分页状态
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)

// 筛选条件
const filters = ref({
  courseId: null,
  taskType: null
})

// 对话框控制
const showDetailDialog = ref(false)
const showFeedbackDialog = ref(false)

// 图表引用
const gradeChart = ref()
const subjectChart = ref()
const typeChart = ref()

// 搜索表单
const searchForm = reactive({
  courseId: '',
  type: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 选中的成绩
const selectedGrade = ref(null)

// 图表实例
let gradeChartInstance = null
let subjectChartInstance = null
let typeChartInstance = null

// 加载成绩数据
const loadGrades = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await api.get('/api/v1/grades/student', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        course_id: filters.value.courseId || null,
        task_type: filters.value.taskType || null
      }
    });
    
    console.log('API响应:', response.data);
    
    // 处理不同格式的API响应
    if (response.data && response.data.success) {
      // 标准格式
      grades.value = response.data.data?.content || response.data.data || [];
      totalItems.value = response.data.data?.totalElements || response.data.data?.length || 0;
    } else if (response.data && Array.isArray(response.data)) {
      // 直接数组格式
      grades.value = response.data;
      totalItems.value = response.data.length;
    } else if (response.data && response.data.content) {
      // 分页格式
      grades.value = response.data.content;
      totalItems.value = response.data.totalElements || response.data.content.length;
    } else {
      console.log('API返回格式不符合预期，数据为空');
      grades.value = [];
      totalItems.value = 0;
    }
    
    console.log('处理后的成绩数据:', grades.value);
    console.log('总数据条数:', totalItems.value);
    
  } catch (err) {
    console.error('加载成绩数据失败:', err);
    error.value = '加载成绩数据失败: ' + (err.response?.data?.message || err.message);
    grades.value = [];
    totalItems.value = 0;
  } finally {
    loading.value = false;
  }
};

// 计算统计数据
const computedStats = computed(() => {
  const gradeList = grades.value || [];
  
  if (gradeList.length === 0) {
    return {
      totalGrades: 0,
      averageScore: 0,
      passRate: 0,
      classRank: 'N/A'
    };
  }

  const totalGrades = gradeList.length;
  const totalScore = gradeList.reduce((sum, grade) => {
    const percentage = grade.percentage || (grade.score && grade.max_score ? (grade.score / grade.max_score) * 100 : 0);
    return sum + percentage;
  }, 0);
  const averageScore = totalScore / totalGrades;
  
  const passCount = gradeList.filter(grade => {
    const percentage = grade.percentage || (grade.score && grade.max_score ? (grade.score / grade.max_score) * 100 : 0);
    return percentage >= 60;
  }).length;
  const passRate = (passCount / totalGrades) * 100;
  
  // 找到最高成绩作为排名参考
  const maxScore = Math.max(...gradeList.map(grade => 
    grade.percentage || (grade.score && grade.max_score ? (grade.score / grade.max_score) * 100 : 0)
  ));
  
  const classRank = maxScore >= 90 ? '优秀' : maxScore >= 80 ? '良好' : maxScore >= 70 ? '中等' : '待提高';
  
  return {
    totalGrades,
    averageScore: Math.round(averageScore * 10) / 10,
    passRate: Math.round(passRate * 10) / 10,
    classRank
  };
});

// 图表数据计算
const chartData = computed(() => {
  const gradeList = grades.value || [];
  
  if (gradeList.length === 0) {
    return {
      trend: { categories: [], data: [] },
      distribution: [],
      comparison: { categories: [], data: [] }
    };
  }

  // 成绩趋势图数据（按提交时间排序）
  const sortedGrades = [...gradeList].sort((a, b) => 
    new Date(a.submitted_at || a.graded_at) - new Date(b.submitted_at || b.graded_at)
  );
  
  const trendCategories = sortedGrades.map(grade => {
    const date = new Date(grade.submitted_at || grade.graded_at);
    return `${date.getMonth() + 1}/${date.getDate()}`;
  });
  
  const trendData = sortedGrades.map(grade => 
    grade.percentage || (grade.score && grade.max_score ? (grade.score / grade.max_score) * 100 : 0)
  );

  // 学科分布数据
  const subjectMap = {};
  gradeList.forEach(grade => {
    const subject = grade.course_name || '未知课程';
    if (!subjectMap[subject]) {
      subjectMap[subject] = { total: 0, count: 0 };
    }
    const percentage = grade.percentage || (grade.score && grade.max_score ? (grade.score / grade.max_score) * 100 : 0);
    subjectMap[subject].total += percentage;
    subjectMap[subject].count += 1;
  });
  
  const distribution = Object.entries(subjectMap).map(([name, data]) => ({
    name,
    value: Math.round((data.total / data.count) * 10) / 10
  }));

  // 成绩类型对比数据
  const typeMap = {
    assignment: { name: '作业', total: 0, count: 0 },
    project: { name: '项目', total: 0, count: 0 },
    exam: { name: '考试', total: 0, count: 0 },
    discussion: { name: '讨论', total: 0, count: 0 }
  };
  
  gradeList.forEach(grade => {
    const type = grade.task_type || 'assignment';
    if (typeMap[type]) {
      const percentage = grade.percentage || (grade.score && grade.max_score ? (grade.score / grade.max_score) * 100 : 0);
      typeMap[type].total += percentage;
      typeMap[type].count += 1;
    }
  });
  
  const comparisonCategories = Object.values(typeMap)
    .filter(item => item.count > 0)
    .map(item => item.name);
  
  const comparisonData = Object.values(typeMap)
    .filter(item => item.count > 0)
    .map(item => Math.round((item.total / item.count) * 10) / 10);

  return {
    trend: { categories: trendCategories, data: trendData },
    distribution,
    comparison: { categories: comparisonCategories, data: comparisonData }
  };
});

// 监听数据变化更新图表
watch(grades, () => {
  if (grades.value && grades.value.length > 0) {
    nextTick(() => {
      updateCharts()
    })
  }
}, { deep: true })

// 生命周期
onMounted(() => {
  loadCourses()
  nextTick(() => {
    initCharts()
    // 确保图表初始化完成后再加载数据
    setTimeout(() => {
      loadGrades()
    }, 100)
  })
})

// 方法
const loadCourses = async () => {
  try {
    // 加载学生已选课程
    const response = await getCourseList({ studentId: authStore.user.id, size: 1000 })
    courses.value = response.data.content || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
    throw error
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadGrades()
}

const resetSearch = () => {
  searchForm.courseId = ''
  searchForm.type = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadGrades()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadGrades()
}

const viewGradeDetail = (grade) => {
  selectedGrade.value = grade
  showDetailDialog.value = true
}

const viewFeedback = (grade) => {
  selectedGrade.value = grade
  showFeedbackDialog.value = true
}

const handleExportCommand = (command) => {
  // 检查数据
  if (grades.value.length === 0) {
    ElMessage.warning('当前没有成绩数据可以导出')
    return
  }
  
  const [scope, format] = command.split('-')
  const includeFilters = scope === 'current'
  
  // 提示数据来源
  const isRealData = grades.value.some(g => g.id && g.course && g.score !== undefined)
  if (!isRealData) {
    ElMessage.info('当前导出的是演示数据，实际使用时将导出真实成绩')
  }
  
  exportGrades(format, includeFilters)
}

const exportGrades = async (format = 'csv', includeFilters = true) => {
  try {
    ElMessage.loading('正在生成导出文件...')
    
    if (format === 'excel') {
      // Excel导出使用前端处理
      await exportToExcel(includeFilters)
    } else {
      // CSV导出调用后端API
      await exportToCSV(includeFilters)
    }
    
    ElMessage.success(`成绩导出成功 (${format.toUpperCase()})`)
    
  } catch (error) {
    console.error('导出成绩失败:', error)
    ElMessage.error('导出成绩失败: ' + (error.message || '未知错误'))
  }
}

const exportToCSV = async (includeFilters) => {
  // 准备导出参数
  const exportParams = {
    course_id: includeFilters ? (searchForm.courseId || null) : null,
    format: 'csv',
    studentId: authStore.user.id
  }
  
  // 如果包含筛选条件
  if (includeFilters) {
    exportParams.type = searchForm.type
    exportParams.startDate = searchForm.dateRange?.[0]
    exportParams.endDate = searchForm.dateRange?.[1]
  }
  
  // 调用导出API
  const response = await exportGradesAPI(exportParams)
  
  // 创建下载链接
  const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  
  // 生成文件名
  const scope = includeFilters ? '筛选结果' : '全部成绩'
  const fileName = `我的成绩_${scope}_${new Date().toISOString().slice(0, 10)}.csv`
  link.download = fileName
  
  // 触发下载
  document.body.appendChild(link)
  link.click()
  
  // 清理
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const exportToExcel = async (includeFilters) => {
  // 获取要导出的数据
  let dataToExport = grades.value
  
  if (!includeFilters) {
    // 如果不包含筛选条件，重新加载所有数据
    try {
      const params = {
        page: 0,
        size: 1000, // 导出大量数据
        studentId: authStore.user.id
      }
      const response = await getGradeList(params)
      dataToExport = response.data.content || []
    } catch (error) {
      console.warn('无法获取全部数据，使用当前显示的数据')
      dataToExport = grades.value
    }
  }
  
  // 准备Excel数据
  const excelData = [
    // 表头
    [
      '项目名称',
      '课程',
      '类型',
      '得分',
      '满分',
      '百分比',
      '排名',
      '总人数',
      '提交时间',
      '批改时间'
    ],
    // 数据行
    ...dataToExport.map(grade => [
      grade.title || '未知项目',
      grade.course?.title || '未知课程',
      getTypeText(grade.type || ''),
      grade.score || 0,
      grade.maxScore || 100,
      grade.maxScore ? ((grade.score / grade.maxScore) * 100).toFixed(1) + '%' : '0%',
      grade.rank || '-',
      grade.totalStudents || '-',
      grade.submissionTime ? formatTime(grade.submissionTime) : '未提交',
      grade.gradedTime ? formatTime(grade.gradedTime) : '未批改'
    ])
  ]
  
  // 添加统计信息工作表数据
  const stats = computedStats.value
  const statsData = [
    ['统计项目', '数值'],
    ['总成绩数', stats.totalGrades],
    ['平均分', stats.averageScore],
    ['及格率', stats.passRate + '%'],
    ['班级排名', stats.classRank],
    ['', ''],
    ['导出时间', new Date().toLocaleString('zh-CN')],
    ['导出范围', includeFilters ? '当前筛选结果' : '全部成绩'],
    ['数据条数', dataToExport.length]
  ]
  
  // 创建工作簿
  const wb = XLSX.utils.book_new()
  
  // 创建成绩详情工作表
  const ws1 = XLSX.utils.aoa_to_sheet(excelData)
  
  // 设置列宽
  const colWidths = [
    { wch: 25 }, // 项目名称
    { wch: 15 }, // 课程
    { wch: 10 }, // 类型
    { wch: 8 },  // 得分
    { wch: 8 },  // 满分
    { wch: 10 }, // 百分比
    { wch: 8 },  // 排名
    { wch: 8 },  // 总人数
    { wch: 18 }, // 提交时间
    { wch: 18 }  // 批改时间
  ]
  ws1['!cols'] = colWidths
  
  // 创建统计信息工作表
  const ws2 = XLSX.utils.aoa_to_sheet(statsData)
  ws2['!cols'] = [{ wch: 15 }, { wch: 20 }]
  
  // 添加工作表到工作簿
  XLSX.utils.book_append_sheet(wb, ws1, '成绩详情')
  XLSX.utils.book_append_sheet(wb, ws2, '统计信息')
  
  // 生成文件名
  const scope = includeFilters ? '筛选结果' : '全部成绩'
  const fileName = `我的成绩_${scope}_${new Date().toISOString().slice(0, 10)}.xlsx`
  
  // 导出文件
  XLSX.writeFile(wb, fileName)
}

const initCharts = () => {
  // 初始化成绩趋势图
  gradeChartInstance = echarts.init(gradeChart.value)
  
  // 初始化学科成绩图
  subjectChartInstance = echarts.init(subjectChart.value)
  
  // 初始化成绩类型图
  typeChartInstance = echarts.init(typeChart.value)
  
  // 响应式处理
  window.addEventListener('resize', () => {
    gradeChartInstance?.resize()
    subjectChartInstance?.resize()
    typeChartInstance?.resize()
  })
}

const updateCharts = () => {
  if (!gradeChartInstance || !subjectChartInstance || !typeChartInstance) {
    return
  }
  
  const data = chartData.value
  
  // 成绩趋势图
  if (data.trend.categories.length > 0) {
    const gradeOption = {
      title: {
        text: '最近成绩趋势',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const point = params[0]
          return `${point.name}<br/>成绩: ${point.value}%`
        }
      },
      xAxis: {
        type: 'category',
        data: data.trend.categories
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100
      },
      series: [{
        name: '成绩',
        type: 'line',
        data: data.trend.data,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          width: 3
        },
        markLine: {
          data: [
            { yAxis: 60, label: { formatter: '及格线' }, lineStyle: { color: '#E6A23C' } }
          ]
        }
      }]
    }
    gradeChartInstance.setOption(gradeOption)
  }
  
  // 学科成绩分布图
  if (data.distribution.length > 0) {
    const subjectOption = {
      title: {
        text: '各科平均成绩',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c}% ({d}%)'
      },
      series: [{
        name: '平均成绩',
        type: 'pie',
        radius: '60%',
        data: data.distribution,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
    subjectChartInstance.setOption(subjectOption)
  }
  
  // 成绩类型分析图
  if (data.comparison.categories.length > 0) {
    const typeOption = {
      title: {
        text: '各类型成绩对比',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const point = params[0]
          return `${point.name}<br/>平均分: ${point.value}%`
        }
      },
      xAxis: {
        type: 'category',
        data: data.comparison.categories
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100
      },
      series: [{
        name: '平均分',
        type: 'bar',
        data: data.comparison.data,
        itemStyle: {
          color: function(params) {
            const colors = ['#5470C6', '#91CC75', '#FAC858', '#EE6666']
            return colors[params.dataIndex % colors.length]
          }
        },
        markLine: {
          data: [
            { yAxis: 60, label: { formatter: '及格线' }, lineStyle: { color: '#E6A23C' } }
          ]
        }
      }]
    }
    typeChartInstance.setOption(typeOption)
  }
}



// 工具方法
const getTypeText = (type) => {
  const typeMap = {
    HOMEWORK: '作业',
    EXAM: '考试',
    PROJECT: '项目',
    DISCUSSION: '讨论'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    HOMEWORK: '',
    EXAM: 'success',
    PROJECT: 'warning',
    DISCUSSION: 'info'
  }
  return typeMap[type] || ''
}

const getScoreClass = (score, maxScore) => {
  const percentage = (score / maxScore) * 100
  if (percentage >= 90) return 'excellent'
  if (percentage >= 80) return 'good'
  if (percentage >= 60) return 'pass'
  return 'fail'
}

const formatTime = (time) => {
  if (!time) return '未设置'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.student-grades {
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

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.excellent {
  color: #67c23a;
  font-weight: bold;
}

.good {
  color: #409eff;
  font-weight: bold;
}

.pass {
  color: #e6a23c;
}

.fail {
  color: #f56c6c;
  font-weight: bold;
}

.grade-detail {
  padding: 20px 0;
}

.grade-feedback {
  margin-top: 30px;
}

.grade-feedback h3 {
  margin-bottom: 15px;
  color: #303133;
}

.feedback-content {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
}

.mistake-analysis {
  margin-top: 30px;
}

.mistake-analysis h3 {
  margin-bottom: 15px;
  color: #303133;
}

.mistake-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.mistake-question,
.mistake-answer,
.correct-answer,
.mistake-explanation {
  margin-bottom: 10px;
  line-height: 1.6;
}

.correct-answer {
  color: #67c23a;
}

.mistake-explanation {
  color: #909399;
  font-style: italic;
}

.improvement-suggestions {
  margin-top: 30px;
}

.improvement-suggestions h3 {
  margin-bottom: 15px;
  color: #303133;
}

.improvement-suggestions ul {
  padding-left: 20px;
}

.improvement-suggestions li {
  margin-bottom: 8px;
  line-height: 1.6;
}

.feedback-dialog {
  padding: 20px 0;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.feedback-header h3 {
  margin: 0;
  color: #303133;
}

.feedback-score {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.feedback-time {
  margin-top: 20px;
  text-align: right;
  color: #909399;
  font-size: 14px;
}

.analysis-row {
  margin-top: 20px;
}

.analysis-card {
  height: 400px;
}
</style> 