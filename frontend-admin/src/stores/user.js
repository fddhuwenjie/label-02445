import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  
  const login = async (username, password) => {
    const res = await api.post('/api/auth/login', { username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }
  
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  const fetchUserInfo = async () => {
    const res = await api.get('/api/users/info')
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
    return res.data
  }
  
  return {
    token,
    user,
    login,
    logout,
    fetchUserInfo
  }
})
