import request from '@/utils/request'

export const projectApi = {
  save(data) {
    return request({
      url: '/project',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/project/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/project/${id}`,
      method: 'delete'
    })
  },
  getById(id) {
    return request({
      url: `/project/${id}`,
      method: 'get'
    })
  },
  list() {
    return request({
      url: '/project/list',
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/project/page',
      method: 'get',
      params
    })
  }
}
