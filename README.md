# 🍔 F&B Recommender System

> Spring Boot REST API for Food & Beverage recommendation system with shop management, menus, reviews, and blogs.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 📖 Overview

A comprehensive backend system for food and beverage recommendations featuring:

- 🏪 **Shop Management** - Create and manage F&B shops with geospatial search
- 📋 **Menu System** - Organize menus and menu items with rich metadata
- ⭐ **Reviews & Ratings** - User reviews with nested replies
- 📝 **Blog Platform** - Food blogging with comments
- 🔍 **Smart Search** - Search by name, location, category, and more
- 📊 **Analytics** - View counts, ratings, likes tracking

## 🚀 Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Maven 3.9+

### Installation

```bash
# 1. Clone the repository
cd /Users/nguyenhuuloc/Downloads/demo2

# 2. Create database
createdb fnb_recommender_db

# 3. Configure database in application.properties
# Edit src/main/resources/application.properties

# 4. Run application
mvn spring-boot:run
```

### Access Points

- **API Server**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/api-docs

## 🎯 Sample Data

**Automatic data initialization on first run!**

The system includes `DataInitializer` that creates:

- ✅ **13 Users** (1 admin, 3 owners, 9 regular users)
- ✅ **7 Categories** (Coffee, Vietnamese, Fast Food, etc.)
- ✅ **7 Shops** with real-world locations in Ho Chi Minh City
- ✅ **7 Menus** (one per shop)
- ✅ **40+ Menu Items** across all shops
- ✅ **8 Reviews** with owner replies
- ✅ **4 Blogs** about food & dining
- ✅ **5 Comments** on blogs

### Test Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | admin |
| owner_cafe | password123 | owner |
| owner_restaurant | password123 | owner |
| owner_fastfood | password123 | owner |
| user1 - user9 | password123 | user |

👉 See [QUICKSTART.md](QUICKSTART.md) for detailed instructions.

## 📚 Documentation

- 📘 [API Documentation](API_DOCUMENTATION.md) - Complete API reference
- 🚀 [Quick Start Guide](QUICKSTART.md) - Get started in 5 minutes
- 🔧 [Exception Handling](src/main/java/com/example/demo/exception/README.md) - Error handling guide

## 🏗️ Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────┐
│         Controllers (REST)          │  ← HTTP/JSON Interface
├─────────────────────────────────────┤
│      Services (Business Logic)      │  ← Application Logic
├─────────────────────────────────────┤
│    Repositories (Data Access)       │  ← Data Persistence
├─────────────────────────────────────┤
│       Models/Entities (JPA)         │  ← Domain Model
└─────────────────────────────────────┘
```

### Technology Stack

**Core Framework:**
- Spring Boot 4.0.3
- Java 21 (LTS)
- Maven

**Data Layer:**
- Spring Data JPA
- Hibernate ORM
- PostgreSQL 15+
- PostGIS (geospatial queries)

**DTO Mapping:**
- ModelMapper 3.2.0
- Lombok (code generation)

**API Documentation:**
- SpringDoc OpenAPI 2.3.0
- Swagger UI

**Validation & Security:**
- Jakarta Bean Validation
- Spring Security
- BCrypt Password Encoder

## 📦 Project Structure

```
src/main/java/com/example/demo/
├── config/              # Configuration classes
│   ├── CorsConfig.java
│   ├── DataInitializer.java  ← Sample data generator
│   ├── JpaConfig.java
│   ├── MapperConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/          # REST Controllers (8)
├── dto/
│   ├── request/        # Request DTOs (8)
│   └── response/       # Response DTOs (9)
├── exception/          # Exception handling (9)
├── mapper/             # Entity/DTO mappers (9)
├── model/              # JPA Entities (9)
├── repository/         # Data repositories (8)
└── service/
    ├── [interfaces]    # Service interfaces (8)
    └── impl/           # Service implementations (8)
