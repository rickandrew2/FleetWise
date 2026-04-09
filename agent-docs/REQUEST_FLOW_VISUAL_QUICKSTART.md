# FleetWise 2-Minute Visual Quickstart

If you are coming from Node.js and want the fastest way to understand Spring code flow, start here.

## One Real Example

Request: POST /api/vehicles

## Big Picture Diagram

CLIENT
  |
  | 1) Authorization: Bearer <jwt>
  v
[Security Filter Chain]
  |
  | JwtAuthFilter checks token
  v
[VehicleController]
  |
  | @Valid checks VehicleUpsertRequest DTO
  | @PreAuthorize checks role
  v
[VehicleService]
  |
  | business rules
  | - normalize plate
  | - check duplicates
  | - optional EPA data lookup
  v
[VehicleRepository]
  |
  | save entity to DB
  v
[PostgreSQL]
  |
  v
Response: VehicleResponse DTO (201 Created)

If something fails, GlobalExceptionHandler returns clean error JSON.

## Same Flow in Node.js Words

- Security Filter Chain + JwtAuthFilter = middleware stack
- VehicleController = route handler
- VehicleService = business/service layer
- VehicleRepository = Prisma data access calls
- Entity = Prisma model equivalent
- DTO = request/response payload type

## ELI5 Version

- Guard at the gate: JwtAuthFilter
- Front desk: VehicleController
- Rule brain: VehicleService
- Database robot: VehicleRepository
- Filing cabinet: PostgreSQL
- Delivery box: DTOs

## Where Each Piece Lives

- src/main/java/com/fleetwise/auth/JwtAuthFilter.java
- src/main/java/com/fleetwise/vehicle/VehicleController.java
- src/main/java/com/fleetwise/vehicle/VehicleService.java
- src/main/java/com/fleetwise/vehicle/VehicleRepository.java
- src/main/java/com/fleetwise/vehicle/Vehicle.java
- src/main/java/com/fleetwise/vehicle/dto/VehicleUpsertRequest.java
- src/main/java/com/fleetwise/vehicle/dto/VehicleResponse.java
- src/main/java/com/fleetwise/common/GlobalExceptionHandler.java

## 30-Second Memory Trick

Say this out loud:

Gate -> Desk -> Brain -> Robot -> DB -> Box Back

That sentence is most of Spring backend flow in this project.