import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from '@/components/auth/protected-route'
import { AppShell } from '@/components/layout/app-shell'
import { ACCESS_RULES } from '@/lib/access'
import { ComingSoonPage } from '@/pages/coming-soon-page'
import { DashboardPage } from '@/pages/dashboard-page'
import { LoginPage } from '@/pages/login-page'
import { NotFoundPage } from '@/pages/not-found-page'
import { VehiclesPage } from '@/pages/vehicles-page'

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.dashboard} />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.vehicles} />}>
            <Route path="/vehicles" element={<VehiclesPage />} />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.fuelLogs} />}>
            <Route
              path="/fuel-logs"
              element={<ComingSoonPage title="Fuel Logs" description="Fuel log flows will follow dashboard delivery." />}
            />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.routes} />}>
            <Route
              path="/routes"
              element={<ComingSoonPage title="Routes" description="Route management UI is planned in next sprint." />}
            />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.alerts} />}>
            <Route
              path="/alerts"
              element={<ComingSoonPage title="Alerts" description="Alert workflow UI will be added in upcoming phase." />}
            />
          </Route>
          <Route element={<ProtectedRoute allowedRoles={ACCESS_RULES.reports} />}>
            <Route
              path="/reports"
              element={<ComingSoonPage title="Reports" description="Report generation and downloads are next-slice work." />}
            />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
