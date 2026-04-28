<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import * as echarts from 'echarts'

const analyzing = ref(false)
const resultVisible = ref(false)
const loadingHistory = ref(false)
const historyList = ref([])
const chartRef = ref(null)
let chartInstance = null

const dimensionOptions = [
  { key: 'ability_assessment', label: '能力评估' },
  { key: 'development_direction', label: '发展方向' },
  { key: 'resume_optimization', label: '简历优化建议' },
  { key: 'competitiveness_analysis', label: '竞争力分析' },
]

const selectedDimensions = ref(['ability_assessment', 'development_direction', 'competitiveness_analysis'])

const report = reactive({
  id: null,
  analysisType: '',
  dimensions: {},
  reportContent: '',
  strengths: [],
  weaknesses: [],
  suggestions: [],
  competitiveness: {},
  createdAt: '',
})

function initChart(dimensions) {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  chartInstance = echarts.init(chartRef.value)

  const labels = Object.keys(dimensions)
  const values = Object.values(dimensions)
  const indicator = labels.map((name) => ({ name, max: 100 }))

  const option = {
    color: ['oklch(70% 0.14 20)'],
    tooltip: {
      trigger: 'item',
    },
    radar: {
      indicator,
      radius: '65%',
      axisName: {
        color: 'oklch(48% 0.025 30)',
        fontSize: 12,
      },
      splitArea: {
        areaStyle: {
          color: ['oklch(98% 0.005 30)', 'oklch(96% 0.008 30)', 'oklch(94% 0.01 30)'],
        },
      },
      axisLine: {
        lineStyle: { color: 'oklch(88% 0.015 30)' },
      },
      splitLine: {
        lineStyle: { color: 'oklch(88% 0.015 30)' },
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: values,
            name: '能力维度',
            areaStyle: {
              color: 'oklch(70% 0.14 20 / 0.2)',
            },
            lineStyle: {
              color: 'oklch(70% 0.14 20)',
              width: 2,
            },
            itemStyle: {
              color: 'oklch(70% 0.14 20)',
            },
          },
        ],
      },
    ],
  }

  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

async function handleAnalyze() {
  if (selectedDimensions.value.length === 0) {
    ElMessage.warning('请至少选择一个分析维度')
    return
  }

  analyzing.value = true
  resultVisible.value = false
  try {
    const res = await request.post('/ai-analysis', {
      dimensions: selectedDimensions.value,
    }, {
      timeout: 120000,
    })
    const data = res || {}
    Object.assign(report, data)
    resultVisible.value = true
    await nextTick()
    if (data.dimensions && Object.keys(data.dimensions).length > 0) {
      initChart(data.dimensions)
    }
    fetchHistory()
  } catch (error) {
    console.error(error)
  } finally {
    analyzing.value = false
  }
}

async function fetchHistory() {
  loadingHistory.value = true
  try {
    const res = await request.get('/ai-analysis', {
      params: { page: 1, pageSize: 10 },
    })
    historyList.value = res.rows || []
  } catch (error) {
    console.error(error)
    historyList.value = []
  } finally {
    loadingHistory.value = false
  }
}

async function viewHistory(item) {
  try {
    const res = await request.get(`/ai-analysis/${item.id}`)
    const data = res || {}
    Object.assign(report, data)
    resultVisible.value = true
    await nextTick()
    if (data.dimensions && Object.keys(data.dimensions).length > 0) {
      initChart(data.dimensions)
    }
  } catch (error) {
    console.error(error)
  }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm('确定删除这条分析记录吗？', '提示', { type: 'warning' })
    await request.delete(`/ai-analysis/${item.id}`)
    ElMessage.success('删除成功')
    fetchHistory()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

function formatDate(str) {
  if (!str) return ''
  const d = new Date(str)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchHistory()
  window.addEventListener('resize', handleResize)
})
</script>

