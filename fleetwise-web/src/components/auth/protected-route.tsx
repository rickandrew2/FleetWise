import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/providers/auth-provider'
import { hasRoleAccess } from '@/lib/access'
import { Skeleton } from '@/components/ui/skeleton'
import type { UserRole } from '@/types/api'

interface ProtectedRouteProps {
  allowedRoles?: UserRole[]
}

export function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, isBootstrapping, user } = useAuth()
  const location = useLocation()

  if (isBootstrapping) {
    return (
      <main className="mx-auto flex min-h-screen w-full max-w-5xl items-center justify-center p-6">
        <div className="w-full max-w-md space-y-4">
          <Skeleton className="h-5 w-1/2" />
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-24 w-full" />
        </div>
      </main>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (allowedRoles && !hasRoleAccess(user?.role, allowedRoles)) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}
