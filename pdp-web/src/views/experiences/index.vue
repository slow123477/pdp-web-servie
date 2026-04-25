<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增经历')
const isEdit = ref(false)
const saving = ref(false)
const uploadLoading = ref(false)

const filterType = ref('')
const filterSort = ref('desc')

const typeOptions = [
  { label: '全部类型', value: '' },
  { label: '竞赛', value: '竞赛' },
  { label: '项目', value: '项目' },
  { label: '实习', value: '实习' },
]

const sortOptions = [
  { label: '时间倒序', value: 'desc' },
  { label: '时间正序', value: 'asc' },
]

const typeStyle = {
  竞赛: {
    tagBg: 'oklch(96% 0.025 75)',
    tagColor: 'oklch(55% 0.14 75)',
    tagBorder: 'oklch(85% 0.08 75)',
    dot: 'oklch(70% 0.14 75)',
  },
  项目: {
    tagBg: 'oklch(96% 0.02 155)',
    tagColor: 'oklch(48% 0.12 155)',
    tagBorder: 'oklch(85% 0.06 155)',
    dot: 'oklch(68% 0.12 155)',
  },
  实习: {
    tagBg: 'oklch(96% 0.02 280)',
    tagColor: 'oklch(48% 0.13 280)',
    tagBorder: 'oklch(85% 0.06 280)',
    dot: 'oklch(65% 0.13 280)',
  },
}

const form = reactive({
  id: null,
  title: '',
  type: '竞赛',
  startDate: '',
  endDate: '',
  description: '',
  result: '',
  attachments: [],
})

const formRef = ref(null)

