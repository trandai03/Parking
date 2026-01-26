[//]: # (# Tài liệu Swagger cho API Hệ thống Quản lý Bãi đỗ xe)

[//]: # ()
[//]: # (## 1. User Controller &#40;`UserController`&#41;)

[//]: # ()
[//]: # (### 1.1 Login)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Đăng nhập",)

[//]: # (    description = "API này dùng để xác thực người dùng và trả về token đăng nhập.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Đăng nhập thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Thông tin đăng nhập không hợp lệ"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/login"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.2 Create User)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Tạo người dùng mới",)

[//]: # (    description = "API này dùng để tạo tài khoản người dùng mới trong hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Tài khoản được tạo thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ hoặc tài khoản đã tồn tại"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/create"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.3 Verify User)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Xác thực người dùng",)

[//]: # (    description = "API này dùng để xác thực tài khoản người dùng bằng mã xác nhận.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Xác thực tài khoản thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Mã xác nhận không hợp lệ hoặc đã hết hạn"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/verify"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.4 Resend Verification)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Gửi lại mã xác nhận",)

[//]: # (    description = "API này dùng để gửi lại mã xác nhận cho người dùng qua email.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Đã gửi lại mã xác nhận thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Email không tồn tại hoặc tài khoản đã được xác thực"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/resend-verification/{email}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.5 Update User)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Cập nhật thông tin người dùng",)

[//]: # (    description = "API này dùng để cập nhật thông tin cá nhân của người dùng.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Cập nhật thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "401", description = "Không có quyền truy cập"&#41;)

[//]: # (}&#41;)

[//]: # (@PutMapping&#40;"/update"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.6 Change Password)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Đổi mật khẩu",)

[//]: # (    description = "API này dùng để thay đổi mật khẩu của người dùng.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Đổi mật khẩu thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Mật khẩu cũ không đúng hoặc dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "401", description = "Không có quyền truy cập"&#41;)

[//]: # (}&#41;)

[//]: # (@PutMapping&#40;"/change-password"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.7 Delete User)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Xóa người dùng",)

[//]: # (    description = "API này dùng để xóa tài khoản người dùng khỏi hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Xóa tài khoản thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Không thể xóa tài khoản"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy tài khoản"&#41;)

[//]: # (}&#41;)

[//]: # (@DeleteMapping&#40;"/delete/{userId}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.8 Forgot Password)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Quên mật khẩu",)

[//]: # (    description = "API này dùng để gửi email đặt lại mật khẩu cho người dùng.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Đã gửi email đặt lại mật khẩu thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Email không tồn tại"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/forgot-password/{email}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 1.9 Get User Profile)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin người dùng",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của người dùng đang đăng nhập.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Có lỗi xảy ra"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "401", description = "Không có quyền truy cập"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/me"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (## 2. Vehicle Controller &#40;`VehicleController`&#41;)

[//]: # ()
[//]: # (### 2.1 Get All Vehicles)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách tất cả xe",)

[//]: # (    description = "API này dùng để lấy danh sách tất cả xe trong hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 2.2 Get Vehicle By ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin xe theo ID",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của xe theo ID.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 2.3 Get Vehicle By License Plate)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin xe theo biển số",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của xe theo biển số.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy xe với biển số đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/license-plate/{licensePlate}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 2.4 Create Vehicle)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Tạo xe mới",)

[//]: # (    description = "API này dùng để đăng ký xe mới vào hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "201", description = "Tạo xe thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy người dùng để gắn với xe"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 2.5 Update Vehicle)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Cập nhật thông tin xe",)

[//]: # (    description = "API này dùng để cập nhật thông tin của xe.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Cập nhật thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@PutMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 2.6 Delete Vehicle)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Xóa xe",)

[//]: # (    description = "API này dùng để xóa xe khỏi hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "204", description = "Xóa thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@DeleteMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (## 3. Parking Lot Controller &#40;`ParkingLotController`&#41;)

[//]: # ()
[//]: # (### 3.1 Get All Parking Lots)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách tất cả bãi đỗ xe",)

[//]: # (    description = "API này dùng để lấy danh sách tất cả bãi đỗ xe trong hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 3.2 Get Parking Lot By ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin bãi đỗ xe theo ID",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của bãi đỗ xe theo ID.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 3.3 Get Parking Lots By Status)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách bãi đỗ xe theo trạng thái",)

[//]: # (    description = "API này dùng để lấy danh sách các bãi đỗ xe với trạng thái cụ thể &#40;ACTIVE, INACTIVE, etc&#41;.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/status/{status}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 3.4 Create Parking Lot)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Tạo bãi đỗ xe mới",)

[//]: # (    description = "API này dùng để đăng ký bãi đỗ xe mới vào hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "201", description = "Tạo bãi đỗ xe thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy chủ sở hữu"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 3.5 Update Parking Lot)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Cập nhật thông tin bãi đỗ xe",)

[//]: # (    description = "API này dùng để cập nhật thông tin của bãi đỗ xe.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Cập nhật thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@PutMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 3.6 Delete Parking Lot)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Xóa bãi đỗ xe",)

[//]: # (    description = "API này dùng để xóa bãi đỗ xe khỏi hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "204", description = "Xóa thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@DeleteMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 3.7 Update Parking Lot Availability)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Cập nhật số lượng chỗ trống trong bãi đỗ xe",)

[//]: # (    description = "API này dùng để cập nhật số lượng chỗ trống có sẵn trong bãi đỗ xe.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Cập nhật thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Số lượng không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@PatchMapping&#40;"/{id}/availability"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (## 4. Parking Controller &#40;`ParkingController`&#41;)

[//]: # ()
[//]: # (### 4.1 Get All Parking Sessions)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách tất cả phiên gửi xe",)

[//]: # (    description = "API này dùng để lấy danh sách tất cả các phiên gửi xe trong hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/sessions"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 4.2 Get Parking Session By ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin phiên gửi xe theo ID",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của phiên gửi xe theo ID.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy phiên gửi xe với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/sessions/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 4.3 Get Active Parking Sessions)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách phiên gửi xe đang hoạt động",)

[//]: # (    description = "API này dùng để lấy danh sách các phiên gửi xe đang hoạt động.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/sessions/active"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 4.4 Get Active Session By Vehicle ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy phiên gửi xe đang hoạt động của xe",)

[//]: # (    description = "API này dùng để lấy thông tin phiên gửi xe đang hoạt động cho một xe cụ thể.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy phiên gửi xe đang hoạt động"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/sessions/vehicle/{vehicleId}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 4.5 Get Sessions By License Plate)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách phiên gửi xe theo biển số",)

[//]: # (    description = "API này dùng để lấy danh sách các phiên gửi xe cho một biển số xe cụ thể.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/sessions/license-plate/{licensePlate}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 4.6 Create Entry Session)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Tạo phiên gửi xe khi xe vào bãi",)

[//]: # (    description = "API này dùng để tạo phiên gửi xe mới khi xe vào bãi đỗ xe, với hình ảnh biển số xe.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "201", description = "Tạo phiên gửi xe thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy bãi đỗ xe"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/entry/{lotId}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 4.7 Complete Exit Session)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Hoàn thành phiên gửi xe khi xe ra khỏi bãi",)

[//]: # (    description = "API này dùng để hoàn thành phiên gửi xe khi xe ra khỏi bãi đỗ xe, với hình ảnh biển số xe.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Hoàn thành phiên gửi xe thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy phiên gửi xe"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ hoặc biển số không khớp"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/exit/{sessionId}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 4.8 Recognize License Plate)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Nhận diện biển số xe",)

[//]: # (    description = "API này dùng để nhận diện biển số xe từ hình ảnh được tải lên.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Nhận diện thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Không thể xử lý hình ảnh"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/recognize"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (## 5. Payment Controller &#40;`PaymentController`&#41;)

[//]: # ()
[//]: # (### 5.1 Get All Payments)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách tất cả thanh toán",)

[//]: # (    description = "API này dùng để lấy danh sách tất cả các thanh toán trong hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 5.2 Get Payment By ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin thanh toán theo ID",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của thanh toán theo ID.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy thanh toán với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 5.3 Get Payments By Session ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách thanh toán theo phiên gửi xe",)

[//]: # (    description = "API này dùng để lấy danh sách các thanh toán cho một phiên gửi xe cụ thể.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/session/{sessionId}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 5.4 Create Payment)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Tạo thanh toán mới",)

[//]: # (    description = "API này dùng để tạo thanh toán mới cho một phiên gửi xe.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "201", description = "Tạo thanh toán thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy phiên gửi xe"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 5.5 Update Payment Status)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Cập nhật trạng thái thanh toán",)

[//]: # (    description = "API này dùng để cập nhật trạng thái của thanh toán &#40;PENDING, COMPLETED, FAILED&#41;.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Cập nhật thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Trạng thái không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy thanh toán với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@PatchMapping&#40;"/{id}/status"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 5.6 Delete Payment)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Xóa thanh toán",)

[//]: # (    description = "API này dùng để xóa thanh toán khỏi hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "204", description = "Xóa thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy thanh toán với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@DeleteMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (## 6. Employee Controller &#40;`EmployeeController`&#41;)

[//]: # ()
[//]: # (### 6.1 Get All Employees)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách tất cả nhân viên",)

[//]: # (    description = "API này dùng để lấy danh sách tất cả nhân viên trong hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 6.2 Get Employee By ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin nhân viên theo ID",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của nhân viên theo ID.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy nhân viên với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 6.3 Get Employees By Parking Lot)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách nhân viên theo bãi đỗ xe",)

[//]: # (    description = "API này dùng để lấy danh sách nhân viên làm việc tại một bãi đỗ xe cụ thể.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/parking-lot/{parkingLotId}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 6.4 Get Employees By Status)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách nhân viên theo trạng thái",)

[//]: # (    description = "API này dùng để lấy danh sách nhân viên với trạng thái cụ thể &#40;ACTIVE, INACTIVE&#41;.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/status/{status}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 6.5 Create Employee)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Tạo nhân viên mới",)

[//]: # (    description = "API này dùng để đăng ký nhân viên mới vào hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "201", description = "Tạo nhân viên thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy bãi đỗ xe để gắn với nhân viên"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 6.6 Update Employee)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Cập nhật thông tin nhân viên",)

[//]: # (    description = "API này dùng để cập nhật thông tin của nhân viên.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Cập nhật thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "400", description = "Dữ liệu không hợp lệ"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy nhân viên với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@PutMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 6.7 Delete Employee)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Xóa nhân viên",)

[//]: # (    description = "API này dùng để xóa nhân viên khỏi hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "204", description = "Xóa thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy nhân viên với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@DeleteMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (## 7. Revenue Stat Controller &#40;`RevenueStatController`&#41;)

[//]: # ()
[//]: # (### 7.1 Get All Revenue Stats)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách tất cả thống kê doanh thu",)

[//]: # (    description = "API này dùng để lấy danh sách tất cả các thống kê doanh thu trong hệ thống.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping)

[//]: # (```)

[//]: # ()
[//]: # (### 7.2 Get Revenue Stat By ID)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy thông tin thống kê doanh thu theo ID",)

[//]: # (    description = "API này dùng để lấy thông tin chi tiết của thống kê doanh thu theo ID.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy thông tin thành công"&#41;,)

[//]: # (    @ApiResponse&#40;responseCode = "404", description = "Không tìm thấy thống kê doanh thu với ID đã cho"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/{id}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 7.3 Get Revenue Stats By Parking Lot)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách thống kê doanh thu theo bãi đỗ xe",)

[//]: # (    description = "API này dùng để lấy danh sách thống kê doanh thu cho một bãi đỗ xe cụ thể.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/parking-lot/{parkingLotId}"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 7.4 Get Revenue Stats By Date Range)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Lấy danh sách thống kê doanh thu theo khoảng thời gian",)

[//]: # (    description = "API này dùng để lấy danh sách thống kê doanh thu trong một khoảng thời gian cụ thể.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Lấy danh sách thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@GetMapping&#40;"/date-range"&#41;)

[//]: # (```)

[//]: # ()
[//]: # (### 7.5 Generate Revenue Stats For Date)

[//]: # ()
[//]: # (```java)

[//]: # (@Operation&#40;)

[//]: # (    summary = "Tạo thống kê doanh thu cho một ngày cụ thể",)

[//]: # (    description = "API này dùng để tạo thống kê doanh thu thủ công cho một ngày cụ thể.")

[//]: # (&#41;)

[//]: # (@ApiResponses&#40;value = {)

[//]: # (    @ApiResponse&#40;responseCode = "200", description = "Tạo thống kê thành công"&#41;)

[//]: # (}&#41;)

[//]: # (@PostMapping&#40;"/generate/{date}"&#41;)

[//]: # (```)
