<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const saving = ref(false)

const form = reactive({
  gpaStandard: '4.0',
  gpaScale: {
    standard: '4.0',
    scales: [
      { min: 90, max: 100, point: 4.0 },
      { min: 85, max: 89, point: 3.7 },
      { min: 82, max: 84, point: 3.3 },
      { min: 78, max: 81, point: 3.0 },
      { min: 75, max: 77, point: 2.7 },
      { min: 72, max: 74, point: 2.3 },
      { min: 68, max: 71, point: 2.0 },
      { min: 64, max: 67, point: 1.5 },
      { min: 60, max: 63, point: 1.0 },
      { min: 0, max: 59, point: 0.0 },
    ],
  },
  aiDimensions: {
    ability_assessment: true,
    development_direction: true,
    resume_optimization: false,
    competitiveness_analysis: true,
  },
})

const gpaStandardOptions = [
  { label: '4.0 制', value: '4.0' },
  { label: '5.0 制', value: '5.0' },
  { label: '百分制加权', value: 'weighted' },
]

const aiDimensionLabels = {
  ability_assessment: '能力评估',
  development_direction: '发展方向',
  resume_optimization: '简历优化建议',
  competitiveness_analysis: '竞争力分析',
}

async function fetchSettings() {
  loading.value = true
  try {
    const res = await request.get('/settings')
    const data = res || {}
    if (data.gpaStandard) form.gpaStandard = data.gpaStandard
    if (data.gpaScale) Object.assign(form.gpaScale, data.gpaScale)
    if (data.aiDimensions) Object.assign(form.aiDimensions, data.aiDimensions)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleStandardChange(val) {
  form.gpaScale.standard = val
}

function addScaleRow() {
  form.gpaScale.scales.push({ min: 0, max: 100, point: 0 })
}

function removeScaleRow(index) {
  form.gpaScale.scales.splice(index, 1)
}

function resetGpaScale() {
  const defaults = {
    '4.0': [
      { min: 90, max: 100, point: 4.0 },
      { min: 85, max: 89, point: 3.7 },
      { min: 82, max: 84, point: 3.3 },
      { min: 78, max: 81, point: 3.0 },
      { min: 75, max: 77, point: 2.7 },
      { min: 72, max: 74, point: 2.3 },
      { min: 68, max: 71, point: 2.0 },
      { min: 64, max: 67, point: 1.5 },
      { min: 60, max: 63, point: 1.0 },
      { min: 0, max: 59, point: 0.0 },
    ],
    '5.0': [
      { min: 90, max: 100, point: 5.0 },
      { min: 85, max: 89, point: 4.5 },
      { min: 82, max: 84, point: 4.0 },
      { min: 78, max: 81, point: 3.5 },
      { min: 75, max: 77, point: 3.0 },
      { min: 72, max: 74, point: 2.5 },
      { min: 68, max: 71, point: 2.0 },
      { min: 64, max: 67, point: 1.5 },
      { min: 60, max: 63, point: 1.0 },
      { min: 0, max: 59, point: 0.0 },
    ],
    weighted: [
      { min: 90, max: 100, point: 95 },
      { min: 85, max: 89, point: 87 },
      { min: 82, max: 84, point: 83 },
      { min: 78, max: 81, point: 79 },
      { min: 75, max: 77, point: 76 },
      { min: 72, max: 74, point: 73 },
      { min: 68, max: 71, point: 69 },
      { min: 64, max: 67, point: 65 },
      { min: 60, max: 63, point: 61 },
      { min: 0, max: 59, point: 0 },
    ],
  }
  form.gpaScale.scales = JSON.parse(JSON.stringify(defaults[form.gpaStandard] || defaults['4.0']))
}

async function handleSave() {
  saving.value = true
  try {
    await request.put('/settings', {
      gpaStandard: form.gpaStandard,
      gpaScale: form.gpaScale,
      aiDimensions: form.aiDimensions,
    })
    ElMessage.success('设置已保存')
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchSettings()
})
</script>

<template>
  <div class="settings-page">
    <div class="page-header-bar">
      <div>
        <h1 class="page-title">系统设置</h1>
        <p class="page-subtitle">配置 GPA 计算方式、AI 分析维度与主题</p>
      </div>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else>
      <!-- GPA 计算标准 -->
      <div class="setting-card">
        <h3 class="card-title">GPA 计算标准</h3>
        <div class="form-group">
          <label class="form-label">计算方式</label>
          <div class="radio-group">
            <label
              v-for="opt in gpaStandardOptions"
              :key="opt.value"
              class="radio-item"
              :class="{ active: form.gpaStandard === opt.value }"
            >
              <input
                v-model="form.gpaStandard"
                type="radio"
                :value="opt.value"
                @change="handleStandardChange(opt.value)"
              />
              <span>{{ opt.label }}</span>
            </label>
          </div>
        </div>
      </div>

      <!-- 绩点对照表 -->
      <div class="setting-card">
        <div class="card-header">
          <h3 class="card-title">绩点对照表</h3>
          <div class="card-actions">
            <button class="btn-text" @click="resetGpaScale">恢复默认</button>
            <button class="btn-text" @click="addScaleRow">+ 添加行</button>
          </div>
        </div>
        <div class="scale-table-wrapper">
          <table class="scale-table">
            <thead>
              <tr>
                <th>成绩下限</th>
                <th>成绩上限</th>
                <th>对应绩点</th>
                <th style="width: 60px"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in form.gpaScale.scales" :key="index">
                <td>
                  <el-input-number v-model="row.min" :min="0" :max="100" :controls="false" style="width: 100%" />
                </td>
                <td>
                  <el-input-number v-model="row.max" :min="0" :max="100" :controls="false" style="width: 100%" />
                </td>
                <td>
                  <el-input-number v-model="row.point" :min="0" :max="100" :step="0.1" :controls="false" style="width: 100%" />
                </td>
                <td>
                  <button class="btn-icon danger" @click="removeScaleRow(index)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- AI 分析维度 -->
      <div class="setting-card">
        <h3 class="card-title">AI 分析维度</h3>
        <p class="card-desc">选择你希望在 AI 分析报告中关注的维度</p>
        <div class="dimension-list">
          <label
            v-for="(label, key) in aiDimensionLabels"
            :key="key"
            class="dimension-item"
          >
            <el-switch v-model="form.aiDimensions[key]" />
            <span class="dimension-label">{{ label }}</span>
          </label>
        </div>
      </div>

      <!-- Save Button -->
      <div class="save-bar">
        <button class="btn-primary" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中...' : '保存设置' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.settings-page {
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

.loading-state {
  text-align: center;
  padding: 4rem;
  color: oklch(62% 0.02 30);
  font-size: 0.875rem;
}

/* Setting Card */
.setting-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 1.25rem;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.card-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.125rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0;
}

.card-desc {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  margin: -0.5rem 0 1rem;
}

.card-actions {
  display: flex;
  gap: 0.75rem;
}

/* Form */
.form-group {
  margin-top: 0.5rem;
}

.form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: oklch(48% 0.025 30);
  margin-bottom: 0.75rem;
}

