<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const dataTypeOptions = ['课程', '成绩', '经历', '成就', '角色']

const importType = ref('课程')
const importing = ref(false)
const importFile = ref(null)

const exportType = ref('课程')
const exporting = ref(false)

const filterType = ref('')
const filterStatus = ref('')

const loading = ref(false)
const rawList = ref([])

const statusMap = {
  pending: { label: '处理中', class: 'status-pending' },
  success: { label: '成功', class: 'status-success' },
  fail: { label: '失败', class: 'status-fail' },
}

function handleFileChange(file) {
  importFile.value = file.raw || file
}

async function handleImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    formData.append('dataType', importType.value)
    await request.post('/data/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    ElMessage.success('导入成功')
    importFile.value = null
    fetchOperations()
  } catch (error) {
    console.error(error)
  } finally {
    importing.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const res = await request.get('/data/export', {
      params: { dataType: exportType.value },
    })
    const url = res.data?.downloadUrl
    if (url) {
      window.open(url, '_blank')
      ElMessage.success('导出成功')
      fetchOperations()
    } else {
      ElMessage.warning('未获取到下载链接')
    }
  } catch (error) {
    console.error(error)
  } finally {
    exporting.value = false
  }
}

async function fetchOperations() {
  loading.value = true
  try {
    const res = await request.get('/data/operations', {
      params: {
        operationType: filterType.value || undefined,
        status: filterStatus.value || undefined,
        page: 1,
        pageSize: 50,
      },
    })
    rawList.value = res.data?.rows || []
  } catch (error) {
    console.error(error)
    rawList.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(str) {
  if (!str) return ''
  const d = new Date(str)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchOperations()
})
</script>

<template>
  <div class="data-page">
    <div class="page-header-bar">
      <div>
        <h1 class="page-title">数据导入导出</h1>
        <p class="page-subtitle">批量导入或导出你的个人数据</p>
      </div>
    </div>

    <div class="action-grid">
      <!-- Import Card -->
      <div class="action-card">
        <div class="card-icon">📥</div>
        <h3 class="card-title">数据导入</h3>
        <p class="card-desc">支持 CSV / Excel 格式文件批量导入</p>

        <div class="form-group">
          <label class="form-label">数据类型</label>
          <select v-model="importType" class="form-select">
            <option v-for="t in dataTypeOptions" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">选择文件</label>
          <el-upload
            drag
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            accept=".csv,.xlsx,.xls"
            @change="handleFileChange"
          >
            <div class="upload-inner">
              <div class="upload-icon">📁</div>
              <div class="upload-text">点击或拖拽文件到此处</div>
              <div class="upload-hint">支持 .csv / .xlsx / .xls</div>
            </div>
          </el-upload>
        </div>

        <button class="btn-primary" :disabled="importing" @click="handleImport">
          {{ importing ? '导入中...' : '开始导入' }}
        </button>
      </div>

      <!-- Export Card -->
      <div class="action-card">
        <div class="card-icon">📤</div>
        <h3 class="card-title">数据导出</h3>
        <p class="card-desc">将数据导出为 CSV / Excel 格式</p>

        <div class="form-group">
          <label class="form-label">数据类型</label>
          <select v-model="exportType" class="form-select">
            <option v-for="t in dataTypeOptions" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>

        <div class="export-preview">
          <div class="preview-item">
            <span class="preview-label">导出格式</span>
            <span class="preview-value">Excel (.xlsx)</span>
          </div>
          <div class="preview-item">
            <span class="preview-label">包含字段</span>
            <span class="preview-value">全部字段</span>
          </div>
        </div>

        <button class="btn-primary" :disabled="exporting" @click="handleExport">
          {{ exporting ? '导出中...' : '导出数据' }}
        </button>
      </div>
    </div>

    <!-- History -->
    <div class="history-section">
      <div class="history-header">
        <h3 class="section-title">操作记录</h3>
        <div class="history-filters">
          <select v-model="filterType" class="filter-select" @change="fetchOperations">
            <option value="">全部类型</option>
            <option value="import">导入</option>
            <option value="export">导出</option>
          </select>
          <select v-model="filterStatus" class="filter-select" @change="fetchOperations">
            <option value="">全部状态</option>
            <option value="success">成功</option>
            <option value="pending">处理中</option>
            <option value="fail">失败</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="loading-state">加载中...</div>
      <div v-else-if="rawList.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无操作记录</div>
      </div>
      <div v-else class="history-table-wrapper">
        <table class="history-table">
          <thead>
            <tr>
              <th>操作类型</th>
              <th>数据类型</th>
              <th>状态</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in rawList" :key="item.id">
              <td>
                <span class="type-tag" :class="item.operationType">{{ item.operationType === 'import' ? '导入' : '导出' }}</span>
              </td>
              <td>{{ item.dataType }}</td>
              <td>
                <span class="status-badge" :class="statusMap[item.status]?.class">
                  {{ statusMap[item.status]?.label || item.status }}
                </span>
              </td>
              <td>{{ formatDate(item.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.data-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 3rem 2rem;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Noto Sans SC', sans-serif;
}

.page-header-bar {
  margin-bottom: 2rem;
}

.page-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 0.25rem;
}

.page-subtitle {
  font-size: 0.9375rem;
  color: oklch(62% 0.02 30);
  margin: 0;
}

/* Action Grid */
.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.25rem;
  margin-bottom: 2rem;
}

.action-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

.card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: oklch(96% 0.02 195);
  color: oklch(68% 0.12 195);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin-bottom: 0.75rem;
}

