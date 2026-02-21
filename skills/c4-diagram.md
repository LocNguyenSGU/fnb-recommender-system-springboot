# C4 Diagram - Hệ Thống Web Địa Phương với Chatbot

## Level 1: System Context Diagram

```mermaid
C4Context
    title System Context Diagram - Hệ Thống Web Địa Phương

    Person(guest, "Guest", "Người dùng vãng lai")
    Person(user, "User", "Người dùng đã đăng ký")
    Person(owner, "Chủ Quán", "Quản lý quán ăn")
    Person(admin, "Admin", "Quản trị hệ thống")

    System(webSystem, "Web Địa Phương", "Hệ thống tìm kiếm và đánh giá quán ăn địa phương với AI chatbot")

    System_Ext(googleMaps, "Google Maps API", "Hiển thị vị trí quán ăn")
    System_Ext(cloudStorage, "Cloud Storage", "Lưu trữ hình ảnh")
    System_Ext(aiService, "AI Service", "OpenAI/Gemini cho chatbot")
    System_Ext(emailService, "Email Service", "Gửi email xác thực")

    Rel(guest, webSystem, "Tìm kiếm quán, xem review")
    Rel(user, webSystem, "Đăng review, lưu yêu thích, chat với AI")
    Rel(owner, webSystem, "Quản lý quán, menu, phản hồi review")
    Rel(admin, webSystem, "Duyệt nội dung, quản lý user")

    Rel(webSystem, googleMaps, "Lấy bản đồ, geocoding")
    Rel(webSystem, cloudStorage, "Upload/Download ảnh")
    Rel(webSystem, aiService, "Gọi API chatbot")
    Rel(webSystem, emailService, "Gửi email")
```

## Level 2: Container Diagram

```mermaid
C4Container
    title Container Diagram - Kiến Trúc Hệ Thống

    Person(user, "User", "Người dùng")
    Person(owner, "Chủ Quán", "Quản lý quán")
    Person(admin, "Admin", "Quản trị viên")

    Container_Boundary(webSystem, "Web Địa Phương System") {
        Container(spa, "Web Application", "React/Next.js", "Giao diện người dùng, responsive design")
        Container(api, "API Gateway", "Node.js/Express", "REST API cho tất cả nghiệp vụ")
        Container(chatbotService, "Chatbot Service", "Python/Node.js", "Xử lý AI chatbot, NLP, intent recognition")
        Container(searchService, "Search Service", "Elasticsearch/PostgreSQL FTS", "Full-text search cho quán và món")
        ContainerDb(db, "Database", "PostgreSQL", "Lưu users, restaurants, menus, reviews, blogs")
        ContainerDb(cache, "Cache", "Redis", "Session, rate limiting, cache data")
        Container(fileStorage, "File Storage", "S3/Cloudinary", "Lưu trữ ảnh quán, menu, review")
    }

    System_Ext(googleMaps, "Google Maps", "Map integration")
    System_Ext(aiService, "OpenAI/Gemini", "LLM cho chatbot")
    System_Ext(emailService, "SendGrid/SES", "Email service")

    Rel(user, spa, "Sử dụng", "HTTPS")
    Rel(owner, spa, "Quản lý quán", "HTTPS")
    Rel(admin, spa, "Quản trị", "HTTPS")

    Rel(spa, api, "Gọi API", "JSON/HTTPS")
    Rel(api, chatbotService, "Xử lý chat", "gRPC/HTTP")
    Rel(api, searchService, "Tìm kiếm", "HTTP")
    Rel(api, db, "Read/Write", "SQL")
    Rel(api, cache, "Cache/Session", "Redis Protocol")
    Rel(api, fileStorage, "Upload/Download", "S3 API")
    
    Rel(chatbotService, aiService, "Gọi LLM", "HTTPS")
    Rel(chatbotService, db, "Lấy context data", "SQL")
    Rel(searchService, db, "Index/Query", "SQL")
    
    Rel(api, googleMaps, "Geocoding/Maps")
    Rel(api, emailService, "Gửi email")

    UpdateRelStyle(user, spa, $offsetY="-50")
    UpdateRelStyle(spa, api, $offsetX="-50")
```

## Level 3: Component Diagram - API Gateway

