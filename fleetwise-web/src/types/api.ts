export type UserRole = 'ADMIN' | 'FLEET_MANAGER' | 'DRIVER'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  expiresInMs: number
  email: string
  role: UserRole
}

export interface MeResponse {
  status: 'authenticated'
  email: string
  role: UserRole
  userId: string
}

export interface ApiErrorResponse {
  status?: number
  message?: string
  fieldErrors?: Record<string, string>
}

export interface DashboardSummaryResponse {
  monthToDateFuelCost: number
  fleetEfficiencyScore: number | null
  activeAlertsCount: number
}

export interface TopDriverResponse {
  driverId: string
  driverName: string
  averageEfficiencyScore: number | null
  routeCount: number
}

export interface CostTrendPointResponse {
  month: string
  totalCost: number
}

export interface VehicleResponse {
  id: string
  plateNumber: string
  make: string
  model: string
  year: number
  fuelType: string | null
  tankCapacityLiters: number | null
  epaVehicleId: number | null
  combinedMpg: number | null
  cityMpg: number | null
  highwayMpg: number | null
  assignedDriverId: string | null
  createdAt: string
}

export interface VehicleUpsertRequest {
  plateNumber: string
  make: string
  model: string
  year: number
  fuelType?: string | null
  tankCapacityLiters?: number | null
  epaVehicleId?: number | null
  assignedDriverId?: string | null
}
