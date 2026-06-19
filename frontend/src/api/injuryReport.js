import request from '@/utils/request'

export const injuryReportApi = {
  save(data) {
    return request({
      url: '/injury-report',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/injury-report/${id}`,
      method: 'put',
      data
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
  updateInsurance(id, data) {
    return request({
      url: `/injury-report/${id}/insurance`,
      method: 'put',
      data
    })
  },
  processConclusion(id, result, resultRemark) {
    return request({
      url: `/injury-report/${id}/conclusion`,
      method: 'put',
      params: { result, resultRemark }
    })
  },
  getMaterials(id) {
    return request({
      url: `/injury-report/${id}/materials`,
      method: 'get'
    })
  },
  addMaterial(data) {
    return request({
      url: '/injury-report/material',
      method: 'post',
      data
    })
  },
  deleteMaterial(id) {
    return request({
      url: `/injury-report/material/${id}`,
      method: 'delete'
    })
  }
}
