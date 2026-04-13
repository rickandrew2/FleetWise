import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Area, AreaChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { RefreshCcw, TrendingUp, Wallet, TriangleAlert, CircleGauge } from 'lucide-react'
import {
  dashboardCostTrendRequest,
  dashboardForecastRequest,
  dashboardSummaryRequest,
  dashboardTopDriversRequest,
  fuelLogsListRequest,
  fuelPriceHistoryRequest,
  fuelPricesCurrentRequest,
  parseApiError,
  vehiclesListRequest,
} from '@/lib/api'
import { formatPhpCurrency } from '@/lib/currency'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { PageEmptyState, PageErrorState, PageLoadingState } from '@/components/ui/page-state'

function formatMonthLabel(month: string) {
  const [year, monthNumber] = month.split('-')
  const date = new Date(Number(year), Number(monthNumber) - 1, 1)
  return date.toLocaleDateString(undefined, { month: 'short', year: '2-digit' })
}

function normalizeFuelType(rawFuelType: string | null | undefined) {
  const normalized = rawFuelType?.trim().toUpperCase()
  switch (normalized) {
    case 'DIESEL':
    case 'DIESEL_PLUS':
      return 'DIESEL' as const
    case 'GASOLINE_91':
    case 'GASOLINE':
    case 'UNLEADED':
    case 'REGULAR':
      return 'GASOLINE_91' as const
    case 'GASOLINE_95':
    case 'PREMIUM':
      return 'GASOLINE_95' as const
    default:
      return null
  }
}

function confidenceBadgeClass(confidence: 'HIGH' | 'MEDIUM' | 'LOW') {
  if (confidence === 'HIGH') {
    return 'bg-emerald-100 text-emerald-900'
  }
  if (confidence === 'MEDIUM') {
    return 'bg-amber-100 text-amber-900'
  }
  return 'bg-red-100 text-red-900'
}

