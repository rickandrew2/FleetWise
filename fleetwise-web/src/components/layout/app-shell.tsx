import { useState } from 'react'
import { Activity, Car, Fuel, LayoutDashboard, MapPinned, Siren, FileStack, Menu, X } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ACCESS_RULES, hasRoleAccess } from '@/lib/access'
import { cn } from '@/lib/utils'
import { useAuth } from '@/providers/auth-provider'

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, allowedRoles: ACCESS_RULES.dashboard },
  { to: '/vehicles', label: 'Vehicles', icon: Car, allowedRoles: ACCESS_RULES.vehicles },
  { to: '/fuel-logs', label: 'Fuel Logs', icon: Fuel, allowedRoles: ACCESS_RULES.fuelLogs },
  { to: '/routes', label: 'Routes', icon: MapPinned, allowedRoles: ACCESS_RULES.routes },
  { to: '/alerts', label: 'Alerts', icon: Siren, allowedRoles: ACCESS_RULES.alerts },
  { to: '/reports', label: 'Reports', icon: FileStack, allowedRoles: ACCESS_RULES.reports },
]

export function AppShell() {
  const { user, logout } = useAuth()
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false)
  const visibleNavItems = navItems.filter((item) => hasRoleAccess(user?.role, item.allowedRoles))

  return (
    <div className="min-h-screen">
      <header className="border-b border-border/70 bg-white/75 backdrop-blur-md">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-primary/10 p-2 text-primary">
              <Activity className="h-5 w-5" />
            </div>
            <div>
              <p className="text-base font-semibold leading-tight">FleetWise</p>
              <p className="text-xs text-muted-foreground">Fuel intelligence dashboard</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="icon"
              className="min-h-11 min-w-11 sm:hidden"
              onClick={() => setIsMobileNavOpen((previous) => !previous)}
              aria-label={isMobileNavOpen ? 'Close navigation menu' : 'Open navigation menu'}
            >
              {isMobileNavOpen ? <X className="h-4 w-4" /> : <Menu className="h-4 w-4" />}
            </Button>
            <Badge variant="secondary" className="hidden sm:inline-flex">{user?.role}</Badge>
            <div className="hidden text-right sm:block">
              <p className="text-sm font-semibold">{user?.email}</p>
              <p className="text-xs text-muted-foreground">ID: {user?.userId}</p>
            </div>
            <Button variant="outline" size="sm" onClick={logout}>
              Log out
            </Button>
          </div>
        </div>
      </header>

      {isMobileNavOpen ? (
        <div
          className="fixed inset-0 z-40 bg-black/40 sm:hidden"
          onClick={() => setIsMobileNavOpen(false)}
          aria-hidden="true"
        >
          <aside
            className="h-full w-72 max-w-[85vw] border-r border-border/70 bg-background p-4 shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="mb-4 flex items-center justify-between">
              <p className="text-sm font-semibold">Navigation</p>
              <Button
                variant="ghost"
                size="icon"
                className="min-h-11 min-w-11"
                onClick={() => setIsMobileNavOpen(false)}
                aria-label="Close navigation drawer"
              >
                <X className="h-4 w-4" />
              </Button>
            </div>

            <nav className="flex flex-col gap-2">
              {visibleNavItems.map(({ to, label, icon: Icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  onClick={() => setIsMobileNavOpen(false)}
                  className={({ isActive }) =>
                    cn(
                      'inline-flex min-h-11 items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                      isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-secondary',
                    )
                  }
                >
                  <Icon className="h-4 w-4" />
                  {label}
                </NavLink>
              ))}
            </nav>
          </aside>
        </div>
      ) : null}

      <div className="mx-auto grid w-full max-w-6xl gap-6 px-4 py-6 sm:grid-cols-[220px_1fr] sm:px-6">
        <aside className="surface-panel hidden h-fit p-3 sm:block">
          <nav className="flex flex-col gap-2">
            {visibleNavItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  cn(
                    'inline-flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                    isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-secondary',
                  )
                }
              >
                <Icon className="h-4 w-4" />
                {label}
              </NavLink>
            ))}
          </nav>
        </aside>

        <main>
          <Outlet />
        </main>
      </div>
    </div>
  )
}
