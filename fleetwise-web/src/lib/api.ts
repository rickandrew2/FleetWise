import axios, { AxiosError } from 'axios'
import { z } from 'zod'
import { env } from '@/lib/env'
import type {
  ApiErrorResponse,
  AuthResponse,
  CostTrendPointResponse,
  DashboardSummaryResponse,
  LoginRequest,
  MeResponse,
  TopDriverResponse,
  VehicleResponse,
  VehicleUpsertRequest,
} from '@/types/api'

const TOKEN_KEY = 'FLEETWISE_TOKEN'
export const AUTH_EXPIRED_EVENT = 'fleetwise:auth-expired'

const authResponseSchema = z.object({
  token: z.string().min(1),
  expiresInMs: z.number().int().positive(),
  email: z.string().email(),
  role: z.enum(['ADMIN', 'FLEET_MANAGER', 'DRIVER']),
})

const meResponseSchema = z.object({
  status: z.literal('authenticated'),
  email: z.string().email(),
  role: z.enum(['ADMIN', 'FLEET_MANAGER', 'DRIVER']),
  userId: z.string().uuid(),
})

const dashboardSummarySchema = z.object({
  monthToDateFuelCost: z.number(),
  fleetEfficiencyScore: z.number().nullable(),
  activeAlertsCount: z.number().int(),
})

const topDriverSchema = z.object({
  driverId: z.string().uuid(),
  driverName: z.string(),
  averageEfficiencyScore: z.number().nullable(),
  routeCount: z.number().int(),
})

const costTrendPointSchema = z.object({
  month: z.string().regex(/^\d{4}-\d{2}$/),
  totalCost: z.number(),
})

const vehicleSchema = z.object({
  id: z.string().uuid(),
  plateNumber: z.string(),
  make: z.string(),
  model: z.string(),
  year: z.number().int(),
  fuelType: z.string().nullable(),
  tankCapacityLiters: z.number().nullable(),
  epaVehicleId: z.number().int().nullable(),
  combinedMpg: z.number().nullable(),
  cityMpg: z.number().nullable(),
  highwayMpg: z.number().nullable(),
  assignedDriverId: z.string().uuid().nullable(),
  createdAt: z.string(),
})

const apiErrorPayloadSchema = z.object({
  status: z.number().optional(),
  message: z.string().optional(),
  fieldErrors: z.record(z.string(), z.string()).optional(),
})

function parseWithSchema<T>(schema: z.ZodType<T>, payload: unknown, context: string): T {
  const parsed = schema.safeParse(payload)
  if (!parsed.success) {
    throw new Error(`Received invalid ${context} payload from API.`)
  }
  return parsed.data
}

const api = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: 15_000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT))
    }
    return Promise.reject(error)
  },
)

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function parseApiError(error: unknown): ApiErrorResponse {
  if (axios.isAxiosError(error)) {
    const payload = apiErrorPayloadSchema.safeParse(error.response?.data)
    return {
      status: error.response?.status,
      message: payload.success ? payload.data.message || error.message : error.message,
      fieldErrors: payload.success ? payload.data.fieldErrors : undefined,
    }
  }

  if (error instanceof Error) {
    return { message: error.message }
  }

  return { message: 'An unexpected error occurred.' }
}

export async function loginRequest(payload: LoginRequest) {
  const { data } = await api.post<AuthResponse>('/api/auth/login', payload)
  return parseWithSchema(authResponseSchema, data, 'auth response')
}

export async function meRequest() {
  const { data } = await api.get<MeResponse>('/api/protected/me')
  return parseWithSchema(meResponseSchema, data, 'user profile')
}

export async function dashboardSummaryRequest() {
  const { data } = await api.get<DashboardSummaryResponse>('/api/dashboard/summary')
  return parseWithSchema(dashboardSummarySchema, data, 'dashboard summary')
}

export async function dashboardTopDriversRequest() {
  const { data } = await api.get<TopDriverResponse[]>('/api/dashboard/top-drivers')
  return parseWithSchema(z.array(topDriverSchema), data, 'top drivers list')
}

export async function dashboardCostTrendRequest() {
  const { data } = await api.get<CostTrendPointResponse[]>('/api/dashboard/cost-trend')
  return parseWithSchema(z.array(costTrendPointSchema), data, 'cost trend list')
}

export async function vehiclesListRequest() {
  const { data } = await api.get<VehicleResponse[]>('/api/vehicles')
  return parseWithSchema(z.array(vehicleSchema), data, 'vehicles list')
}

export async function createVehicleRequest(payload: VehicleUpsertRequest) {
  const { data } = await api.post<VehicleResponse>('/api/vehicles', payload)
  return parseWithSchema(vehicleSchema, data, 'vehicle create response')
}

export async function updateVehicleRequest(vehicleId: string, payload: VehicleUpsertRequest) {
  const { data } = await api.put<VehicleResponse>(`/api/vehicles/${vehicleId}`, payload)
  return parseWithSchema(vehicleSchema, data, 'vehicle update response')
}

export async function deleteVehicleRequest(vehicleId: string) {
  await api.delete(`/api/vehicles/${vehicleId}`)
}
