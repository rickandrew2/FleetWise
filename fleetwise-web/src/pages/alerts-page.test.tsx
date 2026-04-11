import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { AlertsPage } from '@/pages/alerts-page'
import {
  alertUnreadCountRequest,
  alertsListRequest,
  markAlertAsReadRequest,
  parseApiError,
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
    alertsListRequest: vi.fn(),
    alertUnreadCountRequest: vi.fn(),
    markAlertAsReadRequest: vi.fn(),
    parseApiError: vi.fn(),
  }
})

const mockedUseAuth = vi.mocked(useAuth)
const mockedVehiclesList = vi.mocked(vehiclesListRequest)
const mockedAlertsList = vi.mocked(alertsListRequest)
const mockedUnreadCount = vi.mocked(alertUnreadCountRequest)
const mockedMarkRead = vi.mocked(markAlertAsReadRequest)
const mockedParseApiError = vi.mocked(parseApiError)

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
      <AlertsPage />
    </QueryClientProvider>,
  )
}

describe('AlertsPage', () => {
  it('marks unread alert as read', async () => {
    mockedUseAuth.mockReturnValue({
      isAuthenticated: true,
      isBootstrapping: false,
      user: {
        status: 'authenticated',
        email: 'manager@fleetwise.test',
        role: 'FLEET_MANAGER',
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

    mockedAlertsList.mockResolvedValue([
      {
        id: '550e8400-e29b-41d4-a716-446655440010',
        vehicleId: '550e8400-e29b-41d4-a716-446655440001',
        driverId: null,
        alertType: 'HIGH_COST',
        message: 'Fuel cost exceeded threshold',
        thresholdValue: 100,
        actualValue: 130,
        isRead: false,
        triggeredAt: '2026-04-11T11:00:00Z',
      },
    ])

    mockedUnreadCount.mockResolvedValue({ unreadCount: 1 })
    mockedMarkRead.mockResolvedValue({
      id: '550e8400-e29b-41d4-a716-446655440010',
      vehicleId: '550e8400-e29b-41d4-a716-446655440001',
      driverId: null,
      alertType: 'HIGH_COST',
      message: 'Fuel cost exceeded threshold',
      thresholdValue: 100,
      actualValue: 130,
      isRead: true,
      triggeredAt: '2026-04-11T11:00:00Z',
    })
    mockedParseApiError.mockReturnValue({ message: 'Validation failed' })

    renderWithClient()

    const user = userEvent.setup()

    await user.click(await screen.findByRole('button', { name: 'Mark Read' }))

    expect(mockedMarkRead).toHaveBeenCalled()
    expect(mockedMarkRead.mock.calls[0][0]).toBe('550e8400-e29b-41d4-a716-446655440010')
  })
})
