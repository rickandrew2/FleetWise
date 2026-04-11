import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { VehiclesPage } from '@/pages/vehicles-page'
import {
  createVehicleRequest,
  deleteVehicleRequest,
  parseApiError,
  updateVehicleRequest,
  vehiclesListRequest,
} from '@/lib/api'
import { useAuth } from '@/providers/auth-provider'

vi.mock('@/providers/auth-provider', () => ({
  useAuth: vi.fn(),
}))

vi.mock('@/lib/notify', () => ({
  notifyApiError: vi.fn(),
  notifySuccess: vi.fn(),
}))

vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api')
  return {
    ...actual,
    vehiclesListRequest: vi.fn(),
    createVehicleRequest: vi.fn(),
    updateVehicleRequest: vi.fn(),
    deleteVehicleRequest: vi.fn(),
    parseApiError: vi.fn(),
  }
})

const mockedUseAuth = vi.mocked(useAuth)
const mockedVehiclesList = vi.mocked(vehiclesListRequest)
const mockedCreateVehicle = vi.mocked(createVehicleRequest)
const mockedUpdateVehicle = vi.mocked(updateVehicleRequest)
const mockedDeleteVehicle = vi.mocked(deleteVehicleRequest)
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
      <VehiclesPage />
    </QueryClientProvider>,
  )
}

describe('VehiclesPage', () => {
  it('submits create vehicle payload from form', async () => {
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

    mockedVehiclesList.mockResolvedValue([])
    mockedCreateVehicle.mockResolvedValue({
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
    })
    mockedUpdateVehicle.mockResolvedValue({} as never)
    mockedDeleteVehicle.mockResolvedValue(undefined)
    mockedParseApiError.mockReturnValue({ message: 'Validation failed' })

    renderWithClient()

    const user = userEvent.setup()

    await user.click(await screen.findByRole('button', { name: 'Add Vehicle' }))
    await user.type(screen.getByLabelText('Plate Number'), 'abc-123')
    await user.type(screen.getByLabelText('Make'), 'Toyota')
    await user.type(screen.getByLabelText('Model'), 'Corolla')
    await user.clear(screen.getByLabelText('Year'))
    await user.type(screen.getByLabelText('Year'), '2022')
    await user.type(screen.getByLabelText('Fuel Type'), 'Petrol')

    await user.click(screen.getByRole('button', { name: 'Create vehicle' }))

    expect(mockedCreateVehicle).toHaveBeenCalled()
    expect(mockedCreateVehicle.mock.calls[0][0]).toEqual(
      expect.objectContaining({
        plateNumber: 'ABC-123',
        make: 'Toyota',
        model: 'Corolla',
        year: 2022,
      }),
    )
  })
})
