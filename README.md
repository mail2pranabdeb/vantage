# Vantage Admin Platform

A modern business management system built with Spring Boot and React.

## Tech Stack

### Backend
- **Java 17** + **Spring Boot 4.0.3**
- **Spring Security** - Authentication & Authorization
- **Spring MVC** - Web Layer
- **Spring Data JPA** - Persistence Framework
- **H2 Database** - Embedded Database (file-based at `./data/bms`)
- **Quartz** - Job Scheduling
- **Apache Velocity** - Template Engine
- **Caffeine** - Local Caching

### Frontend
- **React 19** + **Vite**
- **React Router** - Client-side Routing
- **Lucide React** - Icon Library

## Project Structure

```
vantage-master/
├── vantage-admin/              # Main application module
│   ├── vantage-ui/             # React frontend
│   └── src/main/java/          # Backend entry point
├── vantage-common/             # Shared utilities & common code
├── vantage-framework/          # Core framework components (security, config, interceptors)
├── vantage-modules/            # Business modules
│   ├── bms-module-system/      # System management (users, roles, permissions)
│   ├── bms-module-quartz/      # Job scheduling module
│   └── bms-module-generator/   # Code generation module
└── data/                       # H2 database files
```

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 20+ (for frontend builds)
- Maven 3.6+ (or use the included Maven wrapper)

### Build (Backend + Frontend)

Build everything in a single command:

```bash
./mvnw clean package -pl vantage-admin -am -DskipTests
```

On Windows:
```cmd
mvnw.cmd clean package -pl vantage-admin -am -DskipTests
```

This builds all backend modules and the React frontend, packaging everything into an executable JAR.

**Note:** The build process automatically installs Node.js and npm, then builds the frontend. This happens during the `generate-resources` phase.

### Run

```bash
# Run with Spring Boot
./mvnw spring-boot:run -pl vantage-admin

# Or run the packaged JAR directly
java -jar vantage-admin/target/vantage-admin-0.0.1-SNAPSHOT.jar
```

On Windows:
```cmd
mvnw.cmd spring-boot:run -pl vantage-admin
:: Or
java -jar vantage-admin\target\vantage-admin-0.0.1-SNAPSHOT.jar
```

### Access the Application

| Service | URL |
|---------|-----|
| Application | http://localhost:8081 |
| H2 Console | http://localhost:8081/h2-console |

**H2 Console Login:**
- JDBC URL: `jdbc:h2:file:./data/bms`
- Username: `sa`
- Password: *(leave blank)*

## Development

### Backend

```bash
# Compile all modules
./mvnw clean install

# Run with hot reload
./mvnw spring-boot:devtools -pl vantage-admin
```

### Frontend

```bash
cd vantage-admin/vantage-ui

# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build
```

## Configuration

Edit [`vantage-admin/src/main/resources/application.yml`](vantage-admin/src/main/resources/application.yml) to customize:

- Server port (default: 8081)
- Database connection
- Logging levels

## Database

The application uses H2 database in file mode with Oracle compatibility. Data persists in `./data/bms.db`.

Schema and initial data are loaded from:
- `classpath:schema.sql`
- `classpath:data.sql`

## Modules

| Module | Description |
|--------|-------------|
| `vantage-common` | Shared utilities, constants, and base classes |
| `vantage-framework` | Core framework: security, web config, interceptors |
| `vantage-module-system` | User management, roles, permissions, menus |
| `vantage-module-quartz` | Scheduled job management |
| `vantage-module-generator` | Code generation for entities and CRUD |

## License

MIT License
