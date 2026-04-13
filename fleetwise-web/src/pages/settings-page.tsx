import type { FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BellRing, MailCheck, Save } from 'lucide-react'
import {
  parseApiError,
  updateUserNotificationPreferencesRequest,
  userNotificationPreferencesRequest,
} from '@/lib/api'
import { notifyApiError, notifySuccess } from '@/lib/notify'
import { useAuth } from '@/providers/auth-provider'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageErrorState, PageLoadingState } from '@/components/ui/page-state'

export function SettingsPage() {
  const queryClient = useQueryClient()
  const { user } = useAuth()

  const preferencesQuery = useQuery({
    queryKey: ['userNotificationPreferences'],
    queryFn: userNotificationPreferencesRequest,
  })

  const updateMutation = useMutation({
    mutationFn: updateUserNotificationPreferencesRequest,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['userNotificationPreferences'] })
      notifySuccess('Notification preferences updated.')
    },
    onError: (error) => {
      const parsed = parseApiError(error)
      notifyApiError(parsed, 'Failed to update notification preferences.')
    },
  })

  function handleSave(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const formData = new FormData(event.currentTarget)
    const emailNotificationsEnabled = formData.get('emailNotificationsEnabled') === 'on'
    const notificationEmailValue = String(formData.get('notificationEmail') || '').trim().toLowerCase()

    void updateMutation.mutateAsync({
      emailNotificationsEnabled,
      notificationEmail: notificationEmailValue ? notificationEmailValue : null,
    })
  }

  if (preferencesQuery.isLoading) {
    return <PageLoadingState />
  }

  if (preferencesQuery.error) {
    const parsed = parseApiError(preferencesQuery.error)
    return (
      <PageErrorState
        title="Unable to load settings"
        description={parsed.message || 'Unexpected API error.'}
        onRetry={() => {
          void preferencesQuery.refetch()
        }}
      />
    )
  }

  const effectiveEmail = preferencesQuery.data?.effectiveNotificationEmail || user?.email || 'N/A'

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold">Settings</h1>
        <p className="text-sm text-muted-foreground">Manage email notifications for alerts and weekly reports.</p>
      </header>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BellRing className="h-5 w-5 text-primary" />
            Email Notifications
          </CardTitle>
          <CardDescription>
            Toggle notifications and choose a preferred recipient email for FleetWise alerts and weekly reports.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-5" onSubmit={handleSave}>
          <div className="flex items-start gap-3 rounded-lg border border-border/80 p-4">
            <input
              id="email-notifications-enabled"
              name="emailNotificationsEnabled"
              type="checkbox"
              className="mt-1 h-4 w-4 rounded border-input accent-primary"
              defaultChecked={preferencesQuery.data?.emailNotificationsEnabled || false}
            />
            <div className="space-y-1">
              <Label htmlFor="email-notifications-enabled" className="text-sm font-medium">
                Enable notification emails
              </Label>
              <p className="text-xs text-muted-foreground">
                When enabled, FleetWise sends alert and weekly report updates to your configured email.
              </p>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="notification-email">Preferred notification email (optional)</Label>
            <Input
              id="notification-email"
              name="notificationEmail"
              type="email"
              maxLength={100}
              placeholder={user?.email || 'you@example.com'}
              defaultValue={preferencesQuery.data?.notificationEmail || ''}
            />
            <p className="text-xs text-muted-foreground">
              Leave blank to use your account email ({user?.email || 'not available'}).
            </p>
          </div>

          <div className="rounded-lg border border-border/80 bg-secondary/30 p-3 text-sm text-muted-foreground">
            <p className="inline-flex items-center gap-2">
              <MailCheck className="h-4 w-4 text-primary" />
              Effective recipient: <span className="font-medium text-foreground">{effectiveEmail}</span>
            </p>
          </div>

          <div className="flex justify-end">
            <Button
              type="submit"
              className="min-h-11 min-w-11"
              disabled={updateMutation.isPending}
            >
              <Save className="mr-2 h-4 w-4" />
              Save Preferences
            </Button>
          </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
