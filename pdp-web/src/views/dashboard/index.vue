<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()

const username = computed(() => userStore.userInfo?.username || '同学')

const today = new Date()
const dateStr = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`

const loading = ref(false)

const dashboardData = ref({
  cumulativeGpa: 0,
  gpaTrend: '0',
  totalCredits: 0,
  currentCredits: 0,
  experienceCount: 0,
  experienceBreakdown: {},
  achievementCount: 0,
  achievementBreakdown: {},
  roleCount: 0,
  recentActivities: [],
  gpaHistory: [],
  aiSummary: '',
})

const gpaHistory = computed(() => {
  const history = dashboardData.value.gpaHistory || []
  if (history.length === 0) return []
  const maxGpa = Math.max(...history.map((i) => i.gpa), 0.01)
  return history.map((item, index) => ({
    semester: item.semester,
    gpa: item.gpa,
    height: Math.max(4, Math.round((item.gpa / maxGpa) * 100)),
    isCurrent: index === history.length - 1,
  }))
})

const recentActivities = computed(() => dashboardData.value.recentActivities || [])

const CHART_W = 600
const CHART_H = 180
const PAD = { t: 25, r: 20, b: 25, l: 20 }

const trendPoints = computed(() => {
  if (gpaHistory.value.length === 0) return []
  const gpas = gpaHistory.value.map((i) => i.gpa)
  const maxGpa = Math.max(...gpas, 0.01)
  const minGpa = Math.min(...gpas, 0)
  const range = maxGpa - minGpa || 1
  const availW = CHART_W - PAD.l - PAD.r
  const availH = CHART_H - PAD.t - PAD.b
  return gpaHistory.value.map((item, index) => {
    const x = PAD.l + (gpaHistory.value.length === 1 ? availW / 2 : (index / (gpaHistory.value.length - 1)) * availW)
    const y = PAD.t + availH - ((item.gpa - minGpa) / range) * availH
    return { x, y, isCurrent: item.isCurrent, gpa: item.gpa, semester: item.semester }
  })
})

const trendLinePoints = computed(() => trendPoints.value.map((p) => `${p.x},${p.y}`).join(' '))

function goTo(path) {
  router.push(path)
}

function breakdownText(obj) {
  if (!obj) return ''
  return Object.entries(obj)
    .map(([k, v]) => `${k} ${v}`)
    .join(' · ')
}

async function fetchDashboard() {
  loading.value = true
  try {
    const res = await request.get('/dashboard')
    if (res) {
      dashboardData.value = { ...dashboardData.value, ...res }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDashboard()
})
</script>

<template>
  <div class="dashboard">
    <!-- Welcome -->
    <header class="welcome">
      <div>
        <h1 class="welcome-title">
          欢迎回来，<em>{{ username }}</em>
        </h1>
        <p class="welcome-sub">
          你已经记录了 <strong>{{ gpaHistory.length }}</strong> 个学期的成长轨迹，累计获得
          <strong>{{ dashboardData.totalCredits }}</strong> 学分、<strong>{{ dashboardData.achievementCount }}</strong> 项成就
        </p>
      </div>
      <div class="welcome-date">
        <div>{{ dateStr }}</div>
        <div style="margin-top: 4px">大三 · 第二学期</div>
      </div>
    </header>

    <!-- Metrics Grid -->
    <section class="metrics" aria-label="核心数据概览">
      <!-- GPA Card (large, spans 2 rows) -->
      <article class="metric-card metric-card--gpa">
        <div>
          <div class="metric-header">
            <span class="metric-icon" aria-hidden="true">📊</span>
            <span class="metric-label">累计 GPA</span>
          </div>
          <div class="metric-value tabular">{{ dashboardData.cumulativeGpa.toFixed(2) }}</div>
          <div class="metric-trend">
            {{ dashboardData.gpaTrend && dashboardData.gpaTrend !== '0' ? `↑ ${dashboardData.gpaTrend} 较上学期` : '' }}
          </div>
        </div>
        <div v-if="gpaHistory.length" class="mini-chart" aria-hidden="true">
          <div
            v-for="item in gpaHistory"
            :key="item.semester"
            class="mini-bar"
            :class="{ 'is-current': item.isCurrent }"
            :style="{ height: item.height + '%' }"
          >
            <span class="mini-bar-label">{{ item.semester }}</span>
          </div>
        </div>
      </article>

      <!-- Credits -->
      <article class="metric-card metric-card--credits">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">📚</span>
          <span class="metric-label">总学分</span>
        </div>
        <div class="metric-value tabular">{{ dashboardData.totalCredits }}</div>
        <div class="metric-sub">本学期已修 {{ dashboardData.currentCredits }} 学分</div>
      </article>

      <!-- Experiences -->
      <article class="metric-card metric-card--experience">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">🚀</span>
          <span class="metric-label">经历</span>
        </div>
        <div class="metric-value tabular">{{ dashboardData.experienceCount }}</div>
        <div class="metric-sub">{{ breakdownText(dashboardData.experienceBreakdown) }}</div>
      </article>

      <!-- Achievements -->
      <article class="metric-card metric-card--achievement">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">🏆</span>
          <span class="metric-label">成就</span>
        </div>
        <div class="metric-value tabular">{{ dashboardData.achievementCount }}</div>
        <div class="metric-sub">{{ breakdownText(dashboardData.achievementBreakdown) }}</div>
      </article>

      <!-- Roles -->
      <article class="metric-card metric-card--role">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">🎯</span>
          <span class="metric-label">角色</span>
        </div>
        <div class="metric-value tabular">{{ dashboardData.roleCount }}</div>
        <div class="metric-sub">担任的职务与角色</div>
      </article>
    </section>

    <!-- Content Grid -->
    <div class="content-grid">
      <!-- Main Column -->
      <div class="content-main">
        <!-- GPA Trend Chart -->
        <section class="section-card">
          <div class="section-header">
            <h2 class="section-title">GPA 趋势</h2>
            <a class="section-link" @click="goTo('/courses')">查看详情 →</a>
          </div>
          <div class="chart-container">
            <svg
              v-if="gpaHistory.length"
              class="trend-chart"
              :viewBox="`0 0 ${CHART_W} ${CHART_H}`"
              preserveAspectRatio="none"
              role="img"
              aria-label="各学期 GPA 折线图"
            >
              <line
                v-for="gy in [PAD.t, PAD.t + (CHART_H - PAD.t - PAD.b) / 2, CHART_H - PAD.b]"
                :key="gy"
                :x1="PAD.l"
                :y1="gy"
                :x2="CHART_W - PAD.r"
                :y2="gy"
                stroke="oklch(88% 0.015 30)"
                stroke-width="0.5"
                stroke-dasharray="4"
              />
              <polyline
                :points="trendLinePoints"
                fill="none"
                stroke="oklch(70% 0.14 20)"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle
                v-for="p in trendPoints"
                :key="p.x"
                :cx="p.x"
                :cy="p.y"
                r="4"
                :fill="p.isCurrent ? 'oklch(70% 0.14 20)' : 'white'"
                stroke="oklch(70% 0.14 20)"
                stroke-width="2"
              />
              <text
                v-for="p in trendPoints"
                :key="'v' + p.x"
                :x="p.x"
                :y="p.y - 10"
                text-anchor="middle"
                font-size="12"
                fill="oklch(25% 0.02 30)"
              >{{ p.gpa.toFixed(2) }}</text>
              <text
                v-for="p in trendPoints"
                :key="'l' + p.x"
                :x="p.x"
                :y="CHART_H - 8"
                text-anchor="middle"
                font-size="11"
                fill="oklch(62% 0.02 30)"
              >{{ p.semester }}</text>
            </svg>
            <div v-else class="empty-chart">暂无 GPA 数据</div>
          </div>
        </section>

        <!-- Recent Activity Timeline -->
        <section class="section-card">
          <div class="section-header">
            <h2 class="section-title">最近动态</h2>
            <a class="section-link" @click="goTo('/experiences')">查看全部 →</a>
          </div>
          <div v-if="recentActivities.length" class="timeline">
            <div
              v-for="(item, index) in recentActivities"
              :key="index"
              class="timeline-item"
              :class="`type-${item.type}`"
            >
              <div class="timeline-date">{{ item.date }}</div>
              <div class="timeline-content">{{ item.content }}</div>
              <span class="timeline-tag" :class="`type-${item.type}`">
                {{ item.type === 'experience' ? '实习经历' : item.type === 'achievement' ? '成就荣誉' : '担任角色' }}
              </span>
            </div>
          </div>
          <div v-else class="empty-timeline">暂无动态</div>
        </section>
      </div>

      <!-- Side Column -->
      <aside class="content-side">
        <!-- AI Analysis -->
        <div class="ai-card">
          <div class="ai-header">
            <span class="ai-icon" aria-hidden="true">✨</span>
            <h2 class="ai-title">AI 发展分析</h2>
          </div>
          <p class="ai-summary" v-html="dashboardData.aiSummary || 'AI 分析报告暂未生成，快去录入你的课程、经历和成就数据吧！'" />
          <button class="ai-btn" @click="goTo('/ai-analysis')">
            查看完整报告 →
          </button>
        </div>

        <!-- Quick Actions -->
        <div class="quick-actions">
          <h3 class="quick-actions-title">快速操作</h3>
          <ul class="action-list">
            <li class="action-item" @click="goTo('/courses')">
              <span class="action-icon courses" aria-hidden="true">📖</span>
              <div>
                <div class="action-text">添加课程</div>
                <div class="action-desc">记录本学期新修课程</div>
              </div>
            </li>
            <li class="action-item" @click="goTo('/experiences')">
              <span class="action-icon experiences" aria-hidden="true">🚀</span>
              <div>
                <div class="action-text">记录经历</div>
                <div class="action-desc">竞赛、项目或实习经历</div>
              </div>
            </li>
            <li class="action-item" @click="goTo('/achievements')">
              <span class="action-icon achievements" aria-hidden="true">🏆</span>
              <div>
                <div class="action-text">录入成就</div>
                <div class="action-desc">奖项、荣誉或证书</div>
              </div>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  max-width: 1280px;
  margin: 0 auto;
  padding: 3rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

/* Welcome */
.welcome {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
}

.welcome-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2.5rem;
  font-weight: 400;
  margin-bottom: 0.5rem;
  line-height: 1.25;
  color: oklch(25% 0.02 30);
}

.welcome-title em {
  font-style: normal;
  color: oklch(70% 0.14 20);
}

.welcome-sub {
  font-size: 1rem;
  color: oklch(48% 0.025 30);
  max-width: 45ch;
  line-height: 1.6;
}

.welcome-sub strong {
  color: oklch(58% 0.16 20);
  font-weight: 600;
}

.welcome-date {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  text-align: right;
  flex-shrink: 0;
}

/* Metrics Grid */
.metrics {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  grid-template-rows: auto auto;
  gap: 1rem;
}

.metric-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}

.metric-card:hover {
  background: oklch(94% 0.015 30);
  box-shadow: 0 4px 12px oklch(70% 0.01 30 / 0.08);
  transform: translateY(-2px);
}

.metric-card--gpa {
  grid-row: 1 / 3;
  justify-content: space-between;
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.metric-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
}

.metric-card--gpa .metric-icon {
  background: oklch(96% 0.02 20);
}

.metric-card--credits .metric-icon {
  background: oklch(96% 0.02 195);
}

.metric-card--experience .metric-icon {
  background: oklch(96% 0.02 155);
}

.metric-card--achievement .metric-icon {
  background: oklch(96% 0.025 75);
}

.metric-card--role .metric-icon {
  background: oklch(96% 0.02 280);
}

.metric-label {
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
}

.metric-value {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2.5rem;
  color: oklch(25% 0.02 30);
  line-height: 1;
  margin-bottom: 0.25rem;
}

.metric-card--gpa .metric-value {
  font-size: 3.5rem;
  color: oklch(58% 0.16 20);
}

.metric-trend {
  font-size: 0.875rem;
  color: oklch(68% 0.12 155);
  font-weight: 500;
}

.metric-sub {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
}

/* Mini chart inside GPA card */
.mini-chart {
  margin-top: 1.5rem;
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
  height: 80px;
}

.mini-bar {
  flex: 1;
  border-radius: 6px 6px 0 0;
  background: oklch(85% 0.08 20);
  position: relative;
  transition: height 0.3s ease;
}

.mini-bar.is-current {
  background: oklch(70% 0.14 20);
}

.mini-bar-label {
  position: absolute;
  bottom: calc(100% + 4px);
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
  white-space: nowrap;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  align-items: start;
}

.section-card {
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  padding: 1.5rem;
}

.section-card + .section-card {
  margin-top: 1.5rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.section-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
}

.section-link {
  font-size: 0.875rem;
  color: oklch(70% 0.14 20);
  transition: color 0.2s ease;
  cursor: pointer;
}

.section-link:hover {
  color: oklch(58% 0.16 20);
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* GPA Chart */
.chart-container {
  padding-top: 0.75rem;
}

.trend-chart {
  width: 100%;
  height: 180px;
  overflow: visible;
}

.trend-chart text {
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Noto Sans SC', sans-serif;
}

.empty-chart,
.empty-timeline {
  text-align: center;
  padding: 2rem;
  color: oklch(62% 0.02 30);
  font-size: 0.875rem;
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
  padding-bottom: 1rem;
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
  border: 2px solid oklch(70% 0.14 20);
}

.timeline-item.type-experience::before {
  border-color: oklch(68% 0.12 155);
}

.timeline-item.type-achievement::before {
  border-color: oklch(70% 0.14 75);
}

.timeline-item.type-role::before {
  border-color: oklch(65% 0.13 280);
}

.timeline-date {
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
  margin-bottom: 0.25rem;
}

.timeline-content {
  font-size: 0.875rem;
  color: oklch(25% 0.02 30);
  line-height: 1.5;
}

.timeline-tag {
  display: inline-block;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 100px;
  margin-top: 0.25rem;
}

.timeline-tag.type-experience {
  background: oklch(96% 0.02 155);
  color: oklch(68% 0.12 155);
}

.timeline-tag.type-achievement {
  background: oklch(96% 0.025 75);
  color: oklch(70% 0.14 75);
}

.timeline-tag.type-role {
  background: oklch(96% 0.02 280);
  color: oklch(65% 0.13 280);
}

/* AI Card */
.ai-card {
  background: oklch(96% 0.025 300);
  border: 1px solid oklch(88% 0.02 300);
  border-radius: 16px;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
}

.ai-card::before {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: oklch(90% 0.04 300 / 0.3);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  position: relative;
}

.ai-icon {
  font-size: 1.5rem;
}

.ai-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(62% 0.15 300);
}

.ai-summary {
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
  line-height: 1.7;
  margin-bottom: 1rem;
  position: relative;
}

.ai-summary :deep(h2),
.ai-summary :deep(h3) {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1rem;
  font-weight: 500;
  color: oklch(25% 0.02 30);
  margin: 0.75rem 0 0.375rem;
}

.ai-summary :deep(h2:first-child),
.ai-summary :deep(h3:first-child) {
  margin-top: 0;
}

.ai-summary :deep(p) {
  margin: 0.375rem 0;
}

.ai-summary :deep(strong) {
  color: oklch(58% 0.16 20);
  font-weight: 600;
}

.ai-summary :deep(ul) {
  margin: 0.375rem 0;
  padding-left: 1.25rem;
}

.ai-summary :deep(li) {
  margin: 0.25rem 0;
}

.ai-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: oklch(62% 0.15 300);
  color: white;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s ease;
  position: relative;
  border: none;
  cursor: pointer;
  font-family: inherit;
}

.ai-btn:hover {
  background: oklch(55% 0.17 300);
  box-shadow: 0 4px 12px oklch(70% 0.01 30 / 0.08);
  transform: translateY(-1px);
}

/* Quick Actions */
.quick-actions {
  margin-top: 1.5rem;
}

.quick-actions-title {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
}

.action-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 10px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.action-item:hover {
  background: oklch(94% 0.015 30);
}

.action-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
}

.action-icon.courses {
  background: oklch(96% 0.02 195);
}

.action-icon.experiences {
  background: oklch(96% 0.02 155);
}

.action-icon.achievements {
  background: oklch(96% 0.025 75);
}

.action-text {
  font-size: 0.875rem;
  color: oklch(25% 0.02 30);
  font-weight: 500;
}

.action-desc {
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
}

/* Responsive */
@media (max-width: 960px) {
  .dashboard {
    padding: 2rem 1.5rem;
    gap: 2rem;
  }

  .metrics {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto auto auto;
  }

  .metric-card--gpa {
    grid-column: 1 / 3;
    grid-row: 1;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .welcome {
    flex-direction: column;
    align-items: flex-start;
  }

  .welcome-date {
    text-align: left;
  }
}

@media (max-width: 560px) {
  .welcome-title {
    font-size: 2rem;
  }

  .metrics {
    grid-template-columns: 1fr;
  }

  .metric-card--gpa {
    grid-column: 1;
  }

  .dashboard {
    padding: 1.5rem 1rem;
  }
}

.tabular {
  font-variant-numeric: tabular-nums;
}
</style>
