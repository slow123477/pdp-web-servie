<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 筛选条件
const filterSemester = ref('')
const filterType = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 课程列表
const courseList = ref([])
const loading = ref(false)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('添加课程')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  courseName: '',
  courseType: '必修',
  credits: null,
  semester: '',
  academicYear: '',
  score: null,
  gradePoint: null,
})

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  credits: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
}

const semesterOptions = [
  '2024-2025-1',
  '2024-2025-2',
  '2023-2024-1',
  '2023-2024-2',
  '2022-2023-1',
  '2022-2023-2',
]

const typeOptions = ['必修', '选修', '通识']

// 课程列表和分页由后端处理

function fetchList() {
  loading.value = true
  request
    .get('/courses', {
      params: {
        semester: filterSemester.value || undefined,
        courseType: filterType.value || undefined,
        page: currentPage.value,
        pageSize: pageSize.value,
      },
    })
    .then((res) => {
      courseList.value = res.rows || []
      total.value = res.total || 0
    })
    .catch(() => {
      courseList.value = []
      total.value = 0
    })
    .finally(() => {
      loading.value = false
    })
}

function handleFilter() {
  currentPage.value = 1
  fetchList()
}

function resetFilter() {
  filterSemester.value = ''
  filterType.value = ''
  currentPage.value = 1
  fetchList()
}

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '添加课程'
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑课程'
  Object.assign(form, row)
  dialogVisible.value = true
}

function resetForm() {
  form.id = null
  form.courseName = ''
  form.courseType = '必修'
  form.credits = null
  form.semester = ''
  form.academicYear = ''
  form.score = null
  form.gradePoint = null
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    if (isEdit.value) {
      handleUpdate()
    } else {
      handleAdd()
    }
  })
}

