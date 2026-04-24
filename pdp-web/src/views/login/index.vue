<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rememberMe = ref(false)
const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/auth/login', form)
    userStore.setToken(res.token)
    userStore.setUserInfo(res)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <!-- Left: Brand -->
    <div class="auth-brand">
      <div class="brand-content">
        <span class="brand-logo" aria-hidden="true">🌱</span>
        <h1 class="brand-title">成长规划</h1>
        <p class="brand-desc">记录你的大学四年，见证每一步成长</p>
      </div>
    </div>

    <!-- Right: Form -->
    <div class="auth-form-container">
      <div class="auth-card">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-subtitle">登录你的成长规划账号</p>

        <form class="auth-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="login-username">邮箱 / 学号</label>
            <input
              id="login-username"
              v-model="form.username"
              type="text"
              placeholder="请输入邮箱或学号"
              autocomplete="username"
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="form-group">
            <label for="login-password">密码</label>
            <input
              id="login-password"
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="form-options">
            <label class="checkbox">
              <input v-model="rememberMe" type="checkbox" />
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="auth-footer">
          还没有账号？<a @click="router.push('/register')">立即注册</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.auth-page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Noto Sans SC', sans-serif;
}

/* Left: Brand */
.auth-brand {
  background: linear-gradient(160deg, oklch(92% 0.06 20), oklch(88% 0.08 30));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  position: relative;
  overflow: hidden;
}

.auth-brand::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: oklch(85% 0.1 20 / 0.2);
}

.auth-brand::after {
  content: '';
  position: absolute;
  bottom: -60px;
  left: -60px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: oklch(90% 0.08 55 / 0.15);
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.brand-logo {
  font-size: 4rem;
  line-height: 1;
  margin-bottom: 1rem;
  display: block;
}

.brand-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2.5rem;
  color: oklch(58% 0.16 20);
  margin-bottom: 0.75rem;
}

.brand-desc {
  font-size: 1rem;
  color: oklch(48% 0.025 30);
  max-width: 32ch;
  margin: 0 auto;
  line-height: 1.7;
}

/* Right: Form */
.auth-form-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

.form-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2rem;
  margin-bottom: 0.25rem;
  color: oklch(25% 0.02 30);
}

.form-subtitle {
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.5rem;
}

.form-group input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-size: 1rem;
  font-family: inherit;
  color: oklch(25% 0.02 30);
  background: oklch(98% 0.008 30);
  transition: all 0.2s ease;
}

.form-group input::placeholder {
  color: oklch(62% 0.02 30);
}

.form-group input:focus {
  outline: none;
  border-color: oklch(70% 0.14 20);
  box-shadow: 0 0 0 3px oklch(85% 0.08 20 / 0.3);
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: oklch(48% 0.025 30);
  cursor: pointer;
}

.checkbox input {
  width: 18px;
  height: 18px;
  accent-color: oklch(70% 0.14 20);
  cursor: pointer;
}

.forgot-link {
  color: oklch(70% 0.14 20);
  font-size: 0.875rem;
  transition: color 0.2s ease;
  text-decoration: none;
}

.forgot-link:hover {
  color: oklch(58% 0.16 20);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.btn-primary {
  width: 100%;
  padding: 0.75rem 1.5rem;
  background: oklch(70% 0.14 20);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background: oklch(58% 0.16 20);
  box-shadow: 0 4px 12px oklch(70% 0.01 30 / 0.08);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.auth-footer {
  text-align: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid oklch(88% 0.015 30);
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
}

.auth-footer a {
  color: oklch(70% 0.14 20);
  font-weight: 500;
  transition: color 0.2s ease;
  cursor: pointer;
}

.auth-footer a:hover {
  color: oklch(58% 0.16 20);
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* Responsive */
@media (max-width: 800px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-brand {
    min-height: 200px;
    padding: 2rem;
  }

  .brand-logo {
    font-size: 3rem;
  }
  .brand-title {
    font-size: 2rem;
  }

  .auth-form-container {
    padding: 1.5rem;
  }
}

/* Accessibility */
a:focus-visible,
button:focus-visible,
input:focus-visible {
  outline: 2px solid oklch(70% 0.14 20);
  outline-offset: 2px;
}
</style>
