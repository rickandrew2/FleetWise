import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AppShell } from '@/components/layout/app-shell'
import { useAuth } from '@/providers/auth-provider'

vi.mock('@/providers/auth-provider', () => ({
  useAuth: vi.fn(),
}))

const mockedUseAuth = vi.mocked(useAuth)

describe('AppShell navigation', () => {
  it('hides admin-only modules for DRIVER role', () => {
    mockedUseAuth.mockReturnValue({
      isAuthenticated: true,
      isBootstrapping: false,
      user: {
        status: 'authenticated',
        email: 'driver@fleetwise.test',
        role: 'DRIVER',
        userId: '550e8400-e29b-41d4-a716-446655440000',
      },
      login: vi.fn(),
      logout: vi.fn(),
      error: null,
      clearError: vi.fn(),
    })

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<AppShell />}>
            <Route path="/dashboard" element={<p>Dashboard Content</p>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByText('Dashboard')).toBeInTheDocument()
    expect(screen.queryByText('Vehicles')).not.toBeInTheDocument()
    expect(screen.queryByText('Reports')).not.toBeInTheDocument()
    expect(screen.getByText('Fuel Logs')).toBeInTheDocument()
  })
})
