# Business Management Platform — Build Guide v2

A login-gated web app for a business owner to manage customers, suppliers, products, and expenses, with insights (overall and per-customer) to support growth decisions. Login is phone-number-first (not email), and product stock is tracked lightly — not a full inventory system.

---

## 1. Final Entity / Database Design

### User (business owner account)
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| business_name | String | |
| phone_number | String | **unique**, used for login |
| email | String | optional |
| password_hash | String | never store plain text |
| created_at | Timestamp | |

### Customer
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | scopes customer to the owning business |
| name | String | |
| phone_number | String | required, **not unique** — family members may share a number; indexed for fast lookup |
| email | String | optional |
| is_regular | Boolean | fully automatic (3+ transactions in last 60 days), no manual override |
| created_at | Timestamp | |
| — | — | `address` removed — low real-world fill rate |

### Supplier
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | |
| company_name | String | required |
| dealer_name | String | optional — specific contact under the company |
| phone_number | String | required, **not unique** — one company number may serve multiple dealers; indexed |
| email | String | optional |
| items_supplied | String (TEXT) | free text |

### Product
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | |
| name | String | |
| current_stock | Integer | running quantity |
| unit | String | optional, e.g. "pcs", "kg" |
| default_price | BigDecimal | optional — auto-fills into transaction/expense line items, overridable per line |
| barcode | String | optional, **unique** — reserved for future scan-to-fill support |

### Transaction (one customer visit/checkout)
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| customer_id | FK → Customer | |
| amount | BigDecimal | total for this visit — auto-computed from items |
| date | Date | |
| description | String | optional |

### TransactionItem (one product line within a transaction)
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| transaction_id | FK → Transaction | |
| product_id | FK → Product | nullable — a line can exist without stock tracking |
| quantity | Integer | |
| price | BigDecimal | price per unit **at time of this sale** |

### Expense (one expense event — rent, salary, or a supplier delivery)
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | |
| category | String | rent, salaries, utilities, supplier_payment, other |
| amount | BigDecimal | total — auto-computed from items when supplier-linked |
| date | Date | |
| supplier_id | FK → Supplier | nullable |
| description | String | optional |

### ExpenseItem (one product line within a supplier delivery)
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| expense_id | FK → Expense | |
| product_id | FK → Product | nullable |
| quantity | Integer | units received — increases stock automatically |
| price | BigDecimal | cost per unit for this delivery |

**Relationships, at a glance:**
```
User 1---* Customer 1---* Transaction 1---* TransactionItem *---1 Product (optional)
User 1---* Supplier 1---* Expense (nullable link) 1---* ExpenseItem *---1 Product (optional)
User 1---* Expense
User 1---* Product
```

---

## 2. API Endpoint Spec (updated)

### Auth
```
POST /auth/register     { businessName, phoneNumber, email (optional), password }
POST /auth/login        { phoneNumber, password } → returns JWT
```

### Customers
```
GET    /customers                    list all (paginated)
GET    /customers/search?phone=...   lookup by phone number (may return multiple)
GET    /customers/{id}               single customer
POST   /customers                    create
PUT    /customers/{id}               update (isRegular is read-only, never accepted here)
DELETE /customers/{id}               delete
GET    /customers/{id}/insights      per-customer insights
```

### Suppliers
```
GET    /suppliers
GET    /suppliers/search?phone=...   lookup by phone number (may return multiple)
GET    /suppliers/{id}
POST   /suppliers
PUT    /suppliers/{id}
DELETE /suppliers/{id}
POST   /suppliers/{id}/deliveries    record a delivery → auto-creates Expense + ExpenseItems, increases stock
```

### Products
```
GET    /products
POST   /products
GET    /products/{id}
```

### Transactions
```
GET    /customers/{id}/transactions   all transactions for a customer
POST   /transactions                  { customerId, date, description, items: [{ productId (optional), quantity, price (optional, defaults to product's defaultPrice) }] }
```

