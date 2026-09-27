# Business Management Platform

A login-gated web app for a business owner to manage customers, suppliers, and expenses, with insights (overall and per-customer) to support growth decisions.

---

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/akhilgarg/businessplatform/
│   │   │   ├── BusinessplatformApplication.java
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Supplier.java
│   │   │   │   ├── Transaction.java
│   │   │   │   └── Expense.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── SupplierRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── ExpenseRepository.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── SupplierController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── ExpenseController.java
│   │   │   │   └── InsightsController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── SupplierService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   └── InsightsService.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── CustomerDTO.java
│   │   │   │   ├── SupplierDTO.java
│   │   │   │   ├── TransactionDTO.java
│   │   │   │   ├── ExpenseDTO.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── CustomerInsightsResponse.java
│   │   │   │   └── OverviewInsightsResponse.java
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── UnauthorizedException.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/akhilgarg/businessplatform/
│           └── (unit/integration tests, later)
│
├── pom.xml
├── mvnw / mvnw.cmd
└── .gitignore
```

### What each layer does

**`entity/`** — Your database tables, as Java classes. Each class annotated `@Entity` maps directly to a MySQL table (e.g. `Customer.java` → `customer` table). Relationships (`@ManyToOne`, `@OneToMany`) live here.

**`repository/`** — One interface per entity, extending `JpaRepository<EntityType, IdType>`. Spring Data JPA auto-generates all the basic CRUD (`save`, `findById`, `findAll`, `delete`) just from the interface. Custom query methods for insights (e.g. `SUM`, `GROUP BY`) go here too.

**`controller/`** — The actual REST API endpoints (`GET /customers`, `POST /transactions`, etc.). Controllers receive HTTP requests, delegate the real work to a `service`, and return the response. Thin layer — no business logic here.

**`service/`** — Where the actual business logic lives: calling repositories, computing things (like `is_regular` status), enforcing the "scope everything to logged-in user" rule, and building the insights calculations.

**`dto/`** (Data Transfer Objects) — Plain classes that define exactly what shape of JSON goes in/out of the API, separate from the database entities. Keeps sensitive fields (like password hash) out of API responses.

**`security/`** — JWT generation/validation, the Spring Security filter chain config, and how Spring Security loads a user's details during login. Makes `/customers`, `/suppliers`, etc. require a valid token.

**`exception/`** — Centralized error handling, so the API returns clean JSON errors (e.g. `404 Customer not found`) instead of raw stack traces.

This structure follows a layered architecture: `Controller → Service → Repository → Entity`, the standard Spring Boot pattern.

---

## Tech Stack

| Layer | Tool |
|---|---|
| Backend | Spring Boot, Spring Data JPA, Spring Security, JWT |
| Database | MySQL |
| Frontend | React, Chart.js/Recharts, Axios |
| DevOps | Docker, Docker Compose, GitHub Actions |