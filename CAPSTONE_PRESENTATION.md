# E-Banking Capstone Presentation Script

---

## Slide 1: Title Slide
**Good [morning/afternoon], everyone. My name is [Your Name] and today I'm presenting my capstone project: E-Banking — a secure banking system REST API built with Spring Boot.**

---

## Slide 2: Project Overview
**E-Banking is a comprehensive banking backend API for managing user accounts, bank accounts, transactions, and administrative oversight. It demonstrates a modern Spring Boot REST API with JWT authentication and role-based access control.**

**Key Features:**
- User registration and JWT-based authentication
- Account enrollment and management for customers
- Secure fund transfers between accounts
- Transaction history tracking
- Admin dashboard for viewing all accounts and transactions
- Comprehensive validation and error handling
- RESTful API design with proper HTTP status codes
- Security with Spring Security and JWT tokens
- Data persistence with JPA and MS SQL Server

**Business Value:** This API supports core banking operations, enables secure financial transactions, and provides administrative tools for bank management.

---

## Slide 3: Technical Stack
**The application uses a typical Spring Boot backend stack for enterprise REST APIs:**

**Backend:**
- Java 21
- Spring Boot 3.2.3
- Spring Web for REST controllers
- Spring Data JPA for persistence
- Spring Security for authentication and authorization
- JWT (JJWT) for token-based auth
- Bean Validation for input validation

**Database & Infrastructure:**
- MS SQL Server for production persistence
- H2 database available for testing
- Maven build management
- Lombok for reducing boilerplate code
- JUnit 5 and Mockito for testing

---

## Slide 4: System Architecture
**The application follows a layered architecture with clear separation of concerns:**

- Presentation: REST API endpoints with JSON responses
- Controller: Spring MVC request handling and validation
- Service: Business logic, validation, and data processing
- Repository: JPA data access and query methods
- Database: MS SQL Server-backed entity storage

**Architecture diagram:**

```
┌─────────────────────────────────────────┐
│     Frontend (Angular/React/etc.)      │
└──────────────┬──────────────────────────┘
               │ HTTP Requests / JSON
┌──────────────▼──────────────────────────┐
│  Controllers (Auth, Account, Transaction│
├──────────────────────────────────────────┤
│  Services (Auth, Account, Transaction)  │
├──────────────────────────────────────────┤
│  Repositories (JPA / DB access)         │
├──────────────────────────────────────────┤
│  Models / Entities (User, Account, Trans)│
└──────────────┬──────────────────────────┘
               │ JPA / Hibernate
┌──────────────▼──────────────────────────┐
│           MS SQL Server Database        │
└─────────────────────────────────────────┘
```

**Database Relationships:**

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

---

## Slide 5: Key Controllers
**Primary controllers and their responsibilities:**
- **AuthController**: user login, registration, and API health check
- **AccountController**: account retrieval, enrollment, and customer account management
- **TransactionController**: fund transfers and transaction history
- **AdminController**: administrative views of all accounts and transactions

**API flows:**
- Public access for authentication endpoints
- Authenticated users can manage their accounts and transactions
- Admins have elevated access to system-wide data

**API Flow:**

