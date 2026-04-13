import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { DashboardPage } from '@/pages/dashboard-page'
import {
  dashboardCostTrendRequest,
  dashboardSummaryRequest,
  dashboardTopDriversRequest,
  fuelLogsListRequest,
  fuelPriceHistoryRequest,
  fuelPricesCurrentRequest,
  vehiclesListRequest,
} from '@/lib/api'

vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api')
  return {
    ...actual,
    dashboardSummaryRequest: vi.fn(),
    dashboardTopDriversRequest: vi.fn(),
    dashboardCostTrendRequest: vi.fn(),
    fuelPricesCurrentRequest: vi.fn(),
    fuelPriceHistoryRequest: vi.fn(),
    vehiclesListRequest: vi.fn(),
    fuelLogsListRequest: vi.fn(),
  }
})

const mockedSummary = vi.mocked(dashboardSummaryRequest)
const mockedTopDrivers = vi.mocked(dashboardTopDriversRequest)
const mockedCostTrend = vi.mocked(dashboardCostTrendRequest)
const mockedFuelPricesCurrent = vi.mocked(fuelPricesCurrentRequest)
const mockedFuelPriceHistory = vi.mocked(fuelPriceHistoryRequest)
const mockedVehicles = vi.mocked(vehiclesListRequest)
const mockedFuelLogs = vi.mocked(fuelLogsListRequest)

function renderWithClient() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <DashboardPage />
    </QueryClientProvider>,
  )
}

describe('DashboardPage', () => {
  it('renders empty states when no drivers and no trend data exist', async () => {
    mockedSummary.mockResolvedValue({
      monthToDateFuelCost: 0,
      fleetEfficiencyScore: null,
      activeAlertsCount: 0,
    })
    mockedTopDrivers.mockResolvedValue([])
    mockedCostTrend.mockResolvedValue([])
    mockedFuelPricesCurrent.mockResolvedValue([])
    mockedFuelPriceHistory.mockResolvedValue([])
    mockedVehicles.mockResolvedValue([])
    mockedFuelLogs.mockResolvedValue([])

    renderWithClient()

    expect(await screen.findByText('No trend data yet')).toBeInTheDocument()
    expect(screen.getByText('No ranked drivers')).toBeInTheDocument()
  })

  it('keeps dashboard visible when optional fuel-price endpoints fail', async () => {
    mockedSummary.mockResolvedValue({
      monthToDateFuelCost: 120000,
      fleetEfficiencyScore: 1.03,
      activeAlertsCount: 2,
    })
    mockedTopDrivers.mockResolvedValue([])
    mockedCostTrend.mockResolvedValue([])
    mockedFuelPricesCurrent.mockRejectedValue(new Error('Request failed with status code 404'))
    mockedFuelPriceHistory.mockRejectedValue(new Error('Request failed with status code 404'))
    mockedVehicles.mockResolvedValue([
      {
        id: '550e8400-e29b-41d4-a716-446655440099',
        plateNumber: 'ABC-111',
        make: 'Toyota',
        model: 'Hiace',
        year: 2020,
        fuelType: 'DIESEL',
        tankCapacityLiters: 65,
        epaVehicleId: null,
        combinedMpg: null,
        cityMpg: null,
        highwayMpg: null,
        assignedDriverId: null,
        createdAt: '2026-04-11T10:00:00Z',
      },
    ])
    mockedFuelLogs.mockResolvedValue([
      {
        id: '550e8400-e29b-41d4-a716-446655440100',
        vehicleId: '550e8400-e29b-41d4-a716-446655440099',
        driverId: null,
        logDate: '2026-04-11',
        odometerReadingKm: null,
        litersFilled: 40,
        pricePerLiter: 70,
        totalCost: 2800,
        stationName: 'Test Station',
        stationLat: null,
        stationLng: null,
        notes: null,
        createdAt: '2026-04-11T10:01:00Z',
      },
    ])

    renderWithClient()

    expect(await screen.findByText('Fleet Snapshot')).toBeInTheDocument()
    expect(screen.getByText('Fuel price feed is temporarily unavailable. Showing fallback values when available.')).toBeInTheDocument()
    expect(await screen.findByText(/Recent fill-up fallback|Estimated from recent fill-up logs/)).toBeInTheDocument()
    expect(screen.queryByText('Unable to load dashboard')).not.toBeInTheDocument()
  })
})