<template>
  <div class="ai-page">
    <div class="page-header-bar">
      <div>
        <h1 class="page-title">AI 智能分析</h1>
        <p class="page-subtitle">基于你的课程、经历、成就与角色数据，生成个人发展分析报告</p>
      </div>
    </div>

    <!-- Dimension Selection -->
    <div class="analysis-panel">
      <h3 class="panel-title">选择分析维度</h3>
      <div class="dimension-options">
        <label
          v-for="opt in dimensionOptions"
          :key="opt.key"
          class="dimension-checkbox"
          :class="{ active: selectedDimensions.includes(opt.key) }"
        >
          <input v-model="selectedDimensions" type="checkbox" :value="opt.key" />
          <span>{{ opt.label }}</span>
        </label>
      </div>
      <button class="btn-primary" :disabled="analyzing" @click="handleAnalyze">
        {{ analyzing ? '分析中...' : '生成分析报告' }}
      </button>
    </div>

    <!-- Result Panel -->
    <div v-if="resultVisible" class="result-panel">
      <div class="result-header">
        <h3 class="result-title">{{ report.analysisType || '个人发展分析报告' }}</h3>
        <span v-if="report.createdAt" class="result-date">{{ formatDate(report.createdAt) }}</span>
      </div>

      <!-- Radar Chart -->
      <div v-if="report.dimensions && Object.keys(report.dimensions).length > 0" class="chart-wrapper">
        <div ref="chartRef" class="radar-chart"></div>
      </div>

      <!-- Report Content -->
      <div v-if="report.reportContent" class="report-content" v-html="report.reportContent"></div>

      <!-- Cards Grid -->
      <div class="insight-grid">
        <div v-if="report.strengths?.length" class="insight-card strengths">
          <h4 class="insight-title">核心优势</h4>
          <ul class="insight-list">
            <li v-for="(item, idx) in report.strengths" :key="idx">
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </li>
          </ul>
        </div>

        <div v-if="report.weaknesses?.length" class="insight-card weaknesses">
          <h4 class="insight-title">待提升领域</h4>
          <ul class="insight-list">
            <li v-for="(item, idx) in report.weaknesses" :key="idx">
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </li>
          </ul>
        </div>

        <div v-if="report.suggestions?.length" class="insight-card suggestions">
          <h4 class="insight-title">发展建议</h4>
          <ul class="insight-list">
            <li v-for="(item, idx) in report.suggestions" :key="idx">
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </li>
          </ul>
        </div>
      </div>

      <!-- Competitiveness -->
      <div v-if="report.competitiveness && Object.keys(report.competitiveness).length > 0" class="competitiveness-section">
        <h4 class="section-title">竞争力评估</h4>
        <div class="competitiveness-list">
          <div
            v-for="(score, name) in report.competitiveness"
            :key="name"
            class="competitiveness-item"
          >
            <div class="competitiveness-header">
              <span>{{ name }}</span>
              <span class="competitiveness-score">{{ score }}</span>
            </div>
            <div class="competitiveness-bar">
              <div class="competitiveness-fill" :style="{ width: `${score}%` }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- History -->
    <div class="history-panel">
      <h3 class="panel-title">分析历史</h3>
      <div v-if="loadingHistory" class="loading-state">加载中...</div>
      <div v-else-if="historyList.length === 0" class="empty-state">
        <div class="empty-text">暂无分析记录</div>
        <div class="empty-hint">选择维度并点击「生成分析报告」开始第一次分析</div>
      </div>
      <div v-else class="history-list">
        <div v-for="item in historyList" :key="item.id" class="history-item">
          <div class="history-info">
            <div class="history-type">{{ item.analysisType || '个人发展分析' }}</div>
            <div class="history-date">{{ formatDate(item.createdAt) }}</div>
          </div>
          <div class="history-actions">
            <button class="btn-text" @click="viewHistory(item)">查看</button>
            <button class="btn-text danger" @click="handleDelete(item)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ai-page {
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

/* Analysis Panel */
.analysis-panel {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.panel-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.125rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 1rem;
}

.dimension-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-bottom: 1.25rem;
}

.dimension-checkbox {
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

.dimension-checkbox:hover {
  border-color: oklch(70% 0.14 20);
}

.dimension-checkbox.active {
  background: oklch(96% 0.02 20);
  border-color: oklch(70% 0.14 20);
  color: oklch(58% 0.16 20);
  font-weight: 500;
}

.dimension-checkbox input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

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

/* Result Panel */
.result-panel {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}

.result-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0;
}

.result-date {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
}

.chart-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.radar-chart {
  width: 100%;
  max-width: 480px;
  height: 360px;
}

.report-content {
  font-size: 0.9375rem;
  line-height: 1.7;
  color: oklch(25% 0.02 30);
  margin-bottom: 1.5rem;
}

.report-content :deep(h1),
.report-content :deep(h2),
.report-content :deep(h3) {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  color: oklch(25% 0.02 30);
  margin: 1rem 0 0.5rem;
}

.report-content :deep(p) {
  margin: 0.5rem 0;
}

/* Insight Grid */
.insight-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.insight-card {
  background: oklch(98% 0.008 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 12px;
  padding: 1rem;
}

.insight-card.strengths {
  border-left: 3px solid oklch(70% 0.12 155);
}

.insight-card.weaknesses {
  border-left: 3px solid oklch(65% 0.14 45);
}

.insight-card.suggestions {
  border-left: 3px solid oklch(60% 0.14 250);
}

.insight-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 0.75rem;
}

.insight-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.insight-list li {
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
}

.insight-list li strong {
  display: block;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.125rem;
}

.insight-list li p {
  margin: 0;
  line-height: 1.5;
}

/* Competitiveness */
.competitiveness-section {
  background: oklch(98% 0.008 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 12px;
  padding: 1rem;
}

.section-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin: 0 0 0.75rem;
}

.competitiveness-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.competitiveness-item {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.competitiveness-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
}

.competitiveness-score {
  font-weight: 600;
  color: oklch(58% 0.16 20);
}

.competitiveness-bar {
  height: 8px;
  background: oklch(92% 0.01 30);
  border-radius: 100px;
  overflow: hidden;
}

.competitiveness-fill {
  height: 100%;
  background: oklch(70% 0.14 20);
  border-radius: 100px;
  transition: width 0.6s ease;
}

/* History Panel */
.history-panel {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
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

.empty-text {
  font-size: 1rem;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.25rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.875rem 1rem;
  background: oklch(98% 0.008 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  transition: all 0.2s ease;
}

.history-item:hover {
  background: oklch(96% 0.01 30);
}

.history-type {
  font-size: 0.9375rem;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.125rem;
}

.history-date {
  font-size: 0.8125rem;
  color: oklch(62% 0.02 30);
}

.history-actions {
  display: flex;
  gap: 0.75rem;
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

/* Responsive */
@media (max-width: 768px) {
  .ai-page {
    padding: 2rem 1.5rem;
  }

  .radar-chart {
    height: 280px;
  }

  .insight-grid {
    grid-template-columns: 1fr;
  }

  .result-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}

/* Accessibility */
button:focus-visible,
input:focus-visible {
  outline: 2px solid oklch(70% 0.14 20);
  outline-offset: 2px;
}
</style>
