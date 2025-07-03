import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import NProgress from 'nprogress'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false, title: '注册' }
  },
  
  // 学生路由
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '学生仪表板' }
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('@/views/student/Courses.vue'),
        meta: { title: '我的课程' }
      },
      {
        path: 'courses/:courseId',
        name: 'StudentCourseDetail',
        component: () => import('@/views/student/CourseDetail.vue'),
        meta: { title: '课程详情' }
      },

      {
        path: 'ai-assistant',
        name: 'StudentAIAssistant',
        component: () => import('@/views/student/AIAssistant.vue'),
        meta: { title: 'AI学习助手' }
      },
      {
        path: 'exams',
        name: 'StudentExams',
        component: () => import('@/views/student/Exams.vue'),
        meta: { title: '我的考试' }
      },
      {
        path: 'grades',
        name: 'StudentGrades',
        component: () => import('@/views/student/Grades.vue'),
        meta: { title: '我的成绩' }
      },
              {
          path: 'notifications',
          name: 'StudentNotifications',
          component: () => import('@/views/common/Notifications.vue'),
          meta: { title: '通知中心' }
        },
        {
          path: 'learning-path',
          name: 'StudentLearningPath',
          component: () => import('@/views/student/LearningPath.vue'),
          meta: { title: '学习路径' }
        },
        {
          path: 'knowledge-graph',
          name: 'StudentKnowledgeGraph',
          component: () => import('@/views/student/KnowledgeGraphV2.vue'),
          meta: { title: '知识图谱' }
        },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { title: '个人资料' }
      }
    ]
  },

  // 教师路由
  {
    path: '/teacher',
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/Dashboard.vue'),
        meta: { title: '教师仪表板' }
      },
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: () => import('@/views/teacher/Courses.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'courses/create',
        name: 'TeacherCreateCourse',
        component: () => import('@/views/teacher/CreateCourse.vue'),
        meta: { title: '创建课程' }
      },
      {
        path: 'courses/:courseId',
        name: 'TeacherCourseDetail',
        component: () => import('@/views/teacher/CourseDetail.vue'),
        meta: { title: '课程详情' }
      },
      {
        path: 'knowledge',
        name: 'TeacherKnowledgeManagement',
        component: () => import('@/views/teacher/KnowledgeManagement.vue'),
        meta: { title: '知识点管理' }
      },

      {
        path: 'analytics',
        name: 'TeacherAnalytics',
        component: () => import('@/views/teacher/Analytics.vue'),
        meta: { title: '教学分析' }
      },
      {
        path: 'exams',
        name: 'TeacherExams',
        component: () => import('@/views/teacher/Exams.vue'),
        meta: { title: '考试管理' }
      },
      {
        path: 'grades',
        name: 'TeacherGrades',
        component: () => import('@/views/teacher/Grades.vue'),
        meta: { title: '成绩管理' }
      },
      {
        path: 'notifications',
        name: 'TeacherNotifications',
        component: () => import('@/views/common/Notifications.vue'),
        meta: { title: '通知中心' }
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { title: '个人资料' }
      }
    ]
  },

  // 管理员路由
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理员仪表板' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/CourseManagement.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'analytics',
        name: 'AdminAnalytics',
        component: () => import('@/views/admin/Analytics.vue'),
        meta: { title: '数据分析' }
      },
      {
        path: 'exams',
        name: 'AdminExams',
        component: () => import('@/views/admin/ExamManagement.vue'),
        meta: { title: '考试管理' }
      },
      {
        path: 'grades',
        name: 'AdminGrades',
        component: () => import('@/views/admin/GradeManagement.vue'),
        meta: { title: '成绩管理' }
      },
      {
        path: 'questions',
        name: 'AdminQuestions',
        component: () => import('@/views/admin/QuestionManagement.vue'),
        meta: { title: '题目管理' }
      },
      {
        path: 'notifications',
        name: 'AdminNotifications',
        component: () => import('@/views/admin/NotificationManagement.vue'),
        meta: { title: '通知管理' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { title: '个人资料' }
      }
    ]
  },

  // 错误页面
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '权限不足' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const authStore = useAuthStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - AI智慧课程平台` : 'AI智慧课程平台'
  
  if (requiresAuth) {
    if (!authStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    // 检查用户角色权限
    if (to.meta.role && authStore.userRole !== to.meta.role) {
      ElMessage.error('权限不足')
      next('/403')
      return
    }
  } else {
    // 如果已登录用户访问登录/注册页，重定向到对应的仪表板
    if (authStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
      const defaultRoute = authStore.getDefaultRoute()
      next(defaultRoute)
      return
    }
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router 