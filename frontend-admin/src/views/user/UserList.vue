<template>
  <div class="user-list">
    <div class="page-card">
      <div class="card-header">
        <span class="card-title">用户管理</span>
        <el-button type="primary" @click="handleAdd">添加用户</el-button>
      </div>
      
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索用户名/姓名/学号" style="width: 250px" clearable @keyup.enter="fetchData" />
        <el-select v-model="role" placeholder="角色" style="width: 120px" clearable @change="fetchData">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="社团负责人" value="LEADER" />
          <el-option label="学生" value="STUDENT" />
        </el-select>
        <el-select v-model="status" placeholder="状态" style="width: 120px" clearable @change="fetchData">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>
      
      <el-table :data="users" v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="studentId" label="学号" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'LEADER' ? 'warning' : ''">
              {{ row.role === 'ADMIN' ? '管理员' : row.role === 'LEADER' ? '社团负责人' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1 && row.role !== 'ADMIN'" type="warning" size="small" link @click="handleStatus(row.id, 0)">禁用</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" link @click="handleStatus(row.id, 1)">启用</el-button>
            <el-button v-if="row.role !== 'ADMIN'" type="danger" size="small" link @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 添加用户对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加用户" width="550px">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="addForm.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="社团负责人" value="LEADER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="550px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="editForm.studentId" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="社团负责人" value="LEADER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="重置密码">
          <el-input v-model="editForm.newPassword" type="password" placeholder="留空则不修改密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看用户详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="用户详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ viewData.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewData.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ viewData.studentId }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="viewData.role === 'ADMIN' ? 'danger' : viewData.role === 'LEADER' ? 'warning' : ''">
            {{ viewData.role === 'ADMIN' ? '管理员' : viewData.role === 'LEADER' ? '社团负责人' : '学生' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ viewData.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">
            {{ viewData.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(viewData.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import dayjs from 'dayjs'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const role = ref('')
const status = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 添加用户
const addDialogVisible = ref(false)
const addFormRef = ref()
const addLoading = ref(false)
const addForm = reactive({
  username: '', password: '', realName: '', studentId: '', email: '', phone: '', role: 'STUDENT'
})
const addRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 编辑用户
const editDialogVisible = ref(false)
const editFormRef = ref()
const editLoading = ref(false)
const editForm = reactive({
  id: null, username: '', realName: '', studentId: '', email: '', phone: '', role: '', newPassword: ''
})
const editRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 查看详情
const viewDialogVisible = ref(false)
const viewData = ref({})

const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/users/list', {
      params: { page: page.value, size: size.value, keyword: keyword.value, role: role.value, status: status.value }
    })
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(addForm, { username: '', password: '', realName: '', studentId: '', email: '', phone: '', role: 'STUDENT' })
  addDialogVisible.value = true
}

const submitAdd = async () => {
  await addFormRef.value.validate()
  addLoading.value = true
  try {
    await api.post('/api/users/add', addForm)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    fetchData()
  } finally {
    addLoading.value = false
  }
}

const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    studentId: row.studentId,
    email: row.email,
    phone: row.phone,
    role: row.role,
    newPassword: ''
  })
  editDialogVisible.value = true
}

const submitEdit = async () => {
  await editFormRef.value.validate()
  editLoading.value = true
  try {
    await api.put('/api/users/update', editForm)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    fetchData()
  } finally {
    editLoading.value = false
  }
}

const handleStatus = async (id, status) => {
  await api.put(`/api/users/${id}/status`, null, { params: { status } })
  ElMessage.success('操作成功')
  fetchData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
  await api.delete(`/api/users/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>
