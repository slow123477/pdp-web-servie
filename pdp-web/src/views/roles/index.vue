<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const isEdit = ref(false)
const saving = ref(false)

const filterStatus = ref('')
const filterSort = ref('desc')

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '在职', value: '1' },
  { label: '已离职', value: '0' },
]

const sortOptions = [
  { label: '时间倒序', value: 'desc' },
  { label: '时间正序', value: 'asc' },
]

const form = reactive({
  id: null,
  roleName: '',
  organization: '',
  startDate: '',
  endDate: '',
  isCurrent: 1,
  responsibilities: '',
})

const formRef = ref(null)

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
}

const rawList = ref([])

const list = computed(() => {
  let data = [...rawList.value]
  if (filterStatus.value !== '') {
    const target = Number(filterStatus.value)
    data = data.filter((item) => item.isCurrent === target)
  }
  data.sort((a, b) => {
    const da = new Date(a.startDate).getTime()
    const db = new Date(b.startDate).getTime()
    return filterSort.value === 'asc' ? da - db : db - da
  })
  return data
})

function resetForm() {
  form.id = null
  form.roleName = ''
  form.organization = ''
  form.startDate = ''
  form.endDate = ''
  form.isCurrent = 1
  form.responsibilities = ''
}

function openAddDialog() {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

function openEditDialog(row) {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
}

function formatDateRange(start, end, isCurrent) {
  if (!start) return ''
  const s = start
  const e = end || (isCurrent ? '至今' : '-')
  return `${s} ~ ${e}`
}

function formatTimelineDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      roleName: form.roleName,
      organization: form.organization || null,
      startDate: form.startDate,
      endDate: form.endDate || null,
      isCurrent: form.isCurrent,
      responsibilities: form.responsibilities || null,
    }
    if (isEdit.value) {
      await request.put('/roles', { id: form.id, ...payload })
      ElMessage.success('修改成功')
    } else {
      await request.post('/roles', payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
    await request.delete(`/roles/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await request.get('/roles', {
      params: {
        isCurrent: filterStatus.value !== '' ? Number(filterStatus.value) : undefined,
        page: 1,
        pageSize: 100,
      },
    })
    rawList.value = res.rows || []
  } catch (error) {
    console.error(error)
    rawList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="roles-page">
    <!-- Header -->
    <div class="page-header-bar">
      <div>
        <h1 class="page-title">角色管理</h1>
      </div>
      <button class="btn-primary" @click="openAddDialog">+ 添加角色</button>
    </div>

    <!-- Filter -->
    <div class="filter-bar">
      <select v-model="filterStatus" class="filter-select" @change="fetchList">
        <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
      </select>
      <select v-model="filterSort" class="filter-select">
        <option v-for="s in sortOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
      </select>
    </div>

    <!-- List -->
    <div v-if="loading" class="loading-state">加载中...</div>
    <template v-else>
      <div v-if="list.length === 0" class="empty-state">
        <div class="empty-icon">👤</div>
        <div class="empty-text">暂无角色记录</div>
        <div class="empty-hint">点击右上角「添加角色」添加第一条记录</div>
      </div>

      <div v-else class="role-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="role-card"
          :class="{ 'is-current': item.isCurrent === 1 }"
        >
          <div class="role-main">
            <div class="role-icon">{{ item.isCurrent === 1 ? '👤' : '👋' }}</div>
            <div class="role-info">
              <h3 class="role-name">{{ item.roleName }}</h3>
              <div v-if="item.organization" class="role-org">{{ item.organization }}</div>
              <div class="role-date">
                {{ formatDateRange(item.startDate, item.endDate, item.isCurrent) }}
              </div>
            </div>
            <span class="status-tag" :class="item.isCurrent === 1 ? 'current' : 'past'">
              {{ item.isCurrent === 1 ? '在职' : '已离职' }}
            </span>
          </div>

          <p v-if="item.responsibilities" class="role-desc">{{ item.responsibilities }}</p>

          <div class="role-actions">
            <button class="btn-text" @click="openEditDialog(item)">编辑</button>
            <button class="btn-text danger" @click="handleDelete(item)">删除</button>
          </div>
        </div>
      </div>
    </template>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="例如：班级学习委员" />
        </el-form-item>

        <el-form-item label="所属组织 / 单位">
          <el-input v-model="form.organization" placeholder="例如：计算机科学与技术3班" />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="开始日期" prop="startDate" style="flex: 1">
            <el-date-picker
              v-model="form.startDate"
              type="date"
              placeholder="选择日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="结束日期" style="flex: 1">
            <el-date-picker
              v-model="form.endDate"
              type="date"
              placeholder="留空表示至今"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </div>

        <el-form-item label="是否在职">
          <el-radio-group v-model="form.isCurrent">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">已离职</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="职责描述">
          <el-input
            v-model="form.responsibilities"
            type="textarea"
            :rows="3"
            placeholder="描述你的职责范围和工作内容"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <button class="btn-secondary" @click="dialogVisible = false">取消</button>
          <button class="btn-primary" :disabled="saving" @click="handleSave">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.roles-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 3rem 2rem;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Noto Sans SC', sans-serif;
}

.page-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.page-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
}

.btn-primary {
  padding: 0.5rem 1rem;
  background: oklch(70% 0.14 20);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.875rem;
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

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: oklch(96% 0.012 30);
  color: oklch(25% 0.02 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-size: 1rem;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background: oklch(94% 0.015 30);
}

/* Filter */
.filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.filter-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-family: inherit;
  font-size: 0.875rem;
  color: oklch(25% 0.02 30);
  background: oklch(96% 0.012 30);
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-select:focus {
  outline: none;
  border-color: oklch(70% 0.14 20);
  box-shadow: 0 0 0 3px oklch(85% 0.08 20 / 0.3);
}

/* Loading & Empty */
.loading-state {
  text-align: center;
  padding: 4rem;
  color: oklch(62% 0.02 30);
  font-size: 0.875rem;
}

.empty-state {
  text-align: center;
  padding: 5rem 2rem;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-text {
  font-size: 1.125rem;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.5rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
}

/* Role List */
.role-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.role-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
  transition: all 0.2s ease;
}

.role-card:hover {
  background: oklch(94% 0.015 30);
  box-shadow: 0 1px 2px oklch(70% 0.01 30 / 0.06);
}

.role-card.is-current {
  border-left: 3px solid oklch(70% 0.14 20);
}

.role-main {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.role-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: oklch(96% 0.02 195);
  color: oklch(68% 0.12 195);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.role-info {
  flex: 1;
  min-width: 0;
}

.role-name {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 0.25rem;
}

.role-org {
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
  margin-bottom: 0.125rem;
}

.role-date {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
}

.status-tag {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 100px;
  font-weight: 500;
  flex-shrink: 0;
}

.status-tag.current {
  background: oklch(96% 0.02 155);
  color: oklch(48% 0.12 155);
  border: 1px solid oklch(85% 0.06 155);
}

.status-tag.past {
  background: oklch(96% 0.012 30);
  color: oklch(62% 0.02 30);
  border: 1px solid oklch(88% 0.015 30);
}

.role-desc {
  font-size: 0.9375rem;
  color: oklch(48% 0.025 30);
  line-height: 1.6;
  margin: 0.75rem 0 0;
  padding-left: calc(40px + 0.75rem);
}

.role-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.75rem;
  padding-left: calc(40px + 0.75rem);
}

.btn-text {
  padding: 0;
  background: none;
  border: none;
  color: oklch(70% 0.14 20);
  font-size: 0.875rem;
  font-family: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}

.btn-text:hover {
  color: oklch(58% 0.16 20);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.btn-text.danger {
  color: oklch(55% 0.18 25);
}

.btn-text.danger:hover {
  color: oklch(45% 0.2 25);
}

/* Dialog form */
.form-row {
  display: flex;
  gap: 1rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

/* Responsive */
@media (max-width: 768px) {
  .roles-page {
    padding: 2rem 1.5rem;
  }

  .page-header-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .role-main {
    flex-wrap: wrap;
  }

  .role-desc,
  .role-actions {
    padding-left: 0;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}

/* Accessibility */
button:focus-visible,
select:focus-visible,
input:focus-visible {
  outline: 2px solid oklch(70% 0.14 20);
  outline-offset: 2px;
}
</style>
