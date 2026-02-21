# F&B Recommender System API

Spring Boot REST API cho hệ thống đề xuất nhà hàng/quán ăn với quản lý shop, menu, đánh giá và blog.

## 🚀 Công Nghệ

- **Spring Boot 4.0.3** - Framework chính
- **Java 21** - LTS version
- **PostgreSQL** - Database với PostGIS extension
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM
- **ModelMapper 3.2.0** - DTO/Entity mapping
- **Lombok** - Giảm boilerplate code
- **SpringDoc OpenAPI 2.3.0** - Swagger documentation
- **Jakarta Validation** - Request validation

## 📦 Cấu Trúc Dự Án

```
src/main/java/com/example/demo/
├── config/              # Các file cấu hình
│   ├── CorsConfig.java
│   ├── JpaConfig.java
│   ├── MapperConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/          # REST Controllers (8 files)
│   ├── UserController.java
│   ├── CategoryController.java
│   ├── ShopController.java
│   ├── MenuController.java
│   ├── MenuItemController.java
│   ├── ReviewController.java
│   ├── BlogController.java
│   └── CommentController.java
├── dto/
│   ├── request/        # Request DTOs (8 files)
│   └── response/       # Response DTOs (9 files + PagedResponse)
├── exception/          # Custom exceptions (7 files)
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── BadRequestException.java
│   ├── InvalidInputException.java
│   ├── UnauthorizedException.java
│   ├── ForbiddenException.java
│   ├── ServiceUnavailableException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── mapper/             # Entity/DTO Mappers (9 files)
├── model/              # JPA Entities (9 files)
│   ├── User.java
│   ├── Category.java
│   ├── Shop.java
│   ├── Menu.java
│   ├── MenuItem.java
│   ├── Review.java
│   ├── Blog.java
│   ├── Comment.java
│   └── Reply.java
├── repository/         # JPA Repositories (8 files)
└── service/
    ├── [Interface]/    # Service interfaces (8 files)
    └── impl/           # Service implementations (8 files)
```

## 🛠️ Cài Đặt & Chạy

### 1. Yêu cầu
- Java 21+
- PostgreSQL 15+
- Maven 3.9+

### 2. Cấu hình Database

```sql
-- Tạo database
CREATE DATABASE fnb_recommender_db;

-- Kích hoạt PostGIS extension (cho tìm kiếm theo khoảng cách)
CREATE EXTENSION IF NOT EXISTS postgis;
```

### 3. Cấu hình Application

Chỉnh sửa `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fnb_recommender_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Compile & Run

```bash
# Compile project
mvn clean compile

# Run application
mvn spring-boot:run
```

Server sẽ chạy tại: `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Truy cập Swagger UI tại: **http://localhost:8080/swagger-ui.html**

### OpenAPI JSON
API docs JSON: **http://localhost:8080/api-docs**

## 🔗 API Endpoints

### 👤 Users - `/api/users`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/users` | Lấy tất cả users |
| GET | `/api/users/{id}` | Lấy user theo ID |
| GET | `/api/users/username/{username}` | Lấy user theo username |
| GET | `/api/users/email/{email}` | Lấy user theo email |
| POST | `/api/users` | Tạo user mới |
| PUT | `/api/users/{id}` | Cập nhật user |
| DELETE | `/api/users/{id}` | Xóa user |
| GET | `/api/users/exists/username/{username}` | Kiểm tra username tồn tại |
| GET | `/api/users/exists/email/{email}` | Kiểm tra email tồn tại |

### 📂 Categories - `/api/categories`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/categories` | Lấy tất cả categories |
| GET | `/api/categories/{id}` | Lấy category theo ID |
| GET | `/api/categories/name/{name}` | Lấy category theo tên |
| POST | `/api/categories` | Tạo category mới |
| PUT | `/api/categories/{id}` | Cập nhật category |
| DELETE | `/api/categories/{id}` | Xóa category |

### 🏪 Shops - `/api/shops`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/shops` | Lấy tất cả shops |
| GET | `/api/shops/{id}` | Lấy shop theo ID |
| GET | `/api/shops/owner/{ownerId}` | Lấy shops theo chủ shop |
| GET | `/api/shops/category/{categoryId}` | Lấy shops theo category |
| GET | `/api/shops/status/{status}` | Lấy shops theo trạng thái |
| GET | `/api/shops/search?name={name}` | Tìm shops theo tên |
| GET | `/api/shops/nearby?lat={lat}&lng={lng}&radius={r}` | Tìm shops trong bán kính |
| POST | `/api/shops` | Tạo shop mới |
| PUT | `/api/shops/{id}` | Cập nhật shop |
| DELETE | `/api/shops/{id}` | Xóa shop |

### 📋 Menus - `/api/menus`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/menus` | Lấy tất cả menus |
| GET | `/api/menus/{id}` | Lấy menu theo ID |
| GET | `/api/menus/shop/{shopId}` | Lấy menus theo shop |
| GET | `/api/menus/search?name={name}` | Tìm menus theo tên |
| POST | `/api/menus` | Tạo menu mới |
| PUT | `/api/menus/{id}` | Cập nhật menu |
| DELETE | `/api/menus/{id}` | Xóa menu |

### 🍔 Menu Items - `/api/menu-items`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/menu-items` | Lấy tất cả menu items |
| GET | `/api/menu-items/{id}` | Lấy menu item theo ID |
| GET | `/api/menu-items/menu/{menuId}` | Lấy items theo menu |
| GET | `/api/menu-items/menu/{menuId}/available` | Lấy items available theo menu |
| GET | `/api/menu-items/hot` | Lấy món hot |
| GET | `/api/menu-items/signature` | Lấy món signature |
| GET | `/api/menu-items/top-viewed` | Lấy món được xem nhiều nhất |
| GET | `/api/menu-items/search?name={name}` | Tìm items theo tên |
| POST | `/api/menu-items` | Tạo menu item mới |
| PUT | `/api/menu-items/{id}` | Cập nhật menu item |
| DELETE | `/api/menu-items/{id}` | Xóa menu item |
| POST | `/api/menu-items/{id}/view` | Tăng lượt xem |

