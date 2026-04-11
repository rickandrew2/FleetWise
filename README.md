# FleetWise

FleetWise helps Philippine logistics teams reduce fuel waste by tracking every fill-up, scoring driver efficiency, surfacing alerts automatically, and generating operational reports.

This repository uses [FleetWise_README.md](FleetWise_README.md) as the canonical, living project guide.

Read product context first:
- [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) for the plain-language business explanation
- [FleetWise_README.md](FleetWise_README.md) for architecture, schema, APIs, and implementation roadmap

If you are starting implementation, begin there first:
- architecture and module flow
- schema and business rules
- API contracts
- roadmap and testing strategy
- documentation maintenance workflow

## Quick Start

1. Complete local environment setup using [DEV_SETUP_WINDOWS.md](DEV_SETUP_WINDOWS.md)
2. From repository root, install root helper scripts once: `npm install`
3. Start full stack from root with one command: `npm run dev`
4. Read [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md)
5. Open [FleetWise_README.md](FleetWise_README.md)
6. Follow the Week 1 roadmap tasks first
7. Build module-by-module in the documented order
8. Update FleetWise_README.md whenever requirements or contracts change

## Development Commands

| Command | Purpose |
| --- | --- |
| `npm run dev` | Start Docker Postgres, Spring Boot API, and Vite frontend in parallel. |
| `npm run dev:db` | Start Postgres service only using Docker Compose. |
| `npm run dev:backend` | Start Spring Boot backend only. |
| `npm run dev:frontend` | Start Vite frontend only. |
| `npm run test` | Run backend tests and frontend lint checks. |
| `npm run build` | Build backend package and frontend production bundle. |
| `npm run down` | Stop Docker Compose infrastructure services. |

### Java 25 Toolchain Requirement

Builds enforce Java 25 and Maven 3.9+ in all environments.

Add a `~/.m2/toolchains.xml` file with a JDK 25 entry (update the `jdkHome` path for your machine):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
	<toolchain>
		<type>jdk</type>
		<provides>
			<version>25</version>
		</provides>
		<configuration>
			<jdkHome>C:/Users/Acer/.jdk/jdk-25</jdkHome>
		</configuration>
	</toolchain>
</toolchains>
```
