import { Suspense, lazy } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from '@/components/auth/protected-route'
import { AppShell } from '@/components/layout/app-shell'
import { PageLoadingState } from '@/components/ui/page-state'
import { ACCESS_RULES } from '@/lib/access'

const DashboardPage = lazy(async () => ({ default: (await import('@/pages/dashboard-page')).DashboardPage }))
const VehiclesPage = lazy(async () => ({ default: (await import('@/pages/vehicles-page')).VehiclesPage }))
const FuelLogsPage = lazy(async () => ({ default: (await import('@/pages/fuel-logs-page')).FuelLogsPage }))
const RoutesPage = lazy(async () => ({ default: (await import('@/pages/routes-page')).RoutesPage }))
const AlertsPage = lazy(async () => ({ default: (await import('@/pages/alerts-page')).AlertsPage }))
const ReportsPage = lazy(async () => ({ default: (await import('@/pages/reports-page')).ReportsPage }))
const LoginPage = lazy(async () => ({ default: (await import('@/pages/login-page')).LoginPage }))
const NotFoundPage = lazy(async () => ({ default: (await import('@/pages/not-found-page')).NotFoundPage }))

function LazyPage({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<PageLoadingState />}>{children}</Suspense>
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LazyPage><LoginPage /></LazyPage>} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.dashboard} />}>
            <Route path="/dashboard" element={<LazyPage><DashboardPage /></LazyPage>} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.vehicles} />}>
            <Route path="/vehicles" element={<LazyPage><VehiclesPage /></LazyPage>} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.fuelLogs} />}>
            <Route path="/fuel-logs" element={<LazyPage><FuelLogsPage /></LazyPage>} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.routes} />}>
            <Route path="/routes" element={<LazyPage><RoutesPage /></LazyPage>} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.alerts} />}>
            <Route path="/alerts" element={<LazyPage><AlertsPage /></LazyPage>} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.reports} />}>
            <Route path="/reports" element={<LazyPage><ReportsPage /></LazyPage>} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<LazyPage><NotFoundPage /></LazyPage>} />
    </Routes>
  )
}
