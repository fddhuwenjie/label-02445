<template>
  <div class="activity-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">活动管理</span>
        <el-button type="primary" @click="$router.push('/activities/create')">
          <el-icon><Plus /></el-icon>创建活动
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索活动名称" style="width: 200px" clearable @keyup.enter="fetchData" />
        <el-select v-model="clubId" placeholder="选择社团" style="width: 200px" clearable @change="fetchData">
          <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
        </el-select>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="$router.push(`/activities/${row.id}/edit`)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import dayjs from 'dayjs'

const activities = ref([])
const clubs = ref([])
const loading = ref(false)
const keyword = ref('')
const clubId = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: '' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 0: '已取消', 1: '报名中', 2: '进行中', 3: '已结束' }
  return texts[status] || ''
}

const fetchClubs = async () => {
  const res = await api.get('/api/clubs/list', { params: { page: 1, size: 100, status: 1 } })
  clubs.value = res.data.records
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/activities/list', {
      params: { page: page.value, size: size.value, keyword: keyword.value, clubId: clubId.value }
    })
    activities.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定要删除该活动吗？', '提示', { type: 'warning' })
  await api.delete(`/api/activities/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => {
  fetchClubs()
  fetchData()
})
</script>