### Expenses
```
GET    /expenses                      list (filterable by category/date range)
POST   /expenses                      manual expense, no items (rent, salaries, utilities, other)
PUT    /expenses/{id}
DELETE /expenses/{id}
```

### Insights
```
GET /insights/overview
  → total revenue, total expenses, profit, regular vs one-time revenue split,
    top 5 customers by spend, expense breakdown by category, monthly trend

GET /customers/{id}/insights
  → total_spend, transaction_count, avg_order_value, first_purchase,
    last_purchase, days_since_last_purchase, monthly_spend_trend, status
```

---

## 3. Build Order (revised)

### Day 1 — Backend core ✅ (mostly done)
1. Spring Boot project generated (Spring Web, Spring Data JPA, MySQL Driver, Spring Security, Validation)
2. All 8 entities defined: `User`, `Customer`, `Supplier`, `Product`, `Transaction`, `TransactionItem`, `Expense`, `ExpenseItem`
3. Repositories next: one `JpaRepository` per entity
4. CRUD controllers/services for `Customer`, `Supplier`, `Product`, `Expense`
5. Test each with Postman/Swagger before moving on

### Day 2 — Auth + Transactions
1. Spring Security + JWT: register/login endpoints, `BCryptPasswordEncoder` for hashing
2. Secure all endpoints except `/auth/**` — require a valid JWT
3. Scope every query to the logged-in user (`user_id` filtering) — critical
4. `Transaction` + `TransactionItem` creation flow:
    - Accepts a list of items
    - Auto-computes `amount` as `SUM(item.quantity × item.price)`
    - Defaults each item's `price` to `product.defaultPrice` unless overridden
    - Deducts `product.currentStock` for every item with a non-null product
5. `recalculateRegularStatus()` called after every new transaction

### Day 3 — Supplier deliveries + Insights
1. `POST /suppliers/{id}/deliveries` — the auto-expense flow:
    - Accepts a list of items (product, quantity, price)
    - Auto-sets `category = "supplier_payment"`
    - Auto-computes `amount` from items
    - Increases `product.currentStock` for every item
2. Insight service methods using JPQL/native queries (`SUM`, `COUNT`, `GROUP BY month`):
    - Overview insights (revenue, expenses, profit, top customers)
    - Per-customer insights
3. Test insight endpoints against seeded data

### Day 4 — Frontend
1. React app: login/register (phone-first), customer list + detail, supplier list, delivery-recording form, product list, expense list, dashboard
2. Line-item entry UI for transactions and deliveries (add/remove product rows, auto-filled price, running total)
3. Charts: monthly trend, expense breakdown pie, revenue vs expense line
4. JWT stored client-side, attached via Axios interceptor
5. Customer detail page renders per-customer insights + trend chart

### Day 5 — DevOps layer
1. Dockerize backend + frontend
2. `docker-compose.yml`: backend + frontend + MySQL
3. GitHub Actions: `mvn test` + build both images on push
4. Deploy: backend + DB to Render/Railway, frontend to Vercel/Netlify
5. Spring Boot Actuator `/actuator/health` check

### Day 6 (optional)
- Prometheus + Grafana monitoring
- "At-risk regular customer" alerts
- Export insights as CSV/PDF
- Barcode/QR scan-to-fill using the reserved `Product.barcode` field

---

## 4. Key design decisions (interview material)

