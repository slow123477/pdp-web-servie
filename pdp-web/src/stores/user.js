import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref(localStorage.getItem('token') || sessionStorage.getItem('token') || '')
    const userInfo = ref(
      JSON.parse(localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo') || 'null')
    )

    const isLoggedIn = computed(() => !!token.value)

    function setToken(newToken, remember = false) {
      token.value = newToken
      if (remember) {
        localStorage.setItem('token', newToken)
        sessionStorage.removeItem('token')
      } else {
        sessionStorage.setItem('token', newToken)
        localStorage.removeItem('token')
      }
    }

    function setUserInfo(info, remember = false) {
      userInfo.value = info
      const data = JSON.stringify(info)
      if (remember) {
        localStorage.setItem('userInfo', data)
        sessionStorage.removeItem('userInfo')
      } else {
        sessionStorage.setItem('userInfo', data)
        localStorage.removeItem('userInfo')
      }
    }

    function logout() {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
    }

    return {
      token,
      userInfo,
      isLoggedIn,
      setToken,
      setUserInfo,
      logout,
    }
  },
  {
    persist: false,
  }
)
