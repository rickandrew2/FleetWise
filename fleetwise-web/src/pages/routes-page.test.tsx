import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { RoutesPage } from '@/pages/routes-page'
import {
  createRouteLogRequest,
  deleteRouteLogRequest,
  parseApiError,
  routeLogStatsRequest,
  routesListRequest,
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
    routesListRequest: vi.fn(),
    routeLogStatsRequest: vi.fn(),
    createRouteLogRequest: vi.fn(),
    deleteRouteLogRequest: vi.fn(),
    parseApiError: vi.fn(),
    usersListRequest: vi.fn(),
  }
})

const mockedUseAuth = vi.mocked(useAuth)
const mockedVehiclesList = vi.mocked(vehiclesListRequest)
const mockedRoutesList = vi.mocked(routesListRequest)
const mockedRouteStats = vi.mocked(routeLogStatsRequest)
const mockedCreateRoute = vi.mocked(createRouteLogRequest)
const mockedDeleteRoute = vi.mocked(deleteRouteLogRequest)
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
      <RoutesPage />
    </QueryClientProvider>,
  )
}

describe('RoutesPage', () => {
  it('submits create route payload from form', async () => {
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
        fuelType: 'Petrol',
        tankCapacityLiters: 50,
        epaVehicleId: null,
        combinedMpg: null,
        cityMpg: null,
        highwayMpg: null,
        assignedDriverId: null,
        createdAt: '2026-04-11T10:00:00Z',
      },
    ])
    mockedRoutesList.mockResolvedValue([])
    mockedUsersList.mockResolvedValue([
      {
        id: '550e8400-e29b-41d4-a716-446655440000',
        name: 'Admin User',
        email: 'admin@fleetwise.test',
        role: 'ADMIN',
      },
    ])
    mockedRouteStats.mockResolvedValue({
      totalTrips: 0,
      totalDistanceKm: 0,
      averageEfficiencyScore: null,
    })
    mockedCreateRoute.mockResolvedValue({
      id: '550e8400-e29b-41d4-a716-446655440010',
      vehicleId: '550e8400-e29b-41d4-a716-446655440001',
      driverId: null,
      tripDate: '2026-04-11',
      originLabel: 'Depot',
      originLat: 37.77,
      originLng: -122.41,
      destinationLabel: 'Warehouse',
      destinationLat: 37.8,
      destinationLng: -122.27,
      distanceKm: 18.2,
      estimatedDurationMin: 40,
      actualFuelUsedLiters: 2.4,
      expectedFuelLiters: 2.1,
      efficiencyScore: 0.88,
      createdAt: '2026-04-11T10:05:00Z',
    })
    mockedDeleteRoute.mockResolvedValue(undefined)
    mockedParseApiError.mockReturnValue({ message: 'Validation failed' })

    renderWithClient()

    const user = userEvent.setup()

    await user.click(await screen.findByRole('button', { name: 'Add Route' }))
    await user.selectOptions(screen.getByLabelText('Vehicle', { selector: 'select#vehicleId' }), '550e8400-e29b-41d4-a716-446655440001')
    await user.clear(screen.getByLabelText('Origin Latitude'))
    await user.type(screen.getByLabelText('Origin Latitude'), '37.77')
    await user.clear(screen.getByLabelText('Origin Longitude'))
    await user.type(screen.getByLabelText('Origin Longitude'), '-122.41')
    await user.clear(screen.getByLabelText('Destination Latitude'))
    await user.type(screen.getByLabelText('Destination Latitude'), '37.80')
    await user.clear(screen.getByLabelText('Destination Longitude'))
    await user.type(screen.getByLabelText('Destination Longitude'), '-122.27')
    await user.clear(screen.getByLabelText('Actual Fuel Used (L)'))
    await user.type(screen.getByLabelText('Actual Fuel Used (L)'), '2.4')

    await user.click(screen.getByRole('button', { name: 'Save Route' }))

    expect(mockedCreateRoute).toHaveBeenCalled()
    expect(mockedCreateRoute.mock.calls[0][0]).toEqual(
      expect.objectContaining({
        vehicleId: '550e8400-e29b-41d4-a716-446655440001',
        originLat: 37.77,
        originLng: -122.41,
        destinationLat: 37.8,
        destinationLng: -122.27,
        actualFuelUsedLiters: 2.4,
      }),
    )
  })
})