- **Phone number over email for login** — matches real usage patterns in the target market; email stays optional.
- **Phone number is searchable, not a primary key** — real-world identifiers (shared family numbers, shared dealer lines) are unsafe as unique row keys. `id` stays the true PK everywhere; phone number gets an index instead, enabling fast lookup without forcing artificial uniqueness onto data that isn't naturally unique.
- **Line-item model for both sales and deliveries** — a single checkout or a single supplier invoice almost always spans multiple products. Modeling `Transaction`/`Expense` as one event with child `*Item` rows keeps "number of visits" and "number of deliveries" accurate, while still tracking stock per product. This was a direct fix for the "bread, butter, jam — one transaction or three?" problem.
- **Stock stays deliberately minimal** — no SKUs, no reorder thresholds, no multi-warehouse concept. `Product` exists to answer "how many do I have left," not to replace inventory software. The `barcode` and `defaultPrice` fields are the only concessions to future convenience, and both are optional.
- **`isRegular` computed automatically, never manually set** — kept as a stored column for fast filtering, but only ever written by `CustomerService` after a new transaction; no endpoint accepts it as input.
- **User-scoped data isolation** — every query filters by `user_id` at the service layer.
- **BigDecimal for all money fields**, `precision=12, scale=2` at the column level — never float/double.
- **Insights computed on the fly** via aggregation queries, never stored as duplicate fields — `Transaction.amount` and `Expense.amount` are the only "totals" ever persisted, and even those are computed once at creation from line items, not maintained as a running cache.
- **JWT-based stateless auth** — no server-side session storage.

---

## 5. Tech stack summary

| Layer | Tool |
|---|---|
| Backend | Spring Boot 4.x, Spring Data JPA, Spring Security, JWT |
| Database | MySQL 8 |
| Frontend | React, Chart.js/Recharts, Axios |
| DevOps | Docker, Docker Compose, GitHub Actions |
| Deployment | Render/Railway (backend + DB), Vercel/Netlify (frontend) |

---

