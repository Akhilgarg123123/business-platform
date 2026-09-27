## Updated Entity / Database Design

### User (business owner account)
| Field | Type | Notes |
|---|---|---|
| id | UUID/Long | PK |
| business_name | String | |
| email | String | unique, used for login |
| password_hash | String | never store plain text |
| created_at | Timestamp | |

### Customer
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | scopes customer to the owning business |
| name | String | |
| phone_number | String | |
| email | String | optional |
| address | String | optional |
| is_regular | Boolean | manual or auto-computed |
| created_at | Timestamp | |

### Supplier
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | |
| name | String | |
| phone_number | String | |
| email | String | optional |
| items_supplied | String | simple text field |

### Product *(new)*
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | scoped per business |
| name | String | |
| current_stock | Integer | running quantity |
| unit | String | optional, e.g. "pcs", "kg" |

### Transaction (customer purchases)
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| customer_id | FK → Customer | |
| amount | BigDecimal | never float/double |
| date | Date | |
| description | String | optional |
| product_id | FK → Product | **nullable** — only set if this sale should deduct stock |
| quantity | Integer | **nullable** — units sold, only used with product_id |

### Expense
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| user_id | FK → User | |
| category | Enum/String | rent, salaries, utilities, supplier_payment, other |
| amount | BigDecimal | |
| date | Date | |
| supplier_id | FK → Supplier | nullable |
| description | String | optional |
| product_id | FK → Product | **nullable** — only set if this expense restocks a product |
| quantity | Integer | **nullable** — units restocked, only used with product_id |

**Relationships:**
```
User 1---* Customer 1---* Transaction *---1 Product (optional)
User 1---* Supplier 1---* Expense (nullable link) *---1 Product (optional)
User 1---* Expense
User 1---* Product
```

**Stock logic (service layer, not stored triggers):**
- New `Transaction` with `product_id` set → `product.current_stock -= quantity`
- New `Expense` with `product_id` set (typically `category = supplier_payment`) → `product.current_stock += quantity`
- Both fields stay nullable so plain cash entries never require touching stock

---

## Updated Project Structure

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
│   │   │   │   ├── Product.java              ← new
│   │   │   │   ├── Transaction.java
│   │   │   │   └── Expense.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── SupplierRepository.java
│   │   │   │   ├── ProductRepository.java     ← new
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── ExpenseRepository.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── SupplierController.java
│   │   │   │   ├── ProductController.java     ← new
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── ExpenseController.java
│   │   │   │   └── InsightsController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── SupplierService.java
│   │   │   │   ├── ProductService.java        ← new (stock update logic lives here)
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   └── InsightsService.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── CustomerDTO.java
│   │   │   │   ├── SupplierDTO.java
│   │   │   │   ├── ProductDTO.java            ← new
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
│
├── pom.xml
├── mvnw / mvnw.cmd
└── .gitignore
```

### What each layer does

**`entity/`** — Database tables as Java classes. `Product.java` is the new addition: a minimal table (`name`, `current_stock`, `unit`) owned by `User`, referenced optionally by `Transaction` and `Expense`.

**`repository/`** — One interface per entity extending `JpaRepository`. `ProductRepository` needs nothing special beyond standard CRUD — stock increments/decrements happen in the service layer, not here.

**`controller/`** — REST endpoints. `ProductController` is intentionally small:
```
GET  /products        list all products + current stock
POST /products         create a product (name, starting stock, unit)
```
No update/delete endpoints for now — keeps this from growing into inventory-management surface area.

**`service/`** — Business logic. `ProductService` isn't called directly for stock changes — instead, `TransactionService` and `ExpenseService` call into it (e.g. `productService.decreaseStock(productId, quantity)` / `increaseStock(...)`) whenever a transaction or expense includes a `product_id`. This keeps stock mutation centralized in one place even though it's triggered from two different flows.

**`dto/`** — `ProductDTO` is a thin object for the two endpoints above; `TransactionDTO` and `ExpenseDTO` gain optional `productId`/`quantity` fields matching the entity changes.

**`security/`, `exception/`** — Unchanged from the original plan.

Everything else in the architecture — layering, auth flow, insights computation — stays exactly as it was; `Product` slots in as a sixth entity without disturbing anything already decided. Ready to start writing `User.java` when you are, or want this folded into an updated version of the build guide file first?