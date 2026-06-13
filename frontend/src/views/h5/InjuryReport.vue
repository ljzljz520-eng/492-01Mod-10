<template>
  <div class="injury-report-page">
    <van-search
      v-model="searchForm.keyword"
      placeholder="搜索项目名称或受伤人员"
      @search="handleSearch"
      @clear="handleSearch"
      class="mb-3"
    />
    
    <div class="filter-bar mb-3 px-2 flex gap-2">
      <van-dropdown-menu>
        <van-dropdown-item 
          v-model="searchForm.reportStatus" 
          :options="statusOptions"
          @change="handleSearch"
        >
          <template #title>
            <span class="text-sm">状态: {{ getStatusText(searchForm.reportStatus) || '全部' }}</span>
          </template>
        </van-dropdown-item>
      </van-dropdown-menu>
    </div>

    <div class="fixed bottom-20 right-4 z-50">
      <van-button
        type="danger"
        round
        icon="warning-o"
        size="large"
        @click="handleAdd"
        class="shadow-lg"
      >
        上报工伤
      </van-button>
    </div>

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
          class="report-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
          @click="goDetail(item)"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-base font-semibold text-gray-800 flex-1">
                {{ item.injuredPersonName }}
                <span class="text-xs text-gray-500 ml-2">{{ item.projectName }}</span>
              </h3>
              <van-tag 
                :type="getStatusType(item.reportStatus)"
                class="ml-2 rounded-full"
              >
                {{ getStatusText(item.reportStatus) }}
              </van-tag>
            </div>
            <div class="space-y-1 text-sm text-gray-600">
              <p><van-icon name="clock-o" /> {{ formatDateTime(item.injuryTime) }}</p>
              <p><van-icon name="location-o" /> {{ item.injuryLocation }}</p>
              <p v-if="item.workShift"><van-icon name="orders-o" /> {{ item.workShift }} · {{ item.workPosition || '-' }}</p>
            </div>
            <div class="mt-3 pt-3 border-t flex items-center justify-between text-xs">
              <span class="text-gray-400">编号: {{ item.reportNo }}</span>
              <span v-if="item.injuryResult" class="text-blue-600 font-medium">
                {{ getResultText(item.injuryResult) }}
              </span>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <van-popup
      v-model:show="popupVisible"
      :title="popupTitle"
      position="bottom"
      :style="{ height: '90%' }"
      round
      closeable
      close-icon-position="top-right"
    >
      <div class="popup-content p-4">
        <van-form @submit="handleSubmit" ref="formRef">
          <van-cell-group inset>
            <van-field
              v-model="formData.projectId"
              name="projectId"
              label="所属项目"
              placeholder="请选择项目"
              is-link
              readonly
              @click="showProjectPicker = true"
              :rules="[{ required: true, message: '请选择项目' }]"
            >
              <template #input>
                <span :class="formData.projectName ? 'text-gray-800' : 'text-gray-400'">
                  {{ formData.projectName || '请选择项目' }}
                </span>
              </template>
            </van-field>
            <van-field
              v-model="formData.injuredPersonName"
              name="injuredPersonName"
              label="受伤人员姓名"
              placeholder="请输入受伤人员姓名"
              :rules="[{ required: true, message: '请输入受伤人员姓名' }]"
            />
            <van-field
              v-model="formData.injuredPersonPhone"
              name="injuredPersonPhone"
              label="联系电话"
              placeholder="请输入联系电话"
              type="tel"
            />
            <van-field
              v-model="formData.injuredPersonIdcard"
              name="injuredPersonIdcard"
              label="身份证号"
              placeholder="请输入身份证号"
            />
            <van-field
              v-model="injuryTimeDisplay"
              name="injuryTime"
              label="受伤时间"
              placeholder="请选择受伤时间"
              is-link
              readonly
              @click="showInjuryTimePicker = true"
              :rules="[{ required: true, message: '请选择受伤时间' }]"
            />
            <van-field
              v-model="formData.injuryLocation"
              name="injuryLocation"
              label="受伤地点"
              placeholder="请输入具体受伤地点"
              :rules="[{ required: true, message: '请输入受伤地点' }]"
            />
            <van-field
              v-model="formData.workShift"
              name="workShift"
              label="岗位班次"
              placeholder="请选择班次"
              is-link
              readonly
              @click="showShiftPicker = true"
              :rules="[{ required: true, message: '请选择岗位班次' }]"
            />
            <van-field
              v-model="formData.workPosition"
              name="workPosition"
              label="工作岗位"
              placeholder="请输入工作岗位"
              :rules="[{ required: true, message: '请输入工作岗位' }]"
            />
            <van-field
              v-model="formData.witnesses"
              name="witnesses"
              label="见证人"
              placeholder="多个见证人用逗号分隔"
              type="textarea"
              rows="2"
            />
            <van-field
              v-model="formData.hospital"
              name="hospital"
              label="送医医院"
              placeholder="请输入送医医院名称"
            />
            <van-field
              v-model="formData.injuryDescription"
              name="injuryDescription"
              label="伤情描述"
              type="textarea"
              rows="3"
              placeholder="请详细描述受伤情况"
            />
          </van-cell-group>
          <div class="p-4">
            <van-button 
              round 
              block 
              type="danger" 
              native-type="submit"
              :loading="submitting"
              class="mb-3"
            >
              提交上报
            </van-button>
            <van-button 
              round 
              block 
              @click="popupVisible = false"
            >
              取消
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showInjuryTimePicker" position="bottom">
      <van-datetime-picker
        v-model="injuryTimeValue"
        type="datetime"
        title="选择受伤时间"
        :max-date="new Date()"
        @confirm="onInjuryTimeConfirm"
        @cancel="showInjuryTimePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showShiftPicker" position="bottom">
      <van-picker
        :columns="shiftColumns"
        @confirm="onShiftConfirm"
        @cancel="showShiftPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showProjectPicker" position="bottom" :style="{ height: '50%' }">
      <div class="p-4">
        <h3 class="text-base font-semibold mb-3">选择项目</h3>
        <van-radio-group v-model="selectedProjectId">
          <van-cell-group>
            <van-cell
              v-for="p in projectList"
              :key="p.id"
              :title="p.projectName"
              :label="p.projectAddress"
              clickable
              @click="selectProject(p)"
            >
              <template #right-icon>
                <van-radio :name="p.id" />
              </template>
            </van-cell>
          </van-cell-group>
        </van-radio-group>
        <div class="mt-4 flex gap-3">
          <van-button block @click="showProjectPicker = false">取消</van-button>
          <van-button block type="primary" @click="confirmProject">确定</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { injuryReportApi } from '@/api/injuryReport'
