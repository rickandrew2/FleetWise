import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { RefreshCcw, TrendingUp, Wallet, TriangleAlert, CircleGauge } from 'lucide-react'
import {
  dashboardCostTrendRequest,
  dashboardSummaryRequest,
  dashboardTopDriversRequest,
  parseApiError,
} from '@/lib/api'
import { formatPhpCurrency } from '@/lib/currency'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { PageEmptyState, PageErrorState, PageLoadingState } from '@/components/ui/page-state'

function formatMonthLabel(month: string) {
  const [year, monthNumber] = month.split('-')
  const date = new Date(Number(year), Number(monthNumber) - 1, 1)
  return date.toLocaleDateString(undefined, { month: 'short', year: '2-digit' })
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

  const hasError = summaryQuery.error || topDriversQuery.error || costTrendQuery.error
  const isLoading = summaryQuery.isLoading || topDriversQuery.isLoading || costTrendQuery.isLoading

  const trendData = useMemo(
    () => (costTrendQuery.data ?? []).map((point) => ({ ...point, label: formatMonthLabel(point.month) })),
    [costTrendQuery.data],
  )

  const refetchAll = async () => {
    await Promise.all([summaryQuery.refetch(), topDriversQuery.refetch(), costTrendQuery.refetch()])
  }

  if (isLoading) {
    return <PageLoadingState />
  }

  if (hasError) {
    const parsed = parseApiError(hasError)
    return (
      <PageErrorState
        title="Unable to load dashboard"
        description={parsed.message || 'Unexpected API error.'}
        onRetry={refetchAll}
      />
    )
  }

  const summary = summaryQuery.data
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

      <section className="grid gap-4 md:grid-cols-3">
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
      </section>

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
