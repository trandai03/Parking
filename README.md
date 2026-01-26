# 🚗 Hệ thống Quản lý Bãi đỗ xe (Parking Management System)

## 📋 Mô tả dự án

Hệ thống quản lý bãi đỗ xe là một ứng dụng backend được phát triển bằng Spring Boot, cung cấp các API để quản lý toàn bộ quy trình hoạt động của bãi đỗ xe từ việc check-in, check-out, thanh toán đến quản lý nhân viên và thống kê doanh thu.

### 🎯 Tính năng chính

- **🔐 Quản lý người dùng**: Đăng ký, đăng nhập, xác thực email, quản lý profile
- **🚙 Quản lý xe**: Đăng ký xe, nhận diện biển số tự động bằng AI
- **🏢 Quản lý bãi đỗ xe**: Tạo, cập nhật thông tin bãi đỗ, quản lý chỗ trống
- **📱 Quản lý phiên gửi xe**: Check-in/Check-out tự động, theo dõi thời gian
- **💳 Hệ thống thanh toán**: Thanh toán online, tiền mặt, quản lý trạng thái
- **👥 Quản lý nhân viên**: Phân ca làm việc, chấm công, quản lý quyền
- **📊 Thống kê doanh thu**: Báo cáo theo ngày, tháng, bãi đỗ xe
- **🤖 Nhận diện biển số**: Tích hợp AI để nhận diện biển số xe tự động
- **🎫 Quản lý thành viên (Member)**: Đăng ký, gia hạn, khóa/mở khóa thẻ member với các gói tháng/quý/năm

## 🛠️ Công nghệ sử dụng

### Backend Framework
- **Spring Boot 3.3.4** - Framework chính
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - ORM và quản lý database
- **Spring Validation** - Validate dữ liệu đầu vào
- **Spring Mail** - Gửi email xác thực

### Database
- **MySQL** - Cơ sở dữ liệu chính
- **Hibernate** - ORM framework

### Authentication & Security
- **JWT (JSON Web Token)** - Xác thực người dùng
- **BCrypt** - Mã hóa mật khẩu
- **OAuth2** - Đăng nhập social (tùy chọn)

### API Documentation
- **Swagger/OpenAPI 3** - Tài liệu API tự động
- **SpringDoc** - Tích hợp Swagger với Spring Boot

### External Services
- **PlateRecognizer API** - Nhận diện biển số xe
- **Cloudinary** - Lưu trữ hình ảnh trên cloud
- **Mailtrap** - Dịch vụ gửi email

### Development Tools
- **Lombok** - Giảm boilerplate code
- **ModelMapper** - Mapping giữa Entity và DTO
- **Maven** - Quản lý dependencies

## 🏗️ Kiến trúc hệ thống

```
src/main/java/com/project/parking/
├── components/          # Custom components
├── config/             # Cấu hình Spring
├── controller/         # REST Controllers
├── dto/               # Data Transfer Objects
├── enums/             # Enum classes
├── exceptions/        # Custom exceptions
├── filter/            # Security filters
├── model/             # JPA Entities
├── repository/        # Data repositories
├── response/          # Response objects
├── service/           # Business logic
└── utils/             # Utility classes
```

### 📊 Cơ sở dữ liệu

#### Các bảng chính:
- **users**: Thông tin người dùng
- **vehicles**: Thông tin xe
- **parking_lots**: Thông tin bãi đỗ xe
- **parking_sessions**: Phiên gửi xe
- **payments**: Thanh toán
- **employees**: Nhân viên
- **shifts**: Ca làm việc
- **revenue_stats**: Thống kê doanh thu

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống
- **Java 17** hoặc cao hơn
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git**

### 1. Clone repository
```bash
git clone <repository-url>
cd parking
```

### 2. Cấu hình database
```sql
-- Tạo database
CREATE DATABASE parking_management;

-- Tạo user (tùy chọn)
CREATE USER 'parking_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON parking_management.* TO 'parking_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Cấu hình application.properties
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/parking_management
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret.key=your_jwt_secret_key

# Email Configuration
spring.mail.host=your_smtp_host
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_email_password

# Cloudinary Configuration
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

# PlateRecognizer API
platerecognizer.api.url=https://api.platerecognizer.com/v1/plate-reader/
platerecognizer.api.key=your_api_key
```

### 4. Cài đặt dependencies
```bash
mvn clean install
```

### 5. Chạy ứng dụng
```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Truy cập tài liệu API tại: `http://localhost:8080/swagger-ui.html`

### API Endpoints chính

#### 🔐 Authentication
- `POST /api/users/login` - Đăng nhập
- `POST /api/users/create` - Đăng ký tài khoản
- `POST /api/users/verify` - Xác thực email
- `POST /api/users/forgot-password/{email}` - Quên mật khẩu

#### 🚙 Vehicle Management
- `GET /api/vehicles` - Lấy danh sách xe
- `POST /api/vehicles` - Đăng ký xe mới
- `PUT /api/vehicles/{id}` - Cập nhật thông tin xe
- `DELETE /api/vehicles/{id}` - Xóa xe

#### 🏢 Parking Lot Management
- `GET /api/parking-lots` - Lấy danh sách bãi đỗ xe
- `POST /api/parking-lots` - Tạo bãi đỗ xe mới
- `PUT /api/parking-lots/{id}` - Cập nhật bãi đỗ xe
- `PATCH /api/parking-lots/{id}/availability` - Cập nhật chỗ trống

