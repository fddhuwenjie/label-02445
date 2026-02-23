<template>
  <div class="dashboard">
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeClubs || 0 }}</div>
          <div class="stat-label">活跃社团</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.ongoingActivities || 0 }}</div>
          <div class="stat-label">进行中活动</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingApplications || 0 }}</div>
          <div class="stat-label">待审核申请</div>
        </div>
      </div>
    </div>
    
    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="12">
        <div class="page-card dashboard-card">
          <div class="card-header">
            <span class="card-title">最新公告</span>
            <el-button text type="primary" @click="$router.push('/announcements')">查看更多</el-button>
          </div>
          <el-table :data="announcements" style="width: 100%" height="220">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="createdAt" label="发布时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card dashboard-card">
          <div class="card-header">
            <span class="card-title">最新活动</span>
            <el-button text type="primary" @click="$router.push('/activities')">查看更多</el-button>
          </div>
          <el-table :data="activities" style="width: 100%" height="220">
            <el-table-column prop="title" label="活动名称" />
            <el-table-column prop="clubName" label="所属社团" width="120" />
            <el-table-column prop="startTime" label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.startTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
    
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">社团列表</span>
        <el-button text type="primary" @click="$router.push('/clubs')">查看更多</el-button>
      </div>
      <el-table :data="clubs" style="width: 100%">
        <el-table-column prop="name" label="社团名称" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'">
              {{ row.status === 1 ? '正常' : row.status === 0 ? '待审核' : '已解散' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import dayjs from 'dayjs'

const stats = ref({})
const announcements = ref([])
const activities = ref([])
const clubs = ref([])

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

const fetchData = async () => {
  try {
    const [statsRes, announcementsRes, activitiesRes, clubsRes] = await Promise.all([
      api.get('/api/statistics/overview'),
      api.get('/api/announcements/list', { params: { page: 1, size: 5 } }),
      api.get('/api/activities/list', { params: { page: 1, size: 5 } }),
      api.get('/api/clubs/list', { params: { page: 1, size: 5, status: 1 } })
    ])
    stats.value = statsRes.data
    announcements.value = announcementsRes.data.records
    activities.value = activitiesRes.data.records
    clubs.value = clubsRes.data.records
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.dashboard-row {
  align-items: stretch;
}

.dashboard-card {
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

.dashboard-card .card-header {
  flex-shrink: 0;
}

.dashboard-card :deep(.el-table) {
  flex: 1;
}

.dashboard-card :deep(.el-table__inner-wrapper) {
  height: 100%;
}

.dashboard-card :deep(.el-table__body-wrapper) {
  height: calc(100% - 40px);
}
</style>
