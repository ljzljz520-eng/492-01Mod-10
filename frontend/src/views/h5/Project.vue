<template>
  <div class="project-page">
    <van-search
      v-model="searchForm.projectName"
      placeholder="搜索项目名称"
      @search="handleSearch"
      @clear="handleSearch"
      class="mb-3"
    />
    
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadData"
      >
        <div 
          v-for="item in list" 
          :key="item.id"
          class="project-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-base font-semibold text-gray-800 flex-1">{{ item.projectName }}</h3>
              <van-tag 
                :type="item.projectStatus === 'active' ? 'success' : (item.projectStatus === 'suspended' ? 'warning' : 'default')"
                class="ml-2 rounded-full"
              >
                {{ getStatusText(item.projectStatus) }}
              </van-tag>
            </div>
            <p class="text-xs text-gray-500 mb-2">编号：{{ item.projectCode }}</p>
            <div class="space-y-1 text-sm text-gray-600">
              <p><van-icon name="location-o" /> {{ item.projectAddress || '-' }}</p>
              <p><van-icon name="office-o" /> 企业：{{ item.enterpriseName || '-' }}</p>
              <p><van-icon name="friends-o" /> 劳务：{{ item.laborCompanyName || '-' }}</p>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { projectApi } from '@/api/project'

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const list = ref([])

const searchForm = reactive({
  projectName: '',
  projectStatus: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  if (refreshing.value || list.value.length === 0) {
    pagination.current = 1
    if (refreshing.value) {
      list.value = []
      finished.value = false
    }
  }
  
  loading.value = true
  try {
    const res = await projectApi.page({
      current: pagination.current,
      size: pagination.size,
      projectName: searchForm.projectName,
      projectStatus: searchForm.projectStatus
    })
    if (res.code === 200) {
      if (refreshing.value || pagination.current === 1) {
        list.value = res.data.records
        refreshing.value = false
      } else {
        list.value.push(...res.data.records)
      }
      pagination.total = res.data.total
      
      if (list.value.length >= res.data.total) {
        finished.value = true
      } else {
        pagination.current++
      }
    }
  } catch (error) {
    console.error(error)
    finished.value = true
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  finished.value = false
  refreshing.value = true
  loadData()
}

const handleSearch = () => {
  pagination.current = 1
  list.value = []
  finished.value = false
  refreshing.value = false
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
  min-height: calc(100vh - 100px);
  padding-bottom: 20px;
}

.project-card {
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}
</style>
