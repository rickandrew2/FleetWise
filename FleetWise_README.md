# FleetWise 🚛⛽
### Fleet Fuel Intelligence & Route Optimization API
> A production-grade Java/Spring Boot backend system for logistics companies to monitor fleet fuel consumption, score driver efficiency, optimize routes, and auto-generate operational reports.

---

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [How to Use This README](#how-to-use-this-readme)
3. [Tech Stack](#tech-stack)
4. [External APIs & Resources](#external-apis--resources)
5. [System Architecture](#system-architecture)
6. [Architecture Choice](#architecture-choice)
7. [Database Schema](#database-schema)
8. [Module Breakdown](#module-breakdown)
9. [API Endpoints Reference](#api-endpoints-reference)
10. [Business Logic Rules](#business-logic-rules)
11. [Week-by-Week Build Roadmap](#week-by-week-build-roadmap)
12. [Project Structure](#project-structure)
13. [Environment Variables](#environment-variables)
14. [Running the Project](#running-the-project)
15. [Testing Strategy](#testing-strategy)
16. [README Maintenance](#readme-maintenance)
17. [Resume Bullet Points](#resume-bullet-points)

---

## How to Use This README

This file is the main source of truth for FleetWise during development.

Use this document to:
- Decide implementation order (follow the module sequence and roadmap)
- Keep business rules, schema, and API contracts aligned
- Share current project status with teammates or reviewers
- Prevent scope drift while building incrementally

Working rules:
- Start each build session by checking the roadmap and current module section here
- When requirements change, update this README first, then update code
- Keep changes small and explicit (what changed, why, and impact)
- Do not remove historical decisions; move superseded details into the change log

---

## Project Overview

**Problem it solves:**
A logistics company with 20–100+ vehicles spends a massive portion of operating costs on fuel — especially as fuel prices rise. They need to know:
- Which vehicles are consuming more fuel than expected?
- Which drivers are inefficient or overspending?
- Which routes are fuel-heavy and can be optimized?
- What does the monthly fuel cost breakdown look like per vehicle/driver?

**FleetWise** is the backend API system that answers all of these questions.

**Key capabilities:**
- Register fleet vehicles with real manufacturer MPG specs (pulled from FuelEconomy.gov)
- Log every fill-up: liters, cost, odometer reading
- Record routes and calculate actual vs expected fuel consumption
- Score each driver's fuel efficiency
- Fire alerts when consumption anomalies are detected
- Generate weekly/monthly PDF and Excel cost reports automatically

---

## Tech Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Language | Java | 17+ (LTS) | Core language |
| Framework | Spring Boot | 3.x | Application framework |
| Security | Spring Security + JWT | 6.x | Auth & authorization |
| ORM | Spring Data JPA + Hibernate | 3.x | Database access layer |
| Database | PostgreSQL + PostGIS | 15+ | Primary DB + geospatial |
| Batch | Spring Batch | 5.x | Scheduled report jobs |
| Routing | GraphHopper Java SDK | 9.x | Route distance calculation |
| Vehicle Data | FuelEconomy.gov REST API | - | Real MPG data (free, no key) |
| PDF Reports | iText / OpenPDF | 8.x | PDF payslips & reports |
| Excel Reports | Apache POI | 5.x | Excel export |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.x | Auto API documentation |
| Testing | JUnit 5 + Mockito | - | Unit & integration tests |
| Containerization | Docker + Docker Compose | - | Local & prod environment |
| CI/CD | GitHub Actions | - | Automated test & deploy |
| Deployment | Render / Railway | - | Free-tier cloud hosting |

---

## External APIs & Resources

### 1. FuelEconomy.gov API
- **URL:** `https://www.fueleconomy.gov/ws/rest/`
- **Cost:** 100% Free — no API key required
- **What you use it for:** Fetch real manufacturer MPG data when registering a vehicle
- **Key endpoints you'll call:**
  ```
  GET https://www.fueleconomy.gov/ws/rest/vehicle/menu/make          → List all makes
  GET https://www.fueleconomy.gov/ws/rest/vehicle/menu/model?year=2020&make=Toyota  → Models
  GET https://www.fueleconomy.gov/ws/rest/vehicle/{id}               → Full vehicle specs
  GET https://www.fueleconomy.gov/ws/rest/ympg/shared/ympgVehicle/{id} → Community MPG
  ```
- **Response fields you need:** `comb08` (combined MPG), `city08`, `highway08`, `fuelType1`, `barrels08`
- **How to use in Java:** Use Spring's `RestTemplate` or `WebClient` to call this API during vehicle registration

### 2. GraphHopper Routing (Java SDK — self-hosted or API)
- **Option A (Recommended for dev):** Use GraphHopper's free public API
  - Sign up at: `https://www.graphhopper.com/` — free tier = 500 requests/day
  - API Key goes in `.env` as `GRAPHHOPPER_API_KEY`
- **Option B:** Embed GraphHopper as a Java library (fully offline, no limits)
  - Add Maven dependency: `com.graphhopper:graphhopper-core:9.x`
  - Download Philippines OSM data from: `https://download.geofabrik.de/asia/philippines-latest.osm.pbf`
- **What you use it for:** Calculate road distance (km) and estimated travel time between two GPS coordinates for a route log
- **Key API call:**
  ```
  GET https://graphhopper.com/api/1/route?point={lat,lng}&point={lat,lng}&vehicle=car&key={API_KEY}
  ```
- **Response fields you need:** `distance` (meters), `time` (milliseconds)

### 3. OpenRouteService (Alternative to GraphHopper)
- **URL:** `https://openrouteservice.org/`
- **Cost:** Free tier — 2,000 requests/day with free API key
- **Signup:** `https://openrouteservice.org/dev/#/signup`
- **Written in Java** — very natural to integrate with Spring Boot

---

## System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT / FRONTEND                     │
│              (Postman / Swagger UI / Any App)            │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP REST
┌──────────────────────▼──────────────────────────────────┐
│                  SPRING BOOT API                         │
│                                                          │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ Auth Module │  │ Vehicle Svc  │  │  Fuel Log Svc  │  │
│  │ JWT+Sprng   │  │ +FuelEco API │  │                │  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
│                                                          │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │  Route Svc  │  │ Efficiency   │  │  Alert Engine  │  │
│  │ +GraphHopper│  │ Scoring Eng  │  │                │  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
│                                                          │
│  ┌─────────────────────────────────────────────────────┐ │
│  │            Spring Batch Report Jobs                 │ │
│  │         (Scheduled PDF + Excel generation)          │ │
│  └─────────────────────────────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              PostgreSQL + PostGIS Database               │
└─────────────────────────────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
  FuelEconomy.gov API         GraphHopper API
  (vehicle MPG data)          (route distances)
```

**User Roles:**
- `ADMIN` — full access, manage users, view all reports
- `FLEET_MANAGER` — manage vehicles, drivers, view reports, set alert thresholds
- `DRIVER` — log their own fuel fill-ups and routes only

---

## Architecture Choice

FleetWise uses a **Layered Modular Monolith** architecture.

- **Modular by feature**: `auth`, `user`, `vehicle`, `fuellog`, `route`, `alert`, `report`, `dashboard`
- **Layered flow** inside each module: `Controller -> Service -> Repository -> Entity`
- **Cross-cutting infrastructure**: security filters, JWT, exception handling, configuration, and shared response models

Why this choice:

1. Resume-friendly and widely used in Spring Boot production systems.
2. Easier to learn and maintain than microservices for early project stages.
3. Keeps clean boundaries so we can evolve to more advanced architecture later if needed.

---

## Database Schema

### Table: `users`
```sql
id              UUID PRIMARY KEY
name            VARCHAR(100)
email           VARCHAR(100) UNIQUE NOT NULL
password_hash   VARCHAR(255) NOT NULL
role            ENUM('ADMIN', 'FLEET_MANAGER', 'DRIVER')
created_at      TIMESTAMP DEFAULT NOW()
```

### Table: `vehicles`
```sql
id                  UUID PRIMARY KEY
plate_number        VARCHAR(20) UNIQUE NOT NULL
make                VARCHAR(50)         -- e.g. Toyota
model               VARCHAR(50)         -- e.g. Hilux
vehicle_year        INT                -- mapped as `year` in API DTO
fuel_type           VARCHAR(30)         -- e.g. Diesel, Gasoline
tank_capacity_liters DECIMAL(6,2)
epa_vehicle_id      INT                 -- FuelEconomy.gov vehicle ID
combined_mpg        DECIMAL(6,2)        -- from FuelEconomy.gov
city_mpg            DECIMAL(6,2)
highway_mpg         DECIMAL(6,2)
assigned_driver_id  UUID REFERENCES users(id)
created_at          TIMESTAMP DEFAULT NOW()
```

### Table: `fuel_logs`
```sql
id                  UUID PRIMARY KEY
vehicle_id          UUID REFERENCES vehicles(id)
driver_id           UUID REFERENCES users(id)
log_date            DATE NOT NULL
odometer_reading_km DECIMAL(10,2)
liters_filled       DECIMAL(8,2)
price_per_liter     DECIMAL(6,2)
total_cost          DECIMAL(10,2)       -- computed: liters * price
station_name        VARCHAR(100)
station_lat         DECIMAL(10,7)
station_lng         DECIMAL(10,7)
notes               TEXT
created_at          TIMESTAMP DEFAULT NOW()
```

### Table: `route_logs`
```sql
id                  UUID PRIMARY KEY
vehicle_id          UUID REFERENCES vehicles(id)
driver_id           UUID REFERENCES users(id)
trip_date           DATE NOT NULL
origin_label        VARCHAR(150)
origin_lat          DECIMAL(10,7)
origin_lng          DECIMAL(10,7)
destination_label   VARCHAR(150)
destination_lat     DECIMAL(10,7)
destination_lng     DECIMAL(10,7)
distance_km         DECIMAL(8,2)        -- from GraphHopper
estimated_duration_min INT              -- from GraphHopper
actual_fuel_used_liters DECIMAL(8,2)   -- computed from fuel logs
expected_fuel_liters DECIMAL(8,2)      -- computed from vehicle MPG + distance
efficiency_score    DECIMAL(5,2)        -- actual/expected ratio (lower = better)
created_at          TIMESTAMP DEFAULT NOW()
```

### Table: `alerts`
```sql
id                  UUID PRIMARY KEY
vehicle_id          UUID REFERENCES vehicles(id)
driver_id           UUID REFERENCES users(id)
alert_type          ENUM('OVERCONSUMPTION', 'HIGH_COST', 'MAINTENANCE_DUE', 'UNUSUAL_FILLUP')
message             TEXT
threshold_value     DECIMAL(10,2)
actual_value        DECIMAL(10,2)
is_read             BOOLEAN DEFAULT FALSE
triggered_at        TIMESTAMP DEFAULT NOW()
```

### Table: `report_jobs`
```sql
id                  UUID PRIMARY KEY
report_type         ENUM('WEEKLY', 'MONTHLY')
status              ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')
file_path           VARCHAR(255)
generated_at        TIMESTAMP
created_at          TIMESTAMP DEFAULT NOW()
```

---

## Module Breakdown

### Module 1: Auth Module
**Responsibility:** Register users, login, issue JWT tokens, protect routes by role.

**Key classes to build:**
- `AuthController` — `/api/auth/register`, `/api/auth/login`
- `JwtService` — generate & validate JWT tokens
- `UserDetailsServiceImpl` — load user from DB for Spring Security
- `SecurityConfig` — configure filter chain, permit/deny by role
- `JwtAuthFilter` — intercept requests, validate token, set SecurityContext

**Dependencies:** `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`

---

### Module 2: Vehicle Registry Service
**Responsibility:** Register fleet vehicles and enrich them with real MPG data from FuelEconomy.gov.

**Key classes to build:**
- `VehicleController` — CRUD endpoints for vehicles
- `VehicleService` — business logic
- `VehicleRepository` — JPA repository
- `FuelEconomyApiClient` — RestTemplate/WebClient calls to fueleconomy.gov
- `VehicleEntity` — JPA entity

**Key flow:**
1. Fleet manager POSTs a vehicle with plate, make, model, year
2. `FuelEconomyApiClient` calls FuelEconomy.gov to find the EPA vehicle ID
3. Fetches MPG data (combined, city, highway) and saves to DB
4. Vehicle is now registered with real efficiency benchmarks

**FuelEconomy.gov Java call example:**
```java
@Service
public class FuelEconomyApiClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://www.fueleconomy.gov/ws/rest";

    public VehicleEpaData fetchVehicleData(int epaVehicleId) {
        String url = BASE_URL + "/vehicle/" + epaVehicleId;
        // Set Accept: application/json header
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<VehicleEpaData> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, VehicleEpaData.class
        );
        return response.getBody();
    }
}
```

---

### Module 3: Fuel Log Service
**Responsibility:** Record every fuel fill-up per vehicle, track liters, cost, and odometer.

**Key classes to build:**
- `FuelLogController` — POST/GET endpoints
- `FuelLogService` — save logs, compute total cost, trigger alerts
- `FuelLogRepository` — JPA repository
- `FuelLogEntity` — JPA entity

**Key business logic in service:**
- Auto-compute `total_cost = liters_filled * price_per_liter`
- After saving, call `AlertService.checkFuelLog(log)` to evaluate thresholds
- Expose aggregate queries: total spend per vehicle/period, average liters per fill-up

---

### Module 4: Route Log Service
**Responsibility:** Record trips, call GraphHopper for distance, compute fuel efficiency score.

**Key classes to build:**
- `RouteLogController` — POST/GET endpoints
- `RouteLogService` — save route, call GraphHopper, compute efficiency
- `RouteLogRepository` — JPA repository
- `GraphHopperApiClient` — HTTP client for routing API
- `EfficiencyScoringEngine` — computes expected vs actual fuel

**GraphHopper Java call example:**
```java
@Service
public class GraphHopperApiClient {

    @Value("${graphhopper.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public RouteResult getRoute(double originLat, double originLng,
                                 double destLat, double destLng) {
        String url = String.format(
            "https://graphhopper.com/api/1/route?point=%s,%s&point=%s,%s&vehicle=car&key=%s",
            originLat, originLng, destLat, destLng, apiKey
        );
        GraphHopperResponse response = restTemplate.getForObject(url, GraphHopperResponse.class);
        double distanceKm = response.getPaths().get(0).getDistance() / 1000.0;
        int durationMin = (int)(response.getPaths().get(0).getTime() / 60000);
        return new RouteResult(distanceKm, durationMin);
    }
}
```

**Efficiency scoring logic:**
```java
// Expected fuel = distance_km / (combined_mpg * 1.60934)  [converts MPG to km/L]
// Efficiency score = actual_fuel / expected_fuel
// Score < 1.0 = driver is MORE efficient than expected ✅
// Score > 1.2 = 20%+ overconsumption — trigger ALERT ⚠️
public double computeEfficiencyScore(double distanceKm, double actualFuelLiters, double combinedMpg) {
    double kmPerLiter = combinedMpg * 1.60934;
    double expectedFuel = distanceKm / kmPerLiter;
    return actualFuelLiters / expectedFuel;
}
```

---

### Module 5: Alert Engine
**Responsibility:** Rule-based system that checks fuel logs and route scores and fires alerts.

**Key classes to build:**
- `AlertService` — contains all alert-checking rules
- `AlertRepository` — save and query alerts
- `AlertController` — GET alerts (by vehicle, driver, unread)

**Alert rules to implement:**
```
Rule 1 — OVERCONSUMPTION:
  IF route efficiency_score > 1.25 (25% over expected)
  THEN create alert type=OVERCONSUMPTION

Rule 2 — HIGH_COST per fill-up:
  IF total_cost of a single fill-up > configurable threshold (e.g. PHP 5000)
  THEN create alert type=HIGH_COST

Rule 3 — UNUSUAL_FILLUP:
  IF liters_filled > vehicle.tank_capacity_liters * 1.1
  THEN create alert type=UNUSUAL_FILLUP (possible data entry error or siphoning)

Rule 4 — MAINTENANCE_DUE:
  IF current odometer - last_service_odometer > 5000 km
  THEN create alert type=MAINTENANCE_DUE
```

---

### Module 6: Report Generator (Spring Batch)
**Responsibility:** Scheduled jobs that generate PDF and Excel fleet cost reports.

**Key classes to build:**
- `ReportJobConfig` — Spring Batch Job configuration
- `WeeklyReportJobScheduler` — `@Scheduled` trigger every Monday 6AM
- `MonthlyReportJobScheduler` — `@Scheduled` trigger every 1st of month
- `PdfReportGenerator` — uses iText/OpenPDF to produce fleet cost PDF
- `ExcelReportGenerator` — uses Apache POI to produce .xlsx breakdown
- `ReportController` — GET `/api/reports/{id}/download`

**Spring Batch Job structure:**
```
Job: weeklyFleetReportJob
  └── Step 1: aggregateFuelDataStep (ItemReader → ItemProcessor → ItemWriter)
  └── Step 2: generatePdfStep
  └── Step 3: generateExcelStep
  └── Step 4: saveReportRecordStep
```

**PDF report sections to include:**
1. Cover page: Fleet name, report period, generated date
2. Summary table: Total vehicles, total fuel cost, average efficiency score
3. Per-vehicle breakdown: Vehicle, driver, total liters, total cost, avg efficiency
4. Top 5 most expensive routes
5. Active alerts summary

---

## API Endpoints Reference

### Auth
```
POST   /api/auth/register          → Register new user
POST   /api/auth/login             → Login, returns JWT token
```

### Vehicles
```
GET    /api/vehicles               → List all vehicles (MANAGER, ADMIN)
POST   /api/vehicles               → Register vehicle (MANAGER, ADMIN)
GET    /api/vehicles/{id}          → Get vehicle detail
PUT    /api/vehicles/{id}          → Update vehicle
DELETE /api/vehicles/{id}          → Delete vehicle (ADMIN)
GET    /api/vehicles/{id}/stats    → Fuel stats summary for vehicle
POST   /api/vehicles/lookup-epa    → Search FuelEconomy.gov by make/model/year
```

### Fuel Logs
```
GET    /api/fuel-logs              → All logs (filterable by vehicle, driver, date range)
POST   /api/fuel-logs              → Create new fuel log entry
GET    /api/fuel-logs/{id}         → Get single log
DELETE /api/fuel-logs/{id}         → Delete log (ADMIN)
GET    /api/fuel-logs/stats        → Aggregated cost stats (total, avg, by vehicle)
```

### Route Logs
```
GET    /api/routes                 → All route logs
POST   /api/routes                 → Log a new route (calls GraphHopper internally)
GET    /api/routes/{id}            → Route detail with efficiency score
GET    /api/routes/top-inefficient → Top 10 most fuel-inefficient routes
```

### Alerts
```
GET    /api/alerts                 → All alerts (filterable by type, vehicle, read status)
PUT    /api/alerts/{id}/read       → Mark alert as read
GET    /api/alerts/unread-count    → Count of unread alerts
```

### Reports
```
GET    /api/reports                → List generated reports
POST   /api/reports/generate       → Manually trigger a report generation
GET    /api/reports/{id}/download  → Download PDF or Excel file
```

### Dashboard (KPI)
```
GET    /api/dashboard/summary      → Total cost MTD, fleet efficiency score, active alerts count
GET    /api/dashboard/top-drivers  → Driver efficiency leaderboard
GET    /api/dashboard/cost-trend   → Monthly cost trend (last 6 months)
```

---

## Business Logic Rules

### Fuel Efficiency Score
```
Formula: efficiency_score = actual_liters_used / expected_liters

expected_liters = distance_km / (combined_mpg * 1.60934)
  where: 1 MPG = 1.60934 km/L

Score interpretation:
  0.8 - 1.0  → Excellent (under expected consumption) ✅
  1.0 - 1.1  → Normal ✅
  1.1 - 1.25 → Watch (10-25% over expected) ⚠️
  > 1.25     → Alert triggered (25%+ over expected) 🚨
```

### Actual Fuel Used Computation
Since drivers log fill-ups separately from routes, compute actual fuel per route as:
```
actual_fuel_for_route = (odometer_at_end - odometer_at_start) / (combined_mpg * 1.60934)
OR
Use the next fuel log's liters_filled after the route trip date as an approximation
```
> 💡 For simplicity in v1: let the driver manually input `actual_fuel_used_liters` when logging a route. Add auto-computation in v2.

### Cost per KM
```
cost_per_km = total_fuel_cost_in_period / total_km_driven_in_period
```

---

## Week-by-Week Build Roadmap

### ✅ Week 1: Foundation & Core Modules

**Day 1–2: Project Setup**
- [ ] Initialize Spring Boot project via `start.spring.io`
  - Dependencies: Web, Security, JPA, PostgreSQL, Validation, Lombok, DevTools
- [ ] Set up Docker Compose with PostgreSQL + PostGIS
- [ ] Configure `application.yml` with DB connection, JPA settings
- [ ] Set up project package structure (see below)
- [ ] Initialize GitHub repo, add `.gitignore`, README

**Day 3–4: Auth Module**
- [ ] Create `User` entity and `UserRepository`
- [ ] Implement `JwtService` (generate, validate, extract claims)
- [ ] Implement `UserDetailsServiceImpl`
- [ ] Implement `JwtAuthFilter`
- [ ] Configure `SecurityConfig` (permit `/api/auth/**`, secure rest by role)
- [ ] Build `AuthController` (register + login endpoints)
- [ ] Test with Postman: register → login → get token → use token on protected route

**Day 5–7: Vehicle Registry**
- [ ] Create `Vehicle` entity and `VehicleRepository`
- [ ] Build `FuelEconomyApiClient` using `RestTemplate`
- [ ] Build `VehicleService` — register vehicle + enrich with EPA data
- [ ] Build `VehicleController` — CRUD + `/lookup-epa` endpoint
- [ ] Write unit tests for `VehicleService` with Mockito

---

### ✅ Week 2: Intelligence Layer

**Day 8–9: Fuel Log Service**
- [ ] Create `FuelLog` entity and `FuelLogRepository`
- [ ] Build `FuelLogService` — save log, auto-compute total_cost
- [ ] Build `FuelLogController` — POST + GET with filters
- [ ] Add aggregate queries (total cost by vehicle/period)

**Day 10–11: Route Log + GraphHopper Integration**
- [ ] Create `RouteLog` entity and `RouteLogRepository`
- [ ] Build `GraphHopperApiClient`
- [ ] Build `EfficiencyScoringEngine` (efficiency score computation)
- [ ] Build `RouteLogService` — save route, call GraphHopper, compute score
- [ ] Build `RouteLogController`

**Day 12–13: Alert Engine**
- [ ] Create `Alert` entity and `AlertRepository`
- [ ] Build `AlertService` with all 4 alert rules
- [ ] Hook alert checks into `FuelLogService` and `RouteLogService`
- [ ] Build `AlertController`
- [ ] Write integration test: save a fuel log that triggers an alert, verify alert exists

**Day 14: Dashboard KPIs**
- [ ] Build `DashboardController` with summary, top drivers, cost trend endpoints
- [ ] Implement aggregate queries in repositories using JPQL

---

### ✅ Week 3: Reports, Polish & Deploy

**Day 15–16: Report Generator**
- [ ] Add Spring Batch, iText (OpenPDF), Apache POI dependencies
- [ ] Configure Spring Batch (BatchConfig, DataSource)
- [ ] Build `PdfReportGenerator` — fleet cost PDF with tables
- [ ] Build `ExcelReportGenerator` — multi-sheet .xlsx with Apache POI
- [ ] Build `ReportJobConfig` — Spring Batch Job with steps
- [ ] Add `@Scheduled` triggers for weekly and monthly jobs
- [ ] Build `ReportController` — list and download endpoints

**Day 17–18: Testing**
- [ ] Unit tests: `VehicleService`, `FuelLogService`, `EfficiencyScoringEngine`, `AlertService`
- [ ] Integration tests: Auth flow, fuel log → alert trigger, route log → score computed
- [ ] Use `@SpringBootTest` + H2 in-memory DB for integration tests
- [ ] Aim for 60%+ test coverage

**Day 19: Documentation & Swagger**
- [ ] Add SpringDoc OpenAPI dependency
- [ ] Annotate controllers with `@Operation`, `@ApiResponse`
- [ ] Test Swagger UI at `http://localhost:8080/swagger-ui.html`
- [ ] Write `README.md` usage section with example curl commands

**Day 20–21: Containerize & Deploy**
- [ ] Write `Dockerfile` for Spring Boot app
- [ ] Update `docker-compose.yml` to include app + postgres
- [ ] Write GitHub Actions workflow: build → test → Docker build → push to registry
- [ ] Deploy to Render or Railway (free tier)
- [ ] Add seed data script with realistic sample fleet data

---

## Project Structure

```
fleetwise/
├── src/
│   ├── main/
│   │   ├── java/com/fleetwise/
│   │   │   ├── FleetwiseApplication.java
│   │   │   │
│   │   │   ├── auth/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   │
│   │   │   ├── user/
│   │   │   │   ├── User.java              (Entity)
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── UserRole.java          (Enum)
│   │   │   │
│   │   │   ├── vehicle/
│   │   │   │   ├── VehicleController.java
│   │   │   │   ├── VehicleService.java
│   │   │   │   ├── VehicleRepository.java
│   │   │   │   ├── Vehicle.java           (Entity)
│   │   │   │   └── FuelEconomyApiClient.java
│   │   │   │
│   │   │   ├── fuellog/
│   │   │   │   ├── FuelLogController.java
│   │   │   │   ├── FuelLogService.java
│   │   │   │   ├── FuelLogRepository.java
│   │   │   │   └── FuelLog.java           (Entity)
│   │   │   │
│   │   │   ├── route/
│   │   │   │   ├── RouteLogController.java
│   │   │   │   ├── RouteLogService.java
│   │   │   │   ├── RouteLogRepository.java
│   │   │   │   ├── RouteLog.java          (Entity)
│   │   │   │   ├── GraphHopperApiClient.java
│   │   │   │   └── EfficiencyScoringEngine.java
│   │   │   │
│   │   │   ├── alert/
│   │   │   │   ├── AlertController.java
│   │   │   │   ├── AlertService.java
│   │   │   │   ├── AlertRepository.java
│   │   │   │   ├── Alert.java             (Entity)
│   │   │   │   └── AlertType.java         (Enum)
│   │   │   │
│   │   │   ├── report/
│   │   │   │   ├── ReportController.java
│   │   │   │   ├── ReportJobConfig.java   (Spring Batch)
│   │   │   │   ├── ReportScheduler.java
│   │   │   │   ├── PdfReportGenerator.java
│   │   │   │   ├── ExcelReportGenerator.java
│   │   │   │   └── ReportJob.java         (Entity)
│   │   │   │
│   │   │   ├── dashboard/
│   │   │   │   └── DashboardController.java
│   │   │   │
│   │   │   └── common/
│   │   │       ├── ApiResponse.java       (Generic response wrapper)
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── PageResponse.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/              (Flyway migrations — optional but good practice)
│   │           ├── V1__create_users.sql
│   │           ├── V2__create_vehicles.sql
│   │           ├── V3__create_fuel_logs.sql
│   │           └── V4__create_route_logs.sql
│   │
│   └── test/
│       └── java/com/fleetwise/
│           ├── vehicle/VehicleServiceTest.java
│           ├── fuellog/FuelLogServiceTest.java
│           ├── route/EfficiencyScoringEngineTest.java
│           └── alert/AlertServiceTest.java
│
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Environment Variables

Create a `.env` file (never commit this to Git):

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/fleetwise
DB_USERNAME=postgres
DB_PASSWORD=yourpassword

# JWT
JWT_SECRET=your-super-secret-key-at-least-256-bits-long
JWT_EXPIRATION_MS=86400000

# GraphHopper
GRAPHHOPPER_API_KEY=your-graphhopper-api-key

# Reports output directory
REPORTS_OUTPUT_PATH=/app/reports

# App
SERVER_PORT=8080
```

`application.yml` reference:
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION_MS}

graphhopper:
  api:
    key: ${GRAPHHOPPER_API_KEY}
```

---

## Running the Project

### Prerequisites
- Java 17+
- Docker + Docker Compose
- Maven

### 1. Start the database
```bash
docker-compose up -d postgres
```

### 2. Run the Spring Boot app
```bash
./mvnw spring-boot:run
```

### 3. Access Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 4. Run with full Docker Compose (app + db)
```bash
docker-compose up --build
```

### 5. Run tests
```bash
./mvnw test
```

### VS Code Dev Setup (Windows)

Use [DEV_SETUP_WINDOWS.md](DEV_SETUP_WINDOWS.md) for first-time setup of:

- JDK 17
- Maven
- Docker Desktop
- Recommended VS Code extensions
- Project tasks for run/test/database workflows

---

## Testing Strategy

| Test Type | Tool | What to Test |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Service layer business logic (no DB, no HTTP) |
| Integration Tests | @SpringBootTest + H2 | Full request → DB flow |
| API Tests | Postman Collections | All endpoints manually |
| Contract Tests | Swagger UI | Verify request/response shapes |

**Minimum coverage targets:**
- `EfficiencyScoringEngine` → 100% (pure math, easy to test)
- `AlertService` → 100% (rule-based, easy to test)
- `FuelLogService` → 80%+
- `VehicleService` → 80%+

---

## README Maintenance

Update this document whenever any of the following changes:
- API endpoint contracts (path, payload, response, role access)
- Database schema or business logic formulas/rules
- External provider integration details (FuelEconomy, GraphHopper, alternatives)
- Build order, roadmap milestones, or deployment/testing strategy

Update workflow:
1. Edit the affected section in this README.
2. Add one row in the change log with date, section, and summary.
3. If code already exists, ensure implementation matches the updated spec.
4. Continue implementation using the newest README state only.

### Change Log

| Date | Section | Change Summary |
|---|---|---|
| 2026-04-08 | Project documentation baseline | Declared this file as the FleetWise source of truth and added maintenance workflow. |
| 2026-04-08 | Architecture + setup | Added explicit architecture choice (layered modular monolith) and linked VS Code Windows setup guide. |
| 2026-04-08 | Milestone 1 bootstrap | Added initial Spring Boot scaffold (auth baseline, migration, Docker DB config, VS Code setup assets). |
| 2026-04-08 | Build tooling | Adopted Maven Wrapper-first workflow and validated build/tests via wrapper. |

---

## Resume Bullet Points

Once complete, use these on your resume under Projects:

> **FleetWise — Fleet Fuel Intelligence & Route Optimization API** *(Java, Spring Boot 3)*
> Built a production-grade fleet management backend using Spring Boot 3, Spring Security (JWT), and PostgreSQL — integrating the FuelEconomy.gov API for real vehicle MPG data and GraphHopper for route distance calculation. Implemented a rule-based alert engine for fuel overconsumption detection, a driver efficiency scoring system, and automated weekly/monthly PDF and Excel report generation via Spring Batch, iText, and Apache POI. Containerized with Docker and deployed via GitHub Actions CI/CD.

**Tech keywords that will appear on your resume (enterprise-friendly):**
`Java 17` `Spring Boot 3` `Spring Security` `JWT` `Spring Batch` `Spring Data JPA` `Hibernate` `PostgreSQL` `PostGIS` `RESTful API` `GraphHopper` `iText` `Apache POI` `Docker` `GitHub Actions` `JUnit 5` `Mockito` `OpenAPI / Swagger` `CI/CD`

---

## 💡 Tips for Using This With Your AI Coding Assistant

1. **Reference this file at the start of every session** — paste relevant sections as context
2. **Build module by module** — don't jump ahead. Finish Auth before Vehicle, finish Vehicle before Route
3. **Ask your assistant to implement one class at a time** — e.g. *"Implement FuelLogService based on the schema and business logic in my README"*
4. **When stuck on GraphHopper** — ask specifically: *"Help me implement GraphHopperApiClient.java that calls the GraphHopper Directions API, parses distance in km and duration in minutes, and handles errors gracefully"*
5. **When stuck on Spring Batch** — ask: *"Help me set up a Spring Batch job that runs every Monday, reads all fuel logs from the past 7 days, and passes them to my PdfReportGenerator"*
6. **Test as you go** — after each module, write at least the happy-path unit test before moving on

---

*FleetWise — Built by Rick Andrew Macapagal | BS Information Technology - Business Analytics | Batangas State University*