async function handleAdd() {
  try {
    await request.post('/courses', {
      courseName: form.courseName,
      courseType: form.courseType,
      credits: form.credits,
      semester: form.semester,
      academicYear: form.academicYear,
      score: form.score,
      gradePoint: form.gradePoint,
    })
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

async function handleUpdate() {
  try {
    await request.put('/courses', {
      id: form.id,
      courseName: form.courseName,
      courseType: form.courseType,
      credits: form.credits,
      semester: form.semester,
      academicYear: form.academicYear,
      score: form.score,
      gradePoint: form.gradePoint,
    })
    ElMessage.success('修改成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除课程「${row.courseName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await request.delete(`/courses/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="page">
    <!-- Page Header -->
    <div class="page-header">
      <h1 class="page-title">课程管理</h1>
      <button class="btn-primary" @click="openAddDialog">+ 添加课程</button>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <select v-model="filterSemester" @change="handleFilter">
        <option value="">全部学期</option>
        <option v-for="s in semesterOptions" :key="s" :value="s">{{ s }}</option>
      </select>
      <select v-model="filterType" @change="handleFilter">
        <option value="">全部类型</option>
        <option v-for="t in typeOptions" :key="t" :value="t">{{ t }}</option>
      </select>
      <button v-if="filterSemester || filterType" class="btn-text" @click="resetFilter">
        重置筛选
      </button>
    </div>

    <!-- Course List -->
    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else class="course-list">
      <div
        v-for="item in courseList"
        :key="item.id"
        class="course-item"
      >
        <div class="course-info">
          <div class="course-icon">📖</div>
          <div>
            <div class="course-name">{{ item.courseName }}</div>
          </div>
          <span class="course-tag">{{ item.courseType }}</span>
        </div>
        <div class="course-meta">
          <span>{{ item.credits }} 学分</span>
          <span>{{ item.semester }}</span>
          <span class="course-grade">{{ item.score ?? '-' }}</span>
          <div class="course-actions">
            <button class="action-btn" @click="openEditDialog(item)">编辑</button>
            <button class="action-btn danger" @click="handleDelete(item)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && courseList.length === 0" class="empty-state">
      <div class="empty-state-icon">📚</div>
      <div class="empty-state-text">暂无课程数据</div>
      <button class="btn-primary" @click="openAddDialog">添加第一门课程</button>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="course-form"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="form.courseType" placeholder="请选择">
            <el-option label="必修" value="必修" />
            <el-option label="选修" value="选修" />
            <el-option label="通识" value="通识" />
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="form.credits" :min="0" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="如 2024-2025-1" />
        </el-form-item>
        <el-form-item label="学年">
          <el-input v-model="form.academicYear" placeholder="如 2024-2025" />
        </el-form-item>
        <el-form-item label="成绩">
          <el-input-number v-model="form.score" :min="0" :max="100" :step="0.1" />
        </el-form-item>
        <el-form-item label="绩点">
          <el-input-number v-model="form.gradePoint" :min="0" :max="5" :step="0.01" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-secondary" @click="dialogVisible = false">取消</button>
          <button class="btn-primary" @click="handleSubmit">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 3rem 2rem;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Noto Sans SC', sans-serif;
}

.page-header {
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

.btn-secondary {
  padding: 0.5rem 1rem;
  background: transparent;
  color: oklch(48% 0.025 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 0.5rem;
}

.btn-secondary:hover {
  background: oklch(96% 0.012 30);
}

.btn-text {
  padding: 0.5rem 0.75rem;
  background: transparent;
  color: oklch(70% 0.14 20);
  border: none;
  font-size: 0.875rem;
  font-family: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}

.btn-text:hover {
  color: oklch(58% 0.16 20);
  text-decoration: underline;
}

/* Filter */
.filter-bar {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  align-items: center;
}

.filter-bar select {
  padding: 0.5rem 0.75rem;
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 10px;
  font-family: inherit;
  font-size: 0.875rem;
  color: oklch(25% 0.02 30);
  background: oklch(96% 0.012 30);
  cursor: pointer;
  min-width: 120px;
}

/* Course List */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.course-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  background: oklch(96% 0.012 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 16px;
  transition: all 0.2s ease;
}

.course-item:hover {
  background: oklch(94% 0.015 30);
  box-shadow: 0 1px 2px oklch(70% 0.01 30 / 0.06);
}

.course-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.course-icon {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  background: oklch(96% 0.02 195);
  color: oklch(68% 0.12 195);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.course-name {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  color: oklch(25% 0.02 30);
}

.course-tag {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 100px;
  background: oklch(96% 0.02 195);
  color: oklch(68% 0.12 195);
  font-weight: 500;
  flex-shrink: 0;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.course-meta span {
  font-size: 0.875rem;
  color: oklch(48% 0.025 30);
}

.course-grade {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.5rem;
  color: oklch(58% 0.16 20);
  min-width: 40px;
  text-align: center;
}

.course-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.25rem 0.75rem;
  background: transparent;
  color: oklch(48% 0.025 30);
  border: 1px solid oklch(88% 0.015 30);
  border-radius: 6px;
  font-size: 0.75rem;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: oklch(94% 0.015 30);
  color: oklch(25% 0.02 30);
}

.action-btn.danger {
  color: oklch(55% 0.18 25);
  border-color: oklch(80% 0.1 25);
}

.action-btn.danger:hover {
  background: oklch(95% 0.03 25);
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
  padding: 4rem 2rem;
  color: oklch(62% 0.02 30);
}

.empty-state-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
  opacity: 0.5;
}

.empty-state-text {
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

/* Dialog */
:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 1.5rem 1.5rem 0.75rem;
}

:deep(.el-dialog__title) {
  font-family: 'ZCOOL XiaoWei', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 400;
  color: oklch(25% 0.02 30);
}

:deep(.el-dialog__body) {
  padding: 1rem 1.5rem;
}

:deep(.el-dialog__footer) {
  padding: 0.75rem 1.5rem 1.5rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.course-form :deep(.el-form-item__label) {
  color: oklch(48% 0.025 30);
  font-weight: 500;
}

.course-form :deep(.el-input__wrapper) {
  background: oklch(98% 0.008 30);
  box-shadow: 0 0 0 1px oklch(88% 0.015 30) inset;
}

.course-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px oklch(70% 0.14 20) inset;
}

/* Responsive */
@media (max-width: 768px) {
  .page {
    padding: 1.5rem 1rem;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .course-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .course-meta {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 0.5rem;
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
