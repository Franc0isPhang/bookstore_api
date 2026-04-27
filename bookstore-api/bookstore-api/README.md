# Bookstore REST API

A RESTful API for managing a bookstore's catalog, built as a proof-of-concept using Spring Boot 3 and Java 17. Features JWT-based authentication, role-based authorization, and a full CRUD interface for books with multi-author support.

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Testing the API](#testing-the-api)
- [Sample Requests & Responses](#sample-requests--responses)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Design Decisions](#design-decisions)
- [Troubleshooting](#troubleshooting)

---

## Features

- Add new books with one or more authors
- Update existing book information
- Search books by exact title, exact author name, or both
- Delete books (restricted to admin role)
- JWT-based authentication with role-based access control
- Bean Validation on all inputs (ISBN format, required fields, valid ranges)
- Global exception handling with structured JSON error responses
- OpenAPI/Swagger UI documentation
- In-memory H2 database with seeded sample data
- Integration tests covering happy paths and error cases

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security + JWT (jjwt 0.12) |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 (in-memory) |
| API Documentation | SpringDoc OpenAPI 2.6 (Swagger UI) |
| Build Tool | Maven |
| Testing | JUnit 5 + MockMvc + Spring Security Test |

## Prerequisites

Before you start, make sure you have the following installed:

| Tool | Version | Verify Command |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |

### Installing Prerequisites on Windows

1. **JDK 17** — Download [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17) (`.msi` installer). During install, check "Set JAVA_HOME" and "Add to PATH".
2. **Maven** — Download the [binary zip](https://maven.apache.org/download.cgi), extract, and add the `bin` folder to your system PATH.

After installation, open a new Command Prompt and run:
```bash
java -version    # should show 17.x.x
mvn -version     # should show Maven + Java 17
```

## Setup & Installation

### Step 1: Extract the Project
Unzip `bookstore-api.zip` to a location on your machine. **Recommended locations** (keep the path short and avoid OneDrive):
- `C:\dev\bookstore-api`
- `C:\Users\<yourname>\Projects\bookstore-api`


### Step 2: Navigate to the Project Folder
Open Command Prompt and `cd` into the folder containing `pom.xml`:

```bash
cd C:\dev\bookstore-api
```

Verify you're in the right place:
```bash
dir pom.xml
```

### Step 3: Build the Project
```bash
mvn clean install
```

First build takes a few minutes while Maven downloads dependencies. Subsequent builds are fast.

If tests cause issues, skip them:
```bash
mvn clean install -DskipTests
```

## Running the Application

### Start the API
```bash
mvn spring-boot:run
```

Wait for the message:
```
Started BookstoreApplication in X seconds
```

The API is now running on `http://localhost:8080`.

### Stop the API
Press `Ctrl + C` in the terminal.

## Authentication

The API uses **JWT Bearer tokens**. You must log in first to obtain a token, then include it in the `Authorization` header for all protected endpoints.

### Default Users

Two users are seeded automatically on startup:

| Username | Password | Role | Can Delete Books? |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | ✅ Yes |
| `user` | `user123` | USER | ❌ No (returns 403) |

### Login Flow

1. Send credentials to `POST /api/v1/auth/login`
2. Receive a JWT token in the response
3. Include the token as `Authorization: Bearer <token>` in all other requests
4. Token is valid for 1 hour

## API Endpoints

| Method | Path | Auth Required | Role Required | Description |
|---|---|:---:|:---:|---|
| POST | `/api/v1/auth/login` | ❌ | — | Authenticate and receive JWT |
| POST | `/api/v1/books` | ✅ | USER or ADMIN | Add a new book |
| PUT | `/api/v1/books/{isbn}` | ✅ | USER or ADMIN | Update an existing book |
| GET | `/api/v1/books` | ✅ | USER or ADMIN | Search books (optional `title`, `author` query params) |
| DELETE | `/api/v1/books/{isbn}` | ✅ | **ADMIN only** | Delete a book |

### HTTP Status Codes

| Code | Meaning | When Returned |
|---|---|---|
| 200 | OK | Successful read or update |
| 201 | Created | Book successfully added |
| 204 | No Content | Book successfully deleted |
| 400 | Bad Request | Validation failed (invalid ISBN, missing fields, etc.) |
| 401 | Unauthorized | Missing or invalid JWT |
| 403 | Forbidden | User lacks required role (e.g., non-admin trying to delete) |
| 404 | Not Found | Book with given ISBN does not exist |
| 409 | Conflict | Book with given ISBN already exists |
| 500 | Internal Server Error | Unexpected server error |

## Testing the API

You have three options to test the API:

### Option 1: Swagger UI (Recommended for Demo)
Once the app is running, open in your browser:
```
http://localhost:8080/swagger-ui.html
```

1. Expand the **Authentication** section and click `POST /api/v1/auth/login`
2. Click **Try it out**, enter credentials, click **Execute**
3. Copy the `token` value from the response (just the long string, no quotes)
4. Click the **Authorize** button at the top of the page
5. Paste the token (without `Bearer`), click **Authorize**, then **Close**
6. All endpoints are now accessible — try them under the **Books** section


### Option 3: H2 Console (View Database Directly)
While the app is running, visit `http://localhost:8080/h2-console` to inspect the database:
- **JDBC URL:** `jdbc:h2:mem:bookstoredb`
- **Username:** `sa`
- **Password:** *(blank)*

Useful queries:
```sql
SELECT * FROM books;
SELECT * FROM authors;
SELECT * FROM book_authors;
```

## Sample Requests & Responses

### 1. Login

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiI...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "expiresInMs": 3600000
}
```

### 2. Search All Books

**Request:**
```http
GET /api/v1/books
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "isbn": "9780451524935",
    "title": "1984",
    "authors": [
      { "name": "George Orwell", "birthday": "1903-06-25" }
    ],
    "year": 1949,
    "price": 15.99,
    "genre": "Dystopian"
  },
  {
    "isbn": "9780060853983",
    "title": "Good Omens",
    "authors": [
      { "name": "Neil Gaiman", "birthday": "1960-11-10" },
      { "name": "Terry Pratchett", "birthday": "1948-04-28" }
    ],
    "year": 1990,
    "price": 18.75,
    "genre": "Fantasy"
  }
]
```

### 3. Search by Title

**Request:**
```http
GET /api/v1/books?title=1984
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "isbn": "9780451524935",
    "title": "1984",
    "authors": [{ "name": "George Orwell", "birthday": "1903-06-25" }],
    "year": 1949,
    "price": 15.99,
    "genre": "Dystopian"
  }
]
```

### 4. Search by Author

**Request:**
```http
GET /api/v1/books?author=Neil Gaiman
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "isbn": "9780060853983",
    "title": "Good Omens",
    "authors": [
      { "name": "Neil Gaiman", "birthday": "1960-11-10" },
      { "name": "Terry Pratchett", "birthday": "1948-04-28" }
    ],
    "year": 1990,
    "price": 18.75,
    "genre": "Fantasy"
  }
]
```

### 5. Add a New Book

**Request:**
```http
POST /api/v1/books
Authorization: Bearer <token>
Content-Type: application/json

