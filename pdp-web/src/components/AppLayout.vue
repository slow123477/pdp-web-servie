<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const menuItems = computed(() => {
  const layoutRoute = router.getRoutes().find((r) => r.path === '/')
  const hiddenPaths = ['profile', 'settings']
  return (
    layoutRoute?.children
      ?.filter((r) => r.meta?.title && !hiddenPaths.includes(r.name))
      .map((r) => ({
        path: '/' + r.path,
        title: r.meta.title,
      })) || []
  )
})

const userInitial = computed(() => {
  const name = userStore.userInfo?.username || userStore.userInfo?.realName || '用'
  return name.charAt(0)
})

function handleCommand(command) {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  }
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  })
}
</script>

<template>
  <div class="app-wrapper">
    <!-- 顶部导航 -->
    <nav class="top-nav" role="navigation" aria-label="主导航">
      <div class="nav-brand">
        <span class="nav-logo" aria-hidden="true">🌱</span>
        <span class="nav-title">成长规划</span>
      </div>
      <div class="nav-links">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          :class="['nav-link', { 'is-active': activeMenu === item.path }]"
        >
          {{ item.title }}
        </router-link>
      </div>
      <div class="nav-user">
        <span class="user-name">{{ userStore.userInfo?.username || '用户' }}</span>
        <el-dropdown @command="handleCommand" trigger="click">
          <div class="avatar-trigger">
            <img
              v-if="userStore.userInfo?.avatar"
              :src="userStore.userInfo.avatar"
              class="user-avatar"
              alt="头像"
            />
            <div v-else class="user-avatar" aria-label="用户头像">{{ userInitial }}</div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="settings">系统设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </nav>

    <!-- 内容区 -->
    <main class="page-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style scoped lang="scss">
.app-wrapper {
  min-height: 100vh;
  background: oklch(98% 0.008 30);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Noto Sans SC', sans-serif;
}

/* 顶部导航 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 2rem;
  background: oklch(96% 0.012 30);
  border-bottom: 1px solid oklch(88% 0.015 30);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-logo {
  font-size: 1.5rem;
  line-height: 1;
}

.nav-title {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  color: oklch(25% 0.02 30);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.nav-link {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: oklch(25% 0.02 30);
  background: oklch(98% 0.008 30);
}

.nav-link.is-active {
  color: oklch(58% 0.16 20);
  background: oklch(96% 0.02 20);
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-name {
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: oklch(85% 0.08 20);
  color: oklch(58% 0.16 20);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 0.875rem;
  border: 2px solid oklch(98% 0.008 30);
  box-shadow: 0 1px 2px oklch(70% 0.01 30 / 0.06);
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-avatar:hover {
  box-shadow: 0 0 0 3px oklch(85% 0.08 20 / 0.3);
}

.page-content {
  min-height: calc(100vh - 61px);
}

/* 页面过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .top-nav {
    padding: 0.75rem 1.5rem;
  }

  .nav-links {
    display: none;
  }
}
</style>
