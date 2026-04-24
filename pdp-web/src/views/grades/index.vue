<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const courses = ref([
  { id: 1, courseName: '高等数学 I', courseType: '必修', credits: 4, semester: '2023-2024-1', score: 92, gradePoint: 4.0 },
  { id: 2, courseName: '大学英语 I', courseType: '必修', credits: 3, semester: '2023-2024-1', score: 88, gradePoint: 3.7 },
  { id: 3, courseName: '计算机导论', courseType: '必修', credits: 3, semester: '2023-2024-1', score: 95, gradePoint: 4.0 },
  { id: 4, courseName: '线性代数', courseType: '必修', credits: 3, semester: '2023-2024-2', score: 85, gradePoint: 3.3 },
  { id: 5, courseName: '大学物理', courseType: '必修', credits: 4, semester: '2023-2024-2', score: 78, gradePoint: 3.0 },
  { id: 6, courseName: '数据结构', courseType: '必修', credits: 4, semester: '2024-2025-1', score: 90, gradePoint: 4.0 },
  { id: 7, courseName: '概率论与数理统计', courseType: '必修', credits: 3, semester: '2024-2025-1', score: 87, gradePoint: 3.7 },
  { id: 8, courseName: 'Web 前端开发', courseType: '选修', credits: 2, semester: '2024-2025-2', score: 96, gradePoint: 4.0 },
  { id: 9, courseName: '软件工程', courseType: '必修', credits: 3, semester: '2024-2025-2', score: 89, gradePoint: 3.7 },
  { id: 10, courseName: '人工智能导论', courseType: '选修', credits: 2, semester: '2024-2025-2', score: 91, gradePoint: 4.0 },
])

const totalCredits = computed(() => courses.value.reduce((sum, c) => sum + c.credits, 0))
const totalCourses = computed(() => courses.value.length)

const weightedGpa = computed(() => {
  const total = courses.value.reduce((sum, c) => sum + c.gradePoint * c.credits, 0)
  return totalCredits.value > 0 ? (total / totalCredits.value).toFixed(2) : '0.00'
})

const weightedAvg = computed(() => {
  const total = courses.value.reduce((sum, c) => sum + c.score * c.credits, 0)
  return totalCredits.value > 0 ? (total / totalCredits.value).toFixed(1) : '0.0'
})

const semesterStats = computed(() => {
  const map = {}
  for (const c of courses.value) {
    if (!map[c.semester]) map[c.semester] = { courses: [], totalCredits: 0, weightedGp: 0, weightedScore: 0 }
    map[c.semester].courses.push(c)
    map[c.semester].totalCredits += c.credits
    map[c.semester].weightedGp += c.gradePoint * c.credits
    map[c.semester].weightedScore += c.score * c.credits
  }
  const list = Object.keys(map).sort().map((sem) => {
    const s = map[sem]
    return {
      semester: sem,
      gpa: s.totalCredits > 0 ? (s.weightedGp / s.totalCredits).toFixed(2) : '0.00',
      avg: s.totalCredits > 0 ? (s.weightedScore / s.totalCredits).toFixed(1) : '0.0',
      credits: s.totalCredits,
      count: s.courses.length,
    }
  })
  return list
})

const currentSemesterGpa = computed(() => {
  const list = semesterStats.value
  return list.length > 0 ? list[list.length - 1].gpa : '0.00'
})

const gpaTrend = computed(() => {
  return semesterStats.value.map((s) => ({
    semester: s.semester,
    gpa: parseFloat(s.gpa),
    height: Math.round((parseFloat(s.gpa) / 4.0) * 100),
    isCurrent: s.semester === semesterStats.value[semesterStats.value.length - 1]?.semester,
  }))
})

function scoreClass(score) {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 70) return 'pass'
  if (score >= 60) return 'warning'
  return 'danger'
}

function scoreLabel(score) {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  if (score >= 60) return '及格'
  return '不及格'
}
</script>

