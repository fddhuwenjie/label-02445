<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-icon">社</div>
        <span>社团管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="menu"
        background-color="transparent"
        text-color="#a0aec0"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/clubs">
          <el-icon><OfficeBuilding /></el-icon>
          <span>{{ isAdmin ? '社团管理' : '社团列表' }}</span>
        </el-menu-item>
        <el-menu-item index="/my-memberships">
          <el-icon><Tickets /></el-icon>
          <span>我的社团</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin || hasManagedClubs" index="/members">
          <el-icon><User /></el-icon>
          <span>成员管理</span>
        </el-menu-item>
        <el-menu-item index="/activities">
          <el-icon><Calendar /></el-icon>
          <span>{{ isAdmin ? '活动管理' : '活动列表' }}</span>
        </el-menu-item>
        <el-menu-item index="/announcements">
          <el-icon><Bell /></el-icon>
          <span>{{ isAdmin ? '公告管理' : '公告列表' }}</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </aside>
    
    <main class="main-container">
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <div class="avatar">{{ userStore.user?.realName?.charAt(0) || 'U' }}</div>
              <span>{{ userStore.user?.realName || userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const hasManagedClubs = ref(false)

const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

const checkManagedClubs = async () => {
  if (isAdmin.value) {
    hasManagedClubs.value = true
    return
  }
  try {
    const res = await api.get('/api/memberships/my', { params: { page: 1, size: 100, status: 1 } })
    hasManagedClubs.value = res.data.records.some(m => m.role === 'LEADER' || m.role === 'ADMIN')
  } catch {
    hasManagedClubs.value = false
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(checkManagedClubs)
</script>

<style scoped>
.menu :deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 8px;
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #409eff, #66b1ff) !important;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
}
</style>
