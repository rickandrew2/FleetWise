import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell, CheckCircle2, Filter } from 'lucide-react'
import {
  alertUnreadCountRequest,
  alertsListRequest,
  markAlertAsReadRequest,
  parseApiError,
  vehiclesListRequest,
} from '@/lib/api'
import { notifyApiError, notifyError, notifySuccess } from '@/lib/notify'
import { useAuth } from '@/providers/auth-provider'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageEmptyState, PageErrorState, PageLoadingState } from '@/components/ui/page-state'
import type { AlertQueryFilters, AlertType } from '@/types/api'

const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
const alertTypeOptions: AlertType[] = ['OVERCONSUMPTION', 'HIGH_COST', 'MAINTENANCE_DUE', 'UNUSUAL_FILLUP']

type AlertDraftFilters = {
  alertType: AlertType | ''
  vehicleId: string
  driverId: string
  readState: 'all' | 'read' | 'unread'
}

const initialDraftFilters: AlertDraftFilters = {
  alertType: '',
  vehicleId: '',
  driverId: '',
  readState: 'all',
}

function toReadFilterValue(readState: AlertDraftFilters['readState']) {
  if (readState === 'read') {
    return true
  }
  if (readState === 'unread') {
    return false
  }
  return undefined
}

function toAppliedFilters(draft: AlertDraftFilters): AlertQueryFilters {
  return {
    alertType: draft.alertType,
    vehicleId: draft.vehicleId || undefined,
    driverId: draft.driverId || undefined,
    isRead: toReadFilterValue(draft.readState),
  }
}

function shortId(value: string | null) {
  if (!value) {
    return 'N/A'
  }
  return `${value.slice(0, 8)}...`
}

function formatAlertType(value: AlertType) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

