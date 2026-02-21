**# 📑 TÀI LIỆU ĐẶC TẢ USE CASE HỆ THỐNG

## I. Danh Sách Actors

* 👤 Guest: Người dùng vãng lai, chưa đăng nhập.
* 🔐 User: Người dùng đã có tài khoản và đã đăng nhập.
* 👨‍🍳 Chủ quán: Người sở hữu và quản lý thông tin quán ăn.
* Admin hệ thống.
* 🤖 Hệ thống (System): Thành phần xử lý logic và dữ liệu.

## ---

II. Nhóm Use Case Hệ Thống (Auth)

### 🔹 UC-01: Đăng ký tài khoản

| Thuộc tính       | Mô tả                                                       |
| ------------------ | ------------------------------------------------------------- |
| Actor chính       | Guest                                                         |
| Tiền điều kiện | Người dùng chưa có tài khoản.                          |
| Hậu điều kiện  | Tài khoản được tạo và lưu thành công vào Database. |

Luồng xử lý:

1. Main Flow:

* (1) Guest chọn “Đăng ký”.
* (2) Hệ thống hiển thị form (Email, Mật khẩu, Thông tin cá nhân).
* (3) Guest nhập dữ liệu và nhấn “Xác nhận”.
* (4) Hệ thống kiểm tra tính hợp lệ của dữ liệu.
* (5) Hệ thống lưu tài khoản và thông báo thành công.

2. Alternative Flow:

* (5a) Email đã tồn tại: Hệ thống hiển thị thông báo lỗi và yêu cầu nhập lại.

### 🔹 UC-02: Đăng nhập

| Thuộc tính       | Mô tả                                                                  |
| ------------------ | ------------------------------------------------------------------------ |
| Actor chính       | User / Chủ quán                                                        |
| Tiền điều kiện | Đã có tài khoản trên hệ thống.                                   |
| Hậu điều kiện  | Phiên đăng nhập được thiết lập, chuyển hướng tới Dashboard. |

Luồng xử lý:

1. Main Flow: Người dùng nhập Email/Password $\rightarrow$ Hệ thống xác thực $\rightarrow$ Đăng nhập thành công $\rightarrow$ Chuyển đến Dashboard.
2. Alternative Flow: (2a) Sai thông tin: Hệ thống báo lỗi "Sai mật khẩu hoặc email".

### 🔹 UC-03: Đăng xuất

* Actor: User / Chủ quán.
* Main Flow: Người dùng chọn “Đăng xuất” $\rightarrow$ Hệ thống hủy Session/Token $\rightarrow$ Chuyển về trang chủ.

## ---

III. Nhóm Use Case Nghiệp Vụ (CRUD)

### 🔹 UC-04: Quản lý Quán Ăn (Dành cho Chủ quán)

| ID  | Thao tác | Actor      | Mô tả luồng chính                                                                                |
| --- | --------- | ---------- | ---------------------------------------------------------------------------------------------------- |
| 04A | Create    | Chủ quán | Chọn "Thêm quán"$\rightarrow$ Nhập Data (Tên, địa chỉ, ảnh...) $\rightarrow$ Lưu.      |
| 04B | Read      | Guest/User | Truy cập trang danh sách$\rightarrow$ Hệ thống render dữ liệu quán ăn.                     |
| 04C | Update    | Chủ quán | Chọn "Chỉnh sửa" tại quán của mình$\rightarrow$ Cập nhật thông tin $\rightarrow$ Lưu. |
| 04D | Delete    | Chủ quán | Chọn "Xóa"$\rightarrow$ Xác nhận Popup $\rightarrow$ Hệ thống xóa logic/vật lý.         |

### 🔹 UC-05: Quản lý Review (Đánh giá)

* Tạo Review: User đăng nhập $\rightarrow$ Chọn quán $\rightarrow$ Nhập nội dung & số sao $\rightarrow$ Gửi.
* Cập nhật/Xóa: Chỉ áp dụng cho Review do chính User đó tạo ra.

### 🔹 UC-06: Quản lý Blog

* Quy trình: Tương tự CRUD Quán ăn nhưng áp dụng cho đối tượng bài viết (Blog Post). User có quyền tạo, đọc, chỉnh sửa và xóa bài viết của chính mình.

## ---

IV. Nhóm Tính Năng Thông Minh & Tìm Kiếm

### 🔍 UC-07: Tìm kiếm & Bộ lọc (Filter)

* Input: Từ khóa (Keyword), Danh mục (Category), Khu vực (Location).
* Process: Hệ thống truy vấn Database bằng câu lệnh SELECT có điều kiện.
* Output: Danh sách các quán ăn thỏa mãn tiêu chí.

### 🤖 UC-08: Chatbot Gợi Ý

* Mô tả: Sử dụng AI để hỗ trợ tìm kiếm theo ngữ cảnh.
* Luồng chính:

1. User nhập yêu cầu (VD: "Tìm quán phở ngon quận 1").
2. Chatbot phân tích ý định (Intent) và thực thể (Entity).
3. Hệ thống trả về danh sách gợi ý kèm link chi tiết.

### 1. Vai trò của Admin

Bạn đã thêm Admin vào danh sách Actor nhưng chưa có Use Case cụ thể. Với web địa phương, Admin cực kỳ quan trọng để:

* **Duyệt nội dung:** Kiểm soát việc chủ quán đăng thông tin sai sự thật hoặc User đăng Review phản cảm (UC-09: Phê duyệt nội dung).
* **Quản lý tài khoản:** Khóa các tài khoản vi phạm (UC-10: Quản trị người dùng).
* **Quản lý danh mục:** Thêm/Sửa/Xóa các loại hình quán ăn (Phở, Cơm tấm, Cafe...) để đồng bộ bộ lọc (Filter).

### trong Logic nghiệp vụ (Business Logic)

* **Quản lý Menu (Thực đơn):** Một quán ăn không chỉ có thông tin chung, User cần xem  **Danh sách món ăn + Giá cả** . Bạn nên thêm UC-04E: Quản lý thực đơn (CRUD món ăn thuộc quán).
* **Tương tác với Review:** Hiện tại User mới chỉ đăng bài. Một hệ thống "sống" thường cần thêm:
  * **Reaction:** Like/Dislike đánh giá.
  * **Phản hồi:** Chủ quán phản hồi lại đánh giá của khách (quan trọng cho web quảng bá).
* **Yêu thích (Wishlist/Bookmark):** User cần lưu lại các quán "ruột" để xem lại sau (UC-11: Lưu quán ăn yêu thích).

### 3. Phần kỹ thuật & Trải nghiệm người dùng

* **Vị trí (Map Integration):** Quảng bá địa phương mà thiếu bản đồ là một thiếu sót lớn. Cần có Use Case hiển thị vị trí quán trên Google Maps/OpenStreetMap.
* **Xác thực nâng cao:** Quên mật khẩu (Forgot Password) là tính năng bắt buộc phải có trong luồng Auth để đảm bảo tính thực tế.

**
