import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ReportsPage } from '@/pages/reports-page'
import {
  downloadReportRequest,
  generateReportRequest,
  parseApiError,
  reportsListRequest,
} from '@/lib/api'

vi.mock('@/lib/notify', () => ({
  notifyApiError: vi.fn(),
  notifySuccess: vi.fn(),
}))

vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api')
  return {
    ...actual,
    reportsListRequest: vi.fn(),
    generateReportRequest: vi.fn(),
    downloadReportRequest: vi.fn(),
    parseApiError: vi.fn(),
  }
})

const mockedReportsList = vi.mocked(reportsListRequest)
const mockedGenerateReport = vi.mocked(generateReportRequest)
const mockedDownloadReport = vi.mocked(downloadReportRequest)
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
      <ReportsPage />
    </QueryClientProvider>,
  )
}

describe('ReportsPage', () => {
  it('submits generate report payload', async () => {
    mockedReportsList.mockResolvedValue([])
    mockedGenerateReport.mockResolvedValue({
      id: '550e8400-e29b-41d4-a716-446655440011',
      reportType: 'WEEKLY',
      status: 'PENDING',
      filePath: null,
      generatedAt: null,
      createdAt: '2026-04-11T11:10:00Z',
    })
    mockedDownloadReport.mockResolvedValue({
      blob: new Blob(['sample']),
      filename: 'report.bin',
    })
    mockedParseApiError.mockReturnValue({ message: 'Validation failed' })

    renderWithClient()

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Generate Report' }))

    expect(mockedGenerateReport).toHaveBeenCalled()
    expect(mockedGenerateReport.mock.calls[0][0]).toEqual({ reportType: 'WEEKLY' })
  })
})