<template>
  <div class="grades-page">
    <!-- Header -->
    <header class="page-header">
      <div>
        <h1 class="page-title">成绩与 GPA</h1>
        <p class="page-subtitle">
          已修 <strong>{{ totalCredits }}</strong> 学分，
          <strong>{{ totalCourses }}</strong> 门课程，累计 GPA <strong>{{ weightedGpa }}</strong>
        </p>
      </div>
    </header>

    <!-- Metrics -->
    <section class="metrics" aria-label="GPA 概览">
      <article class="metric-card metric-card--gpa">
        <div>
          <div class="metric-header">
            <span class="metric-icon" aria-hidden="true">📊</span>
            <span class="metric-label">累计 GPA</span>
          </div>
          <div class="metric-value tabular">{{ weightedGpa }}</div>
          <div class="metric-sub">满分 4.0</div>
        </div>
        <div class="mini-chart" aria-hidden="true">
          <div
            v-for="item in gpaTrend"
            :key="item.semester"
            class="mini-bar"
            :class="{ 'is-current': item.isCurrent }"
            :style="{ height: item.height + '%' }"
          >
            <span class="mini-bar-label">{{ item.semester.slice(-1) === '1' ? '上' : '下' }}</span>
          </div>
        </div>
      </article>

      <article class="metric-card metric-card--credits">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">📚</span>
          <span class="metric-label">已修学分</span>
        </div>
        <div class="metric-value tabular">{{ totalCredits }}</div>
        <div class="metric-sub">共 {{ totalCourses }} 门课程</div>
      </article>

      <article class="metric-card metric-card--avg">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">📝</span>
          <span class="metric-label">加权平均分</span>
        </div>
        <div class="metric-value tabular">{{ weightedAvg }}</div>
        <div class="metric-sub">百分制</div>
      </article>

      <article class="metric-card metric-card--current">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">🎯</span>
          <span class="metric-label">本学期 GPA</span>
        </div>
        <div class="metric-value tabular">{{ currentSemesterGpa }}</div>
        <div class="metric-sub">{{ semesterStats[semesterStats.length - 1]?.semester || '' }}</div>
      </article>

      <article class="metric-card metric-card--rank">
        <div class="metric-header">
          <span class="metric-icon" aria-hidden="true">🏅</span>
          <span class="metric-label">成绩评级</span>
        </div>
        <div class="metric-value tabular">{{ scoreLabel(parseFloat(weightedAvg)) }}</div>
        <div class="metric-sub">基于加权平均分</div>
      </article>
    </section>

    <!-- Content -->
    <div class="content-grid">
      <!-- Main -->
      <div class="content-main">
        <!-- GPA Trend -->
        <section class="section-card">
          <div class="section-header">
            <h2 class="section-title">GPA 趋势</h2>
            <span class="section-desc">各学期加权 GPA 变化</span>
          </div>
          <div class="chart-container">
            <div class="chart-bars" role="img" aria-label="各学期 GPA 柱状图">
              <div v-for="item in gpaTrend" :key="item.semester" class="chart-bar-group">
                <div
                  class="chart-bar"
                  :class="{ 'is-current': item.isCurrent }"
                  :style="{ height: item.height + '%' }"
                >
                  <span class="chart-bar-value">{{ item.gpa }}</span>
                </div>
                <span class="chart-bar-label" :class="{ 'is-current': item.isCurrent }">
                  {{ item.semester }}
                </span>
              </div>
            </div>
          </div>
        </section>

        <!-- Grade List -->
        <section class="section-card">
          <div class="section-header">
            <h2 class="section-title">成绩明细</h2>
            <span class="section-desc">全部课程成绩记录</span>
          </div>
          <div class="grade-list">
            <div v-for="c in courses" :key="c.id" class="grade-item">
              <div class="grade-main">
                <span class="grade-type-tag" :class="c.courseType">{{ c.courseType }}</span>
                <span class="grade-name">{{ c.courseName }}</span>
                <span class="grade-semester">{{ c.semester }}</span>
              </div>
              <div class="grade-stats">
                <div class="grade-stat">
                  <span class="grade-stat-label">学分</span>
                  <span class="grade-stat-value">{{ c.credits }}</span>
                </div>
                <div class="grade-stat">
                  <span class="grade-stat-label">成绩</span>
                  <span class="grade-stat-value score" :class="scoreClass(c.score)">{{ c.score }}</span>
                </div>
                <div class="grade-stat">
                  <span class="grade-stat-label">绩点</span>
                  <span class="grade-stat-value gp">{{ c.gradePoint }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- Side -->
      <aside class="content-side">
        <div class="section-card">
          <h3 class="side-title">学期统计</h3>
          <div class="semester-list">
            <div v-for="s in semesterStats" :key="s.semester" class="semester-item">
              <div class="semester-name">{{ s.semester }}</div>
              <div class="semester-meta">
                <span>GPA {{ s.gpa }}</span>
                <span>{{ s.count }} 门 / {{ s.credits }} 学分</span>
              </div>
              <div class="semester-bar-bg">
                <div class="semester-bar-fill" :style="{ width: (parseFloat(s.gpa) / 4.0 * 100) + '%' }" />
              </div>
            </div>
          </div>
        </div>

        <div class="quick-actions">
          <h3 class="quick-actions-title">快速操作</h3>
          <ul class="action-list">
            <li class="action-item" @click="router.push('/courses')">
              <span class="action-icon" aria-hidden="true">📖</span>
              <div>
                <div class="action-text">管理课程</div>
                <div class="action-desc">添加或编辑课程信息</div>
              </div>
            </li>
            <li class="action-item" @click="router.push('/settings')">
              <span class="action-icon" aria-hidden="true">⚙️</span>
              <div>
                <div class="action-text">GPA 标准</div>
                <div class="action-desc">设置绩点计算规则</div>
              </div>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped lang="scss">
.grades-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 3rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

/* Header */
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
}

.page-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 2.5rem;
  font-weight: 400;
  margin-bottom: 0.5rem;
  line-height: 1.25;
  color: oklch(25% 0.02 30);
}

.page-subtitle {
  font-size: 1rem;
  color: oklch(48% 0.025 30);
  max-width: 55ch;
  line-height: 1.6;
}

