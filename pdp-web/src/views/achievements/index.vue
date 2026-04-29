<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增成就')
const isEdit = ref(false)
const saving = ref(false)
const uploadLoading = ref(false)

const filterLevel = ref('')
const filterCategory = ref('')
const filterSort = ref('desc')

const levelOptions = [
  { label: '全部级别', value: '' },
  { label: '国家级', value: '国家级' },
  { label: '省级', value: '省级' },
  { label: '校级', value: '校级' },
  { label: '院级', value: '院级' },
]

const categoryOptions = [
  { label: '全部分类', value: '' },
  { label: '学术', value: '学术' },
  { label: '竞赛', value: '竞赛' },
  { label: '志愿', value: '志愿' },
  { label: '其他', value: '其他' },
]

const sortOptions = [
  { label: '时间倒序', value: 'desc' },
  { label: '时间正序', value: 'asc' },
]

const levelIconMap = {
  国家级: '🏆',
  省级: '🥈',
  校级: '⭐',
  院级: '🏅',
}

const levelStyle = {
  国家级: { bg: 'oklch(92% 0.04 55)', color: 'oklch(50% 0.18 55)', border: 'oklch(80% 0.1 55)' },
  省级: { bg: 'oklch(94% 0.03 250)', color: 'oklch(55% 0.14 250)', border: 'oklch(82% 0.08 250)' },
  校级: { bg: 'oklch(96% 0.02 155)', color: 'oklch(48% 0.12 155)', border: 'oklch(85% 0.06 155)' },
  院级: { bg: 'oklch(96% 0.012 30)', color: 'oklch(62% 0.02 30)', border: 'oklch(88% 0.015 30)' },
}

const categoryStyle = {
  学术: { bg: 'oklch(92% 0.04 55)', color: 'oklch(50% 0.18 55)' },
  竞赛: { bg: 'oklch(94% 0.03 250)', color: 'oklch(55% 0.14 250)' },
  志愿: { bg: 'oklch(96% 0.02 155)', color: 'oklch(48% 0.12 155)' },
  其他: { bg: 'oklch(96% 0.012 30)', color: 'oklch(62% 0.02 30)' },
}

const form = reactive({
  id: null,
  name: '',
  level: '校级',
  category: '竞赛',
  issuer: '',
  awardDate: '',
  description: '',
  certificateUrl: '',
})

const formRef = ref(null)

const rules = {
  name: [{ required: true, message: '请输入成就名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择级别', trigger: 'change' }],
  awardDate: [{ required: true, message: '请选择获得日期', trigger: 'change' }],
}

const rawList = ref([])

const list = computed(() => {
  let data = [...rawList.value]
  if (filterLevel.value) {
    data = data.filter((item) => item.level === filterLevel.value)
  }
  if (filterCategory.value) {
    data = data.filter((item) => item.category === filterCategory.value)
  }
  data.sort((a, b) => {
    const da = new Date(a.awardDate).getTime()
    const db = new Date(b.awardDate).getTime()
    return filterSort.value === 'asc' ? da - db : db - da
  })
  return data
})

function resetForm() {
  form.id = null
  form.name = ''
  form.level = '校级'
  form.category = '竞赛'
  form.issuer = ''
  form.awardDate = ''
  form.description = ''
  form.certificateUrl = ''
}

function openAddDialog() {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '新增成就'
  dialogVisible.value = true
}

function openEditDialog(row) {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑成就'
  Object.assign(form, row)
  dialogVisible.value = true
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
}

