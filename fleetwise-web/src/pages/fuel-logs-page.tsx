import { useEffect, useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowUpDown, Filter, Plus, Save, Trash2, X } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import {
  createFuelLogRequest,
  deleteFuelLogRequest,
  fuelPriceCurrentByTypeRequest,
  fuelLogsListRequest,
  fuelLogStatsRequest,
  parseApiError,
  usersListRequest,
  vehiclesListRequest,
} from '@/lib/api'
import { formatPhpCurrency } from '@/lib/currency'
import { notifyApiError, notifyError, notifySuccess } from '@/lib/notify'
import { useAuth } from '@/providers/auth-provider'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageEmptyState, PageErrorState, PageLoadingState } from '@/components/ui/page-state'
import type { FuelLogResponse, FuelLogUpsertRequest, LogQueryFilters, UserSummaryResponse } from '@/types/api'

const dateRegex = /^\d{4}-\d{2}-\d{2}$/

const fuelLogFormSchema = z.object({
  vehicleId: z.string().uuid('Vehicle is required'),
  logDate: z.string().regex(dateRegex, 'Log date is required'),
  odometerReadingKm: z
    .string()
    .optional()
    .refine((value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= 0), {
      message: 'Odometer must be a non-negative number',
    }),
  litersFilled: z
    .string()
    .refine((value) => !Number.isNaN(Number(value)) && Number(value) > 0, { message: 'Liters must be greater than zero' }),
  pricePerLiter: z
    .string()
    .refine((value) => !Number.isNaN(Number(value)) && Number(value) > 0, { message: 'Price per liter must be greater than zero' }),
  stationName: z.string().max(100, 'Station name is too long').optional(),
  stationLat: z
    .string()
    .optional()
    .refine((value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= -90 && Number(value) <= 90), {
      message: 'Latitude must be between -90 and 90',
    }),
  stationLng: z
    .string()
    .optional()
    .refine((value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= -180 && Number(value) <= 180), {
      message: 'Longitude must be between -180 and 180',
    }),
  notes: z.string().max(2000, 'Notes are too long').optional(),
})

type FuelLogFormValues = z.infer<typeof fuelLogFormSchema>

const initialFuelLogFormValues: FuelLogFormValues = {
  vehicleId: '',
  logDate: new Date().toISOString().slice(0, 10),
  odometerReadingKm: '',
  litersFilled: '',
  pricePerLiter: '',
  stationName: '',
  stationLat: '',
  stationLng: '',
  notes: '',
}

const initialFilters: LogQueryFilters = {
  vehicleId: '',
  driverId: '',
  startDate: '',
  endDate: '',
}

type FuelLogSortField = 'logDate' | 'litersFilled' | 'totalCost' | 'odometerReadingKm'
type SortDirection = 'asc' | 'desc'
const EMPTY_FUEL_LOGS: FuelLogResponse[] = []

function toFuelLogPayload(values: FuelLogFormValues): FuelLogUpsertRequest {
  return {
    vehicleId: values.vehicleId,
    logDate: values.logDate,
    odometerReadingKm: values.odometerReadingKm ? Number(values.odometerReadingKm) : null,
    litersFilled: Number(values.litersFilled),
    pricePerLiter: Number(values.pricePerLiter),
    stationName: values.stationName?.trim() || null,
    stationLat: values.stationLat ? Number(values.stationLat) : null,
    stationLng: values.stationLng ? Number(values.stationLng) : null,
    notes: values.notes?.trim() || null,
  }
}

function formatDistance(value: number | null) {
  if (value == null) {
    return 'N/A'
  }
  return `${value.toFixed(1)} km`
}

function shortId(value: string | null) {
  if (!value) {
    return 'N/A'
  }
  return `${value.slice(0, 8)}...`
}

