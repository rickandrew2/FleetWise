import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

interface ComingSoonPageProps {
  title: string
  description: string
}

export function ComingSoonPage({ title, description }: ComingSoonPageProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <p className="text-sm text-muted-foreground">
          This module is intentionally deferred for the next implementation slice.
        </p>
      </CardContent>
    </Card>
  )
}
