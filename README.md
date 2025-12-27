# Library Management System

A Spring Boot-based Library Management System using Eureka for service discovery, API Gateway for routing, Feign for inter-service calls, and RabbitMQ for asynchronous messaging in a scalable microservices architecture.

## Architecture

This project follows a microservices architecture with the following components:

### Services

1. **Eureka Server** (Port 8761)
   - Service discovery and registration
   - Enables dynamic service discovery for all microservices

2. **API Gateway** (Port 8080)
   - Central entry point for all client requests
   - Routes requests to appropriate microservices
   - Load balancing using Eureka client

3. **Book Service** (Port 8081)
   - Manages book inventory
   - CRUD operations for books
   - Publishes book events to RabbitMQ
   - H2 in-memory database

4. **User Service** (Port 8082)
   - Manages library users
   - CRUD operations for users
   - H2 in-memory database

5. **Loan Service** (Port 8083)
   - Manages book loans and returns
   - Uses Feign clients to communicate with Book and User services
   - Publishes loan events to RabbitMQ
   - H2 in-memory database

6. **RabbitMQ** (Ports 5672, 15672)
   - Message broker for asynchronous communication
   - Handles book and loan events

## Technologies Used

- **Spring Boot 3.2.0** - Core framework
- **Spring Cloud 2023.0.0** - Microservices framework
- **Netflix Eureka** - Service discovery
- **Spring Cloud Gateway** - API Gateway
- **OpenFeign** - Declarative REST client
- **RabbitMQ** - Message broker
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Lombok** - Boilerplate code reduction
- **Maven** - Build tool
- **Docker & Docker Compose** - Containerization

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (optional, for containerized deployment)
- RabbitMQ (if running locally without Docker)

## Building the Project

Build all microservices:

```bash
mvn clean install
```

Build individual service:

```bash
cd <service-name>
mvn clean package
```

## Running the Application

### Option 1: Run Locally

1. **Start RabbitMQ** (if not using Docker):
   ```bash
   # Using Homebrew on Mac
   brew services start rabbitmq
   
   # Or using Docker
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```

2. **Start Eureka Server**:
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
   Access at: http://localhost:8761

3. **Start API Gateway**:
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

4. **Start Book Service**:
   ```bash
   cd book-service
   mvn spring-boot:run
   ```

5. **Start User Service**:
   ```bash
   cd user-service
   mvn spring-boot:run
   ```

6. **Start Loan Service**:
   ```bash
   cd loan-service
   mvn spring-boot:run
   ```

### Option 2: Run with Docker Compose

1. **Build all services**:
   ```bash
   mvn clean package
   ```

2. **Start all services**:
   ```bash
   docker-compose up -d
   ```

3. **Stop all services**:
   ```bash
   docker-compose down
   ```

## API Endpoints

All requests should go through the API Gateway at `http://localhost:8080`

### Book Service APIs

- **GET** `/api/books` - Get all books
- **GET** `/api/books/{id}` - Get book by ID
- **GET** `/api/books/isbn/{isbn}` - Get book by ISBN
- **POST** `/api/books` - Create a new book
  ```json
  {
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "totalCopies": 5,
    "availableCopies": 5
  }
  ```
- **PUT** `/api/books/{id}` - Update a book
- **DELETE** `/api/books/{id}` - Delete a book

### User Service APIs

- **GET** `/api/users` - Get all users
- **GET** `/api/users/{id}` - Get user by ID
- **GET** `/api/users/email/{email}` - Get user by email
- **POST** `/api/users` - Create a new user
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890"
  }
  ```
- **PUT** `/api/users/{id}` - Update a user
- **DELETE** `/api/users/{id}` - Delete a user

### Loan Service APIs

- **GET** `/api/loans` - Get all loans
- **GET** `/api/loans/{id}` - Get loan by ID
- **GET** `/api/loans/user/{userId}` - Get loans by user
- **GET** `/api/loans/book/{bookId}` - Get loans by book
- **POST** `/api/loans/borrow?bookId={bookId}&userId={userId}` - Borrow a book
- **POST** `/api/loans/{id}/return` - Return a book

## Testing the Application

### Example Flow

1. **Create a book**:
   ```bash
   curl -X POST http://localhost:8080/api/books \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Clean Code",
       "author": "Robert C. Martin",
       "isbn": "9780132350884",
       "totalCopies": 5,
       "availableCopies": 5
     }'
   ```

2. **Create a user**:
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{
       "firstName": "John",
       "lastName": "Doe",
       "email": "john.doe@example.com",
       "phone": "1234567890"
     }'
   ```

3. **Borrow a book**:
   ```bash
   curl -X POST "http://localhost:8080/api/loans/borrow?bookId=1&userId=1"
   ```

4. **Return a book**:
   ```bash
   curl -X POST http://localhost:8080/api/loans/1/return
   ```

## Monitoring

- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **H2 Console (Book Service)**: http://localhost:8081/h2-console
- **H2 Console (User Service)**: http://localhost:8082/h2-console
- **H2 Console (Loan Service)**: http://localhost:8083/h2-console

## Project Structure

```
library-management-system/
├── eureka-server/          # Service discovery server
├── api-gateway/            # API Gateway
├── book-service/           # Book management service
├── user-service/           # User management service
├── loan-service/           # Loan management service
├── docker-compose.yml      # Docker compose configuration
├── pom.xml                 # Parent POM
└── README.md               # This file
```

## Key Features

- **Service Discovery**: Automatic service registration and discovery using Eureka
- **API Gateway**: Single entry point with routing and load balancing
- **Inter-Service Communication**: Feign clients for synchronous communication
- **Asynchronous Messaging**: RabbitMQ for event-driven architecture
- **Resilience**: Distributed architecture with independent services
- **Scalability**: Easy to scale individual services
- **Containerization**: Docker support for easy deployment

## Future Enhancements

- Add authentication and authorization (Spring Security, JWT)
- Implement circuit breaker pattern (Resilience4j)
- Add distributed tracing (Zipkin/Sleuth)
- Implement API rate limiting
- Add comprehensive test coverage
- Replace H2 with persistent databases (PostgreSQL/MySQL)
- Add logging aggregation (ELK stack)
- Implement caching (Redis)

## License

This project is open source and available under the MIT License.
