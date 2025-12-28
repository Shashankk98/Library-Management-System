# Register Librarian API Documentation

## Overview
This document describes the Librarian Registration functionality in the Library Management System. This endpoint allows the creation of new librarian accounts with administrative privileges.

## API Endpoint

### Register Librarian
- **URL**: `/users/register/librarian`
- **Method**: `POST`
- **Service**: `library-user-service`
- **Port**: `8081`
- **Authentication**: Not Required (Public endpoint)

## Request Details

### Request Body
```json
{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "password": "string"
}
```

### Field Validations
| Field | Type | Required | Validation Rules |
|-------|------|----------|------------------|
| firstName | String | Yes | Cannot be blank |
| lastName | String | Yes | Cannot be blank |
| email | String | Yes | Must be a valid email format, Cannot be blank |
| password | String | Yes | Minimum 6 characters, Cannot be blank |

### Example Request
```bash
POST http://localhost:8081/users/register/librarian
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@library.com",
  "password": "securePassword123"
}
```

## Response Details

### Success Response (HTTP 201 Created)
```json
{
  "id": 1,
  "email": "john.doe@library.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "LIBRARIAN",
  "active": true,
  "createdAt": "2025-12-28T10:30:00"
}
```

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique identifier for the user |
| email | String | User's email address |
| firstName | String | User's first name |
| lastName | String | User's last name |
| role | String | User role (LIBRARIAN) |
| active | Boolean | Account status (true for active) |
| createdAt | DateTime | Account creation timestamp |

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

#### 409 Conflict - Email Already Exists
```json
{
  "timestamp": "2025-12-28T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Email already exists"
}
```

## Implementation Details

### Controller
- **Class**: `UserController`
- **Package**: `in.sb.main.library_user_service.controller`
- **Endpoint Method**: `registerLibrarian(@Valid @RequestBody RegisterRequestDto registerRequestDto)`

### Service Layer
- **Class**: `UserService`
- **Package**: `in.sb.main.library_user_service.service`
- **Method**: `registerLibrarian(RegisterRequestDto registerRequestDto)`

### Business Logic
1. **Email Validation**: Checks if the email already exists in the database
2. **Password Encryption**: Encrypts the password using BCrypt password encoder
3. **User Creation**: Creates a new User entity with:
   - Provided first name, last name, and email
   - Encrypted password
   - Role set to `LIBRARIAN`
   - Active status set to `true`
4. **Database Persistence**: Saves the user to the PostgreSQL database
5. **Response Mapping**: Converts the saved User entity to UserDto and returns with HTTP 201 status

### Database Configuration
- **Database**: PostgreSQL
- **Database Name**: `library_user_db`
- **Host**: `localhost:5432`
- **Table**: `users` (auto-created by JPA)

### Security Features
- Password encryption using Spring Security's `PasswordEncoder` (BCrypt)
- Email uniqueness constraint
- Input validation using Jakarta Bean Validation
- Account activation status management

## Testing

### Using cURL
```bash
curl -X POST http://localhost:8081/users/register/librarian \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@library.com",
    "password": "password123"
  }'
```

### Using Postman
1. Set method to POST
2. Enter URL: `http://localhost:8081/users/register/librarian`
3. Go to Headers tab and add:
   - Key: `Content-Type`
   - Value: `application/json`
4. Go to Body tab, select `raw` and `JSON`, then paste the request body
5. Click Send

## Key Differences from Member Registration

| Feature | Member Registration | Librarian Registration |
|---------|-------------------|----------------------|
| Endpoint | `/users/register` | `/users/register/librarian` |
| Role Assigned | MEMBER | LIBRARIAN |
| Privileges | Limited | Administrative |

## Related Endpoints
- **Register Member**: `POST /users/register` - Register a regular library member
- **Login**: `POST /auth/login` - Authenticate and get JWT token
- **Get Profile**: `GET /users/profile` - Get authenticated user profile

## Notes
- The password is never returned in any response for security reasons
- The librarian account is automatically set to active upon creation
- Duplicate email addresses are not allowed
- All fields are mandatory and must pass validation
- The role is automatically set to LIBRARIAN and cannot be modified during registration

## Version History
- **v1.0** (2025-12-28): Initial implementation with basic librarian registration functionality

