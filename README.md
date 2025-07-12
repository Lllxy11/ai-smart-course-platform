# AI智能课程平台 - 前端应用

## 🧠 项目简介

AI智能课程平台是一个融合人工智能与教学管理的现代化在线教育系统，面向学生、教师与管理员，旨在提供个性化学习体验、高效的教学支持及全面的管理能力。前端采用 Vue 3 和 Element Plus 构建，后端基于 Spring Boot，双方通过 RESTful API 协同工作。

## ✨ 功能特性

### 🎯 多角色支持
- **学生端**: 课程学习、任务提交、AI助手、学习分析
- **教师端**: 课程管理、任务发布、学生管理、教学分析  
- **管理员**: 用户管理、系统监控、数据分析、平台配置

### 🤖 AI智能功能
- **智能对话**: 与AI助手实时对话，获取学习建议
- **学习路径推荐**: 基于学习数据的个性化路径规划
- **智能题目生成**: AI自动生成练习题和考试题
- **学习分析**: 深度分析学习行为和成绩趋势
- **内容总结**: 自动生成学习报告和知识点摘要

### 📊 数据可视化
- **学习进度图表**: ECharts可视化学习趋势
- **成绩分析**: 多维度成绩统计和对比
- **活动热图**: 学习活动时间分布分析
- **知识图谱**: 知识点关系可视化

### 🎨 现代化界面
- **响应式设计**: 支持桌面端和移动端
- **美观UI**: Element Plus组件库 + 自定义样式
- **流畅动画**: 页面切换和交互动效
- **深色模式**: 支持主题切换（开发中）

## 🚀 快速开始

### 环境要求
- Node.js 16+ 
- npm 
- 现代浏览器（Chrome、Firefox、Safari、Edge）

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd ai-smart-course-platform
```

2. **安装依赖**
```bash
npm install
```

3. **启动开发服务器**
```bash
npm run dev
```

4. **Windows用户可直接运行**
```bash
start.bat
```

5. **Linux/Mac用户可运行**
```bash
./start.sh
```

### 访问地址
- 前端开发服务器: http://localhost:5173
- 后端API服务器: http://localhost:8000

## 👥 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | password123 |
| 教师 | teacher1 | password123 |
| 学生 | student1 | password123 |

## 🔧 技术栈

### 前端框架
- **Vue 3**: 渐进式JavaScript框架
- **Vite**: 下一代前端构建工具
- **Vue Router**: 官方路由管理器
- **Pinia**: 状态管理库

### UI组件库
- **Element Plus**: Vue 3组件库
- **Element Plus Icons**: 图标库
- **ECharts**: 数据可视化图表库

### 开发工具
- **ESLint**: 代码质量检查
- **Prettier**: 代码格式化
- **Sass**: CSS预处理器

## 📁 项目结构

```
src/
├── api/                    # API接口封装
│   ├── auth.js            # 认证相关API
│   ├── course.js          # 课程管理API
│   ├── task.js            # 任务管理API
│   ├── ai.js              # AI功能API
│   ├── analytics.js       # 数据分析API
│   ├── user.js            # 用户管理API
│   ├── exam.js            # 考试管理API
│   ├── grade.js           # 成绩管理API
│   ├── notification.js    # 通知管理API
│   ├── video.js           # 视频学习API
│   ├── knowledge.js       # 知识点管理API
│   ├── learningPath.js    # 学习路径API
│   ├── classroom.js       # 班级管理API
│   └── request.js         # Axios请求配置
├── components/            # 公共组件
├── layouts/               # 布局组件
│   ├── StudentLayout.vue  # 学生端布局
│   ├── TeacherLayout.vue  # 教师端布局
│   └── AdminLayout.vue    # 管理员端布局
├── views/                 # 页面组件
│   ├── auth/              # 认证页面
│   ├── student/           # 学生端页面
│   ├── teacher/           # 教师端页面
│   ├── admin/             # 管理员端页面
│   └── common/            # 公共页面
├── stores/                # Pinia状态管理
├── router/                # 路由配置
├── styles/                # 全局样式
└── utils/                 # 工具函数
```

## 🔌 API对接说明

### 后端API基础信息
- **Base URL**: `http://localhost:8000/api/v1`
- **认证方式**: JWT Token
- **数据格式**: JSON

### 主要API模块

#### 1. 认证管理 (`/auth`)
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册  
- `GET /auth/me` - 获取当前用户信息
- `PUT /auth/me` - 更新用户信息
- `POST /auth/change-password` - 修改密码
- `POST /auth/logout` - 用户登出

#### 2. 用户管理 (`/users`)
- `GET /users` - 获取用户列表（分页、筛选）
- `GET /users/{userId}` - 获取用户详情
- `POST /users` - 创建用户（仅管理员）
- `PUT /users/{userId}` - 更新用户信息
- `DELETE /users/{userId}` - 删除用户

#### 3. 课程管理 (`/courses`)
- `GET /courses` - 获取课程列表
- `GET /courses/{courseId}` - 获取课程详情
- `POST /courses` - 创建课程
- `POST /courses/{courseId}/enroll` - 学生选课
- `GET /courses/{courseId}/progress` - 获取学习进度

