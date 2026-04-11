import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { DashboardPage } from '@/pages/dashboard-page'
import {
  dashboardCostTrendRequest,
  dashboardSummaryRequest,
  dashboardTopDriversRequest,
} from '@/lib/api'

vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api')
  return {
    ...actual,
    dashboardSummaryRequest: vi.fn(),
    dashboardTopDriversRequest: vi.fn(),
    dashboardCostTrendRequest: vi.fn(),
  }
})

const mockedSummary = vi.mocked(dashboardSummaryRequest)
const mockedTopDrivers = vi.mocked(dashboardTopDriversRequest)
const mockedCostTrend = vi.mocked(dashboardCostTrendRequest)

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

    renderWithClient()

    expect(await screen.findByText('No trend data yet')).toBeInTheDocument()
    expect(screen.getByText('No ranked drivers')).toBeInTheDocument()
  })
})
