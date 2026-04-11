import type { ReactNode } from 'react'
import { AlertTriangle, RefreshCcw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

export function PageLoadingState() {
  return (
    <div className="space-y-4" aria-live="polite" aria-busy="true">
      <Skeleton className="h-6 w-40" />
      <Skeleton className="h-32 w-full" />
      <Skeleton className="h-24 w-full" />
    </div>
  )
}

interface PageErrorStateProps {
  title?: string
  description: string
  onRetry?: () => void
}

export function PageErrorState({ title = 'Unable to load data', description, onRetry }: PageErrorStateProps) {
  return (
    <Card role="alert" aria-live="assertive">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <AlertTriangle className="h-4 w-4 text-destructive" />
          {title}
        </CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      {onRetry && (
        <CardContent>
          <Button type="button" onClick={onRetry}>
            <RefreshCcw className="mr-2 h-4 w-4" />
            Retry
          </Button>
        </CardContent>
      )}
    </Card>
  )
}

interface PageEmptyStateProps {
  title: string
  description: string
  action?: ReactNode
}

export function PageEmptyState({ title, description, action }: PageEmptyStateProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      {action ? <CardContent>{action}</CardContent> : null}
    </Card>
  )
}