export function FuelLogsPage() {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [draftFilters, setDraftFilters] = useState<LogQueryFilters>(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState<LogQueryFilters>(initialFilters)
  const [sortField, setSortField] = useState<FuelLogSortField>('logDate')
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc')
  const [pageSize, setPageSize] = useState(10)
  const [currentPage, setCurrentPage] = useState(1)
  const [autoFilledFuelType, setAutoFilledFuelType] = useState<string | null>(null)

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

  const fuelLogsQuery = useQuery({
    queryKey: ['fuelLogs', appliedFilters],
    queryFn: () => fuelLogsListRequest(appliedFilters),
  })

  const fuelLogStatsQuery = useQuery({
    queryKey: ['fuelLogStats', appliedFilters],
    queryFn: () => fuelLogStatsRequest(appliedFilters),
  })

  const {
    register,
    handleSubmit,
    reset,
    setError,
    setValue,
    getValues,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FuelLogFormValues>({
    resolver: zodResolver(fuelLogFormSchema),
    defaultValues: initialFuelLogFormValues,
  })

  const selectedVehicleId = watch('vehicleId')

  const selectedVehicleFuelType = useMemo(() => {
    const selectedVehicle = vehiclesQuery.data?.find((vehicle) => vehicle.id === selectedVehicleId)
    return selectedVehicle?.fuelType?.toUpperCase() ?? null
  }, [selectedVehicleId, vehiclesQuery.data])

  const selectedFuelPriceQuery = useQuery({
    queryKey: ['fuel-prices', 'current', selectedVehicleFuelType],
    queryFn: () => fuelPriceCurrentByTypeRequest(selectedVehicleFuelType || ''),
    enabled: Boolean(isCreateOpen && selectedVehicleFuelType),
  })

  const createMutation = useMutation({
    mutationFn: createFuelLogRequest,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['fuelLogs'] }),
        queryClient.invalidateQueries({ queryKey: ['fuelLogStats'] }),
      ])
      notifySuccess('Fuel log created successfully.')
      closeCreateForm()
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      mapFieldErrors(parsed.fieldErrors)
      notifyApiError(parsed, 'Failed to create fuel log.')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: deleteFuelLogRequest,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['fuelLogs'] }),
        queryClient.invalidateQueries({ queryKey: ['fuelLogStats'] }),
      ])
      notifySuccess('Fuel log deleted successfully.')
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      notifyApiError(parsed, 'Failed to delete fuel log.')
    },
  })

  const vehicleLabelById = useMemo(() => {
    const data = vehiclesQuery.data ?? []
    return new Map(data.map((vehicle) => [vehicle.id, `${vehicle.plateNumber} • ${vehicle.make} ${vehicle.model}`]))
  }, [vehiclesQuery.data])

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
  const fuelLogs = fuelLogsQuery.data ?? EMPTY_FUEL_LOGS
  const stats = fuelLogStatsQuery.data
  const canDelete = user?.role === 'ADMIN'

  const usersLookupErrorMessage = usersLookupError
    ? parseApiError(usersLookupError).message || 'Unable to load driver list for filtering.'
    : null

  useEffect(() => {
    if (!isCreateOpen || !selectedFuelPriceQuery.data || !selectedVehicleFuelType) {
      return
    }

    if (autoFilledFuelType === selectedVehicleFuelType) {
      return
    }

    const currentValue = getValues('pricePerLiter')
    if (currentValue && Number(currentValue) > 0) {
      return
    }

    setValue('pricePerLiter', selectedFuelPriceQuery.data.pricePerLiter.toFixed(2), {
      shouldValidate: true,
      shouldDirty: false,
      shouldTouch: true,
    })
    setAutoFilledFuelType(selectedVehicleFuelType)
  }, [
    autoFilledFuelType,
    getValues,
    isCreateOpen,
    selectedFuelPriceQuery.data,
    selectedVehicleFuelType,
    setValue,
  ])

  const sortedFuelLogs = useMemo(() => {
    const data = [...fuelLogs]

    data.sort((a, b) => {
      const leftValue =
        sortField === 'odometerReadingKm' ? (a.odometerReadingKm ?? Number.NEGATIVE_INFINITY) : a[sortField]
      const rightValue =
        sortField === 'odometerReadingKm' ? (b.odometerReadingKm ?? Number.NEGATIVE_INFINITY) : b[sortField]

      if (leftValue < rightValue) {
        return sortDirection === 'asc' ? -1 : 1
      }

      if (leftValue > rightValue) {
        return sortDirection === 'asc' ? 1 : -1
      }

      return 0
    })

    return data
  }, [fuelLogs, sortDirection, sortField])

  const totalRows = sortedFuelLogs.length
  const totalPages = Math.max(1, Math.ceil(totalRows / pageSize))
  const normalizedCurrentPage = Math.min(currentPage, totalPages)

  const pageStartIndex = (normalizedCurrentPage - 1) * pageSize
  const paginatedFuelLogs = sortedFuelLogs.slice(pageStartIndex, pageStartIndex + pageSize)
  const rangeStart = totalRows === 0 ? 0 : pageStartIndex + 1
  const rangeEnd = Math.min(totalRows, pageStartIndex + paginatedFuelLogs.length)

  const isLoading = vehiclesQuery.isLoading || fuelLogsQuery.isLoading || fuelLogStatsQuery.isLoading || (canBrowseUsers && usersQuery.isLoading)

  if (isLoading) {
    return <PageLoadingState />
  }

  const firstError = vehiclesQuery.error || fuelLogsQuery.error || fuelLogStatsQuery.error
  if (firstError) {
    const parsed = parseApiError(firstError)
    return (
      <PageErrorState
        title="Unable to load fuel logs"
        description={parsed.message || 'Unexpected API error.'}
        onRetry={() => {
          void vehiclesQuery.refetch()
          void fuelLogsQuery.refetch()
          void fuelLogStatsQuery.refetch()
        }}
      />
    )
  }

  function mapFieldErrors(fieldErrors?: Record<string, string>) {
    if (!fieldErrors) {
      return
    }

    Object.entries(fieldErrors).forEach(([field, message]) => {
      if (field in initialFuelLogFormValues) {
        setError(field as keyof FuelLogFormValues, { message })
      }
    })
  }

  function closeCreateForm() {
    setIsCreateOpen(false)
    setAutoFilledFuelType(null)
    reset(initialFuelLogFormValues)
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
    setCurrentPage(1)
  }

  function resetFilters() {
    setDraftFilters(initialFilters)
    setAppliedFilters(initialFilters)
    setCurrentPage(1)
  }

  function toggleSort(field: FuelLogSortField) {
    if (sortField === field) {
      setSortDirection((previous) => (previous === 'asc' ? 'desc' : 'asc'))
      return
    }

    setSortField(field)
    setSortDirection('desc')
    setCurrentPage(1)
  }

  function getSortLabel(field: FuelLogSortField) {
    if (sortField !== field) {
      return 'Not sorted'
    }

    return sortDirection === 'asc' ? 'Sorted ascending' : 'Sorted descending'
  }

  const onSubmit = handleSubmit(async (values) => {
    const payload = {
      ...toFuelLogPayload(values),
      driverId: user?.userId ?? null,
    }
    await createMutation.mutateAsync(payload)
  })

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Fuel Logs</h1>
          <p className="text-sm text-muted-foreground">Track fuel events, monitor cost, and audit station-level details.</p>
        </div>

        <Button type="button" onClick={() => setIsCreateOpen(true)} className="min-h-11 min-w-11">
          <Plus className="mr-2 h-4 w-4" />
          Add Fuel Log
        </Button>
      </header>

      <section className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader>
            <CardDescription>Total Logs</CardDescription>
            <CardTitle>{stats?.totalLogs ?? 0}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardDescription>Total Cost</CardDescription>
            <CardTitle>{formatPhpCurrency(stats?.totalCost ?? 0)}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardDescription>Average Liters / Log</CardDescription>
            <CardTitle>{stats?.averageLitersPerLog != null ? `${stats.averageLitersPerLog.toFixed(2)} L` : 'N/A'}</CardTitle>
          </CardHeader>
        </Card>
      </section>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Filter by vehicle, driver, and date range.</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-2">
            <Label htmlFor="fuel-filter-vehicle-id">Vehicle</Label>
            <select
              id="fuel-filter-vehicle-id"
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
            <Label htmlFor="fuel-filter-driver-id">Driver</Label>
            <select
              id="fuel-filter-driver-id"
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
            <Label htmlFor="fuel-filter-start-date">Start Date</Label>
            <Input
              id="fuel-filter-start-date"
              type="date"
              value={draftFilters.startDate || ''}
              onChange={(event) => setDraftFilters((prev) => ({ ...prev, startDate: event.target.value }))}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="fuel-filter-end-date">End Date</Label>
            <Input
              id="fuel-filter-end-date"
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
          <CardTitle>Fuel Activity</CardTitle>
          <CardDescription>Most recent fuel events for the selected filters.</CardDescription>
        </CardHeader>
        <CardContent>
          {fuelLogs.length === 0 ? (
            <PageEmptyState
              title="No fuel logs found"
              description="Create a fuel log or adjust filters to see results."
              action={
                <Button type="button" onClick={() => setIsCreateOpen(true)}>
                  <Plus className="mr-2 h-4 w-4" />
                  Add first fuel log
                </Button>
              }
            />
          ) : (
            <div className="space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <p className="text-sm text-muted-foreground">
                  Showing {rangeStart}-{rangeEnd} of {totalRows} logs
                </p>

                <div className="flex items-center gap-2">
                  <Label htmlFor="fuel-logs-page-size" className="text-xs text-muted-foreground">
                    Rows per page
                  </Label>
                  <select
                    id="fuel-logs-page-size"
                    className="flex h-9 rounded-md border border-input bg-background px-2 py-1 text-sm"
                    value={pageSize}
                    onChange={(event) => {
                      const nextSize = Number(event.target.value)
                      setPageSize(nextSize)
                      setCurrentPage(1)
                    }}
                  >
                    <option value={10}>10</option>
                    <option value={25}>25</option>
                    <option value={50}>50</option>
                  </select>
                </div>
              </div>

              <div className="overflow-x-auto rounded-xl border border-border/80">
              <table className="min-w-full text-sm">
                <thead className="bg-secondary/40 text-left">
                  <tr>
                    <th className="px-4 py-3 font-semibold">
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 text-left"
                        onClick={() => toggleSort('logDate')}
                        aria-label={`Sort by date. ${getSortLabel('logDate')}`}
                      >
                        Date
                        <ArrowUpDown className="h-3.5 w-3.5 text-muted-foreground" />
                      </button>
                    </th>
                    <th className="px-4 py-3 font-semibold">Vehicle</th>
                    <th className="px-4 py-3 font-semibold">Driver</th>
                    <th className="px-4 py-3 font-semibold">
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 text-left"
                        onClick={() => toggleSort('litersFilled')}
                        aria-label={`Sort by liters. ${getSortLabel('litersFilled')}`}
                      >
                        Liters
                        <ArrowUpDown className="h-3.5 w-3.5 text-muted-foreground" />
                      </button>
                    </th>
                    <th className="px-4 py-3 font-semibold">
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 text-left"
                        onClick={() => toggleSort('totalCost')}
                        aria-label={`Sort by total cost. ${getSortLabel('totalCost')}`}
                      >
                        Total Cost
                        <ArrowUpDown className="h-3.5 w-3.5 text-muted-foreground" />
                      </button>
                    </th>
                    <th className="px-4 py-3 font-semibold">Station</th>
                    <th className="px-4 py-3 font-semibold">
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 text-left"
                        onClick={() => toggleSort('odometerReadingKm')}
                        aria-label={`Sort by odometer. ${getSortLabel('odometerReadingKm')}`}
                      >
                        Odometer
                        <ArrowUpDown className="h-3.5 w-3.5 text-muted-foreground" />
                      </button>
                    </th>
                    <th className="px-4 py-3 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedFuelLogs.map((log: FuelLogResponse) => (
                    <tr key={log.id} className="border-t border-border/80">
                      <td className="px-4 py-3">{log.logDate}</td>
                      <td className="px-4 py-3">{vehicleLabelById.get(log.vehicleId) || shortId(log.vehicleId)}</td>
                      <td className="px-4 py-3">{shortId(log.driverId)}</td>
                      <td className="px-4 py-3">{log.litersFilled.toFixed(2)} L</td>
                      <td className="px-4 py-3">{formatPhpCurrency(log.totalCost)}</td>
                      <td className="px-4 py-3">{log.stationName || 'N/A'}</td>
                      <td className="px-4 py-3">{formatDistance(log.odometerReadingKm)}</td>
                      <td className="px-4 py-3">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className="min-h-11 border-destructive/50 text-destructive hover:bg-destructive/10"
                          disabled={!canDelete || deleteMutation.isPending}
                          title={canDelete ? 'Delete fuel log' : 'Only admins can delete fuel logs'}
                          onClick={() => {
                            const confirmed = window.confirm('Delete this fuel log? This action cannot be undone.')
                            if (confirmed) {
                              void deleteMutation.mutateAsync(log.id)
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

              <div className="flex flex-wrap items-center justify-end gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  disabled={normalizedCurrentPage <= 1}
                  onClick={() => setCurrentPage(Math.max(1, normalizedCurrentPage - 1))}
                >
                  Previous
                </Button>
                <p className="text-sm text-muted-foreground">
                  Page {normalizedCurrentPage} of {totalPages}
                </p>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  disabled={normalizedCurrentPage >= totalPages}
                  onClick={() => setCurrentPage(Math.min(totalPages, normalizedCurrentPage + 1))}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {isCreateOpen ? (
        <Card>
          <CardHeader>
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <CardTitle>Add fuel log</CardTitle>
                <CardDescription>Fill in required fields and optional station details.</CardDescription>
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
                <Label htmlFor="logDate">Log Date</Label>
                <Input id="logDate" type="date" {...register('logDate')} />
                {errors.logDate ? <p className="text-sm text-destructive">{errors.logDate.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="litersFilled">Liters Filled</Label>
                <Input id="litersFilled" type="number" inputMode="decimal" step="0.01" {...register('litersFilled')} />
                {errors.litersFilled ? <p className="text-sm text-destructive">{errors.litersFilled.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="pricePerLiter">Price Per Liter</Label>
                <Input id="pricePerLiter" type="number" inputMode="decimal" step="0.01" {...register('pricePerLiter')} />
                {errors.pricePerLiter ? <p className="text-sm text-destructive">{errors.pricePerLiter.message}</p> : null}
                {selectedFuelPriceQuery.data ? (
                  <p className="text-xs text-muted-foreground">Auto-filled from DOE weekly advisory - verify before saving.</p>
                ) : null}
                {selectedFuelPriceQuery.data?.stale ? (
                  <p className="text-xs text-amber-700">Using last successful update. Data may be stale.</p>
                ) : null}
                {selectedFuelPriceQuery.error ? (
                  <p className="text-xs text-muted-foreground">Unable to auto-fill current fuel price for this vehicle type.</p>
                ) : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="odometerReadingKm">Odometer (km)</Label>
                <Input id="odometerReadingKm" type="number" inputMode="decimal" step="0.1" {...register('odometerReadingKm')} />
                {errors.odometerReadingKm ? <p className="text-sm text-destructive">{errors.odometerReadingKm.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label>Driver</Label>
                <Input value={user?.email || 'Unknown user'} readOnly disabled />
              </div>

              <div className="space-y-2">
                <Label htmlFor="stationName">Station Name</Label>
                <Input id="stationName" autoComplete="off" {...register('stationName')} />
                {errors.stationName ? <p className="text-sm text-destructive">{errors.stationName.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="stationLat">Station Latitude</Label>
                <Input id="stationLat" type="number" inputMode="decimal" step="0.0001" {...register('stationLat')} />
                {errors.stationLat ? <p className="text-sm text-destructive">{errors.stationLat.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="stationLng">Station Longitude</Label>
                <Input id="stationLng" type="number" inputMode="decimal" step="0.0001" {...register('stationLng')} />
                {errors.stationLng ? <p className="text-sm text-destructive">{errors.stationLng.message}</p> : null}
              </div>

              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="notes">Notes</Label>
                <textarea
                  id="notes"
                  rows={3}
                  className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  {...register('notes')}
                />
                {errors.notes ? <p className="text-sm text-destructive">{errors.notes.message}</p> : null}
              </div>

              <div className="sm:col-span-2 flex flex-wrap justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={closeCreateForm}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting || createMutation.isPending} className="min-h-11">
                  <Save className="mr-2 h-4 w-4" />
                  Save Fuel Log
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : null}
    </div>
  )
}
