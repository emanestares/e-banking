# How E-Banking Works

This document explains how the current E-Banking application works, including its main components, request flow, and key business rules.

## Project Overview
E-Banking is a Spring Boot REST API for a mini banking system. It supports user authentication, account management, fund transfers, and administrative functions.

## Technology Stack
- Java 21
- Spring Boot 3.2.3
- Spring Web and REST controllers
- Spring Data JPA
- Spring Security with JWT
- MS SQL Server runtime persistence
- H2 available for test isolation
- Maven build system
- JUnit 5 and Spring Boot testing dependencies

## Application Structure
The application follows a layered architecture:
- **Presentation Layer**: REST API endpoints with JSON request/response
- **Controller Layer**: handles web requests and routes
- **Service Layer**: business logic and validation
- **Repository Layer**: JPA persistence and queries
- **Database Layer**: MS SQL Server tables for users, accounts, and transactions

```
┌─────────────────────────────────────────┐
│     Frontend (API Client/Postman)       │
└──────────────┬──────────────────────────┘
               │ HTTP Requests / JSON
┌──────────────▼───────────────────────────┐
│  Controllers (Auth, Account, Transaction │
├──────────────────────────────────────────┤
│  Services (Auth, Account, Transaction)   │
├──────────────────────────────────────────┤
│  Repositories (JPA / DB access)          │
├──────────────────────────────────────────┤
│  Models / Entities (User, Account, Trans)│
└──────────────┬───────────────────────────┘
               │ JPA / Hibernate
┌──────────────▼──────────────────────────┐
│           MS SQL Server Database        │
└─────────────────────────────────────────┘
```

**Detailed Architecture Diagram:**

```
┌─────────────────────────────────────────┐
│     Frontend (API Client/Postman)       │
└──────────────┬──────────────────────────┘
               │ HTTP Requests / JSON
┌──────────────▼───────────────────────────┐
│  Controllers (Auth, Account, Transaction │
├──────────────────────────────────────────┤
│  Services (Auth, Account, Transaction)   │
├──────────────────────────────────────────┤
│  Repositories (JPA / DB access)          │
├──────────────────────────────────────────┤
│  Models / Entities (User, Account, Trans)│
└──────────────┬──────────────────────────┘
               │ JPA / Hibernate
┌──────────────▼──────────────────────────┐
│           MS SQL Server Database        │
└─────────────────────────────────────────┘
```

## Core Controllers
- `AuthController`: login, signup, health check
- `AccountController`: account retrieval, enrollment, customer management
- `TransactionController`: fund transfers, transaction history
- `AdminController`: admin views of accounts and transactions

## Main Endpoints
- `POST /auth/login` — authenticate user and return JWT
- `POST /auth/signup` — register new user
- `GET /auth/health` — API health check
- `GET /accounts/my` — get user's accounts
- `POST /accounts/enroll` — enroll new account
- `GET /accounts/{number}` — get specific account
- `POST /transactions/transfer` — transfer funds
- `GET /transactions/my` — user's transactions
- `GET /transactions/account/{number}` — account transactions
- `GET /admin/accounts` — all accounts (admin)
- `GET /admin/transactions` — all transactions (admin)

## Data Model
### User
- `id` (PK)
- `username` (unique)
- `password`
- `roles` (many-to-many with Role)

### Account
- `id` (PK)
- `accountNumber` (unique)
- `user` (FK to User)
- `balance`
- `accountType` (SAVINGS/CHECKING)

### Transaction
- `id` (PK)
- `account` (FK to Account)
- `toAccount` (FK to Account, nullable)
- `amount`
- `type` (DEPOSIT/WITHDRAWAL/TRANSFER)
- `timestamp`

### Role
- `id` (PK)
- `name` (USER/ADMIN)

### Foreign keys and relationships
- `accounts.user_id` references `users.id`
- `transactions.account_id` references `accounts.id`
- `transactions.to_account_id` references `accounts.id` (for transfers)
- `users_roles` junction table for user-role many-to-many

**Database ER Diagram:**

```
┌──────────┐       ┌────────────┐
│   User   │       │  Account   │
├──────────┤       ├────────────┤
│ id (PK)  │1----N │ id (PK)    │
│ username │       │ accountNum │
│ password │       │ balance    │
│ roles    │       │ user_id(FK)│
└──────────┘       └────────────┘
                   │
                   │1----N
                   ▼
            ┌──────────────┐
            │ Transaction  │
            ├──────────────┤
            │ id (PK)      │
            │ amount       │
            │ type         │
            │ timestamp    │
            │ account_id   │
            │ to_account_id│
            └──────────────┘
```

## Business Rules
- Account numbers must be unique and 10-20 characters
- Transfers require sufficient balance in source account
- Users can only access their own accounts and transactions
- Admins have system-wide access
- Passwords should be encrypted in production
- Transactions are immutable once created

**Business Rules Table:**

| Rule Category | Rule | Validation Method | Error Response |
|---------------|------|-------------------|----------------|
| Account | Unique account numbers | Database constraint | 400 Bad Request |
| Account | Valid account types | Enum validation | 400 Bad Request |
| Transaction | Sufficient balance | Service validation | 400 Insufficient Funds |
| Security | User ownership | Service authorization | 403 Forbidden |
| Security | Admin access | Role-based security | 403 Forbidden |
| Data | Password encryption | BCrypt in production | Security warning |
| Transaction | Immutable records | No update endpoints | 405 Method Not Allowed |

