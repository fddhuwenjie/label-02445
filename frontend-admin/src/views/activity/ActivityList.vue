<template>
  <div class="activity-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">{{ isAdmin ? '活动管理' : '活动列表' }}</span>
        <el-button v-if="canCreateActivity" type="primary" @click="openCreateDialog">
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
      
      <el-table :data="activities" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="活动名称" min-width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="openDetailDialog(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="clubName" label="所属社团" min-width="100" />
        <el-table-column prop="location" label="地点" min-width="100" />
        <el-table-column label="时间" min-width="150">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="报名人数" min-width="100">
          <template #default="{ row }">
            {{ row.currentParticipants }} / {{ row.maxParticipants || '不限' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <div class="op-btns">
              <el-button v-if="row.status === 1 && !row.registered" type="success" size="small" @click="handleRegister(row)" :loading="row.registering">报名</el-button>
              <el-button v-if="row.status === 1 && row.registered" type="warning" size="small" @click="handleCancelRegister(row)" :loading="row.canceling">取消</el-button>
              <el-button v-if="hasViewRegistrationsPermission(row)" type="info" size="small" @click="showRegistrations(row)">名单</el-button>
              <el-button v-if="hasEditActivityPermission(row)" type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button v-if="hasDeleteActivityPermission(row)" type="danger" size="small" plain @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 创建/编辑活动弹窗 -->
    <el-dialog v-model="formVisible" :title="activityFormId ? '编辑活动' : '创建活动'" width="520px" destroy-on-close @close="resetActivityForm">
      <el-form ref="activityFormRef" :model="activityForm" :rules="activityFormRules" label-width="90px">
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="activityForm.clubId" placeholder="请选择社团" style="width: 100%" :disabled="!!activityFormId">
            <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="activityForm.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="activityForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="activityForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="activityForm.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数限制" prop="maxParticipants">
          <el-input-number v-model="activityForm.maxParticipants" :min="0" placeholder="0表示不限" />
          <span style="margin-left: 8px; color: #909399; font-size: 12px;">0=不限</span>
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="activityForm.description" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="activityFormLoading" @click="submitActivityForm">{{ activityFormId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 活动详情弹窗 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="520px" destroy-on-close>
      <div v-if="detailActivity" class="activity-detail-content">
        <h3>{{ detailActivity.title }}</h3>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="所属社团">{{ detailActivity.clubName }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ detailActivity.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ formatDate(detailActivity.startTime) }} ~ {{ formatDate(detailActivity.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="报名人数">{{ detailActivity.currentParticipants }} / {{ detailActivity.maxParticipants || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailActivity.status)">{{ getStatusText(detailActivity.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <p v-if="detailActivity.description" class="detail-desc">{{ detailActivity.description }}</p>
        <div v-if="detailActivity.status === 1" class="detail-actions">
          <el-button v-if="!detailActivity.registered" type="primary" size="small" @click="detailRegister" :loading="detailRegistering">报名</el-button>
          <el-button v-else type="warning" size="small" @click="detailCancelRegister" :loading="detailCanceling">取消报名</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import api from '../../api'
import dayjs from 'dayjs'

const userStore = useUserStore()

const activities = ref([])
const clubs = ref([])
const myClubs = ref([])
const loading = ref(false)
const keyword = ref('')
const clubId = ref(null)
const managedOnly = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const myManagedClubIds = ref([])

const formVisible = ref(false)
const activityFormRef = ref()
const activityFormId = ref(null)
const activityFormLoading = ref(false)
const activityForm = reactive({ id: null, clubId: null, title: '', location: '', startTime: null, endTime: null, maxParticipants: 0, description: '' })
const activityFormRules = {
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const detailVisible = ref(false)
const detailActivity = ref(null)
const detailRegistering = ref(false)
const detailCanceling = ref(false)

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

const hasViewRegistrationsPermission = (activity) => {
  if (isAdmin.value) return true
  return myManagedClubIds.value.includes(activity.clubId)
}

const hasEditActivityPermission = (activity) => {
  if (isAdmin.value) return true
  return myManagedClubIds.value.includes(activity.clubId)
}

const hasDeleteActivityPermission = (activity) => {
  if (isAdmin.value) return true
  return myManagedClubIds.value.includes(activity.clubId)
}

const fetchMyManagedClubs = async () => {
  if (isAdmin.value) return
  try {
    const res = await api.get('/api/memberships/my', { params: { page: 1, size: 100, status: 1 } })
    myManagedClubIds.value = res.data.records
      .filter(m => m.role === 'ADMIN')
      .map(m => m.clubId)
  } catch {
    myManagedClubIds.value = []
  }
}

const fetchClubs = async () => {
  const res = await api.get('/api/clubs/list', { params: { page: 1, size: 100, status: 1 } })
  clubs.value = res.data.records
}

const fetchMyClubs = async () => {
  const res = await api.get('/api/clubs/my')
  myClubs.value = res.data
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

const openCreateDialog = () => {
  activityFormId.value = null
  resetActivityForm()
  formVisible.value = true
}

const openEditDialog = async (row) => {
  activityFormId.value = row.id
  try {
    const res = await api.get(`/api/activities/detail/${row.id}`)
    Object.assign(activityForm, {
      id: res.data.id, clubId: res.data.clubId, title: res.data.title, location: res.data.location || '',
      startTime: res.data.startTime, endTime: res.data.endTime, maxParticipants: res.data.maxParticipants || 0,
      description: res.data.description || ''
    })
  } catch {
    Object.assign(activityForm, { id: row.id, clubId: row.clubId, title: row.title, location: row.location || '', startTime: row.startTime, endTime: row.endTime, maxParticipants: row.maxParticipants || 0, description: '' })
  }
  formVisible.value = true
}

const resetActivityForm = () => {
  Object.assign(activityForm, { id: null, clubId: null, title: '', location: '', startTime: null, endTime: null, maxParticipants: 0, description: '' })
  activityFormRef.value?.resetFields?.()
}

const submitActivityForm = async () => {
  await activityFormRef.value.validate()
  activityFormLoading.value = true
  try {
    if (activityFormId.value) {
      await api.put('/api/activities', activityForm)
      ElMessage.success('保存成功')
    } else {
      await api.post('/api/activities', activityForm)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    fetchData()
  } finally {
    activityFormLoading.value = false
  }
}

const openDetailDialog = async (row) => {
  detailVisible.value = true
  detailActivity.value = null
  try {
    const res = await api.get(`/api/activities/detail/${row.id}`)
    detailActivity.value = res.data
  } catch {
    ElMessage.error('加载失败')
  }
}

const detailRegister = async () => {
  if (!detailActivity.value) return
  detailRegistering.value = true
  try {
    await api.post(`/api/activities/${detailActivity.value.id}/register`)
    ElMessage.success('报名成功')
    detailActivity.value.registered = true
    detailActivity.value.currentParticipants = (detailActivity.value.currentParticipants || 0) + 1
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '报名失败')
  } finally {
    detailRegistering.value = false
  }
}

const detailCancelRegister = async () => {
  if (!detailActivity.value) return
  detailCanceling.value = true
  try {
    await api.post(`/api/activities/${detailActivity.value.id}/cancel`)
    ElMessage.success('已取消报名')
    detailActivity.value.registered = false
    detailActivity.value.currentParticipants = Math.max(0, (detailActivity.value.currentParticipants || 1) - 1)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '取消失败')
  } finally {
    detailCanceling.value = false
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
  if (canCreateActivity.value) fetchMyClubs()
  fetchData()
})
</script>

<style scoped>
.op-btns { display: flex; flex-wrap: wrap; gap: 4px; }
.activity-detail-content h3 { margin: 0 0 12px 0; }
.activity-detail-content .detail-desc { margin: 12px 0; color: #606266; line-height: 1.6; }
.activity-detail-content .detail-actions { margin-top: 12px; }
</style>

