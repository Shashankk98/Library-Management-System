# Library User Service - Authentication & Authorization Flow

## 📋 Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Components](#components)
4. [Authentication Flow](#authentication-flow)
5. [Authorization Flow](#authorization-flow)
6. [API Endpoints](#api-endpoints)
7. [Security Configuration](#security-configuration)
8. [JWT Token Structure](#jwt-token-structure)
9. [Testing Guide](#testing-guide)
10. [Error Handling](#error-handling)

---

## 🎯 Overview

The Library User Service implements a **JWT-based authentication and role-based authorization** system using Spring Security. It supports two user roles:
- **MEMBER**: Regular library users
- **LIBRARIAN**: Library staff with elevated privileges

### Key Features
✅ JWT token-based authentication  
✅ Role-based access control (RBAC)  
✅ BCrypt password encryption  
✅ Stateless session management  
✅ Custom JWT filter for token validation  
✅ Protected endpoints based on user roles  

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Request                           │
│              (Authorization: Bearer <JWT_TOKEN>)                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Security Filter Chain                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         1. JwtAuthenticationFilter                        │  │
│  │    - Extract JWT token from Authorization header         │  │
│  │    - Validate token using JwtUtil                        │  │
│  │    - Extract user email and role from token              │  │
│  │    - Create Authentication object                        │  │
│  │    - Set authentication in SecurityContext               │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           │                                      │
│                           ▼                                      │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         2. SecurityFilterChain                            │  │
│  │    - Check if endpoint requires authentication           │  │
│  │    - Verify user has required role (if needed)           │  │
│  │    - Allow or deny access                                │  │
│  └──────────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Controller Layer                            │
│  - UserController (registration, profile)                       │
│  - AuthController (login)                                       │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Service Layer                              │
│  - UserService (user management)                                │
│  - AuthService (authentication logic)                           │
│  - JwtUtil (JWT token operations)                               │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Repository Layer                             │
│  - UserRepository (database operations)                         │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   PostgreSQL Database                            │
│  - User table (id, email, password, role, etc.)                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧩 Components

### 1. **Entities**

#### User Entity (`User.java`)
```java
@Entity
public class User {
    private Long id;
    private String email;
    private String password;      // BCrypt encrypted
    private String firstName;
    private String lastName;
    private UserRole role;        // MEMBER or LIBRARIAN
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### UserRole Enum (`UserRole.java`)
```java
public enum UserRole {
    LIBRARIAN,
    MEMBER
}
```

---

### 2. **DTOs (Data Transfer Objects)**

#### RegisterRequestDto
- Used for user registration
- Fields: `firstName`, `lastName`, `email`, `password`
- Validation: Email format, password min 6 characters

#### LoginRequestDto
- Used for user login
- Fields: `email`, `password`
- Validation: Email format, required fields

#### LoginResponseDto
- Response after successful login
- Fields: `token` (JWT), `userDto` (user information)

#### UserDto
- User information without password
- Fields: `id`, `email`, `firstName`, `lastName`, `role`, `active`, `createdAt`

---

### 3. **Security Components**

#### JwtUtil (`util/JwtUtil.java`)
**Purpose**: JWT token generation and validation

**Key Methods**:
- `generateToken(User user)`: Creates JWT token with user info
- `validateToken(String token)`: Validates and decodes JWT token
- `getEmailFromToken(String token)`: Extracts email from token
- `getRoleFromToken(String token)`: Extracts role from token
- `getUserIdFromToken(String token)`: Extracts user ID from token

**Token Claims**:
- **Subject**: User's email
- **userId**: User's database ID
- **role**: User's role (MEMBER/LIBRARIAN)
- **firstName**: User's first name
- **lastName**: User's last name
- **iat**: Issued at timestamp
- **exp**: Expiration timestamp (24 hours)

**Configuration** (in `application.yaml`):
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000 # 24 hours in milliseconds
```

---

#### JwtAuthenticationFilter (`filter/JwtAuthenticationFilter.java`)
**Purpose**: Intercepts every request to validate JWT tokens

**Flow**:
1. Extract `Authorization` header from request
2. Check if header starts with "Bearer "
3. Extract token (substring from position 7)
4. Call `JwtUtil.getEmailFromToken(token)` and `JwtUtil.getRoleFromToken(token)`
5. Create `UsernamePasswordAuthenticationToken` with:
   - Principal: email
   - Credentials: null
   - Authorities: `ROLE_` + role (e.g., `ROLE_MEMBER`, `ROLE_LIBRARIAN`)
6. Set authentication in `SecurityContextHolder`
7. Continue filter chain

**Exception Handling**: If token is invalid/expired, request continues without authentication

---

#### SecurityConfig (`config/SecurityConfig.java`)
**Purpose**: Configure Spring Security rules

**Configuration**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)  // Disabled for REST API
        .authorizeHttpRequests(auth -> auth
            // Public endpoints (no authentication required)
            .requestMatchers("/auth/**", "/users/register").permitAll()
            
            // Protected endpoint (only LIBRARIAN can access)
            .requestMatchers("/users/register/librarian").hasRole("LIBRARIAN")
            
            // All other endpoints require authentication
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // No sessions
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

**Key Points**:
- CSRF disabled (REST API doesn't need it)
- Stateless sessions (no server-side session storage)
- JWT filter runs before Spring's default authentication filter

---

### 4. **Service Layer**

#### AuthService (`service/AuthService.java`)
**Purpose**: Handle authentication logic

**login() Method Flow**:
1. Find user by email from database
2. Check if user exists → throw exception if not found
3. Check if user account is active → throw exception if inactive
4. Verify password using `PasswordEncoder.matches()`
5. Generate JWT token using `JwtUtil.generateToken(user)`
6. Build and return `LoginResponseDto` with token and user info

---

#### UserService (`service/UserService.java`)
**Purpose**: Handle user management operations

**Key Methods**:

**registerMember()**:
1. Check if email already exists
2. Create User entity with role = MEMBER
3. Encrypt password using BCrypt
4. Set active = true
5. Save to database
6. Return UserDto (without password)

**registerLibrarian()**:
1. Same as registerMember but role = LIBRARIAN
2. Only accessible by authenticated LIBRARIAN users

**getUserProfileByEmail()**:
1. Find user by email
2. Return user information as UserDto
3. Used by `/users/profile` endpoint

---

### 5. **Controllers**

#### AuthController (`controller/AuthController.java`)
**Endpoints**:
- `POST /auth/login` - User login

#### UserController (`controller/UserController.java`)
**Endpoints**:
- `POST /users/register` - Register as MEMBER (public)
- `POST /users/register/librarian` - Register as LIBRARIAN (protected)
- `GET /users/profile` - Get authenticated user's profile

---

## 🔐 Authentication Flow

### Registration Flow

#### 1. Member Registration (Public)
```
Client                    Controller              Service                Database
  │                          │                       │                      │
  │─── POST /users/register ─▶│                       │                      │
  │    {                      │                       │                      │
  │      firstName: "John",   │                       │                      │
  │      lastName: "Doe",     │                       │                      │
  │      email: "...",        │                       │                      │
  │      password: "..."      │                       │                      │
  │    }                      │                       │                      │
  │                           │                       │                      │
  │                           │── registerMember() ──▶│                      │
  │                           │                       │                      │
  │                           │                       │── Check if email ───▶│
  │                           │                       │    exists            │
  │                           │                       │◀─────────────────────│
  │                           │                       │                      │
  │                           │                       │── Encrypt password   │
  │                           │                       │   (BCrypt)           │
  │                           │                       │                      │
  │                           │                       │── Save User ────────▶│
  │                           │                       │   (role: MEMBER)     │
  │                           │                       │◀─────────────────────│
  │                           │                       │                      │
  │                           │◀── UserDto ───────────│                      │
  │                           │                       │                      │
  │◀── 201 Created ───────────│                       │                      │
  │    {                      │                       │                      │
  │      id: 1,               │                       │                      │
  │      email: "...",        │                       │                      │
  │      role: "MEMBER",      │                       │                      │
  │      ...                  │                       │                      │
  │    }                      │                       │                      │
```

#### 2. Librarian Registration (Protected - Requires LIBRARIAN Role)
```
Client                    JWT Filter              SecurityConfig         Controller
  │                          │                       │                      │
  │─── POST /users/register/librarian ───────────────────────────────────▶│
  │    Authorization:        │                       │                      │
  │    Bearer <TOKEN>        │                       │                      │
  │                          │                       │                      │
  │                          │◀─ Extract token ──────│                      │
  │                          │                       │                      │
  │                          │─ Validate token ────▶ JwtUtil                │
  │                          │  (decode & verify)    │                      │
  │                          │                       │                      │
  │                          │─ Extract role = "LIBRARIAN"                  │
  │                          │                       │                      │
  │                          │─ Set Authentication ─▶│                      │
  │                          │  (ROLE_LIBRARIAN)     │                      │
  │                          │                       │                      │
  │                          │                       │─ hasRole("LIBRARIAN")?
  │                          │                       │  ✅ YES              │
  │                          │                       │                      │
  │                          │                       │──────────────────────▶│
  │                          │                       │   Allow request       │
  │                          │                       │                      │
  │                          │                       │                  registerLibrarian()
  │                          │                       │                  (same as member
  │                          │                       │                   but role=LIBRARIAN)
  │                          │                       │                      │
  │◀─── 201 Created ─────────────────────────────────────────────────────────│
```

---

### Login Flow

```
Client                    Controller              AuthService             JwtUtil              Database
  │                          │                       │                      │                      │
  │─── POST /auth/login ────▶│                       │                      │                      │
  │    {                     │                       │                      │                      │
  │      email: "...",       │                       │                      │                      │
  │      password: "..."     │                       │                      │                      │
  │    }                     │                       │                      │                      │
  │                          │                       │                      │                      │
  │                          │──── login() ─────────▶│                      │                      │
  │                          │                       │                      │                      │
  │                          │                       │── Find user by email ───────────────────────▶│
  │                          │                       │                      │                      │
  │                          │                       │◀─── User entity ─────────────────────────────│
  │                          │                       │                      │                      │
  │                          │                       │─ Check if active?    │                      │
  │                          │                       │  ✅ YES              │                      │
  │                          │                       │                      │                      │
  │                          │                       │─ Verify password     │                      │
  │                          │                       │  (BCrypt.matches)    │                      │
  │                          │                       │  ✅ Match            │                      │
  │                          │                       │                      │                      │
  │                          │                       │── generateToken() ──▶│                      │
  │                          │                       │                      │                      │
  │                          │                       │                      │─ Create JWT:         │
  │                          │                       │                      │   - subject: email   │
  │                          │                       │                      │   - userId: 1        │
  │                          │                       │                      │   - role: MEMBER     │
  │                          │                       │                      │   - firstName: John  │
  │                          │                       │                      │   - lastName: Doe    │
  │                          │                       │                      │   - exp: 24h         │
  │                          │                       │                      │                      │
  │                          │                       │◀── JWT token ────────│                      │
  │                          │                       │                      │                      │
  │                          │◀─ LoginResponseDto ───│                      │                      │
  │                          │   {                   │                      │                      │
  │                          │     token: "eyJ...",  │                      │                      │
  │                          │     userDto: {...}    │                      │                      │
  │                          │   }                   │                      │                      │
  │                          │                       │                      │                      │
  │◀─── 200 OK ──────────────│                       │                      │                      │
  │    {                     │                       │                      │                      │
  │      token: "eyJ...",    │                       │                      │                      │
  │      userDto: {          │                       │                      │                      │
  │        id: 1,            │                       │                      │                      │
  │        email: "...",     │                       │                      │                      │
  │        role: "MEMBER"    │                       │                      │                      │
  │      }                   │                       │                      │                      │
  │    }                     │                       │                      │                      │
```

---

## 🔒 Authorization Flow

### Accessing Protected Endpoint (e.g., GET /users/profile)

```
Client                    JWT Filter              SecurityContext         Controller
  │                          │                       │                      │
  │─── GET /users/profile ──▶│                       │                      │
  │    Authorization:        │                       │                      │
  │    Bearer eyJhbGc...     │                       │                      │
  │                          │                       │                      │
  │                          │─ 1. Extract token     │                      │
  │                          │    from header        │                      │
  │                          │                       │                      │
  │                          │─ 2. Validate token ──▶ JwtUtil.validateToken()
  │                          │    (verify signature, │                      │
  │                          │     check expiration) │                      │
  │                          │                       │                      │
  │                          │─ 3. Extract claims:   │                      │
  │                          │    - email            │                      │
  │                          │    - role             │                      │
  │                          │                       │                      │
  │                          │─ 4. Create Auth ─────▶│                      │
  │                          │    object with:       │                      │
  │                          │    - principal: email │                      │
  │                          │    - authorities:     │                      │
  │                          │      [ROLE_MEMBER]    │                      │
  │                          │                       │                      │
  │                          │                       │─ 5. Store in context │
  │                          │                       │                      │
  │                          │─ 6. Continue filter ──────────────────────▶│
  │                          │    chain              │                      │
  │                          │                       │                      │
  │                          │                       │                  getUserProfileInfo()
  │                          │                       │                  - Get Authentication
  │                          │                       │                  - Extract email
  │                          │                       │                  - Fetch user data
  │                          │                       │                      │
  │◀─── 200 OK ──────────────────────────────────────────────────────────────│
  │    {                     │                       │                      │
  │      id: 1,              │                       │                      │
  │      email: "...",       │                       │                      │
  │      firstName: "John",  │                       │                      │
  │      role: "MEMBER"      │                       │                      │
  │    }                     │                       │                      │
```

---

## 📡 API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. Register Member
```http
POST /users/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "MEMBER",
  "active": true,
  "createdAt": "2025-12-28T10:30:00"
}
```

---

#### 2. Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwicm9sZSI6Ik1FTUJFUiIsImZpcnN0TmFtZSI6IkpvaG4iLCJsYXN0TmFtZSI6IkRvZSIsImlhdCI6MTY0NTc4OTIwMCwiZXhwIjoxNjQ1ODc1NjAwfQ.xxxxxxxxxxxxxxxxxxxxx",
  "userDto": {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "MEMBER",
    "active": true,
    "createdAt": "2025-12-28T10:30:00"
  }
}
```

---

### Protected Endpoints (Authentication Required)

#### 3. Get User Profile
```http
GET /users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response** (200 OK):
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "MEMBER",
  "active": true,
  "createdAt": "2025-12-28T10:30:00"
}
```

---

### Role-Protected Endpoints (LIBRARIAN Only)

#### 4. Register Librarian
```http
POST /users/register/librarian
Authorization: Bearer <LIBRARIAN_TOKEN>
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@library.com",
  "password": "securePass456"
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "email": "jane.smith@library.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "role": "LIBRARIAN",
  "active": true,
  "createdAt": "2025-12-28T10:35:00"
}
```

**Error Response** (403 Forbidden) - If user is not LIBRARIAN:
```json
{
  "timestamp": "2025-12-28T10:35:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/users/register/librarian"
}
```

---

## 🔑 JWT Token Structure

### Token Format
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwicm9sZSI6Ik1FTUJFUiIsImZpcnN0TmFtZSI6IkpvaG4iLCJsYXN0TmFtZSI6IkRvZSIsImlhdCI6MTY0NTc4OTIwMCwiZXhwIjoxNjQ1ODc1NjAwfQ.signature_here
```

### Decoded Token Structure

**Header**:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload**:
```json
{
  "sub": "john.doe@example.com",    // Subject (user email)
  "userId": 1,                       // User ID
  "role": "MEMBER",                  // User role
  "firstName": "John",               // First name
  "lastName": "Doe",                 // Last name
  "iat": 1645789200,                 // Issued at (timestamp)
  "exp": 1645875600                  // Expiration (timestamp)
}
```

**Signature**:
- Algorithm: HMAC-SHA256
- Secret: Configured in `application.yaml` (`jwt.secret`)

### Token Expiration
- **Default**: 24 hours (86400000 milliseconds)
- **Configurable**: Set `jwt.expiration` in `application.yaml`

---

## 🧪 Testing Guide

### Prerequisites
1. PostgreSQL database running on `localhost:5432`
2. Database `library_user_db` created
3. User service running on port `8081`
4. API Gateway running on port `8080` (optional, for routing)

---

### Test Scenarios

#### Scenario 1: Register and Login as Member

**Step 1**: Register a new member
```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

**Expected**: 201 Created with user details (role: MEMBER)

**Step 2**: Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

**Expected**: 200 OK with JWT token

**Step 3**: Get profile (copy token from Step 2)
```bash
curl -X GET http://localhost:8080/users/profile \
  -H "Authorization: Bearer <TOKEN_FROM_STEP_2>"
```

**Expected**: 200 OK with user profile

---

#### Scenario 2: Try to Register Librarian as Member (Should Fail)

**Step 1**: Login as MEMBER (from Scenario 1)

**Step 2**: Try to register librarian
```bash
curl -X POST http://localhost:8080/users/register/librarian \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <MEMBER_TOKEN>" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@library.com",
    "password": "securePass456"
  }'
```

**Expected**: 403 Forbidden (Access Denied)

---

#### Scenario 3: Register First Librarian (Manual Setup)

Since the first librarian cannot be created through the API (chicken-egg problem), you need to manually insert one into the database:

```sql
INSERT INTO "user" (email, password, first_name, last_name, role, active, created_at)
VALUES (
  'admin@library.com',
  '$2a$10$XYZ...',  -- BCrypt hash of 'admin123'
  'Admin',
  'Librarian',
  'LIBRARIAN',
  true,
  NOW()
);
```

Or use a migration script or data initialization class.

**Alternative**: Temporarily make `/users/register/librarian` public in `SecurityConfig` for the first registration, then protect it again.

---

#### Scenario 4: Register Librarian as Librarian (Should Succeed)

**Step 1**: Login as LIBRARIAN
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@library.com",
    "password": "admin123"
  }'
```

**Step 2**: Register new librarian
```bash
curl -X POST http://localhost:8080/users/register/librarian \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <LIBRARIAN_TOKEN>" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@library.com",
    "password": "securePass456"
  }'
```

**Expected**: 201 Created with user details (role: LIBRARIAN)

---

#### Scenario 5: Access Profile Without Token (Should Fail)

```bash
curl -X GET http://localhost:8080/users/profile
```

**Expected**: 401 Unauthorized

---

#### Scenario 6: Access Profile With Expired Token (Should Fail)

```bash
curl -X GET http://localhost:8080/users/profile \
  -H "Authorization: Bearer <EXPIRED_TOKEN>"
```

**Expected**: 401 Unauthorized (or request continues without authentication, then fails authorization check)

---

### Postman Collection

Create a Postman collection with the following requests:

1. **Register Member**
   - Method: POST
   - URL: `{{baseUrl}}/users/register`
   - Body: JSON with user details

2. **Login**
   - Method: POST
   - URL: `{{baseUrl}}/auth/login`
   - Body: JSON with email/password
   - Test Script: Save token to environment variable

3. **Get Profile**
   - Method: GET
   - URL: `{{baseUrl}}/users/profile`
   - Headers: `Authorization: Bearer {{token}}`

4. **Register Librarian**
   - Method: POST
   - URL: `{{baseUrl}}/users/register/librarian`
   - Headers: `Authorization: Bearer {{token}}`
   - Body: JSON with user details

**Environment Variables**:
- `baseUrl`: `http://localhost:8080`
- `token`: (auto-saved from login response)

---

## ⚠️ Error Handling

### Common Error Responses

#### 400 Bad Request - Validation Error
```json
{
  "timestamp": "2025-12-28T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email should be valid"
    },
    {
      "field": "password",
      "message": "Password must be at least 6 characters long"
    }
  ]
}
```

---

#### 401 Unauthorized - No Token or Invalid Token
```json
{
  "timestamp": "2025-12-28T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

**Causes**:
- No Authorization header
- Invalid token format
- Expired token
- Token signature verification failed

---

#### 403 Forbidden - Insufficient Permissions
```json
{
  "timestamp": "2025-12-28T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

**Causes**:
- User doesn't have required role
- Trying to access `/users/register/librarian` without LIBRARIAN role

---

#### 500 Internal Server Error - Business Logic Error
```json
{
  "timestamp": "2025-12-28T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Email already exists"
}
```

**Common Causes**:
- Email already registered
- Invalid email or password (login)
- User account inactive
- User not found

---

## 🔧 Configuration

### application.yaml
```yaml
spring:
  application:
    name: library-user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/library_user_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

server:
  port: 8081

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}

jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000 # 24 hours in milliseconds
```

### Security Configuration Summary

| Endpoint Pattern | Access Rule | Description |
|-----------------|-------------|-------------|
| `/auth/**` | permitAll() | Public - Login endpoints |
| `/users/register` | permitAll() | Public - Member registration |
| `/users/register/librarian` | hasRole("LIBRARIAN") | Protected - Only librarians |
| `/users/profile` | authenticated() | Protected - Any authenticated user |
| `/**` (all others) | authenticated() | Protected - Requires authentication |

---

## 🚀 Deployment Checklist

### Security Considerations

1. **JWT Secret**
   - ✅ Use a strong, random secret key (at least 256 bits)
   - ✅ Store in environment variables, not in code
   - ✅ Rotate secret periodically

2. **Password Security**
   - ✅ BCrypt encryption with default strength (10 rounds)
   - ✅ Minimum password length enforced (6 characters - consider increasing to 8+)

3. **HTTPS**
   - ⚠️ Enable HTTPS in production
   - ⚠️ JWT tokens should only be transmitted over HTTPS

4. **Token Expiration**
   - ✅ 24-hour expiration configured
   - ✅ Consider implementing refresh tokens for better security

5. **Error Messages**
   - ⚠️ Generic error messages for authentication failures (don't reveal if email exists)
   - ⚠️ Don't expose stack traces in production

6. **Rate Limiting**
   - ⚠️ Implement rate limiting on login endpoint
   - ⚠️ Prevent brute force attacks

7. **Database**
   - ✅ Passwords never stored in plain text
   - ✅ User entity uses proper indexing on email field
   - ⚠️ Consider adding account lockout after failed attempts

---

## 📚 References

### Dependencies Used
- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL Driver**
- **com.auth0:java-jwt:4.5.0** - JWT token generation and validation
- **Lombok** - Reduce boilerplate code
- **Spring Cloud Netflix Eureka Client** - Service registration

### Additional Resources
- [JWT.io](https://jwt.io/) - JWT debugger and documentation
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [BCrypt Calculator](https://bcrypt-generator.com/) - Generate BCrypt hashes

---

## 🎯 Summary

### What We Built
✅ Complete JWT-based authentication system  
✅ Role-based authorization (MEMBER, LIBRARIAN)  
✅ Secure password storage with BCrypt  
✅ Stateless session management  
✅ Custom JWT filter for token validation  
✅ Protected endpoints based on user roles  
✅ RESTful API design  

### Key Security Features
- JWT tokens with 24-hour expiration
- HMAC-SHA256 signature algorithm
- BCrypt password encryption
- Role-based access control
- Stateless authentication (no sessions)
- Custom filter-based token validation

### Next Steps
1. Implement refresh tokens for better security
2. Add account lockout after failed login attempts
3. Implement password reset functionality
4. Add email verification for new registrations
5. Create custom exception handling
6. Add comprehensive logging and monitoring
7. Implement rate limiting on sensitive endpoints
8. Add integration tests for authentication flow

---

**Document Version**: 1.0  
**Last Updated**: December 28, 2025  
**Maintained By**: Library Management System Team

