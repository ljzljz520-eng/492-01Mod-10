<template>
  <div class="detail-page">
    <el-page-header @back="goBack" content="返回列表" class="mb-4">
      <template #content>
        <div class="flex items-center gap-3">
          <span class="text-lg font-bold text-gray-800">工伤上报详情</span>
          <el-tag v-if="detail" :type="getStatusType(detail.reportStatus)" round>
            {{ getStatusText(detail.reportStatus) }}
          </el-tag>
        </div>
      </template>
    </el-page-header>

    <div v-if="detail" class="space-y-4">
      <el-card class="shadow-sm rounded-lg border-0" :body-style="{ padding: '0' }">
        <div class="p-6 border-b bg-gradient-to-r from-red-50 via-orange-50 to-yellow-50 rounded-t-lg">
          <div class="flex items-start justify-between">
            <div>
              <h2 class="text-xl font-bold text-gray-800">{{ detail.injuredPersonName }}</h2>
              <p class="text-sm text-gray-500 mt-1">上报编号：{{ detail.reportNo }}</p>
            </div>
            <div class="text-right">
              <el-tag 
                v-if="detail.injuryResult"
                :type="detail.injuryResult === 'recovered' ? 'success' : 'warning'"
                size="large"
                round
                effect="dark"
              >
                {{ getResultText(detail.injuryResult) }}
              </el-tag>
              <p v-else class="text-sm text-gray-500">待出具结论</p>
            </div>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="px-6 pt-2">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border class="mt-4">
              <el-descriptions-item label="所属项目">{{ detail.projectName }}</el-descriptions-item>
              <el-descriptions-item label="关联排班">
                <span v-if="detail.workId">
                  排班 #{{ detail.workId }}
                  <el-button size="small" type="primary" link @click="goWork">查看</el-button>
                </span>
                <span v-else class="text-gray-400">-</span>
              </el-descriptions-item>
              <el-descriptions-item label="受伤时间">
                <span class="text-red-600 font-medium">{{ formatDateTime(detail.injuryTime) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="受伤地点">{{ detail.injuryLocation }}</el-descriptions-item>
              <el-descriptions-item label="岗位班次">{{ detail.workShift }}</el-descriptions-item>
              <el-descriptions-item label="工作岗位">{{ detail.workPosition || '-' }}</el-descriptions-item>
              <el-descriptions-item label="送医医院">{{ detail.hospital || '-' }}</el-descriptions-item>
              <el-descriptions-item label="见证人">{{ detail.witnesses || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ detail.injuredPersonPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ detail.injuredPersonIdcard || '-' }}</el-descriptions-item>
              <el-descriptions-item label="上报人">{{ detail.reportUserName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="上报时间">{{ formatDateTime(detail.reportTime) }}</el-descriptions-item>
              <el-descriptions-item label="伤情描述" :span="2">
                {{ detail.injuryDescription || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="材料清单" name="materials">
            <div class="mt-4 space-y-4">
              <el-alert 
                v-if="missingInfo.missingRequiredCount > 0"
                :title="`缺少 ${missingInfo.missingRequiredCount} 项必填材料`"
                type="warning"
                show-icon
                :closable="false"
                class="mb-4"
              >
                <div class="mt-2">
                  <div v-for="mat in missingInfo.missingRequired" :key="mat.type" class="text-sm mb-1">
                    <el-tag type="danger" size="small" round class="mr-2">必填</el-tag>
                    {{ mat.name }}
                  </div>
                  <div v-for="mat in missingInfo.missingRecommended" :key="mat.type" class="text-sm mb-1">
                    <el-tag type="primary" size="small" effect="plain" round class="mr-2">建议</el-tag>
                    {{ mat.name }}
                  </div>
                </div>
              </el-alert>
              <el-alert 
                v-else
                title="必填材料已齐全"
                type="success"
                show-icon
                :closable="false"
              />

              <div class="mt-4">
                <div class="flex items-center justify-between mb-3">
                  <h4 class="font-semibold text-gray-700">已上传材料 ({{ materials.length }})</h4>
                  <el-button 
                    v-if="currentUser && currentUser.userRole !== 'enterprise'"
                    type="primary" 
                    size="small" 
                    round
                    @click="openUpload"
                  >
                    <el-icon><Plus /></el-icon>
                    新增材料
                  </el-button>
                </div>
                <el-table 
                  v-if="materials.length > 0"
                  :data="materials" 
                  border
                  class="rounded-lg overflow-hidden"
                >
                  <el-table-column prop="materialName" label="材料名称" min-width="150" />
                  <el-table-column label="材料类型" width="120">
                    <template #default="{ row }">
                      {{ getMaterialTypeText(row.materialType) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="fileName" label="文件名称" min-width="150" show-overflow-tooltip />
                  <el-table-column prop="uploadUserName" label="上传人" width="100" />
                  <el-table-column label="上传时间" width="160">
                    <template #default="{ row }">
                      {{ formatDateTime(row.uploadTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80" v-if="currentUser && currentUser.userRole !== 'enterprise'">
                    <template #default="{ row }">
                      <el-button 
                        type="danger" 
                        size="small" 
                        link
                        @click="deleteMaterial(row)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-else description="暂无上传材料" />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="保险资料" name="insurance">
            <div class="mt-4">
              <el-descriptions v-if="detail.insuranceCompany || detail.insurancePolicyNo" :column="2" border>
                <el-descriptions-item label="保险公司">{{ detail.insuranceCompany || '-' }}</el-descriptions-item>
                <el-descriptions-item label="保险单号">{{ detail.insurancePolicyNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="保险保额">
                  {{ detail.insuranceCoverage ? '¥' + detail.insuranceCoverage : '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="补充人">{{ detail.laborUserName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="补充时间">{{ formatDateTime(detail.laborProcessTime) }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ detail.insuranceRemark || '-' }}</el-descriptions-item>
              </el-descriptions>
              <el-empty v-else description="劳务公司暂未补充保险资料" />

              <div v-if="currentUser && (currentUser.userRole === 'labor' || currentUser.userRole === 'admin')" class="mt-4">
                <el-button type="primary" round @click="openInsuranceForm">
                  {{ detail.insuranceCompany ? '更新保险资料' : '补充保险资料' }}
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="处理结论" name="conclusion">
            <div class="mt-4">
              <el-card v-if="detail.injuryResult" shadow="never" class="bg-gray-50">
                <div class="space-y-3">
                  <div class="flex items-center">
                    <span class="text-gray-500 w-24">处理结论：</span>
                    <el-tag 
                      :type="detail.injuryResult === 'recovered' ? 'success' : 'warning'"
                      size="large"
                      round
                    >
                      {{ getResultText(detail.injuryResult) }}
                    </el-tag>
                  </div>
                  <div class="flex items-center">
                    <span class="text-gray-500 w-24">结论时间：</span>
                    <span class="text-gray-800">{{ formatDateTime(detail.resultTime) }}</span>
                  </div>
                  <div class="flex items-start" v-if="detail.resultRemark">
                    <span class="text-gray-500 w-24 flex-shrink-0">备注说明：</span>
                    <span class="text-gray-800">{{ detail.resultRemark }}</span>
                  </div>
                  <div class="flex items-center" v-if="detail.workId">
                    <span class="text-gray-500 w-24">排班档案：</span>
                    <el-button type="primary" size="small" link @click="goWork">
                      查看排班 #{{ detail.workId }}（结论已回写）
                    </el-button>
                  </div>
                </div>
              </el-card>

              <div v-else>
                <el-alert 
                  v-if="currentUser && currentUser.userRole === 'admin'"
                  title="请出具处理结论"
                  type="info"
                  show-icon
                  :closable="false"
                  class="mb-4"
                >
                  出具结论后将自动回写到关联的排班档案
                </el-alert>

                <div v-if="currentUser && currentUser.userRole === 'admin'" class="max-w-lg">
                  <el-form label-width="100px">
                    <el-form-item label="处理结论" required>
                      <el-radio-group v-model="conclusionForm.result">
                        <el-radio value="recovered">
                          <span class="text-green-600 font-medium">复工</span>
                        </el-radio>
                        <el-radio value="suspended">
                          <span class="text-orange-600 font-medium">停工</span>
                        </el-radio>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item label="备注说明">
                      <el-input 
                        v-model="conclusionForm.remark" 
                        type="textarea" 
                        :rows="3"
                        placeholder="请输入结论备注（选填）"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button 
                        type="primary" 
                        :disabled="!conclusionForm.result"
                        :loading="concluding"
                        @click="submitConclusion"
                      >
                        确认出具结论
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>

                <el-empty v-else-if="!detail.injuryResult" description="等待管理员出具处理结论" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <el-dialog 
      v-model="uploadVisible" 
      title="上传证明材料" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="材料类型" required>
          <el-select v-model="uploadForm.materialType" placeholder="请选择材料类型" class="w-full">
            <el-option 
              v-for="m in materialTypeOptions" 
              :key="m.value" 
              :label="m.label" 
              :value="m.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="材料名称" required>
          <el-input v-model="uploadForm.materialName" placeholder="请输入材料名称" />
        </el-form-item>
        <el-form-item label="文件名称">
          <el-input v-model="uploadForm.fileName" placeholder="请输入文件名称（模拟上传）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="uploadVisible = false">取消</el-button>
          <el-button type="primary" :loading="uploading" @click="submitUpload">确认上传</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="insuranceVisible" 
      title="补充保险资料" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="insuranceForm" label-width="100px">
        <el-form-item label="保险公司">
          <el-input v-model="insuranceForm.insuranceCompany" placeholder="请输入保险公司名称" />
        </el-form-item>
        <el-form-item label="保险单号">
          <el-input v-model="insuranceForm.insurancePolicyNo" placeholder="请输入保险单号" />
        </el-form-item>
        <el-form-item label="保险保额">
          <el-input-number 
            v-model="insuranceForm.insuranceCoverage" 
            :min="0" 
            :precision="2"
            placeholder="请输入保险金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="insuranceForm.insuranceRemark" 
            type="textarea" 
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="insuranceVisible = false">取消</el-button>
          <el-button type="primary" :loading="insuranceSubmitting" @click="submitInsurance">确认提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { injuryReportApi } from '@/api/injuryReport'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const reportId = route.params.id

const activeTab = ref('basic')
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
const insuranceVisible = ref(false)
const insuranceSubmitting = ref(false)
const concluding = ref(false)

const materialTypeOptions = [
  { label: '现场照片', value: 'scene_photo' },
  { label: '医疗记录/诊断证明', value: 'medical_record' },
  { label: '受伤人员身份证', value: 'id_card' },
  { label: '见证人证言', value: 'witness_statement' },
  { label: '医院收费单据', value: 'hospital_receipt' },
  { label: '保险单', value: 'insurance_policy' },
  { label: '其他', value: 'other' }
]

const uploadForm = reactive({
  materialType: '',
  materialName: '',
  fileName: ''
})

const insuranceForm = reactive({
  insuranceCompany: '',
  insurancePolicyNo: '',
  insuranceCoverage: null,
  insuranceRemark: ''
})

const conclusionForm = reactive({
  result: '',
  remark: ''
})

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
      insuranceForm.insuranceCompany = res.data.insuranceCompany || ''
      insuranceForm.insurancePolicyNo = res.data.insurancePolicyNo || ''
      insuranceForm.insuranceCoverage = res.data.insuranceCoverage || null
      insuranceForm.insuranceRemark = res.data.insuranceRemark || ''
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

const goBack = () => {
  router.push('/pc/injury-report')
}

const goWork = () => {
  router.push('/pc/work')
}

const openUpload = () => {
  uploadForm.materialType = ''
  uploadForm.materialName = ''
  uploadForm.fileName = ''
  uploadVisible.value = true
}

const submitUpload = async () => {
  if (!uploadForm.materialType || !uploadForm.materialName) {
    ElMessage.warning('请填写完整信息')
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
      ElMessage.success('上传成功')
      uploadVisible.value = false
      loadMaterials()
      loadMissingMaterials()
      loadDetail()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const deleteMaterial = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该材料吗？', '提示', { type: 'warning' })
    const res = await injuryReportApi.deleteMaterial(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
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
      insuranceCoverage: insuranceForm.insuranceCoverage,
      insuranceRemark: insuranceForm.insuranceRemark
    })
    if (res.code === 200) {
      ElMessage.success('保险资料补充成功')
      insuranceVisible.value = false
      loadDetail()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('提交失败')
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
      ElMessage.success('结论已出具，排班档案已同步更新')
      loadDetail()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  } finally {
    concluding.value = false
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

const getMaterialTypeText = (type) => {
  const opt = materialTypeOptions.find(o => o.value === type)
  return opt ? opt.label : type
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
  max-width: 1400px;
  margin: 0 auto;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-descriptions) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
