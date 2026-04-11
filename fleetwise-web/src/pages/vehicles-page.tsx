import { useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, Pencil, Trash2, Search, Save, X } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import {
  createVehicleRequest,
  deleteVehicleRequest,
  parseApiError,
  updateVehicleRequest,
  vehiclesListRequest,
} from '@/lib/api'
import { notifyApiError, notifySuccess } from '@/lib/notify'
import { useAuth } from '@/providers/auth-provider'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageEmptyState, PageErrorState, PageLoadingState } from '@/components/ui/page-state'
import type { VehicleResponse, VehicleUpsertRequest } from '@/types/api'

const vehicleFormSchema = z.object({
  plateNumber: z.string().min(1, 'Plate number is required').max(20, 'Plate number is too long'),
  make: z.string().min(1, 'Make is required').max(50, 'Make is too long'),
  model: z.string().min(1, 'Model is required').max(50, 'Model is too long'),
  year: z
    .string()
    .refine((value) => !Number.isNaN(Number(value)), { message: 'Year is required' })
    .refine((value) => {
      const yearValue = Number(value)
      return yearValue >= 1980 && yearValue <= 2100
    }, { message: 'Year must be between 1980 and 2100' }),
  fuelType: z.string().max(30, 'Fuel type is too long').optional(),
  tankCapacityLiters: z
    .string()
    .optional()
    .refine((value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= 1 && Number(value) <= 1000), {
      message: 'Tank capacity must be between 1 and 1000 liters',
    }),
  epaVehicleId: z
    .string()
    .optional()
    .refine((value) => !value || (!Number.isNaN(Number(value)) && Number(value) > 0), {
      message: 'EPA vehicle ID must be a positive number',
    }),
})

type VehicleFormValues = z.infer<typeof vehicleFormSchema>

const initialVehicleFormValues: VehicleFormValues = {
  plateNumber: '',
  make: '',
  model: '',
  year: String(new Date().getFullYear()),
  fuelType: '',
  tankCapacityLiters: '',
  epaVehicleId: '',
}

function toVehiclePayload(values: VehicleFormValues): VehicleUpsertRequest {
  return {
    plateNumber: values.plateNumber.trim().toUpperCase(),
    make: values.make.trim(),
    model: values.model.trim(),
    year: Number(values.year),
    fuelType: values.fuelType?.trim() || null,
    tankCapacityLiters: values.tankCapacityLiters ? Number(values.tankCapacityLiters) : null,
    epaVehicleId: values.epaVehicleId ? Number(values.epaVehicleId) : null,
    assignedDriverId: null,
  }
}

function toFormValues(vehicle: VehicleResponse): VehicleFormValues {
  return {
    plateNumber: vehicle.plateNumber,
    make: vehicle.make,
    model: vehicle.model,
    year: String(vehicle.year),
    fuelType: vehicle.fuelType || '',
    tankCapacityLiters: vehicle.tankCapacityLiters?.toString() || '',
    epaVehicleId: vehicle.epaVehicleId?.toString() || '',
  }
}

