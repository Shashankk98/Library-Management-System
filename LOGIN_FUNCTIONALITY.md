# Login API Documentation

## Overview
This document describes the Login functionality in the Library Management System. This endpoint authenticates users (both Members and Librarians) and provides JWT tokens for accessing protected resources.

## API Endpoint

### User Login
- **URL**: `/auth/login`
- **Method**: `POST`
- **Service**: `library-user-service`
- **Port**: `8081`
- **Authentication**: Not Required (Public endpoint)

## Request Details

### Request Body
```json
{
  "email": "string",
  "password": "string"
}
```

### Field Validations
| Field | Type | Required | Validation Rules |
|-------|------|----------|------------------|
| email | String | Yes | Must be a valid email format, Cannot be blank |
| password | String | Yes | Minimum 6 characters, Cannot be blank |

### Example Request
```bash
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "email": "john.doe@library.com",
  "password": "securePassword123"
}
```

## Response Details

### Success Response (HTTP 200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBsaWJyYXJ5LmNvbSIsInJvbGUiOiJMSUJSQVJJQU4iLCJpYXQiOjE3MDM3NjQ4MDAsImV4cCI6MTcwMzg1MTIwMH0.xxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "userDto": {
    "id": 1,
    "email": "john.doe@library.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "LIBRARIAN",
    "active": true,
    "createdAt": "2025-12-28T10:30:00"
  }
}
```

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| token | String | JWT authentication token (valid for 24 hours) |
| userDto | Object | User information object |
| userDto.id | Long | Unique identifier for the user |
| userDto.email | String | User's email address |
| userDto.firstName | String | User's first name |
| userDto.lastName | String | User's last name |
| userDto.role | String | User role (MEMBER or LIBRARIAN) |
| userDto.active | Boolean | Account status |
| userDto.createdAt | DateTime | Account creation timestamp |

### Error Responses

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
    }
  ]
}
```

#### 401 Unauthorized - Invalid Credentials
```json
{
  "timestamp": "2025-12-28T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Invalid email or password"
}
```

#### 403 Forbidden - Inactive Account
```json
{
  "timestamp": "2025-12-28T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "User account is inactive"
}
```

## Implementation Details

### Controller
- **Class**: `AuthController`
- **Package**: `in.sb.main.library_user_service.controller`
- **Endpoint Method**: `login(@Valid @RequestBody LoginRequestDto loginRequestDto)`

### Service Layer
- **Class**: `AuthService`
- **Package**: `in.sb.main.library_user_service.service`
- **Method**: `login(LoginRequestDto loginRequestDto)`

### Business Logic
1. **User Lookup**: Finds user by email in the database
2. **Account Status Check**: Verifies that the user account is active
3. **Password Verification**: Compares provided password with stored encrypted password using BCrypt
4. **JWT Token Generation**: Creates a JWT token containing user information
5. **Response Construction**: Builds login response with token and user details

### JWT Token Configuration
- **Secret Key**: Configured in `application.yaml` (jwt.secret)
- **Expiration Time**: 86400000 milliseconds (24 hours)
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Token Contains**: 
  - Subject (email)
  - Role (MEMBER or LIBRARIAN)
  - Issued At (iat)
  - Expiration (exp)

### Database Configuration
- **Database**: PostgreSQL
- **Database Name**: `library_user_db`
- **Host**: `localhost:5432`
- **Table**: `users`

### Security Features
- Password verification using BCrypt password encoder
- JWT token-based authentication
- Account activation status verification
- Input validation using Jakarta Bean Validation
- Secure password comparison to prevent timing attacks

## Using the JWT Token

### Authenticated Requests
After successful login, include the JWT token in subsequent requests to protected endpoints:

```bash
GET http://localhost:8081/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBsaWJyYXJ5LmNvbSIsInJvbGUiOiJMSUJSQVJJQU4iLCJpYXQiOjE3MDM3NjQ4MDAsImV4cCI6MTcwMzg1MTIwMH0.xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Token Structure
The JWT token consists of three parts separated by dots:
1. **Header**: Algorithm and token type
2. **Payload**: Claims (user email, role, expiration)
3. **Signature**: Verification signature

## Testing

### Using cURL
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@library.com",
    "password": "securePassword123"
  }'
```