/* Radio Group */
.radio-group {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.radio-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: oklch(98% 0.008 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
  cursor: pointer;
  transition: all 0.2s ease;
}

.radio-item:hover {
  border-color: oklch(70% 0.14 20);
}

.radio-item.active {
  background: oklch(96% 0.02 20);
  border-color: oklch(70% 0.14 20);
  color: oklch(58% 0.16 20);
  font-weight: 500;
}

.radio-item input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

/* Scale Table */
.scale-table-wrapper {
  overflow-x: auto;
}

.scale-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.scale-table th {
  text-align: left;
  padding: 0.625rem 0.75rem;
  color: oklch(48% 0.025 30);
  font-weight: 500;
  border-bottom: 1px solid oklch(88% 0.015 30);
}

.scale-table td {
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid oklch(92% 0.01 30);
}

/* Dimension List */
.dimension-list {
  display: flex;
  flex-direction: column;
  gap: 0.875rem;
}

.dimension-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.dimension-label {
  font-size: 0.9375rem;
  color: oklch(25% 0.02 30);
}

/* Buttons */
.btn-primary {
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

.btn-icon {
  padding: 0.25rem 0.5rem;
  background: none;
  border: none;
  color: oklch(55% 0.18 25);
  font-size: 0.75rem;
  font-family: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}

.btn-icon:hover {
  color: oklch(45% 0.2 25);
  text-decoration: underline;
}

.save-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

/* Responsive */
@media (max-width: 768px) {
  .settings-page {
    padding: 2rem 1.5rem;
  }

  .radio-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
}

/* Accessibility */
button:focus-visible,
input:focus-visible {
  outline: 2px solid oklch(70% 0.14 20);
  outline-offset: 2px;
}
</style>
