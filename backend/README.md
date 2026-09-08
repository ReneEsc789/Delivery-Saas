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
4. In the `app:` block, replace `app.jwt.secret` with any string of **at least 32 characters**. It's the HS256 signing key for the auth JWT — any locally-generated value works for development; production uses a real secret. The other keys (`app.jwt.expiration`, `app.auth.*`, `app.cors.allowed-origins`) can stay as-is for local work.

**Never commit your real `application.yml`.** It's already listed in `.gitignore`.

### `application.yml` keys

| Key | Purpose |
|---|---|
| `spring.datasource.*` | PostgreSQL connection (URL, username, password) |
| `spring.jpa.hibernate.ddl-auto` | Kept at `validate` — Flyway owns the schema, Hibernate only checks the entities match it |
| `spring.flyway.*` | Migration settings (location, baseline) |
| `app.jwt.secret` | HS256 key used to sign/verify the auth JWT (min 32 chars) |
| `app.jwt.expiration` | Token lifetime, ISO-8601 duration (`PT24H` = 24 h) |
| `app.auth.cookie-name` | Name of the HttpOnly cookie that carries the JWT |
| `app.auth.cookie-secure` | `false` for local HTTP, `true` in production (HTTPS only) |
| `app.cors.allowed-origins` | Front-end origin(s) allowed to call the API with credentials |

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
- **Naming convention:** `V<number>__<description>.sql`
  - Capital `V`, then the version number, then **two** underscores (`__`), then a short snake_case description, then `.sql`.
  - Example: `V15__add_delivery_index.sql`.
  - A single underscore, a lowercase `v`, or a wrong extension makes Flyway ignore or reject the file.
- **Numbering is sequential.** The current highest migration is `V14`, so the next one is `V15`, then `V16`, and so on. Don't reuse or skip numbers.
- Migrations run automatically every time the app starts — **never edit a migration that has already been applied and committed.** If you need to change the schema, create a new migration with the next number.
- After changing the schema, update the matching JPA entities in `<feature>/domain/` so Hibernate's `validate` still passes on startup.
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