<template>
  <div class="my-memberships">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">我的社团</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-change="fetchData">
        <el-tab-pane label="已加入" name="joined" />
        <el-tab-pane label="申请中" name="pending" />
        <el-tab-pane label="历史记录" name="history" />
      </el-tabs>
      
      <el-table :data="memberships" v-loading="loading">
        <el-table-column prop="clubName" label="社团名称">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/clubs/${row.clubId}`)">{{ row.clubName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="我的角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'LEADER' ? 'danger' : row.role === 'ADMIN' ? 'warning' : ''">
              {{ row.role === 'LEADER' ? '负责人' : row.role === 'ADMIN' ? '管理员' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinedAt" label="加入时间">
          <template #default="{ row }">
            {{ row.joinedAt ? formatDate(row.joinedAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === 1 && row.role !== 'LEADER'" type="danger" size="small" @click="handleQuit(row.clubId)">退出社团</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import dayjs from 'dayjs'

const memberships = ref([])
const loading = ref(false)
const activeTab = ref('joined')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已加入', 2: '已拒绝', 3: '已退出' }
  return texts[status] || ''
}

const getStatusParam = () => {
  switch (activeTab.value) {
    case 'joined': return 1
    case 'pending': return 0
    case 'history': return null
    default: return 1
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/memberships/my', {
      params: { page: page.value, size: size.value, status: getStatusParam() }
    })
    memberships.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuit = async (clubId) => {
  await ElMessageBox.confirm('确定要退出该社团吗？', '提示', { type: 'warning' })
  try {
    await api.post(`/api/memberships/quit/${clubId}`)
    ElMessage.success('已退出社团')
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '退出失败')
  }
}

onMounted(fetchData)
</script>
