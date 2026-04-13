import type { UserRole } from '@/types/api'

export const ACCESS_RULES = {
  dashboard: ['ADMIN', 'FLEET_MANAGER', 'DRIVER'],
  vehicles: ['ADMIN', 'FLEET_MANAGER'],
  fuelLogs: ['ADMIN', 'FLEET_MANAGER', 'DRIVER'],
  routes: ['ADMIN', 'FLEET_MANAGER', 'DRIVER'],
  alerts: ['ADMIN', 'FLEET_MANAGER', 'DRIVER'],
  reports: ['ADMIN', 'FLEET_MANAGER'],
  settings: ['ADMIN', 'FLEET_MANAGER', 'DRIVER'],
} as const satisfies Record<string, UserRole[]>

export function hasRoleAccess(userRole: UserRole | null | undefined, allowedRoles: UserRole[]) {
  if (!userRole) {
    return false
  }
  return allowedRoles.includes(userRole)
}