const rules = {
  title: [{ required: true, message: '请输入经历标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择经历类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
}

const rawList = ref([])

const list = computed(() => {
  let data = [...rawList.value]
  if (filterType.value) {
    data = data.filter((item) => item.type === filterType.value)
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
  form.title = ''
  form.type = '竞赛'
  form.startDate = ''
  form.endDate = ''
  form.description = ''
  form.result = ''
  form.attachments = []
}

function openAddDialog() {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '新增经历'
  dialogVisible.value = true
}

function openEditDialog(row) {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑经历'
  Object.assign(form, row)
  if (!form.attachments) form.attachments = []
  dialogVisible.value = true
}

function formatDateRange(start, end) {
  if (!start) return ''
  const s = start
  const e = end || '至今'
  return `${s} ~ ${e}`
}

function formatTimelineDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
}

function addAttachment() {
  form.attachments.push({ name: '', url: '' })
}

function removeAttachment(index) {
  form.attachments.splice(index, 1)
}

async function handleAttachmentUpload(options, index) {
  const formData = new FormData()
  formData.append('file', options.file)
  uploadLoading.value = true
  try {
    const res = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    form.attachments[index].url = res
    if (!form.attachments[index].name) {
      form.attachments[index].name = options.file.name
    }
    ElMessage.success('上传成功')
  } catch (error) {
    console.error(error)
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
      title: form.title,
      type: form.type,
      startDate: form.startDate,
      endDate: form.endDate || null,
      description: form.description,
      result: form.result,
      attachments: form.attachments.filter((a) => a.name || a.url),
    }
    if (isEdit.value) {
      await request.put('/experiences', { id: form.id, ...payload })
      ElMessage.success('修改成功')
    } else {
      await request.post('/experiences', payload)
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
    await ElMessageBox.confirm('确定删除这条经历吗？', '提示', { type: 'warning' })
    await request.delete(`/experiences/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

async function fetchList() {
  loading.value = true
  try {
    // 后端尚未实现，先用 mock 数据
    // const res = await request.get('/experiences', { params: { type: filterType.value, page: 1, pageSize: 100 } })
    // rawList.value = res.rows || []

    rawList.value = [
      {
        id: 1,
        title: '腾讯暑期实习申请',
        type: '实习',
        startDate: '2026-04-01',
        endDate: '2026-07-01',
        description: '投递产品运营岗位，已完成笔试和初面，等待二面结果。准备了产品分析报告和过往项目作品集。',
        result: '',
        attachments: [],
        createdAt: '2026-04-10T10:00:00',
      },
      {
        id: 2,
        title: '全国大学生数学建模竞赛',
        type: '竞赛',
        startDate: '2026-03-01',
        endDate: '2026-03-15',
        description: '与两位队友合作完成"城市交通拥堵预测"题目，使用 Python + MATLAB 进行数据分析和模型构建，获省级一等奖。',
        result: '省级一等奖',
        attachments: [],
        createdAt: '2026-03-20T10:00:00',
      },
      {
        id: 3,
        title: '校园植物识别科研项目',
        type: '项目',
        startDate: '2026-03-01',
        endDate: '2026-06-30',
        description: '加入计算机学院课题组，负责图像识别模块开发，使用 PyTorch 训练 ResNet 模型，准确率达到 92%。',
        result: '优秀结题',
        attachments: [],
        createdAt: '2026-07-01T10:00:00',
      },
      {
        id: 4,
        title: 'ACM 程序设计竞赛（区域赛）',
        type: '竞赛',
        startDate: '2025-11-01',
        endDate: '2025-11-03',
        description: '代表学校参加 ACM-ICPC 亚洲区域赛，团队获得铜牌。主要负责图论和动态规划类题目。',
        result: '铜牌',
        attachments: [],
        createdAt: '2025-11-10T10:00:00',
      },
      {
        id: 5,
        title: '学生选课系统重构',
        type: '项目',
        startDate: '2025-09-01',
        endDate: '2025-12-30',
        description: '课程大作业，使用 Spring Boot + Vue 重构学校旧版选课系统，实现了并发选课的乐观锁机制。',
        result: '优秀毕业设计',
        attachments: [],
        createdAt: '2026-01-05T10:00:00',
      },
    ]
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="experiences-page">
    <!-- Header -->
    <div class="page-header-bar">
      <div>
        <h1 class="page-title">经历管理</h1>
      </div>
      <button class="btn-primary" @click="openAddDialog">+ 添加经历</button>
    </div>

    <!-- Filter -->
    <div class="filter-bar">
      <select v-model="filterType" class="filter-select">
        <option v-for="t in typeOptions" :key="t.value" :value="t.value">{{ t.label }}</option>
      </select>
      <select v-model="filterSort" class="filter-select">
        <option v-for="s in sortOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
      </select>
    </div>

    <!-- Timeline -->
    <div v-if="loading" class="loading-state">加载中...</div>
    <template v-else>
      <div v-if="list.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <div class="empty-text">暂无经历记录</div>
        <div class="empty-hint">点击右上角「添加经历」添加第一条记录</div>
      </div>

      <div v-else class="timeline">
        <div
          v-for="item in list"
          :key="item.id"
          class="timeline-item"
          :style="{ '--dot-color': typeStyle[item.type]?.dot || 'oklch(70% 0.14 20)' }"
        >
          <div class="timeline-date">{{ formatTimelineDate(item.startDate) }}</div>
          <div class="timeline-card">
            <div class="timeline-header">
              <span
                class="timeline-type"
                :style="{
                  background: typeStyle[item.type]?.tagBg,
                  color: typeStyle[item.type]?.tagColor,
                  borderColor: typeStyle[item.type]?.tagBorder,
                }"
              >
                {{ item.type }}
              </span>
              <div class="card-actions">
                <button class="btn-text" @click="openEditDialog(item)">编辑</button>
                <button class="btn-text danger" @click="handleDelete(item)">删除</button>
              </div>
            </div>
            <h3 class="timeline-title">{{ item.title }}</h3>
            <div v-if="item.startDate" class="timeline-meta">
              {{ formatDateRange(item.startDate, item.endDate) }}
            </div>
            <p v-if="item.description" class="timeline-desc">{{ item.description }}</p>

            <div v-if="item.result" class="timeline-result">
              <span class="result-label">成果 / 奖项：</span>
              <span class="result-value">{{ item.result }}</span>
            </div>

            <div v-if="item.attachments?.length" class="timeline-attachments">
              <div v-for="(att, idx) in item.attachments" :key="idx" class="attachment-item">
                <span class="attachment-icon">📎</span>
                <a :href="att.url" target="_blank" class="attachment-name">{{ att.name }}</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="经历标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：全国大学生数学建模竞赛" />
        </el-form-item>

        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="竞赛" value="竞赛" />
            <el-option label="项目" value="项目" />
            <el-option label="实习" value="实习" />
          </el-select>
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

        <el-form-item label="详细描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述你的职责、过程和收获"
          />
        </el-form-item>

        <el-form-item label="成果 / 奖项">
          <el-input v-model="form.result" placeholder="例如：省级一等奖、获得转正 offer" />
        </el-form-item>

        <el-form-item label="附件">
          <div
            v-for="(att, idx) in form.attachments"
            :key="idx"
            class="attachment-input-row"
          >
            <el-input v-model="att.name" placeholder="文件名称" style="flex: 1" />
            <el-input v-model="att.url" placeholder="文件链接" style="flex: 2" />
            <el-upload
              :show-file-list="false"
              :http-request="(opts) => handleAttachmentUpload(opts, idx)"
              accept="*"
              style="display: inline-block"
            >
              <button type="button" class="btn-upload" :disabled="uploadLoading">
                {{ uploadLoading ? '...' : '上传' }}
              </button>
            </el-upload>
            <button type="button" class="btn-icon" @click="removeAttachment(idx)">✕</button>
          </div>
          <button type="button" class="btn-text-add" @click="addAttachment">+ 添加附件</button>
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
.experiences-page {
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

/* Timeline */
.timeline {
  position: relative;
  padding-left: 1.5rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 6px;
  bottom: 6px;
  width: 2px;
  background: oklch(88% 0.015 30);
  border-radius: 1px;
}

.timeline-item {
  position: relative;
  padding-bottom: 1.5rem;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: calc(1.5rem * -1 + 1px);
  top: 5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: oklch(98% 0.008 30);
  border: 2px solid var(--dot-color, oklch(70% 0.14 20));
}

.timeline-date {
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
  margin-bottom: 0.25rem;
}

.timeline-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1rem 1.5rem;
  transition: all 0.2s ease;
}

.timeline-card:hover {
  background: oklch(94% 0.015 30);
  box-shadow: 0 1px 2px oklch(70% 0.01 30 / 0.06);
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.timeline-type {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 100px;
  font-weight: 500;
  border: 1px solid;
}

.timeline-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 0.25rem;
}

.timeline-meta {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  margin-bottom: 0.5rem;
}

.timeline-desc {
  font-size: 0.9375rem;
  color: oklch(48% 0.025 30);
  line-height: 1.6;
  margin: 0 0 0.5rem;
}

.timeline-result {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.result-label {
  color: oklch(62% 0.02 30);
}

.result-value {
  color: oklch(58% 0.16 20);
  font-weight: 500;
}

.timeline-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
}

.attachment-icon {
  font-size: 1rem;
}

.attachment-name {
  color: oklch(70% 0.14 20);
  text-decoration: none;
  transition: color 0.2s ease;
}

.attachment-name:hover {
  color: oklch(58% 0.16 20);
  text-decoration: underline;
  text-underline-offset: 3px;
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

/* Dialog form */
.form-row {
  display: flex;
  gap: 1rem;
}

.attachment-input-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.btn-upload {
  padding: 0 0.75rem;
  height: 32px;
  background: oklch(96% 0.02 20);
  color: oklch(70% 0.14 20);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 8px;
  font-size: 0.875rem;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-upload:hover {
  background: oklch(92% 0.04 20);
  border-color: oklch(70% 0.14 20);
}

.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: oklch(95% 0.03 25);
  color: oklch(55% 0.18 25);
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-icon:hover {
  background: oklch(90% 0.06 25);
}

.btn-text-add {
  padding: 0.5rem 0;
  background: none;
  border: none;
  color: oklch(70% 0.14 20);
  font-size: 0.875rem;
  font-family: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}

.btn-text-add:hover {
  color: oklch(58% 0.16 20);
  text-decoration: underline;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

/* Responsive */
@media (max-width: 768px) {
  .experiences-page {
    padding: 2rem 1.5rem;
  }

  .page-header-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .timeline-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .attachment-input-row {
    flex-wrap: wrap;
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