```

**Total: 82 files** with clean code architecture!

## 🔗 API Endpoints

### Core Resources

| Resource | Endpoint | Description |
|----------|----------|-------------|
| Users | `/api/users` | User management |
| Categories | `/api/categories` | Shop categories |
| Shops | `/api/shops` | Shop CRUD & search |
| Menus | `/api/menus` | Menu management |
| Menu Items | `/api/menu-items` | Food items with filters |
| Reviews | `/api/reviews` | User reviews & ratings |
| Blogs | `/api/blogs` | Food blogging |
| Comments | `/api/comments` | Blog comments |

### Featured Endpoints

🔍 **Geospatial Search:**
```
GET /api/shops/nearby?latitude=10.7769&longitude=106.7009&radius=5000
```

⭐ **Average Rating:**
```
GET /api/reviews/shop/{shopId}/average-rating
```

🔥 **Hot Items:**
```
GET /api/menu-items/hot
```

📊 **Top Viewed:**
```
GET /api/menu-items/top-viewed
```

💖 **Top Liked Blogs:**
```
GET /api/blogs/top-liked
```

👉 Full API reference: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## 🎨 Key Features

### ✅ Implemented

- ✅ **CRUD Operations** for all entities
- ✅ **Clean Architecture** with proper layer separation
- ✅ **DTO Pattern** with validation
- ✅ **Global Exception Handling**
- ✅ **Swagger/OpenAPI Documentation**
- ✅ **Geospatial Queries** (PostGIS)
- ✅ **JSONB Support** for flexible data
- ✅ **Nested Comments/Replies** with Reply model
- ✅ **Auto Data Initialization**
- ✅ **Search & Filtering**
- ✅ **Aggregation Queries** (avg rating, counts)
- ✅ **View/Like Tracking**

### 🚧 Future Enhancements

- Pagination & Sorting
- JWT Authentication
- Role-based Authorization
- File Upload Service
- Redis Caching
- Rate Limiting
- Email Notifications
- Real-time Updates (WebSocket)
- Unit & Integration Tests

## 🧪 Testing Examples

### cURL Commands

**Get all shops:**
```bash
curl http://localhost:8080/api/shops
```

**Create a user:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "fullName": "Test User",
    "email": "test@example.com",
    "phone": "0900000000"
  }'
```

**Search shops:**
```bash
curl "http://localhost:8080/api/shops/search?name=coffee"
```

## 🗄️ Database Schema

**8 Main Tables:**

1. `users` - User accounts (local/OAuth)
2. `categories` - Shop categories
3. `shops` - F&B shops with geolocation
4. `menus` - Shop menus
5. `menu_items` - Food/drink items
6. `reviews` - User reviews with replies
7. `blogs` - Food blogs with likes
8. `comments` - Blog comments with replies

**PostgreSQL Features:**
- JSONB columns for flexible data (images, replies)
- PostGIS for geospatial queries
- Auto-incrementing IDs
- Foreign key constraints
- Timestamp tracking (created_at, updated_at)

## 🔒 Security

**Current Configuration (Development):**
- CSRF: Disabled
- CORS: Allow all origins
- Authentication: Basic Auth
- Authorization: All endpoints public

⚠️ **Production Recommendations:**
- Enable CSRF protection
- Configure specific CORS origins
- Implement JWT authentication
- Add role-based access control
- Enable HTTPS

## 📊 Error Handling

All errors return consistent format:

```json
{
  "timestamp": "2026-02-21T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Shop not found with id: 123",
  "path": "/api/shops/123"
}
```

**HTTP Status Codes:**
- 200: Success
- 201: Created
- 204: No Content
- 400: Bad Request / Validation Failed
- 404: Not Found
- 409: Conflict (Duplicate)
- 500: Server Error

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Nguyen Huu Loc** - *Initial work*

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- PostgreSQL community for robust database
- All open-source contributors

---

Made with ❤️ using Spring Boot

**Star ⭐ this repository if you find it helpful!**