export function DashboardPage() {
  const summaryQuery = useQuery({
    queryKey: ['dashboard', 'summary'],
    queryFn: dashboardSummaryRequest,
  })

  const topDriversQuery = useQuery({
    queryKey: ['dashboard', 'top-drivers'],
    queryFn: dashboardTopDriversRequest,
  })

  const costTrendQuery = useQuery({
    queryKey: ['dashboard', 'cost-trend'],
    queryFn: dashboardCostTrendRequest,
  })

  const forecastQuery = useQuery({
    queryKey: ['dashboard', 'forecast'],
    queryFn: dashboardForecastRequest,
  })

  const fuelPricesQuery = useQuery({
    queryKey: ['fuel-prices', 'current'],
    queryFn: fuelPricesCurrentRequest,
  })

  const fuelPriceHistoryQuery = useQuery({
    queryKey: ['fuel-prices', 'history'],
    queryFn: fuelPriceHistoryRequest,
  })

  const hasBlockingError = summaryQuery.error || topDriversQuery.error || costTrendQuery.error || forecastQuery.error
  const hasFuelPriceError = Boolean(fuelPricesQuery.error || fuelPriceHistoryQuery.error)

  const fallbackVehiclesQuery = useQuery({
    queryKey: ['dashboard', 'fallback', 'vehicles'],
    queryFn: vehiclesListRequest,
    enabled: hasFuelPriceError,
  })

  const fallbackFuelLogsQuery = useQuery({
    queryKey: ['dashboard', 'fallback', 'fuel-logs'],
    queryFn: () => fuelLogsListRequest(),
    enabled: hasFuelPriceError,
  })
  const isLoading = summaryQuery.isLoading || topDriversQuery.isLoading || costTrendQuery.isLoading || forecastQuery.isLoading

  const trendData = useMemo(
    () => (costTrendQuery.data ?? []).map((point) => ({ ...point, label: formatMonthLabel(point.month) })),
    [costTrendQuery.data],
  )

  const currentFuelPrices = useMemo(() => {
    const byType = new Map((fuelPricesQuery.data ?? []).map((entry) => [entry.fuelType, entry]))

    if (byType.size > 0) {
      return {
        diesel: byType.get('DIESEL'),
        gasoline91: byType.get('GASOLINE_91'),
        gasoline95: byType.get('GASOLINE_95'),
        sourceLabel: 'DOE weekly advisory',
      }
    }

    const vehicles = fallbackVehiclesQuery.data ?? []
    const logs = fallbackFuelLogsQuery.data ?? []

    if (vehicles.length === 0 || logs.length === 0) {
      return {
        diesel: undefined,
        gasoline91: undefined,
        gasoline95: undefined,
        sourceLabel: 'Recent fill-up fallback',
      }
    }

    const fuelTypeByVehicleId = new Map(vehicles.map((vehicle) => [vehicle.id, normalizeFuelType(vehicle.fuelType)]))
    const grouped = new Map<string, { total: number; count: number; latestDate: string }>()

    for (const log of logs) {
      const mappedFuelType = fuelTypeByVehicleId.get(log.vehicleId)
      if (!mappedFuelType) {
        continue
      }

      const current = grouped.get(mappedFuelType) ?? {
        total: 0,
        count: 0,
        latestDate: log.logDate,
      }

      current.total += log.pricePerLiter
      current.count += 1
      if (log.logDate > current.latestDate) {
        current.latestDate = log.logDate
      }

      grouped.set(mappedFuelType, current)
    }

    const buildFallbackEntry = (fuelType: 'DIESEL' | 'GASOLINE_91' | 'GASOLINE_95') => {
      const item = grouped.get(fuelType)
      if (!item || item.count === 0) {
        return undefined
      }
      return {
        fuelType,
        pricePerLiter: Number((item.total / item.count).toFixed(2)),
        effectiveDate: item.latestDate,
        source: 'Recent fill-up fallback',
        stale: true,
      }
    }

    return {
      diesel: buildFallbackEntry('DIESEL'),
      gasoline91: buildFallbackEntry('GASOLINE_91'),
      gasoline95: buildFallbackEntry('GASOLINE_95'),
      sourceLabel: 'Estimated from recent fill-up logs',
    }
  }, [fallbackFuelLogsQuery.data, fallbackVehiclesQuery.data, fuelPricesQuery.data])

  const fuelTrendData = useMemo(() => {
    const grouped = new Map<string, { label: string; diesel?: number; gasoline91?: number }>()

    for (const point of fuelPriceHistoryQuery.data ?? []) {
      if (point.fuelType !== 'DIESEL' && point.fuelType !== 'GASOLINE_91') {
        continue
      }
      const key = point.effectiveDate
      const current = grouped.get(key) ?? { label: key.slice(5) }
      if (point.fuelType === 'DIESEL') {
        current.diesel = point.averagePricePerLiter
      }
      if (point.fuelType === 'GASOLINE_91') {
        current.gasoline91 = point.averagePricePerLiter
      }
      grouped.set(key, current)
    }

    return Array.from(grouped.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .slice(-8)
      .map((entry) => entry[1])
  }, [fuelPriceHistoryQuery.data])

  const refetchAll = async () => {
    await Promise.all([
      summaryQuery.refetch(),
      topDriversQuery.refetch(),
      costTrendQuery.refetch(),
      forecastQuery.refetch(),
      fuelPricesQuery.refetch(),
      fuelPriceHistoryQuery.refetch(),
    ])
  }

  if (isLoading) {
    return <PageLoadingState />
  }

  if (hasBlockingError) {
    const parsed = parseApiError(hasBlockingError)
    return (
      <PageErrorState
        title="Unable to load dashboard"
        description={parsed.message || 'Unexpected API error.'}
        onRetry={refetchAll}
      />
    )
  }

  const summary = summaryQuery.data
  const forecast = forecastQuery.data
  const topDrivers = topDriversQuery.data ?? []
  const hasNoTrendData = trendData.length === 0

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Fleet Snapshot</h1>
          <p className="text-sm text-muted-foreground">Month-to-date fuel signals and driver efficiency.</p>
        </div>
        <Button variant="outline" onClick={refetchAll}>
          <RefreshCcw className="mr-2 h-4 w-4" />
          Refresh
        </Button>
      </div>

      <section className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Month-to-Date Fuel Cost</CardDescription>
            <CardTitle className="text-3xl">
              {formatPhpCurrency(summary?.monthToDateFuelCost)}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center text-sm text-muted-foreground">
              <Wallet className="mr-2 h-4 w-4" />
              Tracked from submitted fuel logs
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Fleet Efficiency Score</CardDescription>
            <CardTitle className="text-3xl">{summary?.fleetEfficiencyScore?.toFixed(2) ?? 'N/A'}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center text-sm text-muted-foreground">
              <CircleGauge className="mr-2 h-4 w-4" />
              Lower values indicate better efficiency
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Active Alerts</CardDescription>
            <CardTitle className="text-3xl">{summary?.activeAlertsCount}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center text-sm text-muted-foreground">
              <TriangleAlert className="mr-2 h-4 w-4" />
              Overconsumption, cost, and maintenance signals
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Current PH Fuel Prices</CardDescription>
            <CardTitle className="text-lg">{currentFuelPrices.sourceLabel}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-1 text-sm">
            {hasFuelPriceError ? (
              <p className="text-xs text-muted-foreground">Fuel price feed is temporarily unavailable. Showing fallback values when available.</p>
            ) : null}
            <p>Diesel: {currentFuelPrices.diesel ? `${formatPhpCurrency(currentFuelPrices.diesel.pricePerLiter)}/L` : 'N/A'}</p>
            <p>Gasoline 91: {currentFuelPrices.gasoline91 ? `${formatPhpCurrency(currentFuelPrices.gasoline91.pricePerLiter)}/L` : 'N/A'}</p>
            <p>Gasoline 95: {currentFuelPrices.gasoline95 ? `${formatPhpCurrency(currentFuelPrices.gasoline95.pricePerLiter)}/L` : 'N/A'}</p>
            <p className="pt-1 text-xs text-muted-foreground">
              Effective {currentFuelPrices.diesel?.effectiveDate ?? currentFuelPrices.gasoline91?.effectiveDate ?? 'N/A'}
            </p>
            {(currentFuelPrices.diesel?.stale || currentFuelPrices.gasoline91?.stale || currentFuelPrices.gasoline95?.stale) ? (
              <p className="text-xs text-amber-700">Using last successful update. Data may be stale.</p>
            ) : null}
          </CardContent>
        </Card>
      </section>

      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Next Month Forecast</CardDescription>
          <div className="flex flex-wrap items-center gap-2">
            <CardTitle className="text-2xl">{formatPhpCurrency(forecast?.projectedCost ?? 0)}</CardTitle>
            <Badge className={confidenceBadgeClass(forecast?.confidenceLevel ?? 'LOW')}>
              {forecast?.confidenceLevel ?? 'LOW'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-2 text-sm text-muted-foreground">
          <p>Based on 3-month average consumption at current DOE prices.</p>
          <p>
            Basis: {forecast?.basedOnMonths ?? 0} month(s), avg {forecast?.avgLitersPerMonth?.toFixed(1) ?? '0.0'} L/month,
            blended price {formatPhpCurrency(forecast?.priceUsed ?? 0)}/L
          </p>
          {forecast?.confidenceLevel === 'LOW' ? (
            <p className="text-amber-700">Not enough data for reliable forecast - add more fuel logs.</p>
          ) : null}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Fuel Price Trend (Last 8 Weeks)</CardTitle>
          <CardDescription>
            {hasFuelPriceError
              ? 'Fuel price history is temporarily unavailable.'
              : 'Diesel and Gasoline 91 weekly movement.'}
          </CardDescription>
        </CardHeader>
        <CardContent className="h-44">
          {fuelTrendData.length === 0 ? (
            <PageEmptyState
              title="No fuel trend data"
              description="Trend lines will appear after weekly fuel prices are captured."
            />
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={fuelTrendData} margin={{ top: 8, right: 8, left: 8, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.2} />
                <XAxis dataKey="label" tickMargin={8} />
                <Tooltip formatter={(value) => `${formatPhpCurrency(Number(value ?? 0))}/L`} />
                <Line type="monotone" dataKey="diesel" name="Diesel" stroke="hsl(var(--primary))" strokeWidth={2.5} dot={false} />
                <Line type="monotone" dataKey="gasoline91" name="Gasoline 91" stroke="hsl(var(--accent))" strokeWidth={2.5} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-[1.4fr_1fr]">
        <Card>
          <CardHeader>
            <CardTitle>Fuel Cost Trend</CardTitle>
            <CardDescription>Monthly cost trend from dashboard analytics endpoint.</CardDescription>
          </CardHeader>
          <CardContent className="h-80">
            {hasNoTrendData ? (
              <PageEmptyState
                title="No trend data yet"
                description="Fuel cost history will appear after monthly records are available."
              />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trendData}>
                  <defs>
                    <linearGradient id="costFill" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.36} />
                      <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0.04} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.25} />
                  <XAxis dataKey="label" tickMargin={8} />
                  <YAxis tickFormatter={(value) => formatPhpCurrency(Number(value))} width={90} />
                  <Tooltip
                    formatter={(value) => [formatPhpCurrency(Number(value ?? 0)), 'Total Cost']}
                    labelFormatter={(label) => `Period: ${label}`}
                  />
                  <Area
                    type="monotone"
                    dataKey="totalCost"
                    stroke="hsl(var(--primary))"
                    fill="url(#costFill)"
                    strokeWidth={2.5}
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Top Drivers</CardTitle>
            <CardDescription>Highest average efficiency from current leaderboard.</CardDescription>
          </CardHeader>
          <CardContent>
            {topDrivers.length === 0 ? (
              <PageEmptyState
                title="No ranked drivers"
                description="Driver rankings will appear once routes and efficiency data are recorded."
              />
            ) : (
              <ul className="space-y-3">
                {topDrivers.map((driver) => (
                  <li key={driver.driverId} className="rounded-xl border border-border/70 bg-white/70 p-3">
                    <div className="flex items-center justify-between gap-3">
                      <p className="font-semibold">{driver.driverName}</p>
                      <span className="inline-flex items-center gap-1 text-xs font-medium text-primary">
                        <TrendingUp className="h-3.5 w-3.5" />
                        {driver.averageEfficiencyScore?.toFixed(2) ?? 'N/A'}
                      </span>
                    </div>
                    <p className="mt-1 text-xs text-muted-foreground">{driver.routeCount} recorded routes</p>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
