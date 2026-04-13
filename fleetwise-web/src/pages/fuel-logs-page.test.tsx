import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { FuelLogsPage } from '@/pages/fuel-logs-page'
import {
  createFuelLogRequest,
  deleteFuelLogRequest,
  fuelPriceCurrentByTypeRequest,
  fuelPriceHistoryRequest,
  fuelLogsListRequest,
  fuelLogStatsRequest,
  parseApiError,
  usersListRequest,
  vehiclesListRequest,
} from '@/lib/api'
import { useAuth } from '@/providers/auth-provider'

vi.mock('@/providers/auth-provider', () => ({
  useAuth: vi.fn(),
}))

vi.mock('@/lib/notify', () => ({
  notifyApiError: vi.fn(),
  notifyError: vi.fn(),
  notifySuccess: vi.fn(),
}))

vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api')
  return {
    ...actual,
    vehiclesListRequest: vi.fn(),
    fuelLogsListRequest: vi.fn(),
    fuelLogStatsRequest: vi.fn(),
    fuelPriceCurrentByTypeRequest: vi.fn(),
    fuelPriceHistoryRequest: vi.fn(),
    createFuelLogRequest: vi.fn(),
    deleteFuelLogRequest: vi.fn(),
    parseApiError: vi.fn(),
    usersListRequest: vi.fn(),
  }
})

const mockedUseAuth = vi.mocked(useAuth)
const mockedVehiclesList = vi.mocked(vehiclesListRequest)
const mockedFuelLogsList = vi.mocked(fuelLogsListRequest)
const mockedFuelLogStats = vi.mocked(fuelLogStatsRequest)
const mockedFuelPriceCurrentByType = vi.mocked(fuelPriceCurrentByTypeRequest)
const mockedFuelPriceHistory = vi.mocked(fuelPriceHistoryRequest)
const mockedCreateFuelLog = vi.mocked(createFuelLogRequest)
const mockedDeleteFuelLog = vi.mocked(deleteFuelLogRequest)
const mockedParseApiError = vi.mocked(parseApiError)
const mockedUsersList = vi.mocked(usersListRequest)

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
      <FuelLogsPage />
    </QueryClientProvider>,
  )
}

describe('FuelLogsPage', () => {
  it('submits create fuel log payload from form', async () => {
    mockedUseAuth.mockReturnValue({
      isAuthenticated: true,
      isBootstrapping: false,
      user: {
        status: 'authenticated',
        email: 'admin@fleetwise.test',
        role: 'ADMIN',
        userId: '550e8400-e29b-41d4-a716-446655440000',
      },
      login: vi.fn(),
      logout: vi.fn(),
      error: null,
      clearError: vi.fn(),
    })

    mockedVehiclesList.mockResolvedValue([
      {
        id: '550e8400-e29b-41d4-a716-446655440001',
        plateNumber: 'ABC-123',
        make: 'Toyota',
        model: 'Corolla',
        year: 2022,
        fuelType: 'DIESEL',
        tankCapacityLiters: 50,
        epaVehicleId: null,
        combinedMpg: null,
        cityMpg: null,
        highwayMpg: null,
        assignedDriverId: null,
        createdAt: '2026-04-11T10:00:00Z',
      },
    ])
    mockedFuelLogsList.mockResolvedValue([])
    mockedUsersList.mockResolvedValue([
      {
        id: '550e8400-e29b-41d4-a716-446655440000',
        name: 'Admin User',
        email: 'admin@fleetwise.test',
        role: 'ADMIN',
      },
    ])
    mockedFuelLogStats.mockResolvedValue({
      totalLogs: 0,
      totalCost: 0,
      averageLitersPerLog: null,
    })
    mockedFuelPriceCurrentByType.mockResolvedValue({
      fuelType: 'DIESEL',
      pricePerLiter: 70,
      effectiveDate: '2026-04-08',
      source: 'DOE Weekly Advisory',
      stale: false,
    })
    mockedFuelPriceHistory.mockResolvedValue([])
    mockedCreateFuelLog.mockResolvedValue({
      id: '550e8400-e29b-41d4-a716-446655440010',
      vehicleId: '550e8400-e29b-41d4-a716-446655440001',
      driverId: null,
      logDate: '2026-04-11',
      odometerReadingKm: 12050,
      litersFilled: 40,
      pricePerLiter: 1.5,
      totalCost: 60,
      stationName: 'Fuel Station',
      stationLat: null,
      stationLng: null,
      notes: null,
      createdAt: '2026-04-11T10:05:00Z',
    })
    mockedDeleteFuelLog.mockResolvedValue(undefined)
    mockedParseApiError.mockReturnValue({ message: 'Validation failed' })

    renderWithClient()

    const user = userEvent.setup()

    await user.click(await screen.findByRole('button', { name: 'Add Fuel Log' }))
    await user.selectOptions(screen.getByLabelText('Vehicle', { selector: 'select#vehicleId' }), '550e8400-e29b-41d4-a716-446655440001')
    await user.clear(screen.getByLabelText('Liters Filled'))
    await user.type(screen.getByLabelText('Liters Filled'), '40')
    await user.clear(screen.getByLabelText('Price Per Liter'))
    await user.type(screen.getByLabelText('Price Per Liter'), '1.5')
    await user.clear(screen.getByLabelText('Odometer (km)'))
    await user.type(screen.getByLabelText('Odometer (km)'), '12050')

    await user.click(screen.getByRole('button', { name: 'Save Fuel Log' }))

    expect(mockedCreateFuelLog).toHaveBeenCalled()
    expect(mockedCreateFuelLog.mock.calls[0][0]).toEqual(
      expect.objectContaining({
        vehicleId: '550e8400-e29b-41d4-a716-446655440001',
        litersFilled: 40,
        pricePerLiter: 1.5,
        odometerReadingKm: 12050,
      }),
    )
  })
})
