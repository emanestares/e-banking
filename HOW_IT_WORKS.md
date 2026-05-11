# How E-Banking Works

This document explains how the current E-Banking application works, including its main components, request flow, and key business rules.

## Project Overview
E-Banking is a Spring Boot REST API for a mini banking system. It supports user authentication, account management with different account types, fund transfers, transaction history with detailed records, and administrative functions.

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

## Core Controllers
- `AuthController`: login, signup, health check
- `AccountController`: account retrieval, enrollment with account types, customer management
- `TransactionController`: fund transfers with descriptions, transaction history
- `AdminController`: admin views of accounts and transactions

## Main Endpoints
- `POST /auth/login` — authenticate user and return JWT
- `POST /auth/signup` — register new user
- `GET /auth/health` — API health check
- `GET /accounts/my` — get user's accounts with types and status
- `POST /accounts/enroll` — enroll new account with type and initial deposit
- `GET /accounts/{number}` — get specific account
- `POST /transactions/transfer` — transfer funds with description
- `GET /transactions/my` — user's transactions with details
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
- `accountNumber` (unique, 20 chars)
- `user` (FK to User)
- `balance` (BigDecimal)
- `accountType` (SAVINGS/CHECKING)
- `isActive` (boolean)
- `createdAt` (timestamp)
- `updatedAt` (timestamp)
- `sentTransactions` (one-to-many to Transaction)

### Transaction
- `id` (PK)
- `referenceNumber` (unique, 50 chars)
- `senderAccount` (FK to Account, nullable for deposits)
- `receiverAccount` (FK to Account, nullable for withdrawals)
- `amount` (BigDecimal)
- `transactionType` (TRANSFER/DEPOSIT/WITHDRAWAL)
- `status` (COMPLETED/PENDING/FAILED)
- `description` (optional, 500 chars)
- `createdAt` (timestamp)

### Role
- `id` (PK)
- `name` (USER/ADMIN)

### Foreign keys and relationships
- `accounts.user_id` references `users.id`
- `transactions.sender_account_id` references `accounts.id`
- `transactions.receiver_account_id` references `accounts.id`
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
│          │       │ accountType│
│          │       │ isActive   │
│          │       │ createdAt  │
│          │       │ updatedAt  │
└──────────┘       └────────────┘
                   │
                   │1----N
                   ▼
            ┌──────────────┐
            │ Transaction  │
            ├──────────────┤
            │ id (PK)      │
            │ referenceNum │
            │ sender_id(FK)│
            │ receiver_id  │
            │ amount       │
            │ type         │
            │ status       │
            │ description  │
            │ createdAt    │
            └──────────────┘
```

## Business Rules
- Account numbers must be unique and 10-20 characters
- Account types: SAVINGS, CHECKING
- Transfers require sufficient balance in sender account
- Users can only access their own accounts and transactions
- Admins have system-wide access
- Transactions have unique reference numbers
- Account status can be active/inactive
- Initial deposits required for account enrollment
- Transaction descriptions are optional but limited to 500 chars

**Business Rules Table:**

| Rule Category | Rule | Validation Method | Error Response |
|---------------|------|-------------------|----------------|
| Account | Unique account numbers | Database constraint | 400 Bad Request |
| Account | Valid account types (SAVINGS/CHECKING) | Enum validation | 400 Bad Request |
| Account | Positive initial deposit | Service validation | 400 Bad Request |
| Transaction | Sufficient balance | Service validation | 400 Insufficient Funds |
| Transaction | Valid amount (0.01 to 10M) | Bean validation | 400 Bad Request |
| Security | User ownership | Service authorization | 403 Forbidden |
| Security | Admin access | Role-based security | 403 Forbidden |
| Transaction | Unique reference numbers | Database constraint | 500 Internal Error |
| Data | Account active status | Service check | 400 Account Inactive |

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
3. User enrolls account via `POST /accounts/enroll` with type and deposit
4. User transfers funds via `POST /transactions/transfer` with description
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

Client ── POST /accounts/enroll ──▶ AccountController ── enrollAccount ──▶ AccountService ── validate + save ──▶ Repository ── INSERT ──▶ Database
    ▲                                                                                                           │
    │                                                                                                           ▼
    └─────────────────────────────────────────────────────────────────────────────────────────────────────────── Response ── 200 OK + Account

Client ── POST /transactions/transfer ──▶ TransactionController ── transfer ──▶ TransactionService ── validate accounts ──▶ Repository ── SELECT ──▶ Database
                                                                                   │
                                                                                   ▼
                                                                       ── check balance ──▶ Repository ── SELECT ──▶ Database
                                                                                   │
                                                                                   ▼
                                                                       ── update balances ──▶ Repository ── UPDATE ──▶ Database
                                                                                   │
                                                                                   ▼
                                                                       ── save transaction ──▶ Repository ── INSERT ──▶ Database
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
- Validate sender account ownership and balance
- Validate receiver account exists and is active
- Generate unique reference number
- Update sender account balance (-amount)
- Update receiver account balance (+amount)
- Create transaction record with description
- Return transfer confirmation with reference number

## Account Management
`AccountService` supports:
- Enrolling new accounts with type and initial deposit
- Retrieving accounts by user with full details
- Validating account access permissions and active status
- Generating unique account numbers

## Authentication
`AuthService` supports:
- User registration with role assignment
- JWT token generation and validation
- User details loading for Spring Security

## Persistence
- `UserRepository` manages user accounts and roles
- `AccountRepository` manages bank accounts with types and status
- `TransactionRepository` manages transaction history with references
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
- Implement account type-specific rules and limits
- Add transaction limits and daily caps
- Introduce audit logging for compliance
- Add API documentation with Swagger
- Implement push notifications for transactions
- Add multi-currency support
- Integrate with external payment systems
- Add account deactivation/reactivation
- Implement transaction reversal capabilities