#### 4. 任务管理 (`/tasks`)
- `GET /tasks` - 获取任务列表
- `POST /tasks` - 创建任务
- `POST /tasks/{taskId}/submit` - 提交任务
- `GET /tasks/{taskId}/submissions` - 获取提交列表

#### 5. AI功能 (`/ai`)
- `POST /ai/chat` - 与AI对话
- `POST /ai/generate-questions` - AI生成题目
- `POST /ai/recommend-learning-path` - 推荐学习路径
- `POST /ai/analyze-submission` - 分析提交内容

#### 6. 数据分析 (`/analytics`)
- `GET /analytics/dashboard` - 获取仪表板统计
- `GET /analytics/user-dashboard/{userId}` - 用户仪表板
- `GET /analytics/learning-trends/{userId}` - 学习趋势
- `GET /analytics/knowledge-mastery/{userId}` - 知识掌握情况

### 请求拦截器配置

```javascript
// 自动添加JWT Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 统一错误处理
request.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // 跳转到登录页
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

## 🎯 核心功能实现

### 学生端主要功能

#### 1. 学习仪表板
- 学习统计卡片（课程数、任务数、学习时长）
- 学习进度趋势图表
- 最近课程和任务列表
- 个性化学习建议

#### 2. 课程管理
- 课程列表展示（网格/列表视图）
- 课程搜索和筛选
- 选课功能
- 学习进度跟踪

#### 3. 任务管理
- 任务列表（待完成、已提交、已批改）
- 任务提交（文本+文件上传）
- 任务状态跟踪
- 截止时间提醒

#### 4. AI学习助手
- 实时对话界面
- 智能学习建议
- 学习路径推荐
- 个性化问答

### 教师端主要功能

#### 1. 教学仪表板
- 教学统计概览
- 学生学习进度监控
- 待批改任务提醒
- 课程管理快捷入口

#### 2. 课程管理
- 课程创建和编辑
- 学生管理
- 课程资源上传
- 学习进度分析

#### 3. 任务管理
- 任务发布和管理
- 批量批改功能
- 成绩统计分析
- AI辅助评分

### 管理员端主要功能

#### 1. 系统监控
- 系统状态监控
- 用户活动统计
- 性能指标展示
- 异常告警

#### 2. 用户管理
- 用户列表管理
- 权限分配
- 批量操作
- 用户行为分析

#### 3. 数据分析
- 平台使用统计
- 学习效果分析
- 课程热度排行
- 收入统计

## 🔧 配置说明

### 环境变量配置

创建 `.env.local` 文件：

```env
# API基础地址
VITE_API_BASE_URL=http://localhost:8000/api/v1

# 应用配置
VITE_APP_TITLE=AI智能课程平台
VITE_APP_VERSION=1.0.0

# 开发配置
VITE_DEV_PORT=5173
```

### Vite配置

```javascript
// vite.config.js
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true
      }
    }
  }
})
```

## 📱 响应式设计

### 断点配置
- **xs**: < 768px (手机)
- **sm**: ≥ 768px (平板)
- **md**: ≥ 992px (小型桌面)
- **lg**: ≥ 1200px (大型桌面)
- **xl**: ≥ 1920px (超大屏幕)

### 移动端适配
- 导航栏自动折叠
- 卡片布局自适应
- 触摸友好的交互
- 优化的字体大小

## 🚀 部署说明

### 构建生产版本
```bash
npm run build
```

### 预览构建结果
```bash
npm run preview
```

### Docker部署
```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

## 🔍 开发指南

### 代码规范
- 使用ESLint进行代码检查
- 使用Prettier进行代码格式化
- 遵循Vue 3 Composition API最佳实践

### 组件开发
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 响应式数据
const loading = ref(false)

// 计算属性
const computedValue = computed(() => {
  // 计算逻辑
})

// 生命周期
onMounted(() => {
  // 初始化逻辑
})
</script>

<style lang="scss" scoped>
/* 组件样式 */
</style>
```

### API调用规范
```javascript
// 使用try-catch处理错误
const loadData = async () => {
  try {
    loading.value = true
    const response = await someAPI()
    data.value = response.data
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🆘 常见问题

### Q: 启动时提示端口被占用怎么办？
A: 修改 `vite.config.js` 中的端口配置，或杀死占用端口的进程。

### Q: API请求失败怎么办？
A: 检查后端服务是否启动，确认API地址配置正确。

### Q: 如何添加新的页面？
A: 在 `src/views` 目录下创建Vue组件，然后在 `src/router/index.js` 中添加路由配置。

### Q: 如何自定义主题？
A: 修改 `src/styles/variables.scss` 文件中的CSS变量。

## 📞 技术支持

如有问题或建议，请通过以下方式联系：

- 邮箱1: xliu8525@gmail.com
- 邮箱2: powehi041210@gmail.com
- 邮箱3：1571990047@qq.com
- 邮箱4：986617697@qq.com

## 项目贡献度
- 刘溪月：22%--用户与认证模块+系统配置与安全
- 王瑞琦：22%--AI助手模块+数据分析模块
- 石煜麟：22%--任务与提交模块+测评与分析模块
- 刘嘉伟：22%--课程与选课模块+学习体验模块
- 木拉哈：12%--通知模块+前端共享组件库与构建流程维护
---