export function AlertsPage() {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [draftFilters, setDraftFilters] = useState<AlertDraftFilters>(initialDraftFilters)
  const [appliedFilters, setAppliedFilters] = useState<AlertQueryFilters>(toAppliedFilters(initialDraftFilters))

  const vehiclesQuery = useQuery({
    queryKey: ['vehicles'],
    queryFn: vehiclesListRequest,
  })

  const alertsQuery = useQuery({
    queryKey: ['alerts', appliedFilters],
    queryFn: () => alertsListRequest(appliedFilters),
  })

  const unreadCountQuery = useQuery({
    queryKey: ['alertUnreadCount', appliedFilters.driverId || null],
    queryFn: () => alertUnreadCountRequest(appliedFilters.driverId),
  })

  const markReadMutation = useMutation({
    mutationFn: markAlertAsReadRequest,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['alerts'] }),
        queryClient.invalidateQueries({ queryKey: ['alertUnreadCount'] }),
      ])
      notifySuccess('Alert marked as read.')
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      notifyApiError(parsed, 'Failed to mark alert as read.')
    },
  })

  const vehicleLabelById = useMemo(() => {
    const data = vehiclesQuery.data ?? []
    return new Map(data.map((vehicle) => [vehicle.id, `${vehicle.plateNumber} • ${vehicle.make} ${vehicle.model}`]))
  }, [vehiclesQuery.data])

  const isLoading = vehiclesQuery.isLoading || alertsQuery.isLoading || unreadCountQuery.isLoading
  if (isLoading) {
    return <PageLoadingState />
  }

  const firstError = vehiclesQuery.error || alertsQuery.error || unreadCountQuery.error
  if (firstError) {
    const parsed = parseApiError(firstError)
    return (
      <PageErrorState
        title="Unable to load alerts"
        description={parsed.message || 'Unexpected API error.'}
        onRetry={() => {
          void vehiclesQuery.refetch()
          void alertsQuery.refetch()
          void unreadCountQuery.refetch()
        }}
      />
    )
  }

  const alerts = alertsQuery.data || []
  const unreadCount = unreadCountQuery.data?.unreadCount ?? 0

  function applyFilters() {
    if (draftFilters.vehicleId && !uuidRegex.test(draftFilters.vehicleId)) {
      notifyError('Vehicle ID filter must be a valid UUID.')
      return
    }

    if (draftFilters.driverId && !uuidRegex.test(draftFilters.driverId)) {
      notifyError('Driver ID filter must be a valid UUID.')
      return
    }

    setAppliedFilters(toAppliedFilters(draftFilters))
  }

  function resetFilters() {
    setDraftFilters(initialDraftFilters)
    setAppliedFilters(toAppliedFilters(initialDraftFilters))
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Alerts</h1>
          <p className="text-sm text-muted-foreground">Monitor active signals and acknowledge alerts as they are reviewed.</p>
        </div>
        <Card className="min-w-44">
          <CardContent className="flex items-center justify-between p-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-muted-foreground">Unread</p>
              <p className="text-2xl font-semibold">{unreadCount}</p>
            </div>
            <Bell className="h-5 w-5 text-primary" />
          </CardContent>
        </Card>
      </header>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Filter by type, target entity, and read state.</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-2">
            <Label htmlFor="alert-filter-type">Alert Type</Label>
            <select
              id="alert-filter-type"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={draftFilters.alertType}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, alertType: event.target.value as AlertType | '' }))}
            >
              <option value="">All Types</option>
              {alertTypeOptions.map((type) => (
                <option key={type} value={type}>{formatAlertType(type)}</option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="alert-filter-vehicle-id">Vehicle ID</Label>
            <Input
              id="alert-filter-vehicle-id"
              value={draftFilters.vehicleId}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, vehicleId: event.target.value.trim() }))}
              placeholder="Optional UUID"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="alert-filter-driver-id">Driver ID</Label>
            <Input
              id="alert-filter-driver-id"
              value={draftFilters.driverId}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, driverId: event.target.value.trim() }))}
              placeholder="Optional UUID"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="alert-filter-read-state">Read State</Label>
            <select
              id="alert-filter-read-state"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={draftFilters.readState}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, readState: event.target.value as AlertDraftFilters['readState'] }))}
            >
              <option value="all">All</option>
              <option value="unread">Unread</option>
              <option value="read">Read</option>
            </select>
          </div>

          <div className="sm:col-span-2 lg:col-span-4 flex flex-wrap justify-end gap-2">
            <Button type="button" variant="outline" onClick={resetFilters}>Reset</Button>
            <Button type="button" onClick={applyFilters} className="min-h-11 min-w-11">
              <Filter className="mr-2 h-4 w-4" />
              Apply Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Alert Feed</CardTitle>
          <CardDescription>Review alert context and mark items as read after triage.</CardDescription>
        </CardHeader>
        <CardContent>
          {alerts.length === 0 ? (
            <PageEmptyState
              title="No alerts found"
              description="No alerts match the current filter selection."
            />
          ) : (
            <div className="overflow-x-auto rounded-xl border border-border/80">
              <table className="min-w-full text-sm">
                <thead className="bg-secondary/40 text-left">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Triggered</th>
                    <th className="px-4 py-3 font-semibold">Type</th>
                    <th className="px-4 py-3 font-semibold">Message</th>
                    <th className="px-4 py-3 font-semibold">Vehicle</th>
                    <th className="px-4 py-3 font-semibold">Driver</th>
                    <th className="px-4 py-3 font-semibold">Status</th>
                    <th className="px-4 py-3 font-semibold">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {alerts.map((alert) => (
                    <tr key={alert.id} className="border-t border-border/80">
                      <td className="px-4 py-3">{new Date(alert.triggeredAt).toLocaleString()}</td>
                      <td className="px-4 py-3"><Badge variant="secondary">{formatAlertType(alert.alertType)}</Badge></td>
                      <td className="px-4 py-3">
                        <p className="font-medium">{alert.message}</p>
                        <p className="text-xs text-muted-foreground">
                          Threshold: {alert.thresholdValue ?? 'N/A'} • Actual: {alert.actualValue ?? 'N/A'}
                        </p>
                      </td>
                      <td className="px-4 py-3">{alert.vehicleId ? vehicleLabelById.get(alert.vehicleId) || shortId(alert.vehicleId) : 'N/A'}</td>
                      <td className="px-4 py-3">{shortId(alert.driverId)}</td>
                      <td className="px-4 py-3">
                        {alert.isRead ? (
                          <Badge>Read</Badge>
                        ) : (
                          <Badge className="bg-destructive text-destructive-foreground">Unread</Badge>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className="min-h-11"
                          disabled={alert.isRead || markReadMutation.isPending}
                          onClick={() => {
                            void markReadMutation.mutateAsync(alert.id)
                          }}
                          title={alert.isRead ? 'Already read' : 'Mark alert as read'}
                        >
                          <CheckCircle2 className="mr-1 h-3.5 w-3.5" />
                          Mark Read
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {user?.role === 'DRIVER' ? (
        <p className="text-xs text-muted-foreground">Driver role visibility is scoped by backend access policy.</p>
      ) : null}
    </div>
  )
}
