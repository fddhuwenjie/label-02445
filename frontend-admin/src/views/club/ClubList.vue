<template>
  <div class="club-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">社团管理</span>
        <el-button type="primary" @click="$router.push('/clubs/create')">
          <el-icon><Plus /></el-icon>创建社团
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索社团名称" style="width: 200px" clearable @clear="fetchData" @keyup.enter="fetchData" />
        <el-select v-model="status" placeholder="状态" style="width: 120px" clearable @change="fetchData">
          <el-option label="待审核" :value="0" />
          <el-option label="正常" :value="1" />
          <el-option label="已解散" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>
      
      <el-table :data="clubs" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="社团名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/clubs/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="leaderName" label="负责人" width="100" />
        <el-table-column prop="memberCount" label="成员数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'">
              {{ row.status === 1 ? '正常' : row.status === 0 ? '待审核' : '已解散' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="row.status === 0 && userStore.user?.role === 'ADMIN'" type="success" size="small" @click="handleAudit(row.id, 1)">通过</el-button>
              <el-button v-if="row.status === 0 && userStore.user?.role === 'ADMIN'" type="danger" size="small" @click="handleAudit(row.id, 2)">拒绝</el-button>
              <el-button type="primary" size="small" @click="$router.push(`/clubs/${row.id}/edit`)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
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
import { useUserStore } from '../../stores/user'
import api from '../../api'
import dayjs from 'dayjs'

const userStore = useUserStore()

const clubs = ref([])
const loading = ref(false)
const keyword = ref('')
const status = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/clubs/list', {
      params: { page: page.value, size: size.value, keyword: keyword.value, status: status.value }
    })
    clubs.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAudit = async (id, status) => {
  await api.put(`/api/clubs/${id}/audit`, null, { params: { status } })
  ElMessage.success('操作成功')
  fetchData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定要删除该社团吗？', '提示', { type: 'warning' })
  await api.delete(`/api/clubs/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>
