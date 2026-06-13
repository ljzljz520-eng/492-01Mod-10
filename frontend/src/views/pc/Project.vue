<template>
  <div class="project-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">项目管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="项目名称">
          <el-input 
            v-model="searchForm.projectName" 
            placeholder="请输入项目名称" 
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select 
            v-model="searchForm.projectStatus" 
            placeholder="全部状态" 
            clearable 
            style="width: 180px"
          >
            <el-option label="进行中" value="active" />
            <el-option label="已完成" value="completed" />
            <el-option label="暂停" value="suspended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="projectCode" label="项目编号" width="160" />
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="enterpriseName" label="所属企业" width="160" />
        <el-table-column prop="laborCompanyName" label="劳务公司" width="160" />
        <el-table-column prop="projectAddress" label="项目地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.projectStatus === 'active' ? 'success' : (row.projectStatus === 'suspended' ? 'warning' : 'info')"
              round
            >
              {{ getStatusText(row.projectStatus) }}
            </el-tag>
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { projectApi } from '@/api/project'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  projectName: '',
  projectStatus: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const currentUser = computed(() => {
  try {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
  } catch { return null }
})

const loadData = async () => {
  loading.value = true
  try {
    const user = currentUser.value
    const res = await projectApi.page({
      current: pagination.current,
      size: pagination.size,
      projectName: searchForm.projectName,
      projectStatus: searchForm.projectStatus,
      userId: user?.id
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

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    projectName: '',
    projectStatus: ''
  })
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

const getStatusText = (status) => {
  const texts = {
    active: '进行中',
    completed: '已完成',
    suspended: '暂停'
  }
  return texts[status] || status
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.project-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
