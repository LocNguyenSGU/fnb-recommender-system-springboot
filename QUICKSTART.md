# F&B Recommender System - Quick Start Guide

## 📋 Prerequisites

- Java 21+
- PostgreSQL 15+
- Maven 3.9+

## 🚀 Quick Start

### 1. Create Database

```bash
# Create PostgreSQL database
createdb fnb_recommender_db

# OR using psql
psql -U postgres
CREATE DATABASE fnb_recommender_db;
\q
```

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fnb_recommender_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 3. Run Application

```bash
cd /Users/nguyenhuuloc/Downloads/demo2

# Compile project
mvn clean compile

# Run application
mvn spring-boot:run
```

Server will start at: **http://localhost:8080**

### 4. Access Swagger UI

Open your browser: **http://localhost:8080/swagger-ui.html**

## 🎯 Sample Data

When you run the application for the first time, the system will automatically create sample data:

### 👥 Users (13 users created)

**Admin:**
- Username: `admin`
- Password: `admin123`
- Role: admin

**Shop Owners (3 users):**
- Username: `owner_cafe` / Password: `password123` / Role: owner
- Username: `owner_restaurant` / Password: `password123` / Role: owner
- Username: `owner_fastfood` / Password: `password123` / Role: owner

**Regular Users (9 users):**
- Username: `user1` - `user9`
- Password: `password123`
- Role: user

### 📂 Categories (7 categories)
- Cà phê
- Quán ăn Việt
- Fast Food
- Lẩu & Nướng
- Bánh & Tráng miệng
- Ẩm thực Á
- Nhà hàng sang trọng

### 🏪 Shops (7 shops)
1. **Highlands Coffee Nguyễn Huệ** - Cà phê
2. **The Coffee House Lý Tự Trọng** - Cà phê
3. **Phở 24 Pasteur** - Quán ăn Việt
4. **Cơm Tấm Sườn Nướng Ba Ghiền** - Quán ăn Việt
5. **Burger King Vincom** - Fast Food
6. **Lẩu Phan Đình Phùng** - Lẩu & Nướng
7. **ABC Bakery Hai Bà Trưng** - Bánh & Tráng miệng

### 📋 Menu Items
- **Coffee shops**: 6 items each (Cà phê đen, Cà phê sữa, Bạc xỉu, Cappuccino, Trà sữa, Smoothie)
- **Phở restaurant**: 6 items (Phở bò tái, Phở bò chín, Phở gà, Phở đặc biệt, Nem rán, Gỏi cuốn)
- **Cơm tấm shop**: 4 items (Cơm tấm sườn nướng, Cơm tấm bì, Cơm tấm đặc biệt, Cơm tấm chả)
- **Fast food**: 6 items (Whopper, Cheese Burger, Chicken Burger, French Fries, Onion Rings, Pepsi)
- **Hot pot**: 4 items (Lẩu Thái, Lẩu bò, Lẩu hải sản, Combo rau)
- **Bakery**: 4 items (Bánh mì sandwich, Croissant, Mousse, Tiramisu)

### ⭐ Reviews (8 reviews)
- Users have reviewed shops with ratings from 4-5 stars
- Some reviews have replies from shop owners

### 📝 Blogs (4 blogs)
- "Top 10 quán cà phê đẹp nhất Sài Gòn"
- "Bí quyết chọn phở ngon tại Sài Gòn"
- "Review chuỗi fast food mới tại Việt Nam"
- "Ẩm thực đường phố Sài Gòn - Nét văn hóa đặc sắc"

### 💬 Comments (5 comments)
- Comments on blogs with nested replies

## 🧪 Testing API

### Example API Calls

**1. Get all users:**
```bash
curl http://localhost:8080/api/users
```

**2. Get all shops:**
```bash
curl http://localhost:8080/api/shops
```

**3. Find shops near location (latitude, longitude, radius in meters):**
```bash
curl "http://localhost:8080/api/shops/nearby?latitude=10.7769&longitude=106.7009&radius=5000"
```

**4. Get menu items by menu ID:**
```bash
curl http://localhost:8080/api/menu-items/menu/1
```

**5. Get hot menu items:**
```bash
curl http://localhost:8080/api/menu-items/hot
```

**6. Get reviews by shop ID:**
```bash
curl http://localhost:8080/api/reviews/shop/1
```

**7. Get average rating for a shop:**
```bash
curl http://localhost:8080/api/reviews/shop/1/average-rating
```

**8. Get top liked blogs:**
```bash
curl http://localhost:8080/api/blogs/top-liked
```

**9. Search shops by name:**
```bash
curl "http://localhost:8080/api/shops/search?name=coffee"
```

**10. Create a new user:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "fullName": "New User",
    "email": "newuser@example.com",
    "phone": "0900000000"
  }'
```

## 📊 Database Reset

If you want to reset the database and recreate sample data:

```bash
# Drop and recreate database
dropdb fnb_recommender_db
createdb fnb_recommender_db

# Restart application
mvn spring-boot:run
```

The `DataInitializer` will automatically create all sample data again.

## 🔍 Key Features to Test

1. **CRUD Operations**: Try creating, reading, updating, and deleting entities
2. **Search**: Test search functionality for shops, menus, menu items
3. **Geospatial Queries**: Find shops within a radius of a location
4. **Ratings & Reviews**: Check average ratings calculation
5. **Hot & Signature Items**: Filter menu items by hot/signature flags
6. **Blogs & Comments**: Test social features
7. **Validation**: Try to create entities with invalid data (empty fields, invalid email, etc.)

## 📚 Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Documentation**: See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## 🐛 Troubleshooting

### Issue: Database connection failed
**Solution**: Check PostgreSQL is running and credentials in `application.properties`

### Issue: Port 8080 already in use
**Solution**: Change port in `application.properties`:
```properties
server.port=8081
```

### Issue: Compilation errors
**Solution**: Clean and rebuild:
```bash
mvn clean compile
```

## 📞 Support

For issues or questions, contact: support@example.com

---

Happy coding! 🎉
