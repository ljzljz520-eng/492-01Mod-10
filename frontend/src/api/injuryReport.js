import request from '@/utils/request'

export const injuryReportApi = {
  save(data, userId) {
    return request({
      url: '/injury-report',
      method: 'post',
      data,
      params: userId ? { userId } : {}
    })
  },
  update(id, data, userId) {
    return request({
      url: `/injury-report/${id}`,
      method: 'put',
      data,
      params: userId ? { userId } : {}
    })
  },
  getById(id) {
    return request({
      url: `/injury-report/${id}`,
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/injury-report/page',
      method: 'get',
      params
    })
  },
  checkMissingMaterials(id) {
    return request({
      url: `/injury-report/${id}/check-materials`,
      method: 'get'
    })
  },
  updateInsurance(id, data, userId) {
    return request({
      url: `/injury-report/${id}/insurance`,
      method: 'put',
      data,
      params: userId ? { userId } : {}
    })
  },
  processConclusion(id, result, resultRemark, userId) {
    return request({
      url: `/injury-report/${id}/conclusion`,
      method: 'put',
      params: { result, resultRemark, userId }
    })
  },
  getMaterials(id) {
    return request({
      url: `/injury-report/${id}/materials`,
      method: 'get'
    })
  },
  addMaterial(data, userId) {
    return request({
      url: '/injury-report/material',
      method: 'post',
      data,
      params: userId ? { userId } : {}
    })
  },
  deleteMaterial(id) {
    return request({
      url: `/injury-report/material/${id}`,
      method: 'delete'
    })
  }
}