{
  "isbn": "9780765316790",
  "title": "Mistborn: The Final Empire",
  "authors": [
    { "name": "Brandon Sanderson", "birthday": "1975-12-19" }
  ],
  "year": 2006,
  "price": 22.00,
  "genre": "Fantasy"
}
```

**Response (201 Created):**
```json
{
  "isbn": "9780765316790",
  "title": "Mistborn: The Final Empire",
  "authors": [
    { "name": "Brandon Sanderson", "birthday": "1975-12-19" }
  ],
  "year": 2006,
  "price": 22.00,
  "genre": "Fantasy"
}
```

### 6. Update a Book

**Request:**
```http
PUT /api/v1/books/9780765316790
Authorization: Bearer <token>
Content-Type: application/json

{
  "isbn": "9780765316790",
  "title": "Mistborn: The Final Empire (Special Edition)",
  "authors": [
    { "name": "Brandon Sanderson", "birthday": "1975-12-19" }
  ],
  "year": 2006,
  "price": 29.99,
  "genre": "Fantasy"
}
```

**Response (200 OK):**
```json
{
  "isbn": "9780765316790",
  "title": "Mistborn: The Final Empire (Special Edition)",
  "authors": [
    { "name": "Brandon Sanderson", "birthday": "1975-12-19" }
  ],
  "year": 2006,
  "price": 29.99,
  "genre": "Fantasy"
}
```

### 7. Delete a Book (as Admin)

**Request:**
```http
DELETE /api/v1/books/9780765316790
Authorization: Bearer <admin-token>
```

**Response (204 No Content):** *(empty body)*

### 8. Delete a Book (as User — Forbidden)

**Request:**
```http
DELETE /api/v1/books/9780451524935
Authorization: Bearer <user-token>
```

**Response (403 Forbidden):**
```json
{
  "timestamp": "2026-04-24T10:15:30.123",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: insufficient privileges for this operation",
  "path": "/api/v1/books/9780451524935"
}
```

### 9. Validation Error Example

**Request:**
```http
POST /api/v1/books
Authorization: Bearer <token>
Content-Type: application/json

