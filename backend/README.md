# Delivery SaaS — Backend

REST API backend for the Delivery SaaS platform, built with Java 25 and Spring Boot.

## Tech Stack

- **Java 25**
- **Spring Boot 4.1.1**
- **Spring Web** — REST API
- **Spring Data JPA** + **Hibernate** — persistence
- **PostgreSQL Driver**
- **Flyway** — database migrations
- **Spring Security** — authentication and authorization
- **Jakarta Validation**
- **Maven** (via Maven Wrapper — no need to install Maven separately)

## Architecture

The backend follows a **Vertical Slice Architecture**: code is organized by feature (`auth/`, `orders/`, `deliveries/`, etc.), not by global technical layers. See the root [README.md](../README.md) for the full package structure.

## Prerequisites

Before running the backend, install:

1. **JDK 25** — [Eclipse Temurin](https://adoptium.net) is recommended. Verify with:
   ```
   java --version
   javac --version
   ```
2. **PostgreSQL** (with pgAdmin recommended for a GUI) — [postgresql.org/download](https://www.postgresql.org/download/)
3. **VS Code** with the "Extension Pack for Java" and "Spring Boot Extension Pack" extensions (or any IDE with Spring Boot support)

You do **not** need to install Maven separately — this project uses the Maven Wrapper (`mvnw` / `mvnw.cmd`), which downloads the correct Maven version automatically.

## Database Setup

1. Open pgAdmin (or `psql`) and create a database named exactly:
   ```
   delivery_saas
   ```
2. Confirm the port your PostgreSQL server is running on (default is `5432`) — right-click the server in pgAdmin → Properties → Connection tab.

## Configuration

This project uses `application.yml` for configuration, **which is not committed to the repository** (it contains your local database password).

1. Copy the example file:
   ```
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   ```
   (On Windows PowerShell: `Copy-Item src\main\resources\application.yml.example src\main\resources\application.yml`)
2. Open `src/main/resources/application.yml` and replace `TU_CONTRASEÑA_AQUI` with your own local PostgreSQL password.
3. If your PostgreSQL server uses a different port or database name, update the `spring.datasource.url` line accordingly.

**Never commit your real `application.yml`.** It's already listed in `.gitignore`.

## Running the Backend Locally

From the `backend/` directory:

```
.\mvnw.cmd spring-boot:run
```

(On macOS/Linux: `./mvnw spring-boot:run`)

On startup, Flyway will automatically apply any pending migrations found in `src/main/resources/db/migration/` against your `delivery_saas` database. The API will be available at:

```
http://localhost:8080
```

## Database Migrations (Flyway)

- Migration files live in `src/main/resources/db/migration/`.
- Naming convention: `V<number>__<description>.sql` (e.g. `V1__create_organizations.sql`, `V2__create_users.sql`).
- Migrations run automatically every time the app starts — never edit a migration that has already been applied and committed. If you need to change something, create a new migration instead.
- Do not set `spring.jpa.hibernate.ddl-auto` to `update` or `create` — Flyway is the single source of truth for the schema. It's kept at `validate` in `application.yml`.

## Troubleshooting

**`FATAL: database "delivery_saas" does not exist`**
The database wasn't created correctly (or was created with a different name/case). Recreate it in pgAdmin with the exact name `delivery_saas`, and confirm the port in your `application.yml` matches your server's real port (check via pgAdmin → server Properties → Connection).

**`Failed to determine a suitable driver class`**
`application.yml` is missing or wasn't found. Confirm the file exists at `src/main/resources/application.yml` (not just the `.example` version) and that there's no leftover `application.properties` conflicting with it.

## Testing

```
.\mvnw.cmd test
```

(Testcontainers will be added in a later phase for integration tests against a real PostgreSQL instance.)