export function VehiclesPage() {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [searchTerm, setSearchTerm] = useState('')
  const [editingVehicle, setEditingVehicle] = useState<VehicleResponse | null>(null)
  const [formVisible, setFormVisible] = useState(false)

  const vehiclesQuery = useQuery({
    queryKey: ['vehicles'],
    queryFn: vehiclesListRequest,
  })

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<VehicleFormValues>({
    resolver: zodResolver(vehicleFormSchema),
    defaultValues: initialVehicleFormValues,
  })

  const createMutation = useMutation({
    mutationFn: createVehicleRequest,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['vehicles'] })
      notifySuccess('Vehicle added successfully.')
      closeForm()
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      mapFieldErrors(parsed.fieldErrors)
      notifyApiError(parsed, 'Failed to create vehicle.')
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ vehicleId, payload }: { vehicleId: string; payload: VehicleUpsertRequest }) =>
      updateVehicleRequest(vehicleId, payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['vehicles'] })
      notifySuccess('Vehicle updated successfully.')
      closeForm()
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      mapFieldErrors(parsed.fieldErrors)
      notifyApiError(parsed, 'Failed to update vehicle.')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: deleteVehicleRequest,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['vehicles'] })
      notifySuccess('Vehicle deleted successfully.')
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      notifyApiError(parsed, 'Failed to delete vehicle.')
    },
  })

  function closeForm() {
    setFormVisible(false)
    setEditingVehicle(null)
    reset(initialVehicleFormValues)
  }

  function openCreateForm() {
    setEditingVehicle(null)
    reset(initialVehicleFormValues)
    setFormVisible(true)
  }

  function openEditForm(vehicle: VehicleResponse) {
    setEditingVehicle(vehicle)
    reset(toFormValues(vehicle))
    setFormVisible(true)
  }

  function mapFieldErrors(fieldErrors?: Record<string, string>) {
    if (!fieldErrors) {
      return
    }

    Object.entries(fieldErrors).forEach(([field, message]) => {
      if (field in initialVehicleFormValues) {
        setError(field as keyof VehicleFormValues, { message })
      }
    })
  }

  const onSubmit = handleSubmit(async (values) => {
    const payload = toVehiclePayload(values)

    if (editingVehicle) {
      await updateMutation.mutateAsync({ vehicleId: editingVehicle.id, payload })
      return
    }

    await createMutation.mutateAsync(payload)
  })

  const filteredVehicles = useMemo(() => {
    const allVehicles = vehiclesQuery.data || []
    const search = searchTerm.trim().toLowerCase()
    if (!search) {
      return allVehicles
    }

    return allVehicles.filter((vehicle) => {
      const searchableFields = [vehicle.plateNumber, vehicle.make, vehicle.model, String(vehicle.year)]
      return searchableFields.join(' ').toLowerCase().includes(search)
    })
  }, [searchTerm, vehiclesQuery.data])

  const isDeleting = deleteMutation.isPending
  const canDelete = user?.role === 'ADMIN'

  if (vehiclesQuery.isLoading) {
    return <PageLoadingState />
  }

  if (vehiclesQuery.error) {
    const parsed = parseApiError(vehiclesQuery.error)
    return <PageErrorState title="Unable to load vehicles" description={parsed.message || 'Unexpected API error.'} onRetry={() => { void vehiclesQuery.refetch() }} />
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Vehicles</h1>
          <p className="text-sm text-muted-foreground">Manage your fleet registry and vehicle profile data.</p>
        </div>

        <Button type="button" onClick={openCreateForm} className="min-h-11 min-w-11">
          <Plus className="mr-2 h-4 w-4" />
          Add Vehicle
        </Button>
      </header>

      <Card>
        <CardHeader>
          <CardTitle>Fleet Registry</CardTitle>
          <CardDescription>Search, review, and maintain registered vehicles.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative max-w-md">
            <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
            <Input
              type="search"
              className="pl-9"
              placeholder="Search by plate, make, model, or year"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
            />
          </div>

          {filteredVehicles.length === 0 ? (
            <PageEmptyState
              title="No vehicles found"
              description={vehiclesQuery.data?.length ? 'Try a different search term.' : 'Add your first vehicle to get started.'}
              action={
                vehiclesQuery.data?.length ? null : (
                  <Button type="button" onClick={openCreateForm} className="min-h-11 min-w-11">
                    <Plus className="mr-2 h-4 w-4" />
                    Add first vehicle
                  </Button>
                )
              }
            />
          ) : (
            <div className="overflow-x-auto rounded-xl border border-border/80">
              <table className="min-w-full text-sm">
                <thead className="bg-secondary/40 text-left">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Plate</th>
                    <th className="px-4 py-3 font-semibold">Vehicle</th>
                    <th className="px-4 py-3 font-semibold">Fuel</th>
                    <th className="px-4 py-3 font-semibold">Tank</th>
                    <th className="px-4 py-3 font-semibold">MPG</th>
                    <th className="px-4 py-3 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredVehicles.map((vehicle) => (
                    <tr key={vehicle.id} className="border-t border-border/80">
                      <td className="px-4 py-3 font-medium">{vehicle.plateNumber}</td>
                      <td className="px-4 py-3">
                        <div className="font-medium">{vehicle.make} {vehicle.model}</div>
                        <div className="text-xs text-muted-foreground">{vehicle.year}</div>
                      </td>
                      <td className="px-4 py-3">{vehicle.fuelType || <span className="text-muted-foreground">N/A</span>}</td>
                      <td className="px-4 py-3">
                        {vehicle.tankCapacityLiters ? `${vehicle.tankCapacityLiters.toFixed(1)} L` : <span className="text-muted-foreground">N/A</span>}
                      </td>
                      <td className="px-4 py-3">
                        {vehicle.combinedMpg ? <Badge variant="secondary">{vehicle.combinedMpg.toFixed(1)}</Badge> : <span className="text-muted-foreground">N/A</span>}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex flex-wrap gap-2">
                          <Button type="button" variant="outline" size="sm" className="min-h-11" onClick={() => openEditForm(vehicle)}>
                            <Pencil className="mr-1 h-3.5 w-3.5" />
                            Edit
                          </Button>
                          <Button
                            type="button"
                              variant="outline"
                            size="sm"
                              className="min-h-11 border-destructive/50 text-destructive hover:bg-destructive/10"
                            disabled={!canDelete || isDeleting}
                            onClick={() => {
                              const confirmed = window.confirm(`Delete vehicle ${vehicle.plateNumber}? This action cannot be undone.`)
                              if (confirmed) {
                                void deleteMutation.mutateAsync(vehicle.id)
                              }
                            }}
                            title={canDelete ? 'Delete vehicle' : 'Only admins can delete vehicles'}
                          >
                            <Trash2 className="mr-1 h-3.5 w-3.5" />
                            Delete
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {formVisible ? (
        <Card>
          <CardHeader>
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <CardTitle>{editingVehicle ? 'Edit vehicle' : 'Add vehicle'}</CardTitle>
                <CardDescription>Required fields are marked in validation messages if missing.</CardDescription>
              </div>
              <Button type="button" variant="ghost" onClick={closeForm}>
                <X className="mr-2 h-4 w-4" />
                Close
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <form className="grid gap-4 sm:grid-cols-2" onSubmit={onSubmit} noValidate>
              <div className="space-y-2">
                <Label htmlFor="plateNumber">Plate Number</Label>
                <Input id="plateNumber" autoComplete="off" {...register('plateNumber')} />
                {errors.plateNumber ? <p className="text-sm text-destructive">{errors.plateNumber.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="year">Year</Label>
                <Input id="year" type="number" inputMode="numeric" {...register('year')} />
                {errors.year ? <p className="text-sm text-destructive">{errors.year.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="make">Make</Label>
                <Input id="make" autoComplete="off" {...register('make')} />
                {errors.make ? <p className="text-sm text-destructive">{errors.make.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="model">Model</Label>
                <Input id="model" autoComplete="off" {...register('model')} />
                {errors.model ? <p className="text-sm text-destructive">{errors.model.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="fuelType">Fuel Type</Label>
                <Input id="fuelType" autoComplete="off" {...register('fuelType')} />
                {errors.fuelType ? <p className="text-sm text-destructive">{errors.fuelType.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="tankCapacityLiters">Tank Capacity (L)</Label>
                <Input id="tankCapacityLiters" type="number" inputMode="decimal" step="0.1" {...register('tankCapacityLiters')} />
                {errors.tankCapacityLiters ? <p className="text-sm text-destructive">{errors.tankCapacityLiters.message}</p> : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="epaVehicleId">EPA Vehicle ID</Label>
                <Input id="epaVehicleId" type="number" inputMode="numeric" {...register('epaVehicleId')} />
                {errors.epaVehicleId ? <p className="text-sm text-destructive">{errors.epaVehicleId.message}</p> : null}
              </div>

              <div className="sm:col-span-2 flex flex-wrap justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={closeForm}>
                  Cancel
                </Button>
                <Button
                  type="submit"
                  disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}
                  className="min-h-11"
                >
                  <Save className="mr-2 h-4 w-4" />
                  {editingVehicle ? 'Save changes' : 'Create vehicle'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      ) : null}
    </div>
  )
}
