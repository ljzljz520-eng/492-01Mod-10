<template>
  <div class="h5-layout min-h-screen bg-gray-50 pb-16">
    <van-nav-bar
      :title="navTitle"
      fixed
      placeholder
      class="bg-white shadow-sm"
    >
      <template #right>
        <van-button size="small" type="primary" @click="switchToPC">
          切换到PC端
        </van-button>
      </template>
    </van-nav-bar>
    <div class="px-4 py-2">
      <router-view :key="route.path" />
    </div>
    <van-tabbar v-model="activeTab" route fixed placeholder>
      <van-tabbar-item icon="description-o" to="/h5/injury-report">工伤上报</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/h5/work">排班档案</van-tabbar-item>
      <van-tabbar-item icon="apartment-o" to="/h5/project">项目</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/h5/user">用户</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeTab = ref(0)

const navTitle = computed(() => {
  const titles = {
    '/h5/injury-report': '工伤上报',
    '/h5/work': '排班档案',
    '/h5/project': '项目管理',
    '/h5/file': '文件管理',
    '/h5/user': '用户管理'
  }
  if (route.path.startsWith('/h5/injury-report/')) {
    return '工伤详情'
  }
  return titles[route.path] || '蓝领招聘排班平台'
})

const switchToPC = () => {
  const currentPath = route.path
  const pcPath = currentPath.replace('/h5', '/pc')
  router.push(pcPath)
}
</script>

<style scoped>
.h5-layout {
  padding-bottom: 50px;
}
</style>