import { projectApi } from '@/api/project'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const popupVisible = ref(false)
const popupTitle = ref('上报工伤')
const formRef = ref(null)

const showInjuryTimePicker = ref(false)
const showShiftPicker = ref(false)
const showProjectPicker = ref(false)

const list = ref([])
const projectList = ref([])
const selectedProjectId = ref(null)
const injuryTimeValue = ref(new Date())
const injuryTimeDisplay = ref('')

const searchForm = reactive({
  keyword: '',
  reportStatus: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  projectId: null,
  projectName: '',
  injuredPersonName: '',
  injuredPersonPhone: '',
  injuredPersonIdcard: '',
  injuryTime: null,
  injuryLocation: '',
  workShift: '',
  workPosition: '',
  witnesses: '',
  hospital: '',
  injuryDescription: ''
})

const statusOptions = [
  { text: '全部', value: '' },
  { text: '待补充材料', value: 'pending' },
  { text: '已提交', value: 'submitted' },
  { text: '审核中', value: 'reviewing' },
  { text: '已通过', value: 'approved' },
  { text: '已驳回', value: 'rejected' }
]

const shiftColumns = [
  { text: '白班', value: '白班' },
  { text: '早班', value: '早班' },
  { text: '中班', value: '中班' },
  { text: '晚班', value: '晚班' },
  { text: '夜班', value: '夜班' }
]

const getCurrentUser = () => {
  try {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
  } catch { return null }
}

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
    const user = getCurrentUser()
    const res = await injuryReportApi.page({
      current: pagination.current,
      size: pagination.size,
      reportStatus: searchForm.reportStatus,
      userId: user?.id
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

const loadProjects = async () => {
  try {
    const user = getCurrentUser()
    const res = await projectApi.list(user?.id)
    if (res.code === 200) {
      projectList.value = res.data
    }
  } catch (error) {
    console.error(error)
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

const handleAdd = () => {
  popupTitle.value = '上报工伤'
  Object.assign(formData, {
    id: null,
    projectId: null,
    projectName: '',
    injuredPersonName: '',
    injuredPersonPhone: '',
    injuredPersonIdcard: '',
    injuryTime: null,
    injuryLocation: '',
    workShift: '',
    workPosition: '',
    witnesses: '',
    hospital: '',
    injuryDescription: ''
  })
  injuryTimeValue.value = new Date()
  injuryTimeDisplay.value = ''
  selectedProjectId.value = null
  loadProjects()
  popupVisible.value = true
}

const goDetail = (item) => {
  router.push(`/h5/injury-report/${item.id}`)
}

const selectProject = (p) => {
  selectedProjectId.value = p.id
}

const confirmProject = () => {
  const p = projectList.value.find(x => x.id === selectedProjectId.value)
  if (p) {
    formData.projectId = p.id
    formData.projectName = p.projectName
  }
  showProjectPicker.value = false
}

const onInjuryTimeConfirm = (value) => {
  formData.injuryTime = dayjs(value).format('YYYY-MM-DD HH:mm:ss')
  injuryTimeDisplay.value = dayjs(value).format('YYYY-MM-DD HH:mm')
  showInjuryTimePicker.value = false
}

const onShiftConfirm = ({ selectedOptions }) => {
  formData.workShift = selectedOptions[0].value
  showShiftPicker.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  submitting.value = true
  try {
    const user = getCurrentUser()
    const res = await injuryReportApi.save(formData, user?.id)
    if (res.code === 200) {
      showToast({ type: 'success', message: '上报成功，请尽快上传证明材料' })
      popupVisible.value = false
      setTimeout(() => {
        router.push(`/h5/injury-report/${res.data.id}`)
      }, 800)
      loadData()
    }
  } catch (error) {
    console.error(error)
    showToast({ type: 'fail', message: '上报失败' })
  } finally {
    submitting.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    submitted: 'primary',
    reviewing: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'default'
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
    recovered: '已复工',
    suspended: '停工中'
  }
  return texts[result] || result
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('MM-DD HH:mm')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.injury-report-page {
  min-height: calc(100vh - 100px);
  padding-bottom: 20px;
}

.report-card {
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.report-card:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.popup-content {
  height: 100%;
  overflow-y: auto;
}

:deep(.van-button) {
  border-radius: 20px;
}

:deep(.van-tag) {
  border-radius: 12px;
}
</style>
