<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)

const profile = reactive({
  id: null,
  username: '',
  email: '',
  studentId: '',
  realName: '',
  avatar: '',
  major: '',
  gradeYear: null,
  createdAt: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function fetchUserInfo() {
  loading.value = true
  try {
    const res = await request.get('/auth/user')
    Object.assign(profile, res)
    userStore.setUserInfo(res)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleSaveProfile() {
  savingProfile.value = true
  try {
    await request.put('/users', {
      id: profile.id,
      realName: profile.realName,
      avatar: profile.avatar,
      major: profile.major,
      gradeYear: profile.gradeYear,
      studentId: profile.studentId,
    })
    ElMessage.success('资料保存成功')
    userStore.setUserInfo({ ...userStore.userInfo, ...profile })
  } catch (error) {
    console.error(error)
  } finally {
    savingProfile.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少为 6 位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  savingPassword.value = true
  try {
    await request.put('/users/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error(error)
  } finally {
    savingPassword.value = false
  }
}

async function handleAvatarUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/users/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    profile.avatar = res
    userStore.setUserInfo({ ...userStore.userInfo, avatar: res })
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error(error)
  }
}

const gradeOptions = [
  { label: '大一', value: 1 },
  { label: '大二', value: 2 },
  { label: '大三', value: 3 },
  { label: '大四', value: 4 },
]

function userInitial() {
  const name = profile.realName || profile.username || '用'
  return name.charAt(0)
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <div class="profile-page">
    <div v-if="loading" class="loading-state">加载中...</div>
    <template v-else>
      <h1 class="page-title">个人中心</h1>

      <div class="profile-grid">
        <!-- Sidebar -->
        <aside class="profile-sidebar">
          <div class="avatar-card">
            <img v-if="profile.avatar" :src="profile.avatar" class="avatar-img" alt="头像" />
            <div v-else class="avatar-large">{{ userInitial() }}</div>
            <div class="avatar-name">{{ profile.realName || profile.username }}</div>
            <div class="avatar-email">{{ profile.email }}</div>
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <button class="btn-change-avatar">更换头像</button>
            </el-upload>
          </div>
        </aside>

        <!-- Main -->
        <div class="profile-main">
          <!-- Basic Info -->
          <div class="section-card">
            <h2 class="section-title">基本信息</h2>
            <div class="form-grid">
              <div class="form-group">
                <label>用户名</label>
                <input v-model="profile.username" type="text" disabled />
              </div>
              <div class="form-group">
                <label>真实姓名</label>
                <input v-model="profile.realName" type="text" placeholder="请输入真实姓名" />
              </div>
              <div class="form-group">
                <label>学号</label>
                <input v-model="profile.studentId" type="text" placeholder="请输入学号" />
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input v-model="profile.email" type="text" disabled />
              </div>
              <div class="form-group">
                <label>专业</label>
                <input v-model="profile.major" type="text" placeholder="请输入专业" />
              </div>
              <div class="form-group">
                <label>年级</label>
                <select v-model="profile.gradeYear">
                  <option :value="null">请选择</option>
                  <option v-for="g in gradeOptions" :key="g.value" :value="g.value">{{ g.label }}</option>
                </select>
              </div>
            </div>
            <button class="btn-primary" :disabled="savingProfile" @click="handleSaveProfile">
              {{ savingProfile ? '保存中...' : '保存修改' }}
            </button>
          </div>

          <!-- Password -->
          <div class="section-card">
            <h2 class="section-title">修改密码</h2>
            <div class="form-grid">
              <div class="form-group">
                <label>当前密码</label>
                <input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
              </div>
              <div class="form-group">
                <label>新密码</label>
                <input v-model="passwordForm.newPassword" type="password" placeholder="设置新密码（至少6位）" />
              </div>
              <div class="form-group">
                <label>确认新密码</label>
                <input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" />
              </div>
            </div>
            <button class="btn-primary" :disabled="savingPassword" @click="handleChangePassword">
              {{ savingPassword ? '更新中...' : '更新密码' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 3rem 2rem;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Noto Sans SC', sans-serif;
}

.page-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin-bottom: 1.5rem;
}

.loading-state {
  text-align: center;
  padding: 4rem;
  color: oklch(62% 0.02 30);
  font-size: 0.875rem;
}

/* Grid */
.profile-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 1.5rem;
  align-items: start;
}

/* Sidebar */
.avatar-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.avatar-img {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid oklch(98% 0.008 30);
  box-shadow: 0 1px 2px oklch(70% 0.01 30 / 0.06);
}

.avatar-large {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: oklch(85% 0.08 20);
  color: oklch(58% 0.16 20);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.5rem;
  border: 3px solid oklch(98% 0.008 30);
  box-shadow: 0 1px 2px oklch(70% 0.01 30 / 0.06);
}

.avatar-name {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  color: oklch(25% 0.02 30);
  margin-top: 0.25rem;
}

.avatar-email {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  word-break: break-all;
}

.btn-change-avatar {
  margin-top: 0.25rem;
  padding: 0.5rem 1rem;
  background: transparent;
  color: oklch(70% 0.14 20);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-size: 0.875rem;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-change-avatar:hover {
  background: oklch(96% 0.02 20);
  border-color: oklch(70% 0.14 20);
}

/* Main Cards */
.section-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.section-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin-bottom: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: oklch(25% 0.02 30);
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-size: 1rem;
  font-family: inherit;
  color: oklch(25% 0.02 30);
  background: oklch(98% 0.008 30);
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-group input::placeholder {
  color: oklch(62% 0.02 30);
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: oklch(70% 0.14 20);
  box-shadow: 0 0 0 3px oklch(85% 0.08 20 / 0.3);
}

.form-group input:disabled {
  background: oklch(94% 0.01 30);
  color: oklch(62% 0.02 30);
  cursor: not-allowed;
}

.btn-primary {
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

/* Responsive */
@media (max-width: 768px) {
  .profile-page {
    padding: 2rem 1.5rem;
  }

  .profile-grid {
    grid-template-columns: 1fr;
  }

  .avatar-card {
    flex-direction: row;
    text-align: left;
    align-items: center;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

/* Accessibility */
button:focus-visible,
input:focus-visible,
select:focus-visible {
  outline: 2px solid oklch(70% 0.14 20);
  outline-offset: 2px;
}

:deep(.el-upload) {
  display: inline-block;
}
</style>
