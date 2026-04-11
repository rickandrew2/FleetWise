import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  AUTH_EXPIRED_EVENT,
  clearToken,
  getToken,
  loginRequest,
  meRequest,
  parseApiError,
  setToken,
} from '@/lib/api'
import { notifyInfo } from '@/lib/notify'
import type { ApiErrorResponse, MeResponse } from '@/types/api'

interface LoginPayload {
  email: string
  password: string
}

interface AuthContextValue {
  user: MeResponse | null
  isBootstrapping: boolean
  isAuthenticated: boolean
  login: (payload: LoginPayload) => Promise<void>
  logout: () => void
  error: ApiErrorResponse | null
  clearError: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<MeResponse | null>(null)
  const [isBootstrapping, setIsBootstrapping] = useState(true)
  const [error, setError] = useState<ApiErrorResponse | null>(null)

  const clearAuthState = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  const bootstrap = useCallback(async () => {
    const token = getToken()

    if (!token) {
      setIsBootstrapping(false)
      return
    }

    try {
      const me = await meRequest()
      setUser(me)
    } catch {
      clearAuthState()
    } finally {
      setIsBootstrapping(false)
    }
  }, [clearAuthState])

  useEffect(() => {
    void bootstrap()
  }, [bootstrap])

  useEffect(() => {
    const handleAuthExpired = () => {
      if (user) {
        notifyInfo('Your session expired. Please sign in again.')
      }
      clearAuthState()
    }

    window.addEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
  }, [clearAuthState, user])

  const login = useCallback(async (payload: LoginPayload) => {
    setError(null)
    const auth = await loginRequest(payload)
    setToken(auth.token)

    try {
      const me = await meRequest()
      setUser(me)
    } catch (error) {
      clearAuthState()
      const parsedError = parseApiError(error)
      setError(parsedError)
      throw error
    }
  }, [clearAuthState])

  const logout = useCallback(() => {
    clearAuthState()
    notifyInfo('You have been logged out.')
  }, [clearAuthState])

  const clearError = useCallback(() => {
    setError(null)
  }, [])

  const contextValue = useMemo<AuthContextValue>(
    () => ({
      user,
      isBootstrapping,
      isAuthenticated: Boolean(user),
      login,
      logout,
      error,
      clearError,
    }),
    [clearError, error, isBootstrapping, login, logout, user],
  )

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
