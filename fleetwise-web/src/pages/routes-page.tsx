import { useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Filter, Plus, Save, Trash2, X } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import {
  createRouteLogRequest,
  deleteRouteLogRequest,
  parseApiError,
  routeLogStatsRequest,
  routesListRequest,
  usersListRequest,
  vehiclesListRequest,
} from '@/lib/api'
import { notifyApiError, notifyError, notifySuccess } from '@/lib/notify'
import { useAuth } from '@/providers/auth-provider'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageEmptyState, PageErrorState, PageLoadingState } from '@/components/ui/page-state'
import type { LogQueryFilters, RouteLogResponse, RouteLogUpsertRequest, UserSummaryResponse } from '@/types/api'

const dateRegex = /^\d{4}-\d{2}-\d{2}$/

const routeLogFormSchema = z.object({
  vehicleId: z.string().uuid('Vehicle is required'),
  tripDate: z.string().regex(dateRegex, 'Trip date is required'),
  originLabel: z.string().max(150, 'Origin label is too long').optional(),
  originLat: z
    .string()
    .refine((value) => !Number.isNaN(Number(value)) && Number(value) >= -90 && Number(value) <= 90, {
      message: 'Origin latitude must be between -90 and 90',
    }),
  originLng: z
    .string()
    .refine((value) => !Number.isNaN(Number(value)) && Number(value) >= -180 && Number(value) <= 180, {
      message: 'Origin longitude must be between -180 and 180',
    }),
  destinationLabel: z.string().max(150, 'Destination label is too long').optional(),
  destinationLat: z
    .string()
    .refine((value) => !Number.isNaN(Number(value)) && Number(value) >= -90 && Number(value) <= 90, {
      message: 'Destination latitude must be between -90 and 90',
    }),
  destinationLng: z
    .string()
    .refine((value) => !Number.isNaN(Number(value)) && Number(value) >= -180 && Number(value) <= 180, {
      message: 'Destination longitude must be between -180 and 180',
    }),
  actualFuelUsedLiters: z
    .string()
    .optional()
    .refine((value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= 0), {
      message: 'Fuel used must be a non-negative number',
    }),
})

type RouteLogFormValues = z.infer<typeof routeLogFormSchema>

const initialRouteFormValues: RouteLogFormValues = {
  vehicleId: '',
  tripDate: new Date().toISOString().slice(0, 10),
  originLabel: '',
  originLat: '',
  originLng: '',
  destinationLabel: '',
  destinationLat: '',
  destinationLng: '',
  actualFuelUsedLiters: '',
}

const initialFilters: LogQueryFilters = {
  vehicleId: '',
  driverId: '',
  startDate: '',
  endDate: '',
}

function toRoutePayload(values: RouteLogFormValues): RouteLogUpsertRequest {
  return {
    vehicleId: values.vehicleId,
    tripDate: values.tripDate,
    originLabel: values.originLabel?.trim() || null,
    originLat: Number(values.originLat),
    originLng: Number(values.originLng),
    destinationLabel: values.destinationLabel?.trim() || null,
    destinationLat: Number(values.destinationLat),
    destinationLng: Number(values.destinationLng),
    actualFuelUsedLiters: values.actualFuelUsedLiters ? Number(values.actualFuelUsedLiters) : null,
  }
}

function toOneDecimal(value: number | null) {
  if (value == null) {
    return 'N/A'
  }
  return value.toFixed(1)
}

function shortId(value: string | null) {
  if (!value) {
    return 'N/A'
  }
  return `${value.slice(0, 8)}...`
}

