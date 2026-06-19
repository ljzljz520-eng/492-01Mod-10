import axios from 'axios'
import { ElMessage } from 'element-plus'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

request.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      localStorage.removeItem('user')
      const isMobile = window.innerWidth <= 767
      const prefix = isMobile ? '/h5' : '/pc'
      if (!window.location.pathname.includes('/login')) {
        if (isMobile) {
          showToast({ message: '登录已过期，请重新登录', type: 'fail' })
        } else {
          ElMessage.error('登录已过期，请重新登录')
        }
        setTimeout(() => {
          window.location.href = prefix + '/login'
        }, 1500)
      }
      return Promise.reject(new Error(res.message || '未登录'))
    } else {
      const isMobile = window.innerWidth <= 767
      if (isMobile) {
        showToast({
          message: res.message || '请求失败',
          type: 'fail'
        })
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('user')
      const isMobile = window.innerWidth <= 767
      const prefix = isMobile ? '/h5' : '/pc'
      if (!window.location.pathname.includes('/login')) {
        if (isMobile) {
          showToast({ message: '登录已过期，请重新登录', type: 'fail' })
        } else {
          ElMessage.error('登录已过期，请重新登录')
        }
        setTimeout(() => {
          window.location.href = prefix + '/login'
        }, 1500)
      }
      return Promise.reject(new Error('未登录'))
    }
    const isMobile = window.innerWidth <= 767
    const message = error.message || '网络错误'
    if (isMobile) {
      showToast({
        message: message,
        type: 'fail'
      })
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
