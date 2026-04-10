# FleetWise Dev Setup (Windows + VS Code)

This guide sets up everything needed to build FleetWise locally.

## 1. Required Tools

Install these first:

1. Git
2. JDK 25 (LTS)
3. Docker Desktop
4. VS Code

Optional fallback tool:

- Maven 3.9+ (only needed if you choose not to use Maven Wrapper)

If you use winget, run:

```powershell
winget install -e --id Git.Git
winget install -e --id EclipseAdoptium.Temurin.25.JDK
winget install -e --id Docker.DockerDesktop
winget install -e --id Microsoft.VisualStudioCode
```

If Maven is available in your winget index and you want it globally:

```powershell
winget install -e --id Apache.Maven
```

## 2. Verify Installations

Open a new PowerShell terminal and run:

```powershell
java -version
javac -version
docker --version
git --version
```

Wrapper check (recommended):

```powershell
.\mvnw.cmd -version
```

Expected:
- Java and javac should be 25.x (project target is 25)
- Maven Wrapper should run via `mvnw.cmd`

## 3. Open FleetWise in VS Code

1. Open folder in VS Code
2. Install recommended extensions when prompted
3. Copy `.env.example` to `.env`

PowerShell:

```powershell
Copy-Item .env.example .env
```

Then update `.env` secrets (especially `JWT_SECRET`).

### If `mvnw` files are missing

Use VS Code Spring Initializr to generate a temporary Maven project, then copy these into FleetWise root:

- `mvnw`
- `mvnw.cmd`
- `.mvn/wrapper/` directory

## 4. Start Local Database

```powershell
docker compose up -d postgres
```

Check status:

```powershell
docker ps
```

## 5. Run the App

```powershell
.\mvnw.cmd spring-boot:run
```

If startup succeeds, test:

- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 6. Useful VS Code Tasks

Use `Terminal -> Run Task` and select:

- `FleetWise: Docker DB Up`
- `FleetWise: Docker DB Down`
- `FleetWise: Run App`
- `FleetWise: Run Tests`

## 7. Common Fixes

### `mvn` not recognized

Close all terminals and VS Code, then reopen. If still failing, ensure Maven `bin` is in PATH.

### `mvnw.cmd` not recognized or missing

Ensure `mvnw.cmd` exists in repo root and `.mvn/wrapper` exists. If missing, regenerate from a Spring Initializr project and copy wrapper files.

### Java version not 25

Install JDK 25 and set `JAVA_HOME` to JDK 25 path.

### Docker DB connection refused

Ensure Docker Desktop is running and container `fleetwise-postgres` is healthy.