```
┌─────────────┐
│   Request   │
└──────┬──────┘
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

---

## Slide 6: Data Model & Relationships
**Core entities and how they relate:**

- **User**: authentication credentials and role (USER/ADMIN)
- **Account**: bank account details linked to a user
- **Transaction**: financial transactions (deposits, withdrawals, transfers)
- **Role**: user roles for authorization

**Relationships:**
- User → Account: one-to-many
- Account → Transaction: one-to-many
- Transaction can reference two accounts (from/to for transfers)

**Entity Relationship Table:**

| Entity | Relationship | Target Entity | Cardinality | Description |
|--------|--------------|---------------|-------------|-------------|
| User | owns | Account | 1:N | One user can have multiple accounts |
| Account | has | Transaction | 1:N | One account can have multiple transactions |
| Account | receives | Transaction | 1:N | Account can receive transfers |
| Transaction | from | Account | N:1 | Transaction belongs to source account |
| Transaction | to | Account | N:1 | Transaction can target another account |

**Validation rules:**
- Account numbers must be unique
- Transfers require sufficient balance
- Users can only access their own accounts
- Admins have system-wide access

---

## Slide 7: Security Implementation
**Security is implemented with Spring Security and JWT:**

**Authentication:**
- JWT token-based authentication
- Login endpoint generates access tokens
- Tokens required for protected endpoints

**Authorization:**
- Public access: `/auth/**` endpoints
- Authenticated access: `/accounts/**`, `/transactions/**`
- Admin access: `/admin/**` with ROLE_ADMIN

**Role controls:**
- `ROLE_USER` for standard banking operations
- `ROLE_ADMIN` for administrative oversight

**Security Flow:**

```
┌─────────────┐
│ Unauthenticated│
└──────┬──────┘
       │ Login Success
       ▼
┌─────────────┐
│ Authenticated│
└──────┬──────┘
       │
   ┌───▼────┐
   │ Has    │
   │ ROLE_USER│────▶ User Operations
   └─────────┘
       │
       ▼
   ┌─────────┐
   │ Has     │
   │ ROLE_ADMIN│────▶ Admin Operations
   └─────────┘
```

---

## Slide 8: API Endpoints & Usage
**The API provides RESTful endpoints for banking operations:**

**Authentication:**
- `POST /auth/login` - User login
- `POST /auth/signup` - User registration
- `GET /auth/health` - API health check

**Account Management:**
- `GET /accounts/my` - Get user's accounts
- `POST /accounts/enroll` - Enroll new account
- `GET /accounts/{number}` - Get specific account

**Transactions:**
- `POST /transactions/transfer` - Transfer funds
- `GET /transactions/account/{number}` - Account transactions
- `GET /transactions/my` - User's transactions

**Admin:**
- `GET /admin/accounts` - All accounts
- `GET /admin/transactions` - All transactions

---

## Slide 9: Testing Strategy
**Testing covers service, repository, controller, and security logic:**

**Test types:**
- Unit tests for services and utilities
- Repository tests for data access behavior
- Controller tests for endpoint validation
- Security tests for access control

**Execution:**
- Run all tests with `./mvnw test`

---

## Slide 10: Database Design
**The app uses a normalized relational schema:**

**Tables:**
- `users`: authentication accounts and roles
- `accounts`: bank account details
- `transactions`: transaction records
- `roles`: user role definitions

**Design decisions:**
- Use JPA entities for object mapping
- Enforce unique constraints on account numbers
- Cascade operations for user-account relationships
- Index foreign keys for performance

---

## Slide 11: Performance & Scalability
**Built for reliability and secure banking operations:**

**Application improvements:**
- JWT tokens for stateless authentication
- Input validation to prevent malicious requests
- Proper error handling and logging
- Database indexing for query performance

**Scalability notes:**
- Stateless design enables horizontal scaling
- Can support multiple database instances
- API-first design allows for various frontend integrations

---

## Slide 12: Deployment & Production
**How to run and deploy the API:**

**Build:**
```bash
./mvnw clean package
```

**Run:**
```bash
java -jar target/mini-banking-backend-1.0.0.jar
```

**Environment:**
- Configure MS SQL Server credentials in `application.properties`
- Set strong JWT secret for production
- Enable HTTPS in production deployment
- Configure CORS for specific frontend domains

---

## Slide 13: Challenges & Solutions
**Key challenges addressed:**

**Challenge 1: secure authentication and authorization**
- **Solution**: JWT-based stateless auth with Spring Security role checks

**Challenge 2: transaction integrity and validation**
- **Solution**: business logic validation in services with proper error responses

**Challenge 3: admin oversight without compromising security**
- **Solution**: role-based access control with admin-specific endpoints

**Challenge 4: data consistency in transfers**
- **Solution**: service-level transaction management for fund transfers

---

## Slide 14: Future Enhancements
**Recommended future work:**
- Add password encryption and reset workflows
- Implement account types (savings, checking, etc.)
- Add transaction limits and fraud detection
- Introduce audit logging and compliance features
- Add REST API documentation with Swagger/OpenAPI
- Implement rate limiting and API versioning