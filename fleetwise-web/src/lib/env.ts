const fallbackApiBaseUrl = 'http://localhost:8080'

function resolveApiBaseUrl() {
  const candidate = import.meta.env.VITE_API_BASE_URL?.trim() || fallbackApiBaseUrl

  try {
    const parsed = new URL(candidate)
    return parsed.origin
  } catch {
    return fallbackApiBaseUrl
  }
}

export const env = {
  apiBaseUrl: resolveApiBaseUrl(),
}