.page-subtitle strong {
  color: oklch(58% 0.16 20);
  font-weight: 600;
}

/* Metrics */
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

.metric-card--avg .metric-icon {
  background: oklch(96% 0.02 85);
}

.metric-card--current .metric-icon {
  background: oklch(96% 0.02 155);
}

.metric-card--rank .metric-icon {
  background: oklch(96% 0.025 75);
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

.metric-sub {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
}

/* Mini chart */
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

.section-desc {
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
}

/* GPA Chart */
.chart-container {
  padding-top: 0.75rem;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  height: 160px;
  padding-bottom: 1rem;
  border-bottom: 1px solid oklch(88% 0.015 30);
}

.chart-bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.chart-bar {
  width: 100%;
  max-width: 64px;
  border-radius: 6px 6px 0 0;
  background: oklch(68% 0.12 195);
  position: relative;
  transition: all 0.3s ease;
}

.chart-bar.is-current {
  background: oklch(70% 0.14 20);
}

.chart-bar-value {
  position: absolute;
  bottom: calc(100% + 4px);
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.875rem;
  color: oklch(25% 0.02 30);
  font-weight: 600;
}

.chart-bar-label {
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
}

.chart-bar-label.is-current {
  color: oklch(25% 0.02 30);
  font-weight: 500;
}

/* Grade List */
.grade-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.grade-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  background: oklch(98% 0.008 30);
  border: 1px solid oklch(92% 0.01 30);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.grade-item:hover {
  background: oklch(96% 0.012 30);
  border-color: oklch(88% 0.015 30);
  transform: translateX(4px);
}

.grade-main {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
  min-width: 0;
}

.grade-type-tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-weight: 500;
  flex-shrink: 0;
}

.grade-type-tag.必修 {
  background: oklch(96% 0.02 20);
  color: oklch(58% 0.16 20);
}

.grade-type-tag.选修 {
  background: oklch(96% 0.02 195);
  color: oklch(48% 0.12 195);
}

.grade-name {
  font-size: 0.9375rem;
  font-weight: 500;
  color: oklch(25% 0.02 30);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.grade-semester {
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
  flex-shrink: 0;
}

.grade-stats {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-shrink: 0;
}

.grade-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.125rem;
  min-width: 48px;
}

.grade-stat-label {
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
}

.grade-stat-value {
  font-size: 1rem;
  font-weight: 600;
  color: oklch(25% 0.02 30);
}

.grade-stat-value.score.excellent {
  color: oklch(55% 0.12 155);
}

.grade-stat-value.score.good {
  color: oklch(58% 0.12 85);
}

.grade-stat-value.score.pass {
  color: oklch(60% 0.1 55);
}

.grade-stat-value.score.warning {
  color: oklch(58% 0.14 30);
}

.grade-stat-value.score.danger {
  color: oklch(50% 0.16 25);
}

.grade-stat-value.gp {
  color: oklch(48% 0.12 195);
}

/* Side */
.side-title {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.125rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
  margin-bottom: 1rem;
}

.semester-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.semester-item {
  padding-bottom: 1rem;
  border-bottom: 1px solid oklch(92% 0.01 30);
}

.semester-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.semester-name {
  font-size: 0.9375rem;
  font-weight: 500;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.25rem;
}

.semester-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: oklch(62% 0.02 30);
  margin-bottom: 0.5rem;
}

.semester-bar-bg {
  height: 6px;
  background: oklch(92% 0.01 30);
  border-radius: 3px;
  overflow: hidden;
}

.semester-bar-fill {
  height: 100%;
  background: oklch(70% 0.14 20);
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* Quick Actions */
.quick-actions {
  margin-top: 1.5rem;
}

.quick-actions-title {
  font-size: 1rem;
  font-weight: 500;
  color: oklch(25% 0.02 30);
  margin-bottom: 0.75rem;
}

.action-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  background: oklch(94% 0.015 30);
  box-shadow: 0 4px 12px oklch(70% 0.01 30 / 0.08);
  transform: translateY(-2px);
}

.action-icon {
  font-size: 1.25rem;
  flex-shrink: 0;
}

.action-text {
  font-size: 0.9375rem;
  font-weight: 500;
  color: oklch(25% 0.02 30);
}

.action-desc {
  font-size: 0.75rem;
  color: oklch(62% 0.02 30);
  margin-top: 0.125rem;
}

/* Responsive */
@media (max-width: 960px) {
  .grades-page {
    padding: 2rem 1.5rem;
    gap: 2rem;
  }

  .metrics {
    grid-template-columns: 1fr 1fr;
  }

  .metric-card--gpa {
    grid-column: 1 / 3;
    grid-row: auto;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .content-side {
    order: -1;
  }

  .grade-stats {
    gap: 1rem;
  }
}

@media (max-width: 560px) {
  .page-title {
    font-size: 1.75rem;
  }

  .metrics {
    grid-template-columns: 1fr;
  }

  .metric-card--gpa {
    grid-column: auto;
  }

  .grade-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .grade-stats {
    width: 100%;
    justify-content: space-between;
  }

  .chart-bars {
    gap: 0.5rem;
  }
}
</style>
