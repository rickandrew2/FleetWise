import axios, { AxiosError } from 'axios'
import { z } from 'zod'
import { env } from '@/lib/env'
import type {
  AlertQueryFilters,
  AlertResponse,
  AlertUnreadCountResponse,
  ApiErrorResponse,
  AuthResponse,
  CostTrendPointResponse,
  DashboardSummaryResponse,
  DownloadedReport,
  EpaLookupRequest,
  EpaVehicleOptionResponse,
  FuelPriceCurrentResponse,
  FuelPriceHistoryPointResponse,
  FuelPriceManualUpdateRequest,
  FuelLogResponse,
  FuelLogStatsResponse,
  FuelLogUpsertRequest,
  FuelPriceUpdateResultResponse,
  GenerateReportRequest,
  LoginRequest,
  LogQueryFilters,
  MeResponse,
  ReportJobResponse,
  ReportType,
  RouteLogResponse,
  RouteLogStatsResponse,
  RouteLogUpsertRequest,
  TopDriverResponse,
  UserSummaryResponse,
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

const userSummarySchema = z.object({
  id: z.string().uuid(),
  name: z.string().nullable(),
  email: z.string().email(),
  role: z.enum(['ADMIN', 'FLEET_MANAGER', 'DRIVER']),
})

const epaLookupRequestSchema = z.object({
  year: z.number().int().min(1980).max(2100),
  make: z.string().min(1).max(50),
  model: z.string().min(1).max(50),
})

const epaVehicleOptionSchema = z.object({
  epaVehicleId: z.number().int().positive(),
  label: z.string(),
  combinedMpg: z.number().nullable(),
  fuelType: z.string().nullable(),
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

const isoDateSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}$/)

const fuelLogSchema = z.object({
  id: z.string().uuid(),
  vehicleId: z.string().uuid(),
  driverId: z.string().uuid().nullable(),
  logDate: isoDateSchema,
  odometerReadingKm: z.number().nullable(),
  litersFilled: z.number(),
  pricePerLiter: z.number(),
  totalCost: z.number(),
  stationName: z.string().nullable(),
  stationLat: z.number().nullable(),
  stationLng: z.number().nullable(),
  notes: z.string().nullable(),
  createdAt: z.string(),
})

const fuelLogStatsSchema = z.object({
  totalLogs: z.number().int(),
  totalCost: z.number().nullable(),
  averageLitersPerLog: z.number().nullable(),
})

const fuelPriceTypeSchema = z.enum(['DIESEL', 'GASOLINE_91', 'GASOLINE_95', 'DIESEL_PLUS'])

const fuelPriceCurrentSchema = z.object({
  fuelType: fuelPriceTypeSchema,
  pricePerLiter: z.number().positive(),
  effectiveDate: isoDateSchema,
  source: z.string(),
  stale: z.boolean(),
})

const fuelPriceHistoryPointSchema = z.object({
  fuelType: fuelPriceTypeSchema,
  effectiveDate: isoDateSchema,
  averagePricePerLiter: z.number().positive(),
})

const fuelPriceManualEntrySchema = z.object({
  fuelType: fuelPriceTypeSchema,
  pricePerLiter: z.number().positive(),
  brand: z.string().nullable().optional(),
})

const fuelPriceManualUpdateSchema = z.object({
  effectiveDate: isoDateSchema,
  source: z.string().min(1).max(100),
  entries: z.array(fuelPriceManualEntrySchema).min(1),
})

const fuelPriceUpdateResultSchema = z.object({
  updatedRecords: z.number().int().nonnegative(),
  effectiveDate: isoDateSchema.nullable(),
  fallbackUsed: z.boolean(),
  message: z.string(),
})

const routeLogSchema = z.object({
  id: z.string().uuid(),
  vehicleId: z.string().uuid(),
  driverId: z.string().uuid().nullable(),
  tripDate: isoDateSchema,
  originLabel: z.string().nullable(),
  originLat: z.number(),
  originLng: z.number(),
  destinationLabel: z.string().nullable(),
  destinationLat: z.number(),
  destinationLng: z.number(),
  distanceKm: z.number().nullable(),
  estimatedDurationMin: z.number().int().nullable(),
  actualFuelUsedLiters: z.number().nullable(),
  expectedFuelLiters: z.number().nullable(),
  efficiencyScore: z.number().nullable(),
  createdAt: z.string(),
})

const routeLogStatsSchema = z.object({
  totalTrips: z.number().int(),
  totalDistanceKm: z.number(),
  averageEfficiencyScore: z.number().nullable(),
})

const alertTypeSchema = z.enum(['OVERCONSUMPTION', 'HIGH_COST', 'MAINTENANCE_DUE', 'UNUSUAL_FILLUP'])

const alertSchema = z.object({
  id: z.string().uuid(),
  vehicleId: z.string().uuid().nullable(),
  driverId: z.string().uuid().nullable(),
  alertType: alertTypeSchema,
  message: z.string(),
  thresholdValue: z.number().nullable(),
  actualValue: z.number().nullable(),
  isRead: z.boolean(),
  triggeredAt: z.string(),
})

const alertUnreadCountSchema = z.object({
  unreadCount: z.number().int().nonnegative(),
})

const reportTypeSchema = z.enum(['WEEKLY', 'MONTHLY'])
const reportStatusSchema = z.enum(['PENDING', 'RUNNING', 'COMPLETED', 'FAILED'])

const reportJobSchema = z.object({
  id: z.string().uuid(),
  reportType: reportTypeSchema,
  status: reportStatusSchema,
  filePath: z.string().nullable(),
  generatedAt: z.string().nullable(),
  createdAt: z.string(),
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

function buildLogQueryParams(filters?: LogQueryFilters) {
  if (!filters) {
    return undefined
  }

  const params: Record<string, string> = {}
  const uuidSchema = z.string().uuid()

  if (filters.vehicleId && uuidSchema.safeParse(filters.vehicleId).success) {
    params.vehicleId = filters.vehicleId
  }

  if (filters.driverId && uuidSchema.safeParse(filters.driverId).success) {
    params.driverId = filters.driverId
  }

  if (filters.startDate && isoDateSchema.safeParse(filters.startDate).success) {
    params.startDate = filters.startDate
  }

  if (filters.endDate && isoDateSchema.safeParse(filters.endDate).success) {
    params.endDate = filters.endDate
  }

  if (Object.keys(params).length === 0) {
    return undefined
  }

  return params
}

function buildAlertQueryParams(filters?: AlertQueryFilters) {
  if (!filters) {
    return undefined
  }

  const params: Record<string, string> = {}
  const uuidSchema = z.string().uuid()

  if (filters.alertType && alertTypeSchema.safeParse(filters.alertType).success) {
    params.alertType = filters.alertType
  }

  if (filters.vehicleId && uuidSchema.safeParse(filters.vehicleId).success) {
    params.vehicleId = filters.vehicleId
  }

  if (filters.driverId && uuidSchema.safeParse(filters.driverId).success) {
    params.driverId = filters.driverId
  }

  if (typeof filters.isRead === 'boolean') {
    params.isRead = String(filters.isRead)
  }

  if (Object.keys(params).length === 0) {
    return undefined
  }

  return params
}

function extractFilenameFromHeader(headerValue: string | undefined, fallbackName: string) {
  if (!headerValue) {
    return fallbackName
  }

  const basicMatch = headerValue.match(/filename="?([^";]+)"?/i)
  if (!basicMatch || !basicMatch[1]) {
    return fallbackName
  }

  const sanitized = basicMatch[1].replace(/[^a-zA-Z0-9._-]/g, '_')
  return sanitized || fallbackName
}

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

export async function fuelPricesCurrentRequest() {
  const { data } = await api.get<FuelPriceCurrentResponse[]>('/api/fuel-prices/current')
  return parseWithSchema(z.array(fuelPriceCurrentSchema), data, 'current fuel prices list')
}

export async function fuelPriceCurrentByTypeRequest(fuelType: string) {
  const safeFuelType = parseWithSchema(fuelPriceTypeSchema, fuelType, 'fuel price type')
  const { data } = await api.get<FuelPriceCurrentResponse>(`/api/fuel-prices/current/${safeFuelType}`)
  return parseWithSchema(fuelPriceCurrentSchema, data, 'current fuel price')
}

export async function fuelPriceHistoryRequest() {
  const { data } = await api.get<FuelPriceHistoryPointResponse[]>('/api/fuel-prices/history')
  return parseWithSchema(z.array(fuelPriceHistoryPointSchema), data, 'fuel price history')
}

export async function fuelPricesManualUpdateRequest(payload: FuelPriceManualUpdateRequest) {
  const safePayload = parseWithSchema(fuelPriceManualUpdateSchema, payload, 'fuel price manual update payload')
  const { data } = await api.post<FuelPriceUpdateResultResponse>('/api/fuel-prices/manual-update', safePayload)
  return parseWithSchema(fuelPriceUpdateResultSchema, data, 'fuel price update result')
}

export async function fuelPricesTriggerUpdateRequest() {
  const { data } = await api.post<FuelPriceUpdateResultResponse>('/api/fuel-prices/trigger-update')
  return parseWithSchema(fuelPriceUpdateResultSchema, data, 'fuel price update result')
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

export async function usersListRequest() {
  const { data } = await api.get<UserSummaryResponse[]>('/api/users')
  return parseWithSchema(z.array(userSummarySchema), data, 'users list')
}

export async function lookupEpaVehiclesRequest(payload: EpaLookupRequest) {
  const safePayload = parseWithSchema(epaLookupRequestSchema, payload, 'EPA lookup payload')
  const { data } = await api.post<EpaVehicleOptionResponse[]>('/api/vehicles/lookup-epa', safePayload)
  return parseWithSchema(z.array(epaVehicleOptionSchema), data, 'EPA lookup options')
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

export async function fuelLogsListRequest(filters?: LogQueryFilters) {
  const { data } = await api.get<FuelLogResponse[]>('/api/fuel-logs', {
    params: buildLogQueryParams(filters),
  })
  return parseWithSchema(z.array(fuelLogSchema), data, 'fuel logs list')
}

export async function fuelLogStatsRequest(filters?: LogQueryFilters) {
  const { data } = await api.get<FuelLogStatsResponse>('/api/fuel-logs/stats', {
    params: buildLogQueryParams(filters),
  })
  return parseWithSchema(fuelLogStatsSchema, data, 'fuel logs stats')
}

export async function createFuelLogRequest(payload: FuelLogUpsertRequest) {
  const { data } = await api.post<FuelLogResponse>('/api/fuel-logs', payload)
  return parseWithSchema(fuelLogSchema, data, 'fuel log create response')
}

export async function deleteFuelLogRequest(fuelLogId: string) {
  await api.delete(`/api/fuel-logs/${fuelLogId}`)
}

export async function routesListRequest(filters?: LogQueryFilters) {
  const { data } = await api.get<RouteLogResponse[]>('/api/routes', {
    params: buildLogQueryParams(filters),
  })
  return parseWithSchema(z.array(routeLogSchema), data, 'routes list')
}

export async function routeLogStatsRequest(filters?: LogQueryFilters) {
  const { data } = await api.get<RouteLogStatsResponse>('/api/routes/stats', {
    params: buildLogQueryParams(filters),
  })
  return parseWithSchema(routeLogStatsSchema, data, 'routes stats')
}

export async function createRouteLogRequest(payload: RouteLogUpsertRequest) {
  const { data } = await api.post<RouteLogResponse>('/api/routes', payload)
  return parseWithSchema(routeLogSchema, data, 'route log create response')
}

export async function deleteRouteLogRequest(routeId: string) {
  await api.delete(`/api/routes/${routeId}`)
}

export async function alertsListRequest(filters?: AlertQueryFilters) {
  const { data } = await api.get<AlertResponse[]>('/api/alerts', {
    params: buildAlertQueryParams(filters),
  })
  return parseWithSchema(z.array(alertSchema), data, 'alerts list')
}

export async function markAlertAsReadRequest(alertId: string) {
  const { data } = await api.put<AlertResponse>(`/api/alerts/${alertId}/read`)
  return parseWithSchema(alertSchema, data, 'alert update response')
}

export async function alertUnreadCountRequest(driverId?: string) {
  const params = driverId && z.string().uuid().safeParse(driverId).success ? { driverId } : undefined
  const { data } = await api.get<AlertUnreadCountResponse>('/api/alerts/unread-count', { params })
  return parseWithSchema(alertUnreadCountSchema, data, 'alert unread count')
}

export async function reportsListRequest() {
  const { data } = await api.get<ReportJobResponse[]>('/api/reports')
  return parseWithSchema(z.array(reportJobSchema), data, 'reports list')
}

export async function generateReportRequest(payload: GenerateReportRequest) {
  const safePayload = {
    reportType: parseWithSchema(reportTypeSchema, payload.reportType, 'report type'),
  }
  const { data } = await api.post<ReportJobResponse>('/api/reports/generate', safePayload)
  return parseWithSchema(reportJobSchema, data, 'report generate response')
}

export async function downloadReportRequest(reportId: string): Promise<DownloadedReport> {
  const response = await api.get<Blob>(`/api/reports/${reportId}/download`, {
    responseType: 'blob',
  })
  const filename = extractFilenameFromHeader(response.headers['content-disposition'], `report-${reportId}.bin`)
  return {
    blob: response.data,
    filename,
  }
}

export const REPORT_TYPES: ReportType[] = ['WEEKLY', 'MONTHLY']
