# Professional Expense Tracker API 🚀

A production-quality RESTful backend for expense management built with **Java Spring Boot**, **Spring Security (JWT)**, and **SQLite**.

## 🏗️ Architecture
This project follows **Clean Layered Architecture** for maximum maintainability and scalability:
- **Controller**: REST Endpoints & Request Mapping.
- **Service**: Business Logic & Authorization.
- **Repository**: Data Access Layer (Spring Data JPA).
- **Model/Entity**: Database Schema & Relationships.
- **DTO**: Data Transfer Objects for secure API communication.
- **Security**: JWT-based Authentication & Role-Based Access Control (RBAC).

## ✨ Key Features
- **Security**: Full JWT integration with password encryption (BCrypt).
- **Audit**: Automatic tracking of `createdAt` and `updatedAt` for all records.
- **Filtering**: Advanced date-range filtering and search support.
- **Pagination**: Efficient handling of large data lists via Spring Data Pagination.
- **Analytics**: Built-in APIs for monthly trends and category-wise spending.
- **Validation**: Strict input validation using Jakarta Validation annotations.
- **Error Handling**: Global exception handler for standardized error responses.

## 🛠️ Tech Stack
- **Framework**: Spring Boot 3.2+
- **Security**: Spring Security + JJWT
- **OR/M**: Hibernate / Spring Data JPA
- **Database**: SQLite (Zero-config, portable)
- **Utilities**: Lombok, Jakarta Validation

## 🚀 Setup Instructions

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Steps
1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd expense-tracker
   ```
2. **Build the project**:
   ```bash
   mvn clean install
   ```
3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   The API will be available at `http://localhost:8080`.

## 📡 API Endpoints

### Authentication
- `POST /api/auth/signup`: Create a new account.
- `POST /api/auth/signin`: Log in and receive a JWT token.

### Expenses (JWT Required)
- `GET /api/expenses`: Get all expenses (Supports pagination & date filters).
- `POST /api/expenses`: Create a new expense.
- `PUT /api/expenses/{id}`: Update an existing expense.
- `DELETE /api/expenses/{id}`: Remove an expense.
- `GET /api/expenses/analytics`: Get category usage and monthly trends.

### Categories
- `GET /api/categories`: List all categories.
- `POST /api/categories`: Create category (Admin only).

## 📊 Sample Analytics Response
```json
{
  "success": true,
  "data": {
    "totalExpense": 4500.50,
    "categoryUsage": [
      { "category": "Food", "total": 1200.00 },
      { "category": "Travel", "total": 800.00 }
    ],
    "monthlyTrend": [
      { "month": "2024-02", "total": 2100.00 },
      { "month": "2024-01", "total": 2400.50 }
    ]
  }
}
```