```mermaid
C4Component
    title Component Diagram - API Gateway

    Container(spa, "Web Application", "React/Next.js")
    ContainerDb(db, "Database", "PostgreSQL")
    Container(chatbot, "Chatbot Service", "AI Service")
    Container(cache, "Redis Cache", "Redis")

    Container_Boundary(api, "API Gateway") {
        Component(authController, "Auth Controller", "Controller", "Đăng ký, đăng nhập, forgot password")
        Component(restaurantController, "Restaurant Controller", "Controller", "CRUD quán ăn, filter, search")
        Component(menuController, "Menu Controller", "Controller", "CRUD menu items")
        Component(reviewController, "Review Controller", "Controller", "CRUD reviews, reactions, replies")
        Component(blogController, "Blog Controller", "Controller", "CRUD blogs, comments")
        Component(chatbotController, "Chatbot Controller", "Controller", "Chat endpoint, streaming")
        Component(userController, "User Controller", "Controller", "Profile, bookmarks, preferences")
        Component(adminController, "Admin Controller", "Controller", "Duyệt nội dung, quản lý users")
        
        Component(authMiddleware, "Auth Middleware", "Middleware", "JWT validation, role check")
        Component(rateLimiter, "Rate Limiter", "Middleware", "Chống spam API")
        Component(validator, "Request Validator", "Middleware", "Validate input")
        
        Component(restaurantService, "Restaurant Service", "Service", "Business logic quán ăn")
        Component(reviewService, "Review Service", "Service", "Logic review, tính avg rating")
        Component(searchService, "Search Service", "Service", "Full-text search logic")
        Component(notificationService, "Notification Service", "Service", "Gửi thông báo")
    }

    Rel(spa, authController, "Login/Register")
    Rel(spa, restaurantController, "CRUD quán")
    Rel(spa, menuController, "CRUD menu")
    Rel(spa, reviewController, "CRUD review")
    Rel(spa, blogController, "CRUD blog")
    Rel(spa, chatbotController, "Chat với AI")
    
    Rel(authController, authMiddleware, "Validate token")
    Rel(restaurantController, authMiddleware, "Check auth")
    Rel(chatbotController, rateLimiter, "Limit requests")
    
    Rel(restaurantController, restaurantService, "Gọi logic")
    Rel(reviewController, reviewService, "Xử lý review")
    Rel(restaurantController, searchService, "Tìm kiếm")
    
    Rel(restaurantService, db, "Query restaurants")
    Rel(reviewService, db, "Query/Update reviews")
    Rel(authMiddleware, cache, "Check session")
    Rel(chatbotController, chatbot, "Gọi AI")
    Rel(reviewService, notificationService, "Thông báo chủ quán")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Level 3: Component Diagram - Chatbot Service

```mermaid
C4Component
    title Component Diagram - Chatbot Service

    Container(api, "API Gateway", "Node.js")
    ContainerDb(db, "Database", "PostgreSQL")
    System_Ext(aiService, "OpenAI/Gemini", "LLM API")

    Container_Boundary(chatbotService, "Chatbot Service") {
        Component(chatController, "Chat Controller", "FastAPI/Express", "Nhận chat requests")
        Component(intentRecognizer, "Intent Recognizer", "NLP", "Phân tích ý định user: tìm quán, hỏi món, lọc theo khu vực...")
        Component(entityExtractor, "Entity Extractor", "NER", "Trích xuất: tên món, địa điểm, giá, rating...")
        Component(contextManager, "Context Manager", "Service", "Quản lý ngữ cảnh hội thoại")
        Component(queryGenerator, "Query Generator", "Service", "Tạo SQL query từ intent + entities")
        Component(responseFormatter, "Response Formatter", "Service", "Format kết quả thành câu trả lời tự nhiên")
        Component(llmProxy, "LLM Proxy", "Service", "Gọi OpenAI/Gemini với prompt engineering")
        Component(cacheLayer, "Conversation Cache", "Redis", "Cache lịch sử chat, context")
    }

    Rel(api, chatController, "POST /chat", "JSON")
    
    Rel(chatController, intentRecognizer, "Phân tích input")
    Rel(intentRecognizer, llmProxy, "Gọi LLM nếu cần")
    Rel(intentRecognizer, entityExtractor, "Trích xuất entities")
    
    Rel(entityExtractor, contextManager, "Lưu context")
    Rel(contextManager, cacheLayer, "Cache conversation")
    
    Rel(entityExtractor, queryGenerator, "Generate query")
    Rel(queryGenerator, db, "Execute SQL", "SQL")
    
    Rel(queryGenerator, responseFormatter, "Trả kết quả")
    Rel(responseFormatter, llmProxy, "Tạo response tự nhiên")
    Rel(llmProxy, aiService, "API call", "HTTPS")
    
    Rel(responseFormatter, chatController, "Return response")
    Rel(chatController, api, "JSON response")

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

## Luồng Xử Lý Chatbot

```mermaid
sequenceDiagram
    participant User
    participant Web as Web App
    participant API as API Gateway
    participant Chat as Chatbot Service
    participant LLM as OpenAI/Gemini
    participant DB as PostgreSQL

    User->>Web: "Tìm quán phở ngon quận 1 giá dưới 50k"
    Web->>API: POST /api/chat
    API->>Chat: Forward request
    
    Chat->>Chat: Intent Recognition
    Note over Chat: Intent: SEARCH_RESTAURANT<br/>Entities: {<br/>  dish: "phở",<br/>  district: "quận 1",<br/>  price_max: 50000<br/>}
    
    Chat->>DB: SELECT * FROM restaurants r<br/>JOIN menu_items m ON r.id = m.restaurant_id<br/>WHERE r.district = 'Quận 1'<br/>AND m.name ILIKE '%phở%'<br/>AND m.price < 50000
    DB-->>Chat: [List of restaurants]
    
    Chat->>LLM: Format response với prompt:<br/>"User hỏi: {...}<br/>Data: {...}<br/>Trả lời tự nhiên"
    LLM-->>Chat: "Tôi tìm được 5 quán phở..."
    
    Chat-->>API: JSON response + restaurant links
    API-->>Web: Display chatbot response
    Web-->>User: Hiển thị kết quả + danh sách quán
```
