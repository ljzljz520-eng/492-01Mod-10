<template>
  <div class="injury-report-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">工伤上报管理</span>
          <div class="flex gap-2">
            <el-button
              type="danger" 
              @click="handleAdd"
              class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            >
              <el-icon><Warning /></el-icon>
              上报工伤
            </el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="项目">
          <el-select 
            v-model="searchForm.projectId" 
            placeholder="全部项目" 
            clearable 
            style="width: 200px"
            class="rounded-lg"
          >
            <el-option 
              v-for="p in projectList" 
              :key="p.id" 
              :label="p.projectName" 
              :value="p.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上报状态">
          <el-select 
            v-model="searchForm.reportStatus" 
            placeholder="全部状态" 
            clearable 
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="待补充材料" value="pending" />
            <el-option label="已提交" value="submitted" />
            <el-option label="审核中" value="reviewing" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结论">
          <el-select 
            v-model="searchForm.injuryResult" 
            placeholder="全部结论" 
            clearable 
            style="width: 150px"
            class="rounded-lg"
          >
            <el-option label="复工" value="recovered" />
            <el-option label="停工" value="suspended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            查询
          </el-button>
          <el-button 
            @click="handleReset"
            class="rounded-lg"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="reportNo" label="上报编号" width="180" />
        <el-table-column prop="projectName" label="所属项目" min-width="160" />
        <el-table-column prop="injuredPersonName" label="受伤人员" width="100" />
        <el-table-column prop="injuryTime" label="受伤时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.injuryTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="injuryLocation" label="受伤地点" min-width="150" show-overflow-tooltip />
        <el-table-column label="岗位班次" width="140">
          <template #default="{ row }">
            {{ row.workShift }} · {{ row.workPosition || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="reportStatus" label="上报状态" width="110">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.reportStatus)"
              class="rounded-full px-3 py-1"
            >
              {{ getStatusText(row.reportStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理结论" width="100">
          <template #default="{ row }">
            <el-tag 
              v-if="row.injuryResult"
              :type="row.injuryResult === 'recovered' ? 'success' : 'warning'"
              class="rounded-full px-3 py-1"
            >
              {{ getResultText(row.injuryResult) }}
            </el-tag>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="reportUserName" label="上报人" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link 
              @click="goDetail(row)"
              class="hover:text-blue-600 transition-colors duration-200"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="currentUser && currentUser.userRole === 'admin' && !row.injuryResult"
              type="success" 
              size="small" 
              link 
              @click="openConclusion(row)"
              class="hover:text-green-600 transition-colors duration-200"
            >
              出具结论
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="rounded-lg"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog 
      v-model="conclusionVisible" 
      title="出具处理结论" 
      width="500px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-alert 
        type="info" 
        :closable="false"
        class="mb-4"
        show-icon
      >
        结论将自动回写到关联的排班档案中
      </el-alert>
      <el-form 
        :model="conclusionForm" 
        label-width="100px"
      >
        <el-form-item label="处理结论" required>
          <el-radio-group v-model="conclusionForm.result">
            <el-radio value="recovered">
              <span class="text-green-600 font-medium">复工</span>
              <span class="text-xs text-gray-500 ml-1">（排班档案标记为已复工）</span>
            </el-radio>
            <el-radio value="suspended">
              <span class="text-orange-600 font-medium">停工</span>
              <span class="text-xs text-gray-500 ml-1">（排班档案标记为工伤停工）</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input 
            v-model="conclusionForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入结论备注（选填）"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="conclusionVisible = false" class="rounded-lg">取消</el-button>
          <el-button 
            type="primary" 
            :disabled="!conclusionForm.result"
            :loading="concluding"
            @click="submitConclusion"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            确认出具
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { injuryReportApi } from '@/api/injuryReport'
import { projectApi } from '@/api/project'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const concluding = ref(false)
const tableData = ref([])
const projectList = ref([])
const conclusionVisible = ref(false)
const currentRow = ref(null)

const searchForm = reactive({
  projectId: null,
  reportStatus: '',
  injuryResult: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const conclusionForm = reactive({
  result: '',
  remark: ''
})



const loadData = async () => {
  loading.value = true
  try {
    const res = await injuryReportApi.page({
      current: pagination.current,
      size: pagination.size,
      projectId: searchForm.projectId,
      reportStatus: searchForm.reportStatus
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadProjects = async () => {
  try {
    const res = await projectApi.list()
    if (res.code === 200) {
      projectList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    projectId: null,
    reportStatus: '',
    injuryResult: ''
  })
  handleSearch()
}

const handleAdd = () => {
  router.push('/h5/injury-report')
}

const goDetail = (row) => {
  router.push(`/pc/injury-report/${row.id}`)
}

const openConclusion = (row) => {
  currentRow.value = row
  conclusionForm.result = ''
  conclusionForm.remark = ''
  conclusionVisible.value = true
}

const submitConclusion = async () => {
  if (!conclusionForm.result || !currentRow.value) return
  concluding.value = true
  try {
    const res = await injuryReportApi.processConclusion(
      currentRow.value.id,
      conclusionForm.result,
      conclusionForm.remark
    )
    if (res.code === 200) {
      ElMessage.success('结论已出具，排班档案已同步更新')
      conclusionVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  } finally {
    concluding.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    submitted: 'primary',
    reviewing: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待补充材料',
    submitted: '已提交',
    reviewing: '审核中',
    approved: '已通过',
    rejected: '已驳回'
  }
  return texts[status] || status
}

const getResultText = (result) => {
  const texts = {
    recovered: '复工',
    suspended: '停工'
  }
  return texts[result] || result
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadData()
  loadProjects()
})
</script>

<style scoped>
.injury-report-page {
  max-width: 1600px;
  margin: 0 auto;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
