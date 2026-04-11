import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ProtectedRoute } from '@/components/auth/protected-route'
import { useAuth } from '@/providers/auth-provider'

vi.mock('@/providers/auth-provider', () => ({
  useAuth: vi.fn(),
}))

const mockedUseAuth = vi.mocked(useAuth)

describe('ProtectedRoute', () => {
  it('redirects unauthenticated users to login', () => {
    mockedUseAuth.mockReturnValue({
      isAuthenticated: false,
      isBootstrapping: false,
      user: null,
      login: vi.fn(),
      logout: vi.fn(),
      error: null,
      clearError: vi.fn(),
    })

    render(
      <MemoryRouter initialEntries={['/secure']}>
        <Routes>
          <Route path="/login" element={<p>Login Page</p>} />
          <Route path="/dashboard" element={<p>Dashboard Page</p>} />
          <Route path="/secure" element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route index element={<p>Secure Content</p>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByText('Login Page')).toBeInTheDocument()
  })

  it('redirects to dashboard when role is not allowed', () => {
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
      <MemoryRouter initialEntries={['/secure']}>
        <Routes>
          <Route path="/dashboard" element={<p>Dashboard Page</p>} />
          <Route path="/secure" element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route index element={<p>Secure Content</p>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByText('Dashboard Page')).toBeInTheDocument()
  })
})
