Frontend repository: [Tickety Angular App](https://github.com/nounourek3/Tickety)

 Backend repository: [Tickety Angular App](https://github.com/nounourek3/Tickety-backend)
 
🧭 Overview

The Tickety backend powers the Tickety Travel & Flight Manager web app.
It provides a secure REST API for authentication, trip and flight management, and file uploads to a MinIO cloud-like storage system.
Built with Spring Boot 3, it follows a modular, clean architecture with JWT authentication, JPA persistence, and Dockerized deployment.

🚀 Features

✅ JWT Authentication (Login / Register / Refresh Token)
✅ CRUD for Trips and Flights
✅ File Uploads to MinIO using presigned URLs
✅ Database versioning with Flyway
✅ Data Validation with Jakarta Bean Validation
✅ API Documentation via Swagger / OpenAPI
✅ Docker Compose setup (App + MySQL + MinIO)
✅ Unit and Integration tests (JUnit 5 + Testcontainers)

🧩 Tech Stack

| Layer         | Technology                  |
| ------------- | --------------------------- |
| Framework     | Spring Boot 3.3             |
| Security      | Spring Security + JWT       |
| Data          | Spring Data JPA (Hibernate) |
| Database      | MySQL                       |
| File Storage  | MinIO (S3-compatible)       |
| Mapping       | MapStruct                   |
| Validation    | Jakarta Bean Validation     |
| Documentation | Swagger / OpenAPI           |
| Testing       | JUnit 5 + Testcontainers    |
| Build         | Maven                       |

🧠 Architecture Overview

👩‍💻 User
   ↓
🌐 Angular Frontend (Tickety)
   ↓
⚙️ Spring Boot Backend (REST API, JWT Auth)
   ┣ 🗄️ MySQL – stores users, flights, trips
   ┗ ☁️ MinIO – stores PDF boarding passes (S3-compatible)

🔒 Authentication & Security

✅ JWT Authentication (Login / Register / Refresh Token)
✅ Google OAuth2 Login – sign in securely with Google account
✅ Role-based access for protected endpoints
✅ Refresh token rotation & token expiration handling
✅ CORS and exception handling configured for frontend integration