async function handleCertificateUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  uploadLoading.value = true
  try {
    const res = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    form.certificateUrl = res
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    uploadLoading.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      name: form.name,
      level: form.level,
      category: form.category,
      issuer: form.issuer,
      awardDate: form.awardDate,
      description: form.description,
      certificateUrl: form.certificateUrl || null,
    }
    if (isEdit.value) {
      await request.put('/achievements', { id: form.id, ...payload })
      ElMessage.success('修改成功')
    } else {
      await request.post('/achievements', payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除这条成就吗？', '提示', { type: 'warning' })
    await request.delete(`/achievements/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '删除失败')
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await request.get('/achievements', {
      params: {
        level: filterLevel.value || undefined,
        category: filterCategory.value || undefined,
        page: 1,
        pageSize: 100,
      },
    })
    rawList.value = res.rows || []
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
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
  <div class="achievements-page">
    <!-- Header -->
    <div class="page-header-bar">
      <div>
        <h1 class="page-title">成就管理</h1>
      </div>
      <button type="button" class="btn-primary" @click="openAddDialog">+ 添加成就</button>
    </div>

    <!-- Filter -->
    <div class="filter-bar">
      <select v-model="filterLevel" class="filter-select">
        <option v-for="o in levelOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
      <select v-model="filterCategory" class="filter-select">
        <option v-for="o in categoryOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
      <select v-model="filterSort" class="filter-select">
        <option v-for="o in sortOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
    </div>

    <!-- Grid -->
    <div v-if="loading" class="loading-state">加载中...</div>
    <template v-else>
      <div v-if="list.length === 0" class="empty-state">
        <div class="empty-icon">🏆</div>
        <div class="empty-text">暂无成就记录</div>
        <div class="empty-hint">点击右上角「添加成就」添加第一条记录</div>
      </div>

      <div v-else class="achievement-grid">
        <div v-for="item in list" :key="item.id" class="achievement-card">
          <div class="card-header">
            <div class="achievement-icon">
              {{ levelIconMap[item.level] || '🏅' }}
            </div>
            <div class="card-tags">
              <span
                class="level-tag"
                :style="{
                  background: levelStyle[item.level]?.bg,
                  color: levelStyle[item.level]?.color,
                  borderColor: levelStyle[item.level]?.border,
                }"
              >
                {{ item.level }}
              </span>
              <span
                v-if="item.category"
                class="category-tag"
                :style="{
                  background: categoryStyle[item.category]?.bg,
                  color: categoryStyle[item.category]?.color,
                }"
              >
                {{ item.category }}
              </span>
            </div>
            <div class="card-actions">
              <button type="button" class="btn-text" @click="openEditDialog(item)">编辑</button>
              <button type="button" class="btn-text danger" @click="handleDelete(item)">删除</button>
            </div>
          </div>

          <h3 class="card-name">{{ item.name }}</h3>

          <div class="card-meta">
            <span>{{ item.issuer }}</span>
            <span class="meta-sep">·</span>
            <span>{{ formatDate(item.awardDate) }}</span>
          </div>

          <p v-if="item.description" class="card-desc">{{ item.description }}</p>

          <div v-if="item.certificateUrl" class="card-certificate">
            <img :src="item.certificateUrl" alt="证书" class="certificate-img" />
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
        <el-form-item label="成就名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：全国大学生数学建模竞赛一等奖" />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="级别" prop="level" style="flex: 1">
            <el-select v-model="form.level" placeholder="请选择" style="width: 100%">
              <el-option label="国家级" value="国家级" />
              <el-option label="省级" value="省级" />
              <el-option label="校级" value="校级" />
              <el-option label="院级" value="院级" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类" style="flex: 1">
            <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
              <el-option label="学术" value="学术" />
              <el-option label="竞赛" value="竞赛" />
              <el-option label="志愿" value="志愿" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="颁发机构" style="flex: 1">
            <el-input v-model="form.issuer" placeholder="例如：中国工业与应用数学学会" />
          </el-form-item>
          <el-form-item label="获得日期" prop="awardDate" style="flex: 1">
            <el-date-picker
              v-model="form.awardDate"
              type="date"
              placeholder="选择日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </div>

        <el-form-item label="详细描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述获奖背景、过程或意义"
          />
        </el-form-item>

        <el-form-item label="证书图片">
          <el-upload
            v-if="!form.certificateUrl"
            class="certificate-uploader"
            :show-file-list="false"
            :http-request="handleCertificateUpload"
            accept="image/*"
          >
            <div class="upload-placeholder">
              <div class="upload-icon">📤</div>
              <div>{{ uploadLoading ? '上传中...' : '点击上传证书图片' }}</div>
            </div>
          </el-upload>
          <div v-else class="certificate-preview">
            <img :src="form.certificateUrl" alt="证书预览" />
            <button type="button" class="btn-remove-cert" @click="form.certificateUrl = ''">
              移除图片
            </button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <button type="button" class="btn-secondary" @click="dialogVisible = false">取消</button>
          <button class="btn-primary" :disabled="saving" @click="handleSave">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.achievements-page {
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
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
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

/* Grid */
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.achievement-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
  transition: all 0.2s ease;
}

.achievement-card:hover {
  background: oklch(94% 0.015 30);
  box-shadow: 0 1px 2px oklch(70% 0.01 30 / 0.06);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.achievement-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: oklch(96% 0.025 75);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.card-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  flex: 1;
}

.level-tag {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 100px;
  font-weight: 500;
  border: 1px solid;
}

.category-tag {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 100px;
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;
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

.card-name {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 0.5rem;
}

.card-meta {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  margin-bottom: 0.75rem;
}

.meta-sep {
  margin: 0 0.35rem;
  color: oklch(88% 0.015 30);
}

.card-desc {
  font-size: 0.9375rem;
  color: oklch(48% 0.025 30);
  line-height: 1.6;
  margin: 0 0 0.75rem;
}

.card-certificate {
  margin-top: 0.75rem;
}

.certificate-img {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid oklch(88% 0.015 30);
}

/* Dialog form */
.form-row {
  display: flex;
  gap: 1rem;
}

.certificate-uploader {
  width: 100%;
}

.upload-placeholder {
  width: 100%;
  padding: 2rem;
  border: 2px dashed oklch(88% 0.015 30);
  border-radius: 10px;
  text-align: center;
  color: oklch(62% 0.02 30);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.upload-placeholder:hover {
  border-color: oklch(70% 0.14 20);
  background: oklch(96% 0.02 20);
}

.upload-icon {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.certificate-preview {
  position: relative;
  display: inline-block;
}

.certificate-preview img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 10px;
  border: 1px solid oklch(88% 0.015 30);
}

.btn-remove-cert {
  margin-top: 0.5rem;
  padding: 0.375rem 0.75rem;
  background: oklch(95% 0.03 25);
  color: oklch(55% 0.18 25);
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-remove-cert:hover {
  background: oklch(90% 0.06 25);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

/* Responsive */
@media (max-width: 768px) {
  .achievements-page {
    padding: 2rem 1.5rem;
  }

  .page-header-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .achievement-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-wrap: wrap;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}

/* Accessibility */
button:focus-visible,
select:focus-visible,
a:focus-visible {
  outline: 2px solid oklch(70% 0.14 20);
  outline-offset: 2px;
}
</style>
