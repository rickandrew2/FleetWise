---
agent: agent
description: Implement pending FleetWise features 3, 2, 4, and 6 in sequence with backend, frontend, and tests.
---

## Context
Feature 1 (fuel price tracker) and Feature 5 (email notifications) are already present. Remaining product scope is pre-trip estimate, forecasting, personalized anomaly detection, and weather context for routes.

## Decision
Implement remaining features in required order: Feature 3, Feature 2, Feature 4, Feature 6. Reuse existing modules (`route`, `dashboard`, `alert`, `fuelprice`) and keep endpoint auth, DTO style, and Swagger annotations consistent with current codebase.

## Steps
1. Feature 3: Add route estimate request/response DTOs, service logic using GraphHopper + vehicle MPG + current fuel price, and `POST /api/routes/estimate` endpoint.
2. Feature 3: Add frontend API/types and routes-page UI for calculate-estimate action and results panel.
3. Feature 2: Add forecast domain DTOs and `ForecastService` (3-month consumption + price trend factor), then expose `GET /api/dashboard/forecast`.
4. Feature 2: Add dashboard forecast card with confidence badge and low-confidence message.
5. Feature 4: Add personalized anomaly logic in alert service, `PERSONAL_ANOMALY` enum, driver efficiency profile endpoint, and repository query support.
6. Feature 4: Add frontend alert badge styling for personal anomaly and route-driver trend badges using efficiency profile endpoint.
7. Feature 6: Add route weather columns via Flyway migration, weather client/service async enrichment in route save flow, and route response DTO updates.
8. Feature 6: Add frontend weather icon column and efficiency context note in routes page.
9. Add/adjust tests: route estimate integration, forecast service unit test, personalized anomaly unit test, and any impacted API tests.
10. Run backend and frontend test suites, fix regressions, and update `memory.md` only if a new recurring issue is discovered and resolved.

## Acceptance Criteria
- Route estimate endpoint returns distance, duration, expected liters, estimated cost, current price, and vehicle name.
- Dashboard forecast endpoint and card render projected cost with confidence and basis note.
- Personalized anomaly alerts trigger from rolling driver baseline and new alert type appears in Alerts UI.
- Driver efficiency profile endpoint returns avg/stddev/trips/trend and route list shows trend badges.
- Routes persist weather context asynchronously and UI displays weather icons/context warning.
- At least one unit test covers forecast logic and one unit test covers personalized anomaly logic.

## Status
- [ ] Not started  /  [ ] In progress  /  [x] Complete
Blockers (if any):
- None
