# Resource Planning Ledger

[![CI](https://github.com/YOUR_USERNAME/ResourcePlanningLedger/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/ResourcePlanningLedger/actions/workflows/ci.yml)

**Live URL:** [https://your-app.onrender.com](https://your-app.onrender.com)

CSCI-P532 Project 4 — A system for planning, tracking, and auditing the allocation of resources across a portfolio of work plans, with a double-entry ledger for resource consumption.

## Quick Start

### Docker Compose (recommended)

```bash
docker compose up --build
```

Open [http://localhost:8080](http://localhost:8080).

### Standalone Docker

```bash
# Start PostgreSQL
docker run -d --name rpl-db -e POSTGRES_DB=rpl -e POSTGRES_USER=rpl -e POSTGRES_PASSWORD=rpl -p 5432:5432 postgres:16-alpine

# Build and run the app
docker build -t rpl .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/rpl \
  -e SPRING_DATASOURCE_USERNAME=rpl \
  -e SPRING_DATASOURCE_PASSWORD=rpl \
  rpl
```

## Design Patterns

### State — ActionStateMachine

Each `ProposedAction` holds a `stateName` string that maps to a singleton `ActionState` bean via `ActionStateMachine`. The five concrete states (`ProposedState`, `SuspendedState`, `InProgressState`, `CompletedState`, `AbandonedState`) each implement the `ActionState` interface. Illegal transitions throw `IllegalStateTransitionException`. This pattern was chosen because the action lifecycle has well-defined states with constrained transitions, and the State pattern makes adding new states or transitions a localized change.

### Composite — PlanNode Tree

Both `Plan` (composite) and `ProposedAction` (leaf) implement the `PlanNode` interface. Plans hold child nodes which can be other Plans (sub-plans) or ProposedActions. `getStatus()` on a composite derives from its children, and `getTotalAllocatedQuantity()` sums recursively. This pattern was chosen because plans naturally form a tree structure, and clients need to treat individual actions and sub-plans uniformly.

### Iterator — DepthFirstPlanIterator

`DepthFirstPlanIterator` implements `java.util.Iterator<PlanNode>` using an explicit stack. It traverses the composite tree in depth-first order for the plan summary report (F10). The iterator operates on in-memory objects, making it fully testable without a database. This pattern was chosen to decouple traversal logic from the composite structure.

### Template Method — AbstractLedgerEntryGenerator

`AbstractLedgerEntryGenerator` defines a fixed skeleton for generating ledger entries: select allocations → validate → create transaction → build withdrawal/deposit pairs → post entries → afterPost hook. The `postEntries()` method is final and enforces balanced entries (conservation). `ConsumableLedgerEntryGenerator` is the Week 1 concrete subclass that filters for consumable allocations. This pattern was chosen because the ledger entry generation process has invariant steps (balance checking) with variant steps (which allocations to select, validation rules).

## Architecture

Four-layer architecture following Spring stereotypes:

| Layer          | Spring Stereotype | Responsibility                      |
|----------------|-------------------|--------------------------------------|
| Client         | @RestController   | HTTP routing only; zero business logic |
| Manager        | @Service          | Orchestrates use-case sequences       |
| Engine         | @Service          | Encapsulates one replaceable algorithm |
| ResourceAccess | @Repository       | Atomic business verbs; no SQL in callers |

## Technology Stack

- Java 17, Spring Boot 3.x, Spring Data JPA
- PostgreSQL 16
- Plain HTML + CSS + JavaScript frontend
- Maven, Docker, GitHub Actions CI/CD
- JUnit 5 + Mockito for testing