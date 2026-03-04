<template>
  <div class="activity-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">{{ isAdmin ? '活动管理' : '活动列表' }}</span>
        <el-button v-if="canCreateActivity" type="primary" @click="$router.push('/activities/create')">
          <el-icon><Plus /></el-icon>创建活动
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索活动名称" style="width: 200px" clearable @keyup.enter="fetchData" />
        <el-select v-model="clubId" placeholder="选择社团" style="width: 200px" clearable @change="fetchData" :disabled="managedOnly">
          <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
        </el-select>
        <el-checkbox v-if="!isAdmin && myManagedClubIds.length > 0" v-model="managedOnly" @change="onManagedOnlyChange">
          仅显示我管理的社团活动
        </el-checkbox>
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>
      
      <el-table :data="activities" v-loading="loading">
        <el-table-column prop="title" label="活动名称" min-width="150" />
        <el-table-column prop="clubName" label="所属社团" width="120" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="报名人数" width="100">
          <template #default="{ row }">
            {{ row.currentParticipants }} / {{ row.maxParticipants || '不限' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="row.status === 1 && !row.registered" type="success" size="small" @click="handleRegister(row)" :loading="row.registering">报名</el-button>
              <el-button v-if="row.status === 1 && row.registered" type="warning" size="small" @click="handleCancelRegister(row)" :loading="row.canceling">取消报名</el-button>
              <el-button v-if="canManageActivity(row)" type="info" size="small" @click="showRegistrations(row)">报名列表</el-button>
              <el-button v-if="canManageActivity(row)" type="primary" size="small" @click="$router.push(`/activities/${row.id}/edit`)">编辑</el-button>
              <el-button v-if="canManageActivity(row)" type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @change="fetchData"
      />
    </div>
    
    <!-- 报名列表弹窗 -->
    <el-dialog v-model="registrationsVisible" :title="`${currentActivity?.title} - 报名列表`" width="600px">
      <el-table :data="registrations" v-loading="registrationsLoading">
        <el-table-column prop="userRealName" label="姓名" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="registeredAt" label="报名时间">
          <template #default="{ row }">
            {{ formatDate(row.registeredAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已报名' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="regPage"
        v-model:page-size="regSize"
        :total="regTotal"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @change="fetchRegistrations"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import api from '../../api'
import dayjs from 'dayjs'

const userStore = useUserStore()

const activities = ref([])
const clubs = ref([])
const loading = ref(false)
const keyword = ref('')
const clubId = ref(null)
const managedOnly = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const myManagedClubIds = ref([])

const registrationsVisible = ref(false)
const currentActivity = ref(null)
const registrations = ref([])
const registrationsLoading = ref(false)
const regPage = ref(1)
const regSize = ref(10)
const regTotal = ref(0)

const isAdmin = computed(() => userStore.user?.role === 'ADMIN')
const canCreateActivity = computed(() => isAdmin.value || myManagedClubIds.value.length > 0)

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: '' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 0: '已取消', 1: '报名中', 2: '进行中', 3: '已结束' }
  return texts[status] || ''
}

const canManageActivity = (activity) => {
  if (isAdmin.value) return true
  return myManagedClubIds.value.includes(activity.clubId)
}

const fetchMyManagedClubs = async () => {
  if (isAdmin.value) return
  try {
    const res = await api.get('/api/memberships/my', { params: { page: 1, size: 100, status: 1 } })
    myManagedClubIds.value = res.data.records
      .filter(m => m.role === 'LEADER' || m.role === 'ADMIN')
      .map(m => m.clubId)
  } catch {
    myManagedClubIds.value = []
  }
}

const fetchClubs = async () => {
  const res = await api.get('/api/clubs/list', { params: { page: 1, size: 100, status: 1 } })
  clubs.value = res.data.records
}

const onManagedOnlyChange = () => {
  if (managedOnly.value) clubId.value = null
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, keyword: keyword.value, clubId: clubId.value }
    if (managedOnly.value) params.managedOnly = true
    const res = await api.get('/api/activities/list', { params })
    const activityList = res.data.records
    for (const activity of activityList) {
      try {
        const detailRes = await api.get(`/api/activities/detail/${activity.id}`)
        activity.registered = detailRes.data.registered
      } catch {
        activity.registered = false
      }
    }
    activities.value = activityList
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleRegister = async (activity) => {
  activity.registering = true
  try {
    await api.post(`/api/activities/${activity.id}/register`)
    ElMessage.success('报名成功')
    activity.registered = true
    activity.currentParticipants = (activity.currentParticipants || 0) + 1
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '报名失败')
  } finally {
    activity.registering = false
  }
}

const handleCancelRegister = async (activity) => {
  activity.canceling = true
  try {
    await api.post(`/api/activities/${activity.id}/cancel`)
    ElMessage.success('已取消报名')
    activity.registered = false
    activity.currentParticipants = Math.max(0, (activity.currentParticipants || 1) - 1)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '取消失败')
  } finally {
    activity.canceling = false
  }
}

const showRegistrations = async (activity) => {
  currentActivity.value = activity
  registrationsVisible.value = true
  regPage.value = 1
  await fetchRegistrations()
}

const fetchRegistrations = async () => {
  if (!currentActivity.value) return
  registrationsLoading.value = true
  try {
    const res = await api.get(`/api/activities/${currentActivity.value.id}/registrations`, {
      params: { page: regPage.value, size: regSize.value }
    })
    registrations.value = res.data.records
    regTotal.value = res.data.total
  } catch (e) {
    ElMessage.error('获取报名列表失败')
  } finally {
    registrationsLoading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定要删除该活动吗？', '提示', { type: 'warning' })
  await api.delete(`/api/activities/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(async () => {
  await fetchMyManagedClubs()
  fetchClubs()
  fetchData()
})
</script>