### ⭐ Reviews - `/api/reviews`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/reviews` | Lấy tất cả reviews |
| GET | `/api/reviews/{id}` | Lấy review theo ID |
| GET | `/api/reviews/shop/{shopId}` | Lấy reviews theo shop |
| GET | `/api/reviews/user/{userId}` | Lấy reviews theo user |
| GET | `/api/reviews/shop/{shopId}/average-rating` | Lấy rating trung bình |
| GET | `/api/reviews/shop/{shopId}/count` | Đếm số reviews |
| POST | `/api/reviews` | Tạo review mới |
| PUT | `/api/reviews/{id}` | Cập nhật review |
| DELETE | `/api/reviews/{id}` | Xóa review |

### 📝 Blogs - `/api/blogs`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/blogs` | Lấy tất cả blogs |
| GET | `/api/blogs/{id}` | Lấy blog theo ID |
| GET | `/api/blogs/author/{authorId}` | Lấy blogs theo tác giả |
| GET | `/api/blogs/status/{status}` | Lấy blogs theo trạng thái |
| GET | `/api/blogs/search?title={title}` | Tìm blogs theo tiêu đề |
| GET | `/api/blogs/top-liked` | Lấy blogs được like nhiều nhất |
| POST | `/api/blogs` | Tạo blog mới |
| PUT | `/api/blogs/{id}` | Cập nhật blog |
| DELETE | `/api/blogs/{id}` | Xóa blog |
| POST | `/api/blogs/{id}/like` | Tăng lượt like |

### 💬 Comments - `/api/comments`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/comments` | Lấy tất cả comments |
| GET | `/api/comments/{id}` | Lấy comment theo ID |
| GET | `/api/comments/blog/{blogId}` | Lấy comments theo blog |
| GET | `/api/comments/user/{userId}` | Lấy comments theo user |
| POST | `/api/comments` | Tạo comment mới |
| PUT | `/api/comments/{id}` | Cập nhật comment |
| DELETE | `/api/comments/{id}` | Xóa comment |

## 📊 Database Schema

Hệ thống sử dụng 8 bảng chính:

1. **users** - Thông tin người dùng (local/OAuth)
2. **categories** - Danh mục quán ăn
3. **shops** - Thông tin các quán
4. **menus** - Menu của quán
5. **menu_items** - Món ăn trong menu
6. **reviews** - Đánh giá của user cho shop
7. **blogs** - Bài viết về ẩm thực
8. **comments** - Bình luận trên blog

Các trường JSONB:
- `shops.images` - Danh sách ảnh quán
- `menus.images` - Ảnh bìa menu
- `menu_items.images` - Ảnh món ăn
- `reviews.replies` - Phản hồi của chủ quán
- `blogs.images` - Ảnh trong blog
- `comments.replies` - Trả lời comments

## 🔒 Security

- CSRF: Disabled (cho development)
- Authentication: Basic Auth (admin/admin)
- Authorization: Tất cả endpoints đều public (cần cấu hình thêm cho production)

## ✅ Validation

Tất cả Request DTOs đều có validation:
- `@NotBlank` - Không để trống
- `@NotNull` - Không null
- `@Email` - Format email hợp lệ
- `@Size` - Giới hạn độ dài
- `@Min/@Max` - Giới hạn giá trị số
- `@DecimalMin` - Giá trị tối thiểu cho số thập phân

## 🎯 Features

### ✅ Đã Hoàn Thành
- ✅ Clean Architecture với layer separation
- ✅ 8 REST Controllers với đầy đủ CRUD
- ✅ 16 DTOs (8 Request + 8 Response) với validation
- ✅ 8 JPA Repositories với custom queries
- ✅ 8 Services với business logic
- ✅ 9 Mappers (Entity ↔ DTO conversion)
- ✅ Global Exception Handler
- ✅ 7 Custom exceptions
- ✅ Swagger/OpenAPI documentation
- ✅ CORS configuration
- ✅ JPA Auditing (created_at, updated_at)
- ✅ JSONB support cho PostgreSQL
- ✅ PostGIS Geospatial queries
- ✅ Reply model cho nested comments/reviews

### 🔄 Có Thể Mở Rộng
- Pagination & Sorting đầy đủ
- Unit Tests
- Integration Tests
- JWT Authentication
- Role-based Authorization
- File Upload Service (cho images)
- Redis Caching
- Rate Limiting
- Email Service
- Notification System

## 🐛 Error Handling

Tất cả lỗi được handle bởi `GlobalExceptionHandler`:

| Exception | HTTP Status | Mô tả |
|-----------|-------------|-------|
| ResourceNotFoundException | 404 | Resource không tìm thấy |
| DuplicateResourceException | 409 | Resource đã tồn tại |
| BadRequestException | 400 | Request không hợp lệ |
| InvalidInputException | 400 | Input không đúng format |
| UnauthorizedException | 401 | Chưa authenticate |
| ForbiddenException | 403 | Không có quyền truy cập |
| ServiceUnavailableException | 503 | Service tạm thời không khả dụng |
| MethodArgumentNotValidException | 400 | Validation failed |
| Exception | 500 | Lỗi server chung |

Response format:
```json
{
  "timestamp": "2026-02-21T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 123",
  "path": "/api/users/123"
}
```

## 📝 License

Apache 2.0

## 👥 Author

Nguyen Huu Loc
