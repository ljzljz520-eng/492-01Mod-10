<template>
  <div class="detail-page pb-20">
    <div v-if="detail" class="space-y-3">
      <div class="bg-white mx-2 mt-2 rounded-lg shadow-sm overflow-hidden">
        <div class="p-4 border-b bg-gradient-to-r from-red-50 to-orange-50">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg font-bold text-gray-800">{{ detail.injuredPersonName }}</h3>
              <p class="text-xs text-gray-500 mt-1">编号：{{ detail.reportNo }}</p>
            </div>
            <van-tag :type="getStatusType(detail.reportStatus)" size="medium" round>
              {{ getStatusText(detail.reportStatus) }}
            </van-tag>
          </div>
        </div>
        <div class="p-4 space-y-3 text-sm">
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">所属项目</span>
            <span class="text-gray-800">{{ detail.projectName }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">受伤时间</span>
            <span class="text-gray-800">{{ formatDateTime(detail.injuryTime) }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">受伤地点</span>
            <span class="text-gray-800">{{ detail.injuryLocation }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">岗位班次</span>
            <span class="text-gray-800">{{ detail.workShift }} · {{ detail.workPosition || '-' }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">送医医院</span>
            <span class="text-gray-800">{{ detail.hospital || '-' }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">见证人</span>
            <span class="text-gray-800">{{ detail.witnesses || '-' }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">联系电话</span>
            <span class="text-gray-800">{{ detail.injuredPersonPhone || '-' }}</span>
          </div>
          <div v-if="detail.injuryDescription" class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">伤情描述</span>
            <span class="text-gray-800 flex-1">{{ detail.injuryDescription }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">上报人</span>
            <span class="text-gray-800">{{ detail.reportUserName || '-' }} · {{ formatDateTime(detail.reportTime) }}</span>
          </div>
        </div>
      </div>

      <div v-if="currentUser && (currentUser.userRole === 'supervisor' || currentUser.userRole === 'admin')" 
           class="bg-white mx-2 rounded-lg shadow-sm overflow-hidden">
        <div class="p-4 border-b flex items-center justify-between">
          <h4 class="font-semibold text-gray-800 flex items-center">
            <van-icon name="warning" class="text-orange-500 mr-1" />
            材料清单
          </h4>
          <span v-if="missingInfo.missingRequiredCount > 0" class="text-xs text-red-500">
            缺{{ missingInfo.missingRequiredCount }}项必填
          </span>
          <span v-else class="text-xs text-green-600">材料齐全</span>
        </div>
        <div v-if="missingInfo.allMaterials && missingInfo.allMaterials.length > 0" class="p-4">
          <van-empty v-if="!missingInfo.canSubmit" description="以下材料待补充" image="warning" class="!py-2" />
          <div class="space-y-2">
            <div v-for="mat in missingInfo.allMaterials" :key="mat.type"
                 class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div class="flex items-center">
                <van-tag v-if="mat.level === 'required'" type="danger" size="mini" round class="mr-2">必填</van-tag>
                <van-tag v-else type="primary" size="mini" plain round class="mr-2">建议</van-tag>
                <span class="text-sm text-gray-700">{{ mat.name }}</span>
              </div>
              <van-button size="mini" type="primary" round @click="openUpload(mat)">上传</van-button>
            </div>
          </div>
        </div>
        <div v-else class="p-8 text-center">
          <van-icon name="checked" size="48" class="text-green-500" />
          <p class="text-sm text-gray-500 mt-2">所有必填材料已齐全</p>
        </div>
      </div>

      <div class="bg-white mx-2 rounded-lg shadow-sm overflow-hidden">
        <div class="p-4 border-b flex items-center justify-between">
          <h4 class="font-semibold text-gray-800 flex items-center">
            <van-icon name="photo-o" class="text-blue-500 mr-1" />
            已上传材料 ({{ materials.length }})
          </h4>
          <van-button v-if="currentUser && currentUser.userRole !== 'enterprise'" 
                      size="mini" type="primary" plain round @click="openUpload(null)">
            新增材料
          </van-button>
        </div>
        <div v-if="materials.length > 0" class="p-4">
          <div class="grid grid-cols-3 gap-3">
            <div v-for="m in materials" :key="m.id" class="relative">
              <div class="aspect-square bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden">
                <van-icon name="description" size="32" class="text-gray-400" />
              </div>
              <p class="text-xs text-gray-600 mt-1 truncate">{{ m.materialName }}</p>
              <p class="text-xs text-gray-400">{{ getMaterialTypeText(m.materialType) }}</p>
              <van-icon v-if="currentUser && currentUser.userRole !== 'enterprise'" 
                        name="clear" 
                        class="absolute -top-1 -right-1 text-white bg-red-500 rounded-full text-xs p-0.5"
                        @click="deleteMaterial(m)" />
            </div>
          </div>
        </div>
        <div v-else class="p-8 text-center">
          <van-empty description="暂无上传材料" />
        </div>
      </div>

      <div class="bg-white mx-2 rounded-lg shadow-sm overflow-hidden">
        <div class="p-4 border-b">
          <h4 class="font-semibold text-gray-800 flex items-center">
            <van-icon name="shield-o" class="text-blue-500 mr-1" />
            保险资料
          </h4>
        </div>
        <div v-if="detail.insuranceCompany || detail.insurancePolicyNo" class="p-4 space-y-3 text-sm">
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">保险公司</span>
            <span class="text-gray-800">{{ detail.insuranceCompany || '-' }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">保险单号</span>
            <span class="text-gray-800">{{ detail.insurancePolicyNo || '-' }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">保险保额</span>
            <span class="text-gray-800">{{ detail.insuranceCoverage ? '¥' + detail.insuranceCoverage : '-' }}</span>
          </div>
          <div v-if="detail.insuranceRemark" class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">备注</span>
            <span class="text-gray-800">{{ detail.insuranceRemark }}</span>
          </div>
          <div v-if="detail.laborUserName" class="flex text-xs text-gray-400">
            <span>由 {{ detail.laborUserName }} 于 {{ formatDateTime(detail.laborProcessTime) }} 补充</span>
          </div>
        </div>
        <div v-else class="p-4 text-center text-sm text-gray-400">
          劳务公司还未补充保险资料
        </div>
        <div v-if="currentUser && (currentUser.userRole === 'labor' || currentUser.userRole === 'admin')" class="p-4 pt-0">
          <van-button block type="primary" round size="small" @click="openInsuranceForm">
            {{ detail.insuranceCompany ? '更新保险资料' : '补充保险资料' }}
          </van-button>
        </div>
      </div>

      <div v-if="detail.injuryResult" class="bg-white mx-2 rounded-lg shadow-sm overflow-hidden">
        <div class="p-4 border-b">
          <h4 class="font-semibold text-gray-800 flex items-center">
            <van-icon name="todo-list-o" class="text-green-500 mr-1" />
            处理结论
          </h4>
        </div>
        <div class="p-4 space-y-3 text-sm">
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">结论</span>
            <van-tag :type="detail.injuryResult === 'recovered' ? 'success' : 'warning'" round>
              {{ getResultText(detail.injuryResult) }}
            </van-tag>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">结论时间</span>
            <span class="text-gray-800">{{ formatDateTime(detail.resultTime) }}</span>
          </div>
          <div v-if="detail.resultRemark" class="flex">
            <span class="text-gray-500 w-24 flex-shrink-0">备注</span>
            <span class="text-gray-800">{{ detail.resultRemark }}</span>
          </div>
          <div v-if="detail.workId" class="flex items-center">
            <span class="text-gray-500 w-24 flex-shrink-0">排班档案</span>
            <van-button size="mini" type="primary" plain round @click="goWorkDetail">
              查看排班 #{{ detail.workId }}
            </van-button>
          </div>
        </div>
      </div>

      <div v-if="currentUser && (currentUser.userRole === 'admin') && !detail.injuryResult" 
           class="bg-white mx-2 rounded-lg shadow-sm overflow-hidden">
        <div class="p-4 border-b">
          <h4 class="font-semibold text-gray-800">出具处理结论</h4>
          <p class="text-xs text-gray-500 mt-1">结论将自动回写到排班档案</p>
        </div>
        <div class="p-4 space-y-3">
          <van-radio-group v-model="conclusionForm.result">
            <van-cell-group>
              <van-cell title="复工" value="允许恢复工作">
                <template #right-icon>
                  <van-radio name="recovered" />
                </template>
              </van-cell>
              <van-cell title="停工" value="需要继续休养">
                <template #right-icon>
                  <van-radio name="suspended" />
                </template>
              </van-cell>
            </van-cell-group>
          </van-radio-group>
          <van-field
            v-model="conclusionForm.remark"
            label="备注说明"
            type="textarea"
            rows="2"
            placeholder="请输入结论备注（选填）"
          />
          <van-button 
            block 
            type="primary" 
            round 
            :disabled="!conclusionForm.result"
            :loading="concluding"
            @click="submitConclusion"
          >
            确认出具结论
          </van-button>
        </div>
      </div>
    </div>

    <van-popup v-model:show="uploadVisible" position="bottom" round :style="{ height: '60%' }">
      <div class="p-4">
        <h3 class="text-base font-semibold mb-4">上传证明材料</h3>
        <van-form>
          <van-cell-group inset>
            <van-field
              v-model="uploadForm.materialType"
              label="材料类型"
              placeholder="请选择材料类型"
              is-link
              readonly
              @click="showMaterialTypePicker = true"
              :rules="[{ required: true, message: '请选择材料类型' }]"
            >
              <template #input>
                <span :class="uploadForm.materialType ? 'text-gray-800' : 'text-gray-400'">
                  {{ getMaterialTypeText(uploadForm.materialType) || '请选择材料类型' }}
                </span>
              </template>
            </van-field>
            <van-field
              v-model="uploadForm.materialName"
              label="材料名称"
              placeholder="请输入材料名称"
              :rules="[{ required: true, message: '请输入材料名称' }]"
            />
            <van-field
              v-model="uploadForm.fileName"
              label="文件名称"
              placeholder="请输入文件名称（模拟上传）"
            />
          </van-cell-group>
          <div class="mt-4 flex gap-3">
            <van-button block @click="uploadVisible = false">取消</van-button>
            <van-button block type="primary" :loading="uploading" @click="submitUpload">确认上传</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showMaterialTypePicker" position="bottom">
      <van-picker
        :columns="materialTypeColumns"
        @confirm="onMaterialTypeConfirm"
        @cancel="showMaterialTypePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="insuranceVisible" position="bottom" round :style="{ height: '65%' }">
      <div class="p-4">
        <h3 class="text-base font-semibold mb-4">补充保险资料</h3>
        <van-form>
          <van-cell-group inset>
            <van-field
              v-model="insuranceForm.insuranceCompany"
              label="保险公司"
              placeholder="请输入保险公司名称"
            />
            <van-field
              v-model="insuranceForm.insurancePolicyNo"
              label="保险单号"
              placeholder="请输入保险单号"
            />
            <van-field
              v-model="insuranceForm.insuranceCoverage"
              label="保险保额"
              placeholder="请输入保险金额（元）"
              type="digit"
            />
            <van-field
              v-model="insuranceForm.insuranceRemark"
              label="备注"
              type="textarea"
              rows="2"
              placeholder="请输入备注"
            />
          </van-cell-group>
          <div class="mt-4 flex gap-3">
            <van-button block @click="insuranceVisible = false">取消</van-button>
            <van-button block type="primary" :loading="insuranceSubmitting" @click="submitInsurance">确认提交</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { injuryReportApi } from '@/api/injuryReport'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const reportId = route.params.id

const detail = ref(null)
const materials = ref([])
const missingInfo = ref({
  missingRequired: [],
  missingRecommended: [],
  missingRequiredCount: 0,
  missingRecommendedCount: 0,
  canSubmit: false,
  allMaterials: []
})

const uploadVisible = ref(false)
const uploading = ref(false)
const showMaterialTypePicker = ref(false)
const insuranceVisible = ref(false)
const insuranceSubmitting = ref(false)
const concluding = ref(false)

const uploadForm = reactive({
  materialType: '',
  materialName: '',
  fileName: ''
})

const insuranceForm = reactive({
  insuranceCompany: '',
  insurancePolicyNo: '',
  insuranceCoverage: '',
  insuranceRemark: ''
})

const conclusionForm = reactive({
  result: '',
  remark: ''
})

const materialTypeColumns = [
  { text: '现场照片', value: 'scene_photo' },
  { text: '医疗记录/诊断证明', value: 'medical_record' },
  { text: '受伤人员身份证', value: 'id_card' },
  { text: '见证人证言', value: 'witness_statement' },
  { text: '医院收费单据', value: 'hospital_receipt' },
  { text: '保险单', value: 'insurance_policy' },
  { text: '其他', value: 'other' }
]

const currentUser = computed(() => {
  try {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
  } catch { return null }
})

const loadDetail = async () => {
  try {
    const res = await injuryReportApi.getById(reportId)
    if (res.code === 200) {
      detail.value = res.data
      if (res.data.insuranceCompany) {
        insuranceForm.insuranceCompany = res.data.insuranceCompany
        insuranceForm.insurancePolicyNo = res.data.insurancePolicyNo || ''
        insuranceForm.insuranceCoverage = res.data.insuranceCoverage || ''
        insuranceForm.insuranceRemark = res.data.insuranceRemark || ''
      }
    }
  } catch (error) {
    console.error(error)
  }
}

const loadMaterials = async () => {
  try {
    const res = await injuryReportApi.getMaterials(reportId)
    if (res.code === 200) {
      materials.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadMissingMaterials = async () => {
  try {
    const res = await injuryReportApi.checkMissingMaterials(reportId)
    if (res.code === 200) {
      missingInfo.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const openUpload = (mat) => {
  uploadForm.materialType = mat?.type || ''
  uploadForm.materialName = mat?.name || ''
  uploadForm.fileName = ''
  uploadVisible.value = true
}

const onMaterialTypeConfirm = ({ selectedOptions }) => {
  uploadForm.materialType = selectedOptions[0].value
  if (!uploadForm.materialName) {
    uploadForm.materialName = selectedOptions[0].text
  }
  showMaterialTypePicker.value = false
}

const submitUpload = async () => {
  if (!uploadForm.materialType || !uploadForm.materialName) {
    showToast('请填写完整信息')
    return
  }
  uploading.value = true
  try {
    const res = await injuryReportApi.addMaterial({
      injuryReportId: reportId,
      materialType: uploadForm.materialType,
      materialName: uploadForm.materialName,
      fileName: uploadForm.fileName || uploadForm.materialName
    })
    if (res.code === 200) {
      showToast({ type: 'success', message: '上传成功' })
      uploadVisible.value = false
      loadMaterials()
      loadMissingMaterials()
      loadDetail()
    }
  } catch (error) {
    console.error(error)
    showToast({ type: 'fail', message: '上传失败' })
  } finally {
    uploading.value = false
  }
}

const deleteMaterial = async (m) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定删除该材料吗？'
    })
    const res = await injuryReportApi.deleteMaterial(m.id)
    if (res.code === 200) {
      showToast({ type: 'success', message: '删除成功' })
      loadMaterials()
      loadMissingMaterials()
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const openInsuranceForm = () => {
  insuranceVisible.value = true
}

const submitInsurance = async () => {
  insuranceSubmitting.value = true
  try {
    const res = await injuryReportApi.updateInsurance(reportId, {
      insuranceCompany: insuranceForm.insuranceCompany,
      insurancePolicyNo: insuranceForm.insurancePolicyNo,
      insuranceCoverage: insuranceForm.insuranceCoverage ? parseFloat(insuranceForm.insuranceCoverage) : null,
      insuranceRemark: insuranceForm.insuranceRemark
    })
    if (res.code === 200) {
      showToast({ type: 'success', message: '补充成功' })
      insuranceVisible.value = false
      loadDetail()
    }
  } catch (error) {
    console.error(error)
    showToast({ type: 'fail', message: '提交失败' })
  } finally {
    insuranceSubmitting.value = false
  }
}

const submitConclusion = async () => {
  if (!conclusionForm.result) return
  concluding.value = true
  try {
    const res = await injuryReportApi.processConclusion(
      reportId, 
      conclusionForm.result, 
      conclusionForm.remark
    )
    if (res.code === 200) {
      showToast({ type: 'success', message: '结论已出具，排班档案已更新' })
      loadDetail()
    }
  } catch (error) {
    console.error(error)
    showToast({ type: 'fail', message: '操作失败' })
  } finally {
    concluding.value = false
  }
}

const goWorkDetail = () => {
  if (detail.value?.workId) {
    router.push(`/h5/work`)
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
    recovered: '复工',
    suspended: '停工'
  }
  return texts[result] || result
}

const getMaterialTypeText = (type) => {
  const texts = {
    scene_photo: '现场照片',
    medical_record: '医疗记录',
    id_card: '身份证',
    witness_statement: '见证人证言',
    hospital_receipt: '医院单据',
    insurance_policy: '保险单',
    other: '其他'
  }
  return texts[type] || type
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadDetail()
  loadMaterials()
  loadMissingMaterials()
})
</script>

<style scoped>
.detail-page {
  min-height: calc(100vh - 100px);
}
</style>
