package com.project.parking.controller;

import com.project.parking.dto.request.*;
import com.project.parking.model.User;
import com.project.parking.repository.UserRepository;
import com.project.parking.response.Response;
import com.project.parking.response.user.LoginResponse;
import com.project.parking.response.user.UserResponse;
import com.project.parking.service.TokenService;
import com.project.parking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.v1.prefix:/api/v1}/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs để quản lý người dùng")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final TokenService tokenService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Đăng nhập", description = "API này dùng để xác thực người dùng và trả về token đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "400", description = "Thông tin đăng nhập không hợp lệ")
    })
    @PostMapping("/login")
    public ResponseEntity<Response> login(
            @RequestBody @Valid UserLoginDTO userLoginDTO) {
        try {
            LoginResponse loginResponse = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());
            return ResponseEntity.ok().body(new Response("success", "Login successful", loginResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Tạo người dùng mới", description = "API này dùng để tạo tài khoản người dùng mới trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tài khoản được tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc tài khoản đã tồn tại")
    })
    @PostMapping("/create")
    public ResponseEntity<Response> create(@RequestBody @Valid UserDTO userDTO) {
        try {
            return ResponseEntity.ok().body(new Response("success", "User created successfully",
                    UserResponse.fromUser(userService.createUser(userDTO))));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Xác thực người dùng", description = "API này dùng để xác thực tài khoản người dùng bằng mã xác nhận.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác thực tài khoản thành công"),
            @ApiResponse(responseCode = "400", description = "Mã xác nhận không hợp lệ hoặc đã hết hạn")
    })
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyUserDTO verifyUserDTO) {
        try {
            userService.verifyUser(verifyUserDTO);
            return ResponseEntity.ok("User verified successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Gửi lại mã xác nhận", description = "API này dùng để gửi lại mã xác nhận cho người dùng qua email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đã gửi lại mã xác nhận thành công"),
            @ApiResponse(responseCode = "400", description = "Email không tồn tại hoặc tài khoản đã được xác thực")
    })
    @PostMapping("/resend-verification/{email}")
    public ResponseEntity<?> resendVerification(@PathVariable String email) {
        try {
            userService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Cập nhật thông tin người dùng", description = "API này dùng để cập nhật thông tin cá nhân của người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thông tin thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập")
    })
    @PutMapping("/update")
    public ResponseEntity<Response> update(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserDTO userDTO) {
        try {
            User userDTOUpdated = userService.updateInfo(user.getId(), userDTO);
            return ResponseEntity.ok()
                    .body(new Response("success", "User updated successfully", UserResponse.fromUser(userDTOUpdated)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Đổi mật khẩu", description = "API này dùng để thay đổi mật khẩu của người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Response> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdatePasswordDTO updatePasswordDTO) {
        try {
            User userDTOUpdated = userService.updatePassword(user.getId(), updatePasswordDTO);
            return ResponseEntity.ok()
                    .body(new Response("success", "User updated successfully", UserResponse.fromUser(userDTOUpdated)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Xóa người dùng", description = "API này dùng để xóa tài khoản người dùng khỏi hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa tài khoản thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa tài khoản"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản")
    })
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Response> delete(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok().body(new Response("success", "User deleted successfully",
                    UserResponse.fromUser(userService.deleteUserByUserId(userId))));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Quên mật khẩu", description = "API này dùng để gửi email đặt lại mật khẩu cho người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đã gửi email đặt lại mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Email không tồn tại")
    })
    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<?> forgotPassword(@PathVariable String email) {
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy thông tin người dùng", description = "API này dùng để lấy thông tin chi tiết của người dùng đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "400", description = "Có lỗi xảy ra"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập")
    })
    @GetMapping(value = "/me")
    public ResponseEntity<Response> getProfile(@AuthenticationPrincipal User user) {
        try {
            log.info("User: {}", user);
            User userDetail = userRepository.findById(user.getId()).orElseThrow(() -> new Exception("User not found"));
            return ResponseEntity.ok().body(new Response("success", "User detail", UserResponse.fromUser(userDetail)));
        } catch (Exception e) {
            log.error("Error fetching user details", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }
}
