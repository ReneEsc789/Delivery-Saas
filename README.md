# Delivery SaaS

Delivery SaaS is a logistics management platform designed for delivery companies.

It allows businesses to manage customers, orders, warehouses, drivers, vehicles, and deliveries, while applying real optimization algorithms to solve logistics problems such as driver assignment, warehouse selection, vehicle selection, delivery prioritization, and route calculation.

The system is multi-tenant, allowing different companies to use the same platform while keeping their data completely isolated, and includes user authentication, role-based access control, and full audit tracking of operations.

## Tech Stack

### Backend
- **Java 25** — Main backend programming language.
- **Spring Boot** — Backend framework.
- **Spring Web** — REST API development.
- **Spring Data JPA** — Database access and repositories.
- **Hibernate** — ORM for mapping Java entities to database tables.
- **Spring Security** — Authentication and role-based authorization.
- **JWT + HttpOnly Cookies** — Authentication and session handling.
- **Jakarta Validation** — Request and data validation.
- **Maven** — Dependency management and build automation.

### Database
- **PostgreSQL** — Relational database.
- **Flyway** — Database migrations and schema versioning.

### Algorithms
- **Dijkstra** — Shortest-path route calculation.
- **Greedy / Weighted Scoring** — Driver, warehouse, and vehicle selection.
- **Priority Queue / Heap** — Delivery prioritization.
- **Nearest Neighbor + 2-opt** — Multi-delivery route optimization.
- **Interval Scheduling** — Delivery time window management.

### API Documentation
- **OpenAPI / Swagger** — REST API documentation and testing.

### Testing
- **JUnit 5** — Java testing framework.
- **Mockito** — Unit testing and mocking.
- **Testcontainers** — Integration testing with PostgreSQL containers.

### Frontend
- **React** — User interface.
- **TypeScript** — Type-safe frontend development.
- **Axios** — Communication with the REST API.

### Infrastructure
- **Docker** — Application containerization.
- **Docker Compose** — Runs the application services together.

## Project Structure

```
delivery-saas/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/deliverysaas/
│   │   │   │   ├── auth/
│   │   │   │   ├── organizations/
│   │   │   │   ├── users/
│   │   │   │   ├── customers/
│   │   │   │   ├── drivers/
│   │   │   │   ├── products/
│   │   │   │   ├── warehouses/
│   │   │   │   ├── vehicles/
│   │   │   │   ├── orders/
│   │   │   │   ├── deliveries/
│   │   │   │   ├── audit/
│   │   │   │   └── shared/
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       └── application.yml
│   │   └── test/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
│
├── frontend/
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── README.md
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

The backend and frontend directories contain their own documentation with setup, configuration, and development information specific to each application.

## Architecture

The backend follows a **Vertical Slice Architecture**: code is organized by feature and use case, not by global technical layers (no `controllers/`, `services/`, `repositories/` folders spanning the whole app). Each feature (e.g. `deliveries/`) contains its own use cases, keeping everything a functionality needs close together.

## Main Features

- Multi-tenant organization management
- Customer, driver, and vehicle management
- Product catalog and warehouse inventory
- Order management with items and status tracking
- Delivery management with full status history
- User authentication (JWT + HttpOnly Cookies)
- Role-based access control (ADMIN, MANAGER, DRIVER, CUSTOMER)
- Resource-level authorization (e.g. a driver can only modify their own deliveries)
- Driver assignment algorithm (weighted scoring)
- Warehouse and vehicle selection algorithms
- Delivery prioritization (priority queue)
- Route calculation (Dijkstra)
- Multi-delivery route optimization (Nearest Neighbor + 2-opt)
- Delivery time window scheduling (Interval Scheduling)
- Audit logging of operations
- Role-based dashboards on the frontend

## System Roles

| Role | Description |
|---|---|
| `ADMIN` | Full organization administration |
| `MANAGER` | Logistics operations administration |
| `DRIVER` | Delivery personnel |
| `CUSTOMER` | Places and tracks orders |

## Development Workflow

Development is done using separate branches for each feature, fix, or change.

The `main` branch should contain stable and reviewed code. Development should not be done directly on `main`.

### Before Starting

Before starting a new task, switch to `main` and get the latest changes:

```
git checkout main
git pull
```

Then create a new branch:

```
git checkout -b <type>/<name>
```

### Branch Naming

Branches should use a prefix that describes the type of work being done.

```
feature/    New functionality
fix/        Bug fixes
refactor/   Code restructuring without changing behavior
docs/       Documentation changes
test/       Tests
chore/      Configuration or maintenance
```

Examples:

```
feature/deliveries
feature/driver-assignment
feature/auth
fix/route-calculation
refactor/order-service
docs/backend-readme
test/delivery-priority-queue
chore/flyway-config
```

### Commits

Commit messages should clearly describe the change.

Use the following format:

```
<type>: <description>
```

Common commit types:

```
feat      New functionality
fix       Bug fix
refactor  Code restructuring
docs      Documentation
test      Tests
chore     Configuration or maintenance
```

Examples:

```
feat: add delivery creation endpoint
feat: add driver assignment algorithm
fix: prevent negative vehicle capacity
refactor: simplify order service
docs: update backend setup
test: add dijkstra algorithm tests
chore: configure flyway
```

### Finishing a Task

Once the work is finished:

```
git status
git add .
git commit -m "feat: add delivery management"
```

Push the branch:

```
git push -u origin feature/deliveries
```

Then create a Pull Request to merge the branch into `main`.

The general workflow is:

```
main
 │
 ├── feature/deliveries
 │       │
 │       ├── commits
 │       │
 │       └── Pull Request
 │               ↓
 │              main
 │
 ├── feature/driver-assignment
 │       │
 │       ├── commits
 │       │
 │       └── Pull Request
 │               ↓
 │              main
 │
 └── fix/route-calculation
         │
         ├── commits
         │
         └── Pull Request
                 ↓
                main
```

Avoid committing or developing features directly on `main`.

## Documentation

Specific setup and development instructions are documented inside each part of the project.

```
delivery-saas/
│
├── README.md
│
├── backend/
│   └── README.md
│
└── frontend/
    └── README.md
```

### Backend

The backend documentation contains information about:

- Java and Spring Boot configuration
- Vertical Slice backend architecture
- PostgreSQL configuration
- `application.yml`
- Flyway migrations and database versioning
- Running the backend locally

### Frontend

The frontend documentation will contain information about:

- React and TypeScript configuration
- Axios
- Frontend project structure
- Environment configuration
- Running the frontend locally

## Roadmap

Development is organized in phases:

0. Project setup
1. Database (PostgreSQL, Flyway, migrations)
2. Authentication (JWT, BCrypt, Spring Security)
3. Multi-tenancy
4. Core entities (customers, drivers, products, warehouses, vehicles)
5. Orders
6. Deliveries
7. Optimization algorithms
8. Audit logging
9. Testing
10. Frontend
11. Documentation
12. Deployment