## Security Flow
- Public routes: `/auth/**`
- Authenticated routes: `/accounts/**`, `/transactions/**`
- Admin routes: `/admin/**` (ROLE_ADMIN required)
- JWT tokens issued on login, validated on protected routes
- Spring Security handles authentication and authorization

**Security Flow Diagram:**

```
┌─────────────┐
│   Request   │
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│ Path /auth/?│────▶│   Yes      │────▶ Allow Public
└──────┬──────┘     └─────────────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│ JWT Present?│────▶│   No       │────▶ 401 Unauthorized
└──────┬──────┘     └─────────────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│ JWT Valid?  │────▶│   No       │────▶ 401 Unauthorized
└──────┬──────┘     └─────────────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│ Check Role  │────▶│ Insufficient│────▶ 403 Forbidden
└──────┬──────┘     └─────────────┘
       │
       ▼
┌─────────────┐
│  Allow      │
│  Access     │
└─────────────┘
```

## Data Flow Example
1. User registers via `POST /auth/signup`
2. User logs in via `POST /auth/login`, receives JWT token
3. User enrolls account via `POST /accounts/enroll` with token
4. User transfers funds via `POST /transactions/transfer` with token
5. Admin views all accounts via `GET /admin/accounts` with admin token

**Complete User Flow Diagram:**

```
Client ── POST /auth/signup ──▶ AuthController ── register ──▶ AuthService ── save ──▶ Repository ── INSERT ──▶ Database
    ▲                                                                                                           │
    │                                                                                                           ▼
    └─────────────────────────────────────────────────────────────────────────────────────────────────────────── Response ── 201 Created + JWT

Client ── POST /auth/login ──▶ AuthController ── login ──▶ AuthService ── findByUsername ──▶ Repository ── SELECT ──▶ Database
    ▲                                                                                                           │
    │                                                                                                           ▼
    └─────────────────────────────────────────────────────────────────────────────────────────────────────────── Response ── 200 OK + JWT

Client ── POST /accounts/enroll ──▶ AccountController ── enrollAccount ──▶ AccountService ── save ──▶ Repository ── INSERT ──▶ Database
    ▲                                                                                                           │
    │                                                                                                           ▼
    └─────────────────────────────────────────────────────────────────────────────────────────────────────────── Response ── 200 OK + Account

Client ── POST /transactions/transfer ──▶ TransactionController ── transfer ──▶ TransactionService ── find accounts ──▶ Repository ── SELECT ──▶ Database
                                                                                   │
                                                                                   ▼
                                                                       ── update balances ──▶ Repository ── UPDATE ──▶ Database
                                                                                   │
                                                                                   ▼
                                                                       ── save transactions ──▶ Repository ── INSERT ──▶ Database
                                                                                   │
                                                                                   ▼
                                                                       ── Response ──▶ TransactionController ── 200 OK + Transfer ──▶ Client
```

## User / Admin Workflow
```
┌───────────┐      ┌─────────────────┐        ┌──────────────┐
│  Client   │ ---> │ Authentication  │ --->   │ API Routes   │
│           │      │ (AuthController)│        │              │
└───────────┘      └─────────────────┘        └──────┬───────┘
                                                     │
                                        ┌────────────▼───────────┐
                                        │  USER ROUTES           │
                                        │  /accounts/**          │
                                        │  /transactions/**      │
                                        └────────────┬───────────┘
                                                     │
                                        ┌────────────▼───────────┐
                                        │  ADMIN ROUTES          │
                                        │  /admin/**             │
                                        └────────────────────────┘
```

**Workflow State Diagram:**

```
┌─────────────┐
│[*]         │
└──────┬──────┘
       │
       ▼
┌───────────────┐
│Unauthenticated│
└──────┬────────┘
       │ Login Success
       ▼
┌─────────────┐
│Authenticated│
└──────┬──────┘
       │
   ┌───▼──────┐
   │ Has      │
   │ ROLE_USER│────▶ User Operations
   └──────────┘
       │
       ▼
   ┌───────────┐
   │ Has       │
   │ ROLE_ADMIN│────▶ Admin Operations
   └───────────┘
```

## Transaction Processing
`TransactionService.transfer()` handles:
- Validate source account ownership and balance
- Validate destination account exists
- Update source account balance (-amount)
- Update destination account balance (+amount)
- Create transaction records for both accounts
- Return transfer confirmation

## Account Management
`AccountService` supports:
- Enrolling new accounts for users
- Retrieving accounts by user or account number
- Validating account access permissions
- Generating unique account numbers

## Authentication
`AuthService` supports:
- User registration with role assignment
- JWT token generation and validation
- Password verification
- User details loading for Spring Security

## Persistence
- `UserRepository` manages user accounts and roles
- `AccountRepository` manages bank accounts
- `TransactionRepository` manages transaction history
- Entities map to `users`, `accounts`, `transactions`, `roles` tables

## Running the Application
1. Configure MS SQL Server credentials in `src/main/resources/application.properties`
2. Create the target database
3. Start the app:
```bash
./mvnw spring-boot:run
```
4. API available at `http://localhost:8080/api`

## Testing
- Run `./mvnw test`
- The project includes unit and integration tests for controllers, services, repositories, and security
- H2 is available for test environments

## Future Improvements
- Add BCrypt password hashing
- Implement account types with different rules
- Add transaction limits and daily caps
- Introduce audit logging for compliance
- Add API documentation with Swagger
- Implement push notifications for transactions
- Add multi-currency support
- Integrate with external payment systems