### Using cURL with Token
```bash
# Store token in variable
TOKEN=$(curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@library.com",
    "password": "securePassword123"
  }' | jq -r '.token')

# Use token for authenticated request
curl -X GET http://localhost:8081/users/profile \
  -H "Authorization: Bearer $TOKEN"
```

### Using Postman
1. **Login Request**:
   - Set method to POST
   - Enter URL: `http://localhost:8081/auth/login`
   - Go to Headers tab and add:
     - Key: `Content-Type`
     - Value: `application/json`
   - Go to Body tab, select `raw` and `JSON`, then paste the request body
   - Click Send
   - Copy the `token` value from the response

2. **Authenticated Request**:
   - Create a new request
   - Go to Authorization tab
   - Select Type: `Bearer Token`
   - Paste the token in the Token field
   - Send the request

## Authentication Flow

```
┌──────────┐                                    ┌──────────────────┐
│  Client  │                                    │  library-user-   │
│          │                                    │     service      │
└────┬─────┘                                    └────────┬─────────┘
     │                                                   │
     │  POST /auth/login                                │
     │  { email, password }                             │
     ├──────────────────────────────────────────────────>
     │                                                   │
     │                    Validate credentials           │
     │                    Check account status           │
     │                    Generate JWT token             │
     │                                                   │
     │  200 OK                                           │
     │  { token, userDto }                               │
     <──────────────────────────────────────────────────┤
     │                                                   │
     │  GET /users/profile                              │
     │  Authorization: Bearer <token>                   │
     ├──────────────────────────────────────────────────>
     │                                                   │
     │                    Verify JWT token               │
     │                    Extract user info              │
     │                    Process request                │
     │                                                   │
     │  200 OK                                           │
     │  { user profile data }                            │
     <──────────────────────────────────────────────────┤
     │                                                   │
```

## Common Use Cases

### 1. Librarian Login
```json
{
  "email": "librarian@library.com",
  "password": "librarianPass123"
}
```
**Result**: Returns token with LIBRARIAN role, granting administrative access

### 2. Member Login
```json
{
  "email": "member@example.com",
  "password": "memberPass123"
}
```
**Result**: Returns token with MEMBER role, granting standard user access

### 3. Token Expiration
- Token is valid for 24 hours from issuance
- After expiration, user must login again to get a new token
- Expired tokens will be rejected by protected endpoints

## Security Best Practices

1. **HTTPS**: Always use HTTPS in production to prevent token interception
2. **Token Storage**: Store tokens securely (e.g., in httpOnly cookies or secure storage)
3. **Token Expiration**: Monitor token expiration and refresh before expiry
4. **Logout**: Clear tokens from client storage on logout
5. **Password Policy**: Enforce strong password requirements
6. **Rate Limiting**: Implement rate limiting to prevent brute force attacks
7. **Account Lockout**: Consider implementing account lockout after multiple failed attempts

## Related Endpoints
- **Register Member**: `POST /users/register` - Create a new member account
- **Register Librarian**: `POST /users/register/librarian` - Create a new librarian account
- **Get Profile**: `GET /users/profile` - Get authenticated user's profile information

## Troubleshooting

### Issue: "Invalid email or password"
- **Cause**: Email doesn't exist or password is incorrect
- **Solution**: Verify credentials or register a new account

### Issue: "User account is inactive"
- **Cause**: User account has been deactivated
- **Solution**: Contact administrator to reactivate the account

### Issue: Token rejected on subsequent requests
- **Cause**: Token expired or invalid
- **Solution**: Login again to get a new token

### Issue: 401 Unauthorized on protected endpoints
- **Cause**: Missing or invalid Authorization header
- **Solution**: Include valid Bearer token in Authorization header

## Environment Variables
Configure these in `application.yaml`:
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000 # 24 hours in milliseconds
```

**Important**: Change the secret key in production to a secure, randomly generated value!

## Version History
- **v1.0** (2025-12-28): Initial implementation with JWT-based authentication

