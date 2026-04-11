# FleetWise Web

FleetWise Web is the React frontend for FleetWise, focused on day-to-day fleet operations visibility for fuel costs, route efficiency, alerts, and reporting.

## Product Context

For the plain-language business explanation of FleetWise, read [../PROJECT_CONTEXT.md](../PROJECT_CONTEXT.md) first.

For full repository architecture and backend contracts, use [../FleetWise_README.md](../FleetWise_README.md).

## Tech Stack

- React 19 + TypeScript
- Vite 8
- React Router 6
- TanStack Query 5
- Axios + Zod response validation
- Tailwind CSS
- Vitest + Testing Library

## Available Scripts

Run from this directory:

- `npm run dev` starts the Vite dev server
- `npm run build` runs TypeScript build and creates a production bundle
- `npm run lint` runs ESLint
- `npm run test` starts Vitest in watch mode
- `npm run test:run` runs Vitest once
- `npm run preview` serves the built app locally

## Environment

- `VITE_API_BASE_URL` optional
  - Defaults to `http://localhost:8080` when not set or invalid
  - Only the URL origin is used by the app

## Current App Scope

Implemented routes:

- `/login`
- `/dashboard`
- `/vehicles`
- `/fuel-logs`
- `/routes`
- `/alerts`
- `/reports`

Role-gated pages:

- Dashboard: `ADMIN`, `FLEET_MANAGER`, `DRIVER`
- Vehicles: `ADMIN`, `FLEET_MANAGER`
- Fuel Logs: `ADMIN`, `FLEET_MANAGER`, `DRIVER`
- Routes: `ADMIN`, `FLEET_MANAGER`, `DRIVER`
- Alerts: `ADMIN`, `FLEET_MANAGER`, `DRIVER`
- Reports: `ADMIN`, `FLEET_MANAGER`

## Running from Repository Root

Use the root scripts to run all services together:

- `npm run dev` starts Postgres, Spring Boot API, and this frontend
- `npm run down` stops Docker infrastructure services
