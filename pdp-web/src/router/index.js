import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/index.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '仪表盘', icon: 'DataLine' },
        },
        {
          path: 'courses',
          name: 'courses',
          component: () => import('@/views/courses/index.vue'),
          meta: { title: '课程管理', icon: 'Notebook' },
        },
        {
          path: 'grades',
          name: 'grades',
          component: () => import('@/views/grades/index.vue'),
          meta: { title: '成绩与GPA', icon: 'TrendCharts' },
        },
        {
          path: 'experiences',
          name: 'experiences',
          component: () => import('@/views/experiences/index.vue'),
          meta: { title: '经历管理', icon: 'Briefcase' },
        },
        {
          path: 'achievements',
          name: 'achievements',
          component: () => import('@/views/achievements/index.vue'),
          meta: { title: '成就管理', icon: 'Trophy' },
        },
        {
          path: 'roles',
          name: 'roles',
          component: () => import('@/views/roles/index.vue'),
          meta: { title: '角色管理', icon: 'UserFilled' },
        },
        {
          path: 'ai-analysis',
          name: 'ai-analysis',
          component: () => import('@/views/ai-analysis/index.vue'),
          meta: { title: 'AI 分析', icon: 'ChatDotRound' },
        },
        {
          path: 'data',
          name: 'data',
          component: () => import('@/views/data/index.vue'),
          meta: { title: '导入导出', icon: 'Upload' },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/profile/index.vue'),
          meta: { title: '个人中心', icon: 'User' },
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/settings/index.vue'),
          meta: { title: '系统设置', icon: 'Setting' },
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
