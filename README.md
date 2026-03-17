# Vantage Admin Platform

A modern, modular business management system built with Spring Boot and React.

## Tech Stack

### Backend
- **Java 17** + **Spring Boot 4.0.3**
- **Spring Security** - Authentication & Authorization
- **Spring MVC** - Web Layer
- **Spring Data JPA** - Persistence Framework
- **H2 Database** - Embedded Database (file-based at `./data/vantage`)
- **Quartz** - Job Scheduling
- **Apache Velocity** - Template Engine (code generation)
- **Caffeine** - Local Caching
- **Micrometer** - Metrics & Observability (Prometheus, Tracing)

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
├── vantage-framework/          # Core framework components (security, config, cache)
├── vantage-modules/            # Business modules
│   ├── bms-module-system/      # System management (users, roles, permissions)
│   ├── bms-module-quartz/      # Job scheduling module
│   └── bms-module-generator/   # Code generation module
├── data/                       # H2 database files
└── logs/                       # Application logs
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

**Note:** The build process automatically installs Node.js and npm, then builds the frontend. This happens during the `generate-resources` phase via the `frontend-maven-plugin`.

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
| Actuator Health | http://localhost:8081/actuator/health |
| Actuator Metrics | http://localhost:8081/actuator/metrics |
| Actuator Prometheus | http://localhost:8081/actuator/prometheus |

**H2 Console Login:**
- JDBC URL: `jdbc:h2:file:./data/vantage`
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
- Database connection (H2 file-based with Oracle compatibility mode)
- Logging levels
- Actuator endpoints
- Tracing sampling

## Database

The application uses H2 database in file mode with Oracle compatibility. Data persists in `./data/vantage.mv.db`.

Schema and initial data are loaded from:
- `classpath:schema.sql` - Database schema (17 tables including Quartz scheduler tables)
- `classpath:data.sql` - Initial seed data

### Database Tables

| Table | Description |
|-------|-------------|
| `sys_user` | User accounts |
| `sys_role` | Roles |
| `sys_menu` | Menus & permissions |
| `sys_user_role` | User-Role mapping |
| `sys_role_menu` | Role-Menu mapping |
| `sys_user_post` | User-Post mapping |
| `sys_post` | Job positions |
| `sys_dict_type` | Dictionary types |
| `sys_dict_data` | Dictionary data |
| `sys_config` | System configuration |
| `sys_logininfor` | Login audit logs |
| `sys_oper_log` | Operation audit logs |
| `sys_notice` | System notices |
| `sys_job` | Quartz scheduled jobs |
| `sys_job_log` | Job execution logs |
| `gen_table` | Code generation table metadata |
| `gen_table_column` | Code generation column metadata |
| `QRTZ_*` | Quartz scheduler tables (10 tables) |

## Modules

| Module | Description |
|--------|-------------|
| `vantage-common` | Shared utilities, base controllers, domain events, annotations, exception handling |
| `vantage-framework` | Core framework: security config, cache config, Quartz config, login user context |
| `vantage-module-system` | User/role/menu management, login records, operation logs, config, dict, posts, notices |
| `vantage-module-quartz` | Scheduled job management with Quartz integration |
| `vantage-module-generator` | Code generation for entities, repositories, services, and controllers |

## Features

### Authentication & Authorization
- ✅ Login/Logout with session-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Permission-based method security
- ✅ Login success/failure recording (`sys_logininfor`)
- ✅ BCrypt password hashing (use `HashGen.java` to generate hashes)

### Operation Logging
- ✅ Automatic operation logging via AOP (`@Log` annotation)
- ✅ Records all REST API calls
- ✅ Tracks: user, IP, browser, OS, execution time, request/response
- ✅ Query and filter operations (`sys_oper_log`)

### System Management
- ✅ User management (CRUD)
- ✅ Role management
- ✅ Menu management
- ✅ Config management
- ✅ Dict management
- ✅ Post management
- ✅ Notice management

### Job Scheduling
- ✅ Quartz-based job scheduling
- ✅ Cron expression support
- ✅ Job execution logging
- ✅ Multiple misfire policies
- ✅ Concurrent execution control

### Code Generation
- ✅ Entity generation (JPA entities)
- ✅ Repository generation
- ✅ Service layer generation
- ✅ Controller generation
- ✅ Supports CRUD and tree templates

### Observability
- ✅ Spring Actuator endpoints (health, info, metrics)
- ✅ Prometheus metrics export
- ✅ Distributed tracing (Micrometer Tracing)
- ✅ Structured logging with trace/span IDs

### Additional Features
- ✅ Real-time chat interface (WebSocket-based)
- ✅ Dashboard with metrics overview
- ✅ Responsive UI with sidebar navigation

## API Endpoints

### Authentication
- `POST /login` - User login
- `POST /logout` - User logout

### System
- `GET/POST/PUT/DELETE /system/users` - User management
- `GET/POST/PUT/DELETE /system/roles` - Role management
- `GET/POST/PUT/DELETE /system/menus` - Menu management
- `GET/POST/PUT/DELETE /system/posts` - Post management
- `GET/POST/PUT/DELETE /system/dicts` - Dictionary management
- `GET/POST/PUT/DELETE /system/configs` - Config management
- `GET/POST/PUT/DELETE /system/notices` - Notice management
- `GET /system/logininfor` - Login records
- `GET /system/operlog` - Operation logs

### Jobs
- `GET/POST/PUT/DELETE /quartz/jobs` - Job management
- `GET /quartz/logs` - Job execution logs

### Generator
- `GET /generator/list` - List tables for generation
- `POST /generator/code` - Generate code

## Utility Scripts

### HashGen.java
Generate BCrypt password hashes:
```bash
# Compile
javac -cp "path/to/spring-security-crypto.jar" HashGen.java

# Run
java -cp ".;path/to/spring-security-crypto.jar" HashGen
```

## AI Chat Assistant

The application includes an AI-powered chat assistant with RAG (Retrieval-Augmented Generation).

### Prerequisites

1. **Install Ollama**: Download from [ollama.ai](https://ollama.ai)

2. **Pull a language model** (choose based on your RAM):
   ```bash
   # For 8-16GB RAM (BEST - Only 637MB!)
   ollama pull tinyllama       # 1.1B model, ~637MB RAM, very fast
   
   # For 16-32GB RAM (Good quality)
   ollama pull mistral         # 7B model, ~4-6GB RAM, good quality
   
   # For 32GB+ RAM (Best quality)
   ollama pull llama3          # 8B model, ~8GB RAM, very good
   ```

3. **Enable AI in configuration** (`application.yml`):
   ```yaml
   ai:
     enabled: true
     chat-model: phi3  # Match the model you pulled
   ```

4. **Restart the application**

### Model Comparison

| Model | Parameters | RAM Required | Speed | Quality | Best For |
|-------|-----------|--------------|-------|---------|----------|
| TinyLlama | 1.1B | 637MB | ⚡⚡⚡ Very Fast | Good | Laptops, 8-16GB RAM |
| Mistral | 7B | 4-6GB | ⚡⚡ Fast | Very Good | 16-32GB RAM |
| Llama 3 | 8B | 8GB | ⚡ Medium | Very Good | 32GB+ RAM |

### Features

- **Natural Language Chat**: Ask questions about system features
- **Tool Integration**: Create users, roles, manage system via chat
- **RAG Knowledge Base**: 10 pre-loaded system documentation articles
- **Conversation Memory**: Remembers context across multiple messages
- **Fallback Mode**: Works without AI (rule-based responses)

### Access

Navigate to **Chat** in the admin panel to use the AI assistant.

## License

MIT License