export function RoutesPage() {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [draftFilters, setDraftFilters] = useState<LogQueryFilters>(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState<LogQueryFilters>(initialFilters)

  const vehiclesQuery = useQuery({
    queryKey: ['vehicles'],
    queryFn: vehiclesListRequest,
  })

  const canBrowseUsers = user?.role === 'ADMIN' || user?.role === 'FLEET_MANAGER'

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: usersListRequest,
    enabled: canBrowseUsers,
  })

  const usersLookupError = canBrowseUsers ? usersQuery.error : null

  const routesQuery = useQuery({
    queryKey: ['routes', appliedFilters],
    queryFn: () => routesListRequest(appliedFilters),
  })

  const routeStatsQuery = useQuery({
    queryKey: ['routeStats', appliedFilters],
    queryFn: () => routeLogStatsRequest(appliedFilters),
  })

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<RouteLogFormValues>({
    resolver: zodResolver(routeLogFormSchema),
    defaultValues: initialRouteFormValues,
  })

  const createMutation = useMutation({
    mutationFn: createRouteLogRequest,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['routes'] }),
        queryClient.invalidateQueries({ queryKey: ['routeStats'] }),
      ])
      notifySuccess('Route log created successfully.')
      closeCreateForm()
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      mapFieldErrors(parsed.fieldErrors)
      notifyApiError(parsed, 'Failed to create route log.')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: deleteRouteLogRequest,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['routes'] }),
        queryClient.invalidateQueries({ queryKey: ['routeStats'] }),
      ])
      notifySuccess('Route log deleted successfully.')
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      notifyApiError(parsed, 'Failed to delete route log.')
    },
  })

  const vehicleLabelById = useMemo(() => {
    const data = vehiclesQuery.data ?? []
    return new Map(data.map((vehicle) => [vehicle.id, `${vehicle.plateNumber} • ${vehicle.make} ${vehicle.model}`]))
  }, [vehiclesQuery.data])

  const isLoading = vehiclesQuery.isLoading || routesQuery.isLoading || routeStatsQuery.isLoading || (canBrowseUsers && usersQuery.isLoading)

  if (isLoading) {
    return <PageLoadingState />
  }

  const firstError = vehiclesQuery.error || routesQuery.error || routeStatsQuery.error
  if (firstError) {
    const parsed = parseApiError(firstError)
    return (
      <PageErrorState
        title="Unable to load routes"
        description={parsed.message || 'Unexpected API error.'}
        onRetry={() => {
          void vehiclesQuery.refetch()
          void routesQuery.refetch()
          void routeStatsQuery.refetch()
        }}
      />
    )
  }

  const usersLookupErrorMessage = usersLookupError
    ? parseApiError(usersLookupError).message || 'Unable to load driver list for filtering.'
    : null

  const vehicles = vehiclesQuery.data ?? []
  const availableDrivers: UserSummaryResponse[] = canBrowseUsers
    ? (usersQuery.data && usersQuery.data.length > 0
      ? usersQuery.data
      : user
        ? [{ id: user.userId, name: user.email, email: user.email, role: user.role }]
        : [])
    : user
      ? [{ id: user.userId, name: user.email, email: user.email, role: user.role }]
      : []
  const routes = routesQuery.data || []
  const stats = routeStatsQuery.data
  const canDelete = user?.role === 'ADMIN'

  function mapFieldErrors(fieldErrors?: Record<string, string>) {
    if (!fieldErrors) {
      return
    }

    Object.entries(fieldErrors).forEach(([field, message]) => {
      if (field in initialRouteFormValues) {
        setError(field as keyof RouteLogFormValues, { message })
      }
    })
  }

  function closeCreateForm() {
    setIsCreateOpen(false)
    reset(initialRouteFormValues)
  }

  function applyFilters() {
    if (draftFilters.startDate && !dateRegex.test(draftFilters.startDate)) {
      notifyError('Start date must be in YYYY-MM-DD format.')
      return
    }

    if (draftFilters.endDate && !dateRegex.test(draftFilters.endDate)) {
      notifyError('End date must be in YYYY-MM-DD format.')
      return
    }

    if (draftFilters.startDate && draftFilters.endDate && draftFilters.startDate > draftFilters.endDate) {
      notifyError('Start date must be before end date.')
      return
    }

    setAppliedFilters({ ...draftFilters })
  }

  function resetFilters() {
    setDraftFilters(initialFilters)
    setAppliedFilters(initialFilters)
  }

  const onSubmit = handleSubmit(async (values) => {
    const payload = {
      ...toRoutePayload(values),
      driverId: user?.userId ?? null,
    }
    await createMutation.mutateAsync(payload)
  })

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Routes</h1>
          <p className="text-sm text-muted-foreground">Capture trip coordinates, track route performance, and audit fuel efficiency.</p>
        </div>

        <Button type="button" onClick={() => setIsCreateOpen(true)} className="min-h-11 min-w-11">
          <Plus className="mr-2 h-4 w-4" />
          Add Route
        </Button>
      </header>

      <section className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader>
            <CardDescription>Total Trips</CardDescription>
            <CardTitle>{stats?.totalTrips ?? 0}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardDescription>Total Distance</CardDescription>
            <CardTitle>{toOneDecimal(stats?.totalDistanceKm ?? null)} km</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardDescription>Average Efficiency Score</CardDescription>
            <CardTitle>{toOneDecimal(stats?.averageEfficiencyScore ?? null)}</CardTitle>
          </CardHeader>
        </Card>
      </section>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Filter by vehicle, driver, and trip dates.</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-2">
            <Label htmlFor="route-filter-vehicle-id">Vehicle</Label>
            <select
              id="route-filter-vehicle-id"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={draftFilters.vehicleId || ''}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, vehicleId: event.target.value }))}
            >
              <option value="">All vehicles</option>
              {vehicles.map((vehicle) => (
                <option key={vehicle.id} value={vehicle.id}>
                  {vehicle.plateNumber} • {vehicle.make} {vehicle.model}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="route-filter-driver-id">Driver</Label>
            <select
              id="route-filter-driver-id"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={draftFilters.driverId || ''}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, driverId: event.target.value }))}
            >
              <option value="">All drivers</option>
              {availableDrivers.map((driver) => (
                <option key={driver.id} value={driver.id}>
                  {(driver.name || 'Unknown')} • {driver.email}
                </option>
              ))}
            </select>
            {usersLookupErrorMessage ? (
              <p className="text-xs text-muted-foreground">Driver directory is temporarily unavailable: {usersLookupErrorMessage}</p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="route-filter-start-date">Start Date</Label>
            <Input
              id="route-filter-start-date"
              type="date"
              value={draftFilters.startDate || ''}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, startDate: event.target.value }))}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="route-filter-end-date">End Date</Label>
            <Input
              id="route-filter-end-date"
              type="date"
              value={draftFilters.endDate || ''}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, endDate: event.target.value }))}
            />
          </div>

          <div className="sm:col-span-2 lg:col-span-4 flex flex-wrap justify-end gap-2">
            <Button type="button" variant="outline" onClick={resetFilters}>
              Reset
            </Button>
            <Button type="button" onClick={applyFilters} className="min-h-11 min-w-11">
              <Filter className="mr-2 h-4 w-4" />
              Apply Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Route Activity</CardTitle>
          <CardDescription>Most recent routes for the selected filters.</CardDescription>
        </CardHeader>
        <CardContent>
          {routes.length === 0 ? (
            <PageEmptyState
              title="No routes found"
              description="Create a route entry or adjust filters to see results."
              action={
                <Button type="button" onClick={() => setIsCreateOpen(true)}>
                  <Plus className="mr-2 h-4 w-4" />
                  Add first route
                </Button>
              }
            />
          ) : (
            <div className="overflow-x-auto rounded-xl border border-border/80">
              <table className="min-w-full text-sm">
                <thead className="bg-secondary/40 text-left">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Date</th>
                    <th className="px-4 py-3 font-semibold">Vehicle</th>
                    <th className="px-4 py-3 font-semibold">Driver</th>
                    <th className="px-4 py-3 font-semibold">Origin</th>
                    <th className="px-4 py-3 font-semibold">Destination</th>
                    <th className="px-4 py-3 font-semibold">Distance</th>
                    <th className="px-4 py-3 font-semibold">Efficiency</th>
                    <th className="px-4 py-3 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {routes.map((route: RouteLogResponse) => (
                    <tr key={route.id} className="border-t border-border/80">
                      <td className="px-4 py-3">{route.tripDate}</td>
                      <td className="px-4 py-3">{vehicleLabelById.get(route.vehicleId) || shortId(route.vehicleId)}</td>
                      <td className="px-4 py-3">{shortId(route.driverId)}</td>
                      <td className="px-4 py-3">{route.originLabel || `${route.originLat.toFixed(4)}, ${route.originLng.toFixed(4)}`}</td>
                      <td className="px-4 py-3">{route.destinationLabel || `${route.destinationLat.toFixed(4)}, ${route.destinationLng.toFixed(4)}`}</td>
                      <td className="px-4 py-3">{toOneDecimal(route.distanceKm)} km</td>
                      <td className="px-4 py-3">{toOneDecimal(route.efficiencyScore)}</td>
                      <td className="px-4 py-3">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className="min-h-11 border-destructive/50 text-destructive hover:bg-destructive/10"
                          disabled={!canDelete || deleteMutation.isPending}
                          title={canDelete ? 'Delete route log' : 'Only admins can delete routes'}
                          onClick={() => {
                            const confirmed = window.confirm('Delete this route log? This action cannot be undone.')
                            if (confirmed) {
                              void deleteMutation.mutateAsync(route.id)
                            }
                          }}
                        >
                          <Trash2 className="mr-1 h-3.5 w-3.5" />
                          Delete
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

      {isCreateOpen ? (
        <Card>
          <CardHeader>
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <CardTitle>Add route</CardTitle>
                <CardDescription>Provide route coordinates and optional metadata.</CardDescription>
              </div>
              <Button type="button" variant="ghost" onClick={closeCreateForm}>
                <X className="mr-2 h-4 w-4" />
                Close
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <form className="grid gap-4 sm:grid-cols-2" onSubmit={onSubmit} noValidate>
              <div className="space-y-2">
                <Label htmlFor="vehicleId">Vehicle</Label>
                <select id="vehicleId" className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm" {...register('vehicleId')}>
                  <option value="">Select a vehicle</option>
                  {vehicles.map((vehicle) => (
                    <option key={vehicle.id} value={vehicle.id}>
                      {vehicle.plateNumber} • {vehicle.make} {vehicle.model}
                    </option>
                  ))}
                </select>
                {errors.vehicleId ? <p className="text-sm text-destructive">{errors.vehicleId.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="tripDate">Trip Date</Label>
                <Input id="tripDate" type="date" {...register('tripDate')} />
                {errors.tripDate ? <p className="text-sm text-destructive">{errors.tripDate.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="originLabel">Origin Label</Label>
                <Input id="originLabel" autoComplete="off" placeholder="Optional" {...register('originLabel')} />
                {errors.originLabel ? <p className="text-sm text-destructive">{errors.originLabel.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="destinationLabel">Destination Label</Label>
                <Input id="destinationLabel" autoComplete="off" placeholder="Optional" {...register('destinationLabel')} />
                {errors.destinationLabel ? <p className="text-sm text-destructive">{errors.destinationLabel.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="originLat">Origin Latitude</Label>
                <Input id="originLat" type="number" inputMode="decimal" step="0.0001" {...register('originLat')} />
                {errors.originLat ? <p className="text-sm text-destructive">{errors.originLat.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="originLng">Origin Longitude</Label>
                <Input id="originLng" type="number" inputMode="decimal" step="0.0001" {...register('originLng')} />
                {errors.originLng ? <p className="text-sm text-destructive">{errors.originLng.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="destinationLat">Destination Latitude</Label>
                <Input id="destinationLat" type="number" inputMode="decimal" step="0.0001" {...register('destinationLat')} />
                {errors.destinationLat ? <p className="text-sm text-destructive">{errors.destinationLat.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="destinationLng">Destination Longitude</Label>
                <Input id="destinationLng" type="number" inputMode="decimal" step="0.0001" {...register('destinationLng')} />
                {errors.destinationLng ? <p className="text-sm text-destructive">{errors.destinationLng.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="actualFuelUsedLiters">Actual Fuel Used (L)</Label>
                <Input id="actualFuelUsedLiters" type="number" inputMode="decimal" step="0.01" {...register('actualFuelUsedLiters')} />
                {errors.actualFuelUsedLiters ? <p className="text-sm text-destructive">{errors.actualFuelUsedLiters.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label>Driver</Label>
                <Input value={user?.email || 'Unknown user'} readOnly disabled />
              </div>

              <div className="sm:col-span-2 flex flex-wrap justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={closeCreateForm}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting || createMutation.isPending} className="min-h-11">
                  <Save className="mr-2 h-4 w-4" />
                  Save Route
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : null}
    </div>
  )
}