#### 📱 Parking Session Management
- `GET /api/parking/sessions` - Lấy danh sách phiên gửi xe
- `POST /api/parking/entry/{lotId}` - Check-in xe
- `POST /api/parking/exit/{sessionId}` - Check-out xe
- `POST /api/parking/recognize` - Nhận diện biển số

#### 💳 Payment Management
- `GET /api/payments` - Lấy danh sách thanh toán
- `POST /api/payments` - Tạo thanh toán mới
- `PATCH /api/payments/{id}/status` - Cập nhật trạng thái thanh toán

#### 👥 Employee Management
- `GET /api/employees` - Lấy danh sách nhân viên
- `POST /api/employees` - Thêm nhân viên mới
- `PUT /api/employees/{id}` - Cập nhật thông tin nhân viên

#### 🎫 Member Management (OWNER)
- `GET /api/members` - Lấy danh sách tất cả members
- `GET /api/members/{id}` - Lấy thông tin member theo ID
- `GET /api/members/code/{memberCode}` - Lấy member theo mã thẻ
- `POST /api/members/register` - Đăng ký member mới
- `PUT /api/members/{id}` - Cập nhật thông tin member
- `POST /api/members/{id}/lock` - Khóa thẻ member
- `POST /api/members/{id}/unlock` - Mở khóa thẻ member
- `POST /api/members/{id}/cancel` - Hủy thẻ member
- `POST /api/members/{id}/renew` - Gia hạn thẻ member
- `POST /api/members/search` - Tìm kiếm member (theo SĐT, biển số, mã thẻ...)
- `GET /api/members/statistics` - Thống kê members
- `GET /api/members/expiring` - Danh sách members sắp hết hạn
- `GET /api/members/pricing` - Bảng giá gói thành viên

#### 📊 Revenue Statistics
- `GET /api/revenue-stats` - Lấy thống kê doanh thu
- `GET /api/revenue-stats/parking-lot/{id}` - Thống kê theo bãi đỗ xe
- `POST /api/revenue-stats/generate/{date}` - Tạo thống kê cho ngày

## 🔧 Cấu hình môi trường

### Development
```properties
spring.profiles.active=dev
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
logging.level.org.springframework.security=DEBUG
```

### Production
```properties
spring.profiles.active=prod
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.org.springframework.security=WARN
```

## 🧪 Testing

### Chạy unit tests
```bash
mvn test
```

### Chạy integration tests
```bash
mvn verify
```

### Test coverage
```bash
mvn jacoco:report
```

## 📦 Deployment

### Build JAR file
```bash
mvn clean package
```

### Chạy JAR file
```bash
java -jar target/parking-0.0.1-SNAPSHOT.jar
```

### Docker (tùy chọn)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/parking-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔒 Bảo mật

### Authentication Flow
1. User đăng nhập với email/password
2. Server xác thực và tạo JWT token
3. Client gửi token trong header `Authorization: Bearer <token>`
4. Server validate token cho mỗi request

### Security Features
- **Password Encryption**: BCrypt hashing
- **JWT Token**: Stateless authentication
- **CORS Configuration**: Cross-origin resource sharing
- **Input Validation**: Request data validation
- **SQL Injection Prevention**: JPA/Hibernate protection

## 📊 Monitoring & Logging

### Logging Configuration
```properties
# Application logs
logging.level.com.project.parking=INFO
logging.file.name=logs/parking-app.log

# Database logs
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Health Check
- Endpoint: `GET /actuator/health`
- Database connectivity check
- External services status

## 🤝 Contributing

### Code Style
- Sử dụng **Google Java Style Guide**
- Lombok để giảm boilerplate code
- Meaningful variable và method names
- Comprehensive JavaDoc comments

### Git Workflow
1. Fork repository
2. Tạo feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -m 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Tạo Pull Request

## 🐛 Troubleshooting

### Common Issues

#### Database Connection Error
```
Caused by: java.sql.SQLException: Access denied for user
```
**Solution**: Kiểm tra username/password trong `application.properties`

#### JWT Token Invalid
```
JWT signature does not match locally computed signature
```
**Solution**: Kiểm tra `jwt.secret.key` configuration

#### PlateRecognizer API Error
```
HTTP 401: Unauthorized
```
**Solution**: Kiểm tra API key trong configuration

### Debug Mode
```properties
logging.level.com.project.parking=DEBUG
spring.jpa.show-sql=true
```

## 📞 Support

### Contact Information
- **Email**: support@parkingmanagement.com
- **Documentation**: [Wiki](link-to-wiki)
- **Issues**: [GitHub Issues](link-to-issues)

### FAQ
**Q: Làm sao để thêm bãi đỗ xe mới?**
A: Sử dụng API `POST /api/parking-lots` với thông tin đầy đủ

**Q: Hệ thống có hỗ trợ nhiều loại xe không?**
A: Có, cấu hình trong field `vehicleTypes` của ParkingLot

**Q: Làm sao để backup database?**
A: Sử dụng mysqldump hoặc các tools backup của MySQL

## 📄 License

Dự án này được phát hành dưới [MIT License](LICENSE).

---

## 🚀 Roadmap

### Version 2.0 (Planned)
- [ ] Mobile app integration
- [ ] Real-time notifications
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] Payment gateway integration (VNPay, MoMo)
- [ ] IoT sensor integration
- [ ] Machine learning for demand prediction

### Version 1.1 (In Progress)
- [x] License plate recognition
- [x] Employee shift management
- [x] Revenue statistics
- [ ] Email notifications
- [ ] Advanced reporting

---

**Developed with ❤️ by Parking Management Team**
