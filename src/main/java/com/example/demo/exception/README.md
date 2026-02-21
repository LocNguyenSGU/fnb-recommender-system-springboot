# Exception Handling Guide

## Các Exception Được Hỗ Trợ

### 1. **ResourceNotFoundException** (404 Not Found)
Dùng khi không tìm thấy resource trong database.

```java
// Cách 1: Message tự do
throw new ResourceNotFoundException("User not found");

// Cách 2: Message có cấu trúc
throw new ResourceNotFoundException("User", "id", userId);
// Output: "User not found with id: 123"
```

### 2. **DuplicateResourceException** (409 Conflict)
Dùng khi tạo resource đã tồn tại (duplicate key, unique constraint).

```java
// Cách 1: Message tự do
throw new DuplicateResourceException("Email already exists");

// Cách 2: Message có cấu trúc
throw new DuplicateResourceException("User", "email", email);
// Output: "User already exists with email: test@example.com"
```

### 3. **BadRequestException** (400 Bad Request)
Dùng khi request không hợp lệ về mặt logic.

```java
throw new BadRequestException("Invalid date range: start date must be before end date");

// Với cause
throw new BadRequestException("Failed to parse JSON", jsonException);
```

### 4. **InvalidInputException** (400 Bad Request)  
Dùng khi input data không hợp lệ (ngoài validation annotation).

```java
// Cách 1: Message tự do
throw new InvalidInputException("Price cannot be negative");

// Cách 2: Message có cấu trúc
throw new InvalidInputException("rating", "must be between 1 and 5");
// Output: "Invalid rating: must be between 1 and 5"
```

### 5. **UnauthorizedException** (401 Unauthorized)
Dùng khi user chưa đăng nhập hoặc token không hợp lệ.

```java
// Mặc định
throw new UnauthorizedException();
// Output: "Unauthorized - Authentication required"

// Custom message
throw new UnauthorizedException("Invalid or expired token");
```

### 6. **ForbiddenException** (403 Forbidden)
Dùng khi user đã đăng nhập nhưng không có quyền truy cập.

```java
// Mặc định
throw new ForbiddenException();
// Output: "Forbidden - You don't have permission to access this resource"

// Custom message
throw new ForbiddenException("Only shop owners can update menu items");
```

### 7. **ServiceUnavailableException** (503 Service Unavailable)
Dùng khi service external không khả dụng.

```java
// Cách 1: Message tự do
throw new ServiceUnavailableException("Payment service is down");

// Cách 2: Message có cấu trúc
throw new ServiceUnavailableException("Payment Gateway", "connection timeout");
// Output: "Payment Gateway service is unavailable: connection timeout"
```

## Ví Dụ Sử Dụng Trong Service

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        // Check duplicate
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }
        
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("User", "username", dto.getUsername());
        }
        
        User user = mapToEntity(dto);
        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }
    
    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Check permission (giả sử có SecurityContext)
        if (!hasPermission(user)) {
            throw new ForbiddenException("You can only update your own profile");
        }
        
        updateEntityFromDTO(user, dto);
        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }
}
```

## Response Format

### Success Response (2xx)
```json
{
  "success": true,
  "message": "User created successfully",
  "data": { ... },
  "timestamp": "2026-02-21T15:30:00"
}
```

### Error Response (4xx, 5xx)
```json
{
  "timestamp": "2026-02-21T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 123",
  "path": "/api/users/123"
}
```

### Validation Error (400)
```json
{
  "timestamp": "2026-02-21T15:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Invalid email format",
    "username": "Username is required"
  },
  "path": "/api/users"
}
```

## Best Practices

1. **Dùng exception phù hợp với HTTP status**
   - 404: ResourceNotFoundException
   - 409: DuplicateResourceException
   - 400: BadRequestException, InvalidInputException
   - 401: UnauthorizedException
   - 403: ForbiddenException
   - 503: ServiceUnavailableException

2. **Message rõ ràng, hữu ích**
   ```java
   // ❌ Bad
   throw new ResourceNotFoundException("Not found");
   
   // ✅ Good
   throw new ResourceNotFoundException("Shop", "id", shopId);
   ```

3. **Validation ưu tiên dùng annotation**
   ```java
   // Dùng @NotBlank, @Email, @Min, @Max trong DTO
   // Chỉ dùng InvalidInputException cho logic phức tạp
   ```

4. **Log exception ở service layer nếu cần debug**
   ```java
   try {
       // some logic
   } catch (Exception e) {
       log.error("Error processing order: {}", e.getMessage());
       throw new BadRequestException("Failed to process order", e);
   }
   ```