{
  "isbn": "not-a-valid-isbn",
  "title": "",
  "authors": [],
  "year": 1000,
  "price": -5,
  "genre": ""
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-04-24T10:20:15.789",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/books",
  "details": [
    "isbn: ISBN must be a valid ISBN-10 or ISBN-13",
    "title: Title is required",
    "authors: At least one author is required",
    "year: Year must be 1450 or later",
    "price: Price must be greater than 0",
    "genre: Genre is required"
  ]
}
```

### 10. Duplicate ISBN

**Response (409 Conflict):**
```json
{
  "timestamp": "2026-04-24T10:22:05.456",
  "status": 409,
  "error": "Conflict",
  "message": "Book already exists with ISBN: 9780451524935",
  "path": "/api/v1/books"
}
```

### 11. Book Not Found

**Response (404 Not Found):**
```json
{
  "timestamp": "2026-04-24T10:25:33.789",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with ISBN: 9999999999999",
  "path": "/api/v1/books/9999999999999"
}
```

## Database Schema

The database uses three tables to model the many-to-many relationship between books and authors:

```
┌─────────────────┐         ┌──────────────────┐         ┌───────────────────┐
│     books       │         │   book_authors   │         │      authors      │
├─────────────────┤         ├──────────────────┤         ├───────────────────┤
│ isbn (PK)       │◄────────│ book_isbn (FK)   │         │ id (PK)           │
│ title           │         │ author_id (FK)   │────────►│ name (UNIQUE)     │
│ year            │         └──────────────────┘         │ birthday          │
│ price           │                                      └───────────────────┘
│ genre           │
└─────────────────┘

┌─────────────────┐
│     users       │
├─────────────────┤
│ id (PK)         │
│ username (UQ)   │
│ password        │
│ role (ENUM)     │
└─────────────────┘
```

**How multiple authors are stored:**
Each authorship is a row in `book_authors`. A book with 2 authors gets 2 rows, same ISBN, different author IDs.

Example for "Good Omens":
| book_isbn | author_id |
|---|---|
| 9780060853983 | 4 (Neil Gaiman) |
| 9780060853983 | 5 (Terry Pratchett) |

## Project Structure

```
bookstore-api/
├── pom.xml                              # Maven dependencies
├── README.md                            # This file
├── postman_collection.json              # Postman collection
├── screenshots/                         # API demo screenshots
├── src/
│   ├── main/
│   │   ├── java/com/bookstore/
│   │   │   ├── BookstoreApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java         # Spring Security + JWT setup
│   │   │   │   ├── OpenApiConfig.java          # Swagger UI configuration
│   │   │   │   └── DataInitializer.java        # Seeds default users
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java         # Login endpoint
│   │   │   │   └── BookController.java         # Book CRUD endpoints
│   │   │   ├── service/
│   │   │   │   └── BookService.java            # Business logic
│   │   │   ├── repository/
│   │   │   │   ├── BookRepository.java
│   │   │   │   ├── AuthorRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Book.java                   # @ManyToMany to Author
│   │   │   │   ├── Author.java
│   │   │   │   └── User.java
│   │   │   ├── dto/                            # Request/response objects
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java # Structured error responses
│   │   │   └── security/
│   │   │       ├── JwtTokenProvider.java       # Token generation/validation
│   │   │       ├── JwtAuthenticationFilter.java
│   │   │       ├── CustomUserDetailsService.java
│   │   │       └── JwtAuthenticationEntryPoint.java
│   │   └── resources/
│   │       ├── application.yml                 # App config
│   │       └── data.sql                        # Sample seed data
│   └── test/java/com/bookstore/
│       └── BookControllerIntegrationTest.java  # JUnit 5 + MockMvc tests
```

## Troubleshooting

### "Maven is not recognized"
Maven isn't on your PATH. Verify with `mvn -version`. If it fails, re-check that the Maven `bin` folder is in your system PATH (in Environment Variables).

### Port 8080 already in use
Another app is using port 8080. Either stop that app, or change the port in `application.yml`:
```yaml
server:
  port: 8081
```

### 401 Unauthorized on every request
Your JWT is missing, expired (>1hr), or malformed. Log in again and re-authorize Swagger. When copying the token, make sure you copy *only* the token string, not the surrounding JSON.

### 500 Internal Server Error when adding a book
Check the Spring Boot terminal for the real stack trace. Common causes:
- Duplicate author with same name (the service handles this, but worth checking)
- Invalid ISBN format
- Database state inconsistency — restart the app to reset in-memory data

### How do I reset the database?
Simply restart the app. H2 is in-memory, so stopping the app wipes all data. Seed data reloads on startup from `data.sql`.

---

