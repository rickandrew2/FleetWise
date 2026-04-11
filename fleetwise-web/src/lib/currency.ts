const pesoFormatter = new Intl.NumberFormat('en-PH', {
  style: 'currency',
  currency: 'PHP',
  maximumFractionDigits: 2,
})

export function formatPhpCurrency(value: number | null | undefined) {
  return pesoFormatter.format(value ?? 0)
}
