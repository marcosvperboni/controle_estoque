# Inventory Control (Controle de Estoque)

A complete inventory management system built as a portfolio project, following market best practices: **Clean Code**, **DDD (Domain-Driven Design)**, **TDD**, design patterns and CI/CD.

## Features

- **Product registration** with supplier, brand, expiration date, name, barcode, retail price, wholesale price, purchase price, sale price, quantity in stock, and a minimum-stock reference used for purchase alerts. **Margin** and **markup** are always derived server-side from purchase/sale price, never trusted from client input.
- **Automatic electronic invoice (NF-e) import**: upload the invoice XML and the system automatically registers new products from it, or restocks existing ones (matched by barcode) instead of duplicating them.
- **Automatic purchase list**: any product whose current stock falls to **30% or less of its registered minimum-stock reference** is flagged and surfaced in a dedicated "Lista de Compras" screen, with a suggested purchase quantity and an estimated cost.
- **Full CRUD** (`GET`, `POST`, `PUT`, `DELETE`) persisted in **PostgreSQL**, with validation on every endpoint and a global exception handler returning a consistent error payload.
- **Field-level encryption at rest** for sensitive business data (purchase price and margin) using AES-256-GCM, transparent to the rest of the application via a JPA `AttributeConverter`.
- **Light/dark theme** toggle, including icon and color-token adaptation for both themes.
- **Automated tests** on the backend (JUnit 5 + Mockito + MockMvc/H2) and the frontend (Jasmine/Karma).

## Tech stack

### Backend
- **Java 25**
- **Spring Boot 4** (Web, Data JPA, Validation, Actuator)
- **PostgreSQL** as the database, **Flyway** for versioned migrations
- **AES-256-GCM** (JDK `javax.crypto`, no extra dependency) for encrypting purchase price and margin columns
- JDK's built-in **DOM parser** (`javax.xml.parsers`, hardened against XXE) to read NF-e XML files - no third-party XML library needed
- **JUnit 5 + Mockito** (unit tests) and **MockMvc** (controller/integration tests, backed by **H2** in-memory)
- **Maven** as the build tool

### Frontend
- **Angular 20** (standalone components, signals, `@if`/`@for` control flow)
- **Angular Material** (Material 3 theming with a custom light/dark palette)
- **RxJS**, reactive forms with validation
- **Jasmine/Karma** for unit tests

### Infrastructure
- **Podman** (Containerfiles + `podman-compose.yml` to orchestrate the database, backend and frontend)
- **GitHub Actions** for CI/CD (build and automated tests on every push/PR)

## Architecture

The backend follows a Clean Architecture / DDD-inspired layout:

```
backend/src/main/java/com/marcosperboni/controle_estoque/
├── domain/           # Entity (Produto) and repository contract - pure business rules
├── application/      # Use cases (services), DTOs and business exceptions
├── infrastructure/   # AES-GCM encryption converter, NF-e XML parser, CORS config
└── web/              # REST controllers and global exception handling
```

The frontend is organized by feature:

```
frontend/src/app/
├── core/             # Services, models and the HTTP error interceptor
├── features/
│   ├── produtos/       # Product list + create/edit dialog
│   ├── importar-nfe/   # NF-e XML upload and import result
│   └── lista-compras/  # Automatic low-stock purchase list
```

## Design assumptions

A few decisions were made explicit because they are not fully determined by the request and are worth documenting for anyone reading the code:

- **Minimum-stock reference (`quantidadeMinima`)**: the requirement says a purchase alert should fire when stock is "30% below the stock level". Since a single number can't represent both a current quantity and a threshold, a `quantidadeMinima` field was added as the reference/ideal stock level; the alert fires when `quantidade <= quantidadeMinima * 0.30` (the 30% threshold is configurable via `app.estoque.limiar-compra-percentual`).
- **NF-e defaults**: a Brazilian NF-e XML only carries a purchase price (`vUnCom`) per item - it has no retail/wholesale sale price and no dedicated brand field. When a product is auto-registered from an invoice, the system applies a default 30% markup to derive the sale price, a 10% discount off that for the wholesale price, and takes the first word of the product description as a best-effort brand guess. These are meant as sensible starting values - the user can fine-tune every field afterwards through the product edit screen.
- **Encrypted columns**: purchase price and margin were chosen for encryption at rest since they represent competitively sensitive internal cost data, unlike the retail/wholesale/sale prices, which are customer-facing and stored in plain `NUMERIC` columns.

## How to run

### Option 1 - With Podman (recommended)

Prerequisite: [Podman](https://podman.io/) installed.

```bash
podman compose -f podman-compose.yml up --build
```

This starts three services:
- **PostgreSQL** on host port `5433` (container port `5432`)
- **Backend** (Spring Boot) on host port `8090` (container port `8080`)
- **Frontend** (Angular served via Nginx, with a reverse proxy to `/api`) on host port `4201` (container port `80`)

Ports are shifted from the framework defaults to avoid clashing with other stacks that may already be running on the same machine.

Open `http://localhost:4201`.

### Option 2 - Manually (development)

**Backend** (requires a PostgreSQL instance - adjust the environment variables as needed):

```bash
cd backend
DB_HOST=localhost DB_PORT=5432 DB_NAME=controle_estoque DB_USER=controle_estoque DB_PASSWORD=controle_estoque \
  mvn spring-boot:run
```

**Frontend**:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200` (the frontend points to `http://localhost:8080/api` in development).

## API overview

| Method | Endpoint                        | Description                                             |
|--------|----------------------------------|-----------------------------------------------------------|
| GET    | `/api/produtos`                 | List all products                                          |
| GET    | `/api/produtos/{id}`            | Get one product                                            |
| POST   | `/api/produtos`                 | Register a new product                                     |
| PUT    | `/api/produtos/{id}`            | Update a product                                            |
| DELETE | `/api/produtos/{id}`            | Delete a product                                            |
| GET    | `/api/produtos/lista-compras`   | Products at or below 30% of their minimum-stock reference   |
| POST   | `/api/notas-fiscais/importar`   | Upload an NF-e XML (multipart `arquivo`) - auto-register/restock products |

## Running the tests

```bash
# Backend
cd backend && mvn verify

# Frontend
cd frontend && npx ng test --watch=false --browsers=ChromeHeadless
```