## 6. Project Structure

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
│   │   │   │   ├── Product.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionItem.java
│   │   │   │   ├── Expense.java
│   │   │   │   └── ExpenseItem.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── SupplierRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── TransactionItemRepository.java
│   │   │   │   ├── ExpenseRepository.java
│   │   │   │   └── ExpenseItemRepository.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── SupplierController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── ExpenseController.java
│   │   │   │   └── InsightsController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── SupplierService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   └── InsightsService.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── CustomerDTO.java
│   │   │   │   ├── SupplierDTO.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── TransactionRequest.java
│   │   │   │   ├── TransactionItemRequest.java
│   │   │   │   ├── ExpenseDTO.java
│   │   │   │   ├── DeliveryRequest.java
│   │   │   │   ├── DeliveryItemRequest.java
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
│
├── pom.xml
├── mvnw / mvnw.cmd
└── .gitignore
```

### What each layer/file does

**`entity/`** — Database tables as Java classes.
- `User`, `Customer`, `Supplier`, `Product` — the "master data" tables, each scoped to a `User` (except `User` itself).
- `Transaction` + `TransactionItem` — a sale, split into an event (`Transaction`) and its product lines (`TransactionItem`).
- `Expense` + `ExpenseItem` — the same split, for money going out (manual expenses have no items; supplier deliveries do).

**`repository/`** — One interface per entity, extending `JpaRepository<EntityType, IdType>`. Provides `save`, `findById`, `findAll`, `delete` automatically. Custom finder methods (`findByPhoneNumberContaining`, `sumAmountByUserId`, `countByCustomerIdAndDateAfter`, etc.) get added here as needed by services.

**`controller/`** — REST endpoints only. Parses requests into DTOs, calls a service, returns a response. No business logic lives here — e.g. `SupplierController.recordDelivery()` just forwards the `DeliveryRequest` to `SupplierService`/`ExpenseService` and returns what comes back.

**`service/`** — All business logic:
- `TransactionService` — builds a `Transaction` + its `TransactionItem`s, computes `amount`, deducts stock, triggers `CustomerService.recalculateRegularStatus()`.
- `ExpenseService`/`SupplierService` — the `recordSupplyDelivery()` flow: builds an `Expense` + `ExpenseItem`s, computes `amount`, increases stock, auto-sets `category = "supplier_payment"`.
- `ProductService` — plain CRUD plus the actual `increaseStock()`/`decreaseStock()` methods called by the two services above, so stock mutation logic lives in exactly one place.
- `InsightsService` — all aggregation queries for both overview and per-customer insights.
- `AuthService` — registration (hash + save) and login (verify + issue JWT).

**`dto/`** — Defines exact request/response shapes, decoupled from entities.
- `TransactionRequest`/`TransactionItemRequest` and `DeliveryRequest`/`DeliveryItemRequest` are the input shapes for the two line-item flows — each carries a list of item rows (`productId` optional, `quantity`, `price` optional).
- `RegisterRequest`/`LoginRequest` carry phone-first fields with `@Pattern`/`@NotBlank`/`@Email` (optional) validation.
- Response DTOs (`CustomerInsightsResponse`, `OverviewInsightsResponse`) shape exactly what the frontend needs, independent of entity structure.

**`security/`** — JWT issuing/validation and the Spring Security filter chain; makes every non-auth endpoint require a valid token and exposes "who is the current user" to services for `user_id` scoping.

**`exception/`** — Centralized error handling so failures return clean JSON, not stack traces.

---

## 7. Entity Relationships — how and why each one connects

This section explains each `@ManyToOne`/`@OneToMany` in terms of *why that specific connection exists*, not just the annotation mechanics.

### `Customer.user` (`@ManyToOne`) ↔ implied `User → Customers` (`@OneToMany`, not explicitly coded)
**Why it exists:** every customer belongs to exactly one business. This is what makes multi-tenancy work — Ramesh's shop and Priya's shop can both have a customer named "Suresh" without collision, because each `Customer` row carries a `user_id` pointing back to its owning business. Every customer-related query in the app filters through this relationship (`WHERE customer.user_id = currentUserId`) — it's the backbone of the entire "user-scoped data isolation" security principle.
**Direction chosen:** only `Customer → User` is coded as a Java reference (`@ManyToOne`). We don't need `User.getCustomers()` as a live Java list, since we'd almost never load "all customers" by first loading a `User` object in memory — instead, `CustomerRepository.findByUserId(userId)` queries the `customers` table directly, which is more efficient than pulling a `User` and traversing an in-memory collection.

### `Supplier.user` (`@ManyToOne`)
**Why it exists:** identical reasoning to `Customer.user` — each supplier is scoped to the business that added them. Two shop owners can each have a supplier called "ABC Distributors" as independent records.

### `Product.user` (`@ManyToOne`)
**Why it exists:** stock levels are meaningless without knowing whose stock — "20 units of Rice" only makes sense in the context of one specific shop's inventory. This relationship is what lets `ProductRepository.findByUserId(userId)` return exactly one business's product list, never leaking another business's stock data.

### `Transaction.customer` (`@ManyToOne`)
**Why it exists:** this is the relationship that makes per-customer insights possible at all. `days_since_last_purchase`, `total_spend`, `avg_order_value` — every one of these, from Section 2 of the API spec, is computed by asking "give me all `Transaction` rows where `customer_id = X`." Without this link, you'd have sales data with no way to attribute it to a specific person, and the entire "insights per customer" feature (the reason `Transaction` exists as its own entity rather than just a running total on `Customer`) would be impossible.
**Why not the other direction too:** we don't maintain `Customer.getTransactions()` as a Java list for the same reason as above — insight queries go straight through `TransactionRepository` with aggregation functions (`SUM`, `COUNT`), which is far more efficient than loading every transaction into memory just to sum them in Java.

### `Transaction.items` (`@OneToMany`, `mappedBy = "transaction"`) ↔ `TransactionItem.transaction` (`@ManyToOne`)
**Why this pair exists together:** this is the relationship that solved your "bread, butter, jam" problem directly. One real-world checkout is one `Transaction` row; each distinct product bought in that checkout becomes one `TransactionItem` row underneath it. The `@OneToMany` on `Transaction` is what lets code say `transaction.getItems()` and get back the full basket in one shot — needed both for computing `amount` (`SUM(item.quantity × item.price)`) at creation time, and for deducting stock per product afterward. The `@ManyToOne` on `TransactionItem` is the actual foreign key (`transaction_id`) that makes this possible at the database level — `mappedBy = "transaction"` tells Hibernate "don't create a second, redundant join table; the relationship already lives on the other side."
**Why `cascade = CascadeType.ALL` specifically here:** a `TransactionItem` has no meaning on its own — it can't exist without a parent `Transaction` (you'd never have a line item that isn't part of some checkout). Cascading means saving one `Transaction` object automatically saves all its `items` in the same operation, and deleting a `Transaction` automatically deletes its line items too (no orphaned rows left pointing at a nonexistent transaction).

### `TransactionItem.product` (`@ManyToOne`, nullable)
**Why it exists:** this is the actual stock-linking mechanism. When `TransactionService` processes a saved `Transaction`, it loops over `items`, and for every item where `getProduct() != null`, it calls `productService.decreaseStock(product, quantity)`. This is also why it's **nullable** — a shop owner should be able to log a miscellaneous or untracked sale ("misc item, ₹50") without being forced to link it to a `Product` row that doesn't really represent anything trackable.

### `Expense.user` (`@ManyToOne`)
**Why it exists:** same multi-tenancy reasoning as `Customer.user`/`Product.user` — every rupee spent belongs to exactly one business, and `total_expenses` in the overview insights is computed as `SUM(expense.amount) WHERE user_id = currentUserId`.

### `Expense.supplier` (`@ManyToOne`, nullable)
**Why it exists, and why nullable:** not every expense involves a supplier — rent and salaries don't. But when an expense *is* a supplier delivery, this link is what connects the money spent back to *who* you paid, enabling questions like "how much have I paid ABC Distributors this year" (`SUM(expense.amount) WHERE supplier_id = X`). Nullable because forcing every expense to have a supplier would make rent/salary/utility entries awkward (you'd need a fake "N/A" supplier row, which is exactly the kind of junk-data problem we avoided earlier with `dealer_name`).

### `Expense.items` (`@OneToMany`, `mappedBy = "expense"`) ↔ `ExpenseItem.expense` (`@ManyToOne`)
**Why this pair exists together:** the direct mirror of the `Transaction`/`TransactionItem` pair, solving the identical problem on the money-out side — one supplier invoice (rice + sugar + oil) is one `Expense` event with three `ExpenseItem` rows underneath. Same `cascade = CascadeType.ALL` reasoning: an `ExpenseItem` has no standalone meaning outside its parent delivery.
**Why this pair specifically enabled the "automatic" feature you asked for:** without a collection of items to loop over, `recordSupplyDelivery()` couldn't compute a multi-product total or update stock for more than one product per delivery. This relationship is *the* mechanism that makes "record a delivery once, get stock + expense updated automatically" possible for realistic multi-product invoices.

### `ExpenseItem.product` (`@ManyToOne`, nullable)
**Why it exists:** the mirror of `TransactionItem.product`, but running in the opposite direction on stock — for every `ExpenseItem` with a non-null product, `ProductService.increaseStock(product, quantity)` is called instead of decreasing it. Nullable for the same reason: some expense line items might not map to a tracked product (e.g. a delivery fee charged by the supplier alongside the goods).

---

### The overall pattern, stated once

Every `@ManyToOne` in this schema answers **"who does this row belong to, or what does it apply to"** — and almost all of them trace back, directly or through one hop, to a `User`, which is what makes the entire "user-scoped data isolation" security model enforceable with a single consistent pattern: filter by `user_id`, whether directly (`Customer.user_id`) or by joining through a parent (`Transaction → Customer → user_id`). The two `@OneToMany` relationships (`Transaction.items`, `Expense.items`) exist specifically to model **one real-world event containing multiple products** — they're not generic "convenience" collections, they're the direct structural fix for the multi-product problem you identified twice (once for sales, once for supplier deliveries), and they're what makes the "compute totals + update stock automatically" behavior possible at all.