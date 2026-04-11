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

export interface UserSummaryResponse {
  id: string
  name: string | null
  email: string
  role: UserRole
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

export interface EpaLookupRequest {
  year: number
  make: string
  model: string
}

export interface EpaVehicleOptionResponse {
  epaVehicleId: number
  label: string
  combinedMpg: number | null
  fuelType: string | null
}

export interface LogQueryFilters {
  vehicleId?: string
  driverId?: string
  startDate?: string
  endDate?: string
}

export interface FuelLogResponse {
  id: string
  vehicleId: string
  driverId: string | null
  logDate: string
  odometerReadingKm: number | null
  litersFilled: number
  pricePerLiter: number
  totalCost: number
  stationName: string | null
  stationLat: number | null
  stationLng: number | null
  notes: string | null
  createdAt: string
}

export interface FuelLogUpsertRequest {
  vehicleId: string
  driverId?: string | null
  logDate: string
  odometerReadingKm?: number | null
  litersFilled: number
  pricePerLiter: number
  stationName?: string | null
  stationLat?: number | null
  stationLng?: number | null
  notes?: string | null
}

export interface FuelLogStatsResponse {
  totalLogs: number
  totalCost: number | null
  averageLitersPerLog: number | null
}

export interface RouteLogResponse {
  id: string
  vehicleId: string
  driverId: string | null
  tripDate: string
  originLabel: string | null
  originLat: number
  originLng: number
  destinationLabel: string | null
  destinationLat: number
  destinationLng: number
  distanceKm: number | null
  estimatedDurationMin: number | null
  actualFuelUsedLiters: number | null
  expectedFuelLiters: number | null
  efficiencyScore: number | null
  createdAt: string
}

export interface RouteLogUpsertRequest {
  vehicleId: string
  driverId?: string | null
  tripDate: string
  originLabel?: string | null
  originLat: number
  originLng: number
  destinationLabel?: string | null
  destinationLat: number
  destinationLng: number
  actualFuelUsedLiters?: number | null
}

export interface RouteLogStatsResponse {
  totalTrips: number
  totalDistanceKm: number
  averageEfficiencyScore: number | null
}

export type AlertType = 'OVERCONSUMPTION' | 'HIGH_COST' | 'MAINTENANCE_DUE' | 'UNUSUAL_FILLUP'

export interface AlertResponse {
  id: string
  vehicleId: string | null
  driverId: string | null
  alertType: AlertType
  message: string
  thresholdValue: number | null
  actualValue: number | null
  isRead: boolean
  triggeredAt: string
}

export interface AlertUnreadCountResponse {
  unreadCount: number
}

export interface AlertQueryFilters {
  alertType?: AlertType | ''
  vehicleId?: string
  driverId?: string
  isRead?: boolean
}

export type ReportType = 'WEEKLY' | 'MONTHLY'
export type ReportStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'

export interface ReportJobResponse {
  id: string
  reportType: ReportType
  status: ReportStatus
  filePath: string | null
  generatedAt: string | null
  createdAt: string
}

export interface GenerateReportRequest {
  reportType: ReportType
}

export interface DownloadedReport {
  blob: Blob
  filename: string
}
