# User Management Service

A Spring Boot microservice for managing users, roles, and authentication.
This project demonstrates user registration, role assignment, and secure access management using Spring Security, JWT, and REST APIs.

## 🚀 Features

- User registration and authentication
- Role-based access control (RBAC)
- JWT-based authentication
- RESTful APIs for user and role management
- Database integration (PostgreSQL/MySQL/Oracle – configurable)
- Docker support for containerized deployment

## 🛠 Tech Stack

- Java 17+
- Spring Boot (Web, Security, JPA)
- Hibernate / JPA
- PostgreSQL/MySQL/Oracle (configurable in application.properties)
- Maven (dependency management)
- Docker (containerization)

## 📂 Project Structure
```
Usermanagement-Service/
 ├── src/main/java/com/drupad/usermanagement/
 │    ├── controller/    # REST controllers
 │    ├── model/         # Entities (User, Role, etc.)
 │    ├── repository/    # Spring Data JPA repositories
 │    ├── service/       # Business logic
 │    └── security/      # JWT & Spring Security configuration
 ├── src/main/resources/
 │    ├── application.properties  # DB & app config
 │    └── schema.sql / data.sql   # Optional DB initialization
 ├── Dockerfile
 ├── pom.xml
 └── README.md
```

## ⚙️ Setup & Installation

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL/MySQL/Oracle DB
- Docker (optional, for containerized deployment)

### Clone the repository
```sh
git clone https://github.com/Drupad1522/Usermanagement-Service.git
cd Usermanagement-Service
```

### Configure Database
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Build & Run
```sh
mvn clean install
mvn spring-boot:run
```

## 🐳 Run with Docker

### Build the Docker image:
```sh
docker build -t usermanagement-service .
```

### Run the container:
```sh
docker run -p 8080:8080 usermanagement-service
```

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` → Register a new user
- `POST /api/auth/login` → Authenticate and receive JWT

### Users
- `GET /api/users` → Get all users (Admin only)
- `GET /api/users/{id}` → Get user by ID
- `PUT /api/users/{id}` → Update user
- `DELETE /api/users/{id}` → Delete user

### Roles
- `POST /api/roles` → Create a role
- `GET /api/roles` → Get all roles

## 🔒 Security
- JWT tokens are used for authentication.
- Add token in the `Authorization` header:
  ```
  Authorization: Bearer <your_jwt_token>
  ```

## 🧪 Testing
Run unit tests with:
```sh
mvn test
```

## 📖 Future Enhancements
- Add password reset functionality
- OAuth2 / Social login support
- API documentation with Swagger/OpenAPI

## 👨‍💻 Author
**Drupad Siddaraju**  
📌 GitHub: [Drupad1522](https://github.com/Drupad1522)