.card-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.125rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 0.25rem;
}

.card-desc {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  margin: 0 0 1.25rem;
}

/* Form */
.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: oklch(48% 0.025 30);
  margin-bottom: 0.5rem;
}

.form-select {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-family: inherit;
  font-size: 0.9375rem;
  color: oklch(25% 0.02 30);
  background: oklch(98% 0.008 30);
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-select:focus {
  outline: none;
  border-color: oklch(70% 0.14 20);
  box-shadow: 0 0 0 3px oklch(85% 0.08 20 / 0.3);
}

/* Upload */
.upload-inner {
  padding: 1.5rem 1rem;
  text-align: center;
}

.upload-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.upload-text {
  font-size: 0.9375rem;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.25rem;
}

.upload-hint {
  font-size: 0.8125rem;
  color: oklch(62% 0.02 30);
}

/* Export Preview */
.export-preview {
  background: oklch(98% 0.008 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  padding: 0.875rem 1rem;
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
}

.preview-label {
  color: oklch(62% 0.02 30);
}

.preview-value {
  color: oklch(25% 0.02 30);
  font-weight: 500;
}

/* Buttons */
.btn-primary {
  margin-top: auto;
  padding: 0.625rem 1.5rem;
  background: oklch(70% 0.14 20);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.9375rem;
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

/* History Section */
.history-section {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.section-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.125rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0;
}

.history-filters {
  display: flex;
  gap: 0.75rem;
}

.filter-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-family: inherit;
  font-size: 0.875rem;
  color: oklch(25% 0.02 30);
  background: oklch(98% 0.008 30);
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-select:focus {
  outline: none;
  border-color: oklch(70% 0.14 20);
  box-shadow: 0 0 0 3px oklch(85% 0.08 20 / 0.3);
}

.loading-state {
  text-align: center;
  padding: 2rem;
  color: oklch(62% 0.02 30);
  font-size: 0.875rem;
}

.empty-state {
  text-align: center;
  padding: 2.5rem 1rem;
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

.empty-text {
  font-size: 1rem;
  color: oklch(25% 0.02 30);
}

/* History Table */
.history-table-wrapper {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9375rem;
}

.history-table th {
  text-align: left;
  padding: 0.625rem 0.75rem;
  color: oklch(48% 0.025 30);
  font-weight: 500;
  border-bottom: 1px solid oklch(88% 0.015 30);
}

.history-table td {
  padding: 0.75rem;
  border-bottom: 1px solid oklch(92% 0.01 30);
  color: oklch(25% 0.02 30);
}

.type-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 500;
}

.type-tag.import {
  background: oklch(96% 0.02 155);
  color: oklch(48% 0.12 155);
  border: 1px solid oklch(85% 0.06 155);
}

.type-tag.export {
  background: oklch(96% 0.02 250);
  color: oklch(48% 0.12 250);
  border: 1px solid oklch(85% 0.06 250);
}

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge.status-success {
  background: oklch(96% 0.02 155);
  color: oklch(48% 0.12 155);
  border: 1px solid oklch(85% 0.06 155);
}

.status-badge.status-pending {
  background: oklch(96% 0.02 80);
  color: oklch(55% 0.1 80);
  border: 1px solid oklch(88% 0.05 80);
}

.status-badge.status-fail {
  background: oklch(96% 0.02 25);
  color: oklch(55% 0.18 25);
  border: 1px solid oklch(88% 0.08 25);
}

/* Responsive */
@media (max-width: 768px) {
  .data-page {
    padding: 2rem 1.5rem;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .history-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .history-filters {
    width: 100%;
  }

  .filter-select {
    flex: 1;
  }

  .history-table {
    font-size: 0.875rem;
  }

  .history-table th,
  .history-table td {
    padding: 0.5rem;
  }
}

/* Accessibility */
button:focus-visible,
select:focus-visible {
  outline: 2px solid oklch(70% 0.14 20);
  outline-offset: 2px;
}
</style>
