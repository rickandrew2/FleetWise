import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Download, FileDown, FileStack, Plus } from 'lucide-react'
import {
  downloadReportRequest,
  generateReportRequest,
  parseApiError,
  REPORT_TYPES,
  reportsListRequest,
} from '@/lib/api'
import { notifyApiError, notifySuccess } from '@/lib/notify'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { PageEmptyState, PageErrorState, PageLoadingState } from '@/components/ui/page-state'
import type { ReportStatus, ReportType } from '@/types/api'

function formatStatus(status: ReportStatus) {
  switch (status) {
    case 'COMPLETED':
      return 'Completed'
    case 'FAILED':
      return 'Failed'
    case 'RUNNING':
      return 'Running'
    case 'PENDING':
      return 'Pending'
    default:
      return status
  }
}

function formatReportType(type: ReportType) {
  return type.charAt(0) + type.slice(1).toLowerCase()
}

function downloadBlob(blob: Blob, filename: string) {
  const blobUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = blobUrl
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(blobUrl)
}

export function ReportsPage() {
  const queryClient = useQueryClient()
  const [selectedType, setSelectedType] = useState<ReportType>('WEEKLY')

  const reportsQuery = useQuery({
    queryKey: ['reports'],
    queryFn: reportsListRequest,
  })

  const generateMutation = useMutation({
    mutationFn: generateReportRequest,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['reports'] })
      notifySuccess('Report generation queued successfully.')
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      notifyApiError(parsed, 'Failed to generate report.')
    },
  })

  const downloadMutation = useMutation({
    mutationFn: downloadReportRequest,
    onSuccess: ({ blob, filename }) => {
      downloadBlob(blob, filename)
      notifySuccess('Report download started.')
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      notifyApiError(parsed, 'Failed to download report.')
    },
  })

  const reportCounts = useMemo(() => {
    const reports = reportsQuery.data || []
    return {
      total: reports.length,
      completed: reports.filter((report) => report.status === 'COMPLETED').length,
      running: reports.filter((report) => report.status === 'RUNNING' || report.status === 'PENDING').length,
      failed: reports.filter((report) => report.status === 'FAILED').length,
    }
  }, [reportsQuery.data])

  if (reportsQuery.isLoading) {
    return <PageLoadingState />
  }

  if (reportsQuery.error) {
    const parsed = parseApiError(reportsQuery.error)
    return (
      <PageErrorState
        title="Unable to load reports"
        description={parsed.message || 'Unexpected API error.'}
        onRetry={() => {
          void reportsQuery.refetch()
        }}
      />
    )
  }

  const reports = reportsQuery.data || []

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Reports</h1>
          <p className="text-sm text-muted-foreground">Generate and download operational report packages.</p>
        </div>

        <Card className="min-w-44">
          <CardContent className="flex items-center justify-between p-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-muted-foreground">Total Jobs</p>
              <p className="text-2xl font-semibold">{reportCounts.total}</p>
            </div>
            <FileStack className="h-5 w-5 text-primary" />
          </CardContent>
        </Card>
      </header>

      <section className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader>
            <CardDescription>Completed</CardDescription>
            <CardTitle>{reportCounts.completed}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardDescription>Running / Pending</CardDescription>
            <CardTitle>{reportCounts.running}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardDescription>Failed</CardDescription>
            <CardTitle>{reportCounts.failed}</CardTitle>
          </CardHeader>
        </Card>
      </section>

      <Card>
        <CardHeader>
          <CardTitle>Generate Report</CardTitle>
          <CardDescription>Create a new weekly or monthly report job.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap items-end gap-3">
          <div className="space-y-2">
            <Label htmlFor="report-type">Report Type</Label>
            <select
              id="report-type"
              className="flex h-10 w-48 rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={selectedType}
              onChange={(event) => setSelectedType(event.target.value as ReportType)}
            >
              {REPORT_TYPES.map((type) => (
                <option key={type} value={type}>{formatReportType(type)}</option>
              ))}
            </select>
          </div>

          <Button
            type="button"
            className="min-h-11 min-w-11"
            disabled={generateMutation.isPending}
            onClick={() => {
              void generateMutation.mutateAsync({ reportType: selectedType })
            }}
          >
            <Plus className="mr-2 h-4 w-4" />
            Generate Report
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Report Jobs</CardTitle>
          <CardDescription>Download completed artifacts when available.</CardDescription>
        </CardHeader>
        <CardContent>
          {reports.length === 0 ? (
            <PageEmptyState
              title="No reports generated"
              description="Generate your first report to populate this list."
            />
          ) : (
            <div className="overflow-x-auto rounded-xl border border-border/80">
              <table className="min-w-full text-sm">
                <thead className="bg-secondary/40 text-left">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Created</th>
                    <th className="px-4 py-3 font-semibold">Type</th>
                    <th className="px-4 py-3 font-semibold">Status</th>
                    <th className="px-4 py-3 font-semibold">Generated At</th>
                    <th className="px-4 py-3 font-semibold">File</th>
                    <th className="px-4 py-3 font-semibold">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {reports.map((report) => {
                    const canDownload = report.status === 'COMPLETED'
                    return (
                      <tr key={report.id} className="border-t border-border/80">
                        <td className="px-4 py-3">{new Date(report.createdAt).toLocaleString()}</td>
                        <td className="px-4 py-3">{formatReportType(report.reportType)}</td>
                        <td className="px-4 py-3">
                          <Badge
                            variant="secondary"
                            className={report.status === 'FAILED' ? 'bg-destructive text-destructive-foreground' : undefined}
                          >
                            {formatStatus(report.status)}
                          </Badge>
                        </td>
                        <td className="px-4 py-3">{report.generatedAt ? new Date(report.generatedAt).toLocaleString() : 'N/A'}</td>
                        <td className="px-4 py-3">{report.filePath || 'N/A'}</td>
                        <td className="px-4 py-3">
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            className="min-h-11"
                            disabled={!canDownload || downloadMutation.isPending}
                            title={canDownload ? 'Download report' : 'Report must be completed before download'}
                            onClick={() => {
                              void downloadMutation.mutateAsync(report.id)
                            }}
                          >
                            <Download className="mr-1 h-3.5 w-3.5" />
                            Download
                          </Button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="inline-flex items-center gap-2 text-xs text-muted-foreground">
        <FileDown className="h-3.5 w-3.5" />
        Downloads are initiated only for completed report jobs.
      </div>
    </div>
  )
}
