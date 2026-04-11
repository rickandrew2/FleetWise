import { toast } from 'sonner'
import type { ApiErrorResponse } from '@/types/api'

export function notifySuccess(message: string) {
  toast.success(message)
}

export function notifyInfo(message: string) {
  toast.info(message)
}

export function notifyError(message: string) {
  toast.error(message)
}

export function notifyApiError(error: ApiErrorResponse, fallback = 'Something went wrong. Please try again.') {
  const message = error.message?.trim() || fallback
  toast.error(message)
}
