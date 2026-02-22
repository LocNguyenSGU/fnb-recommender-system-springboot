package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API quản lý đăng nhập, đăng ký, xác thực email, quên mật khẩu và OAuth2")
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(
        summary = "Đăng ký tài khoản mới",
        description = "Tạo tài khoản người dùng mới. Email xác thực sẽ được gửi tới địa chỉ email đã đăng ký."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
            content = @Content(schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"User registered successfully. Please check your email to verify your account.\"}"))),
        @ApiResponse(responseCode = "400", description = "Email đã tồn tại hoặc dữ liệu không hợp lệ",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Email already exists\"}")))  
    })
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Thông tin đăng ký",
            required = true,
            content = @Content(
                schema = @Schema(implementation = RegisterRequest.class),
                examples = @ExampleObject(value = "{\"email\": \"user@example.com\", \"password\": \"SecurePass123!\", \"fullName\": \"Nguyen Van A\"}")
            )
        )
        @Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Xác thực email",
        description = "Xác thực địa chỉ email bằng token nhận được qua email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xác thực thành công",
            content = @Content(schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Email verified successfully. You can now login.\"}"))),
        @ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc đã hết hạn",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Invalid or expired verification token\"}")))  
    })
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
        @io.swagger.v3.oas.annotations.Parameter(description = "Token xác thực nhận qua email", required = true, example = "abc123xyz789")
        @RequestParam String token) {
        MessageResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Đăng nhập",
        description = "Đăng nhập bằng email và mật khẩu. Tài khoản phải được xác thực email trước khi đăng nhập."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
            content = @Content(schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"tokenType\": \"Bearer\", \"expiresIn\": 900000}"))),
        @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Invalid email or password\"}"))) ,
        @ApiResponse(responseCode = "403", description = "Email chưa được xác thực",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Email not verified. Please check your email.\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Thông tin đăng nhập",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(value = "{\"email\": \"user@example.com\", \"password\": \"SecurePass123!\"}")
            )
        )
        @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Làm mới access token",
        description = "Sử dụng refresh token để lấy access token mới khi access token cũ hết hạn"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Làm mới token thành công",
            content = @Content(schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"tokenType\": \"Bearer\", \"expiresIn\": 900000}"))),
        @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ hoặc đã hết hạn",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Invalid or expired refresh token\"}")))  
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Refresh token",
            required = true,
            content = @Content(
                schema = @Schema(implementation = RefreshTokenRequest.class),
                examples = @ExampleObject(value = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}")
            )
        )
        @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Đăng xuất",
        description = "Đăng xuất và vô hiệu hóa refresh token",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng xuất thành công",
            content = @Content(schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Logged out successfully\"}"))),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Unauthorized\"}")))  
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Refresh token cần vô hiệu hóa",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LogoutRequest.class),
                examples = @ExampleObject(value = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}")
            )
        )
        @Valid @RequestBody LogoutRequest request) {
        MessageResponse response = authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Quên mật khẩu",
        description = "Gửi email chứa link đặt lại mật khẩu"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email đặt lại mật khẩu đã được gửi",
            content = @Content(schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Password reset email sent successfully. Please check your email.\"}"))),
        @ApiResponse(responseCode = "404", description = "Email không tồn tại",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Email not found\"}")))  
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email tài khoản cần đặt lại mật khẩu",
            required = true,
            content = @Content(
                schema = @Schema(implementation = ForgotPasswordRequest.class),
                examples = @ExampleObject(value = "{\"email\": \"user@example.com\"}")
            )
        )
        @Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Đặt lại mật khẩu",
        description = "Đặt lại mật khẩu bằng token nhận được qua email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công",
            content = @Content(schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Password reset successfully. You can now login with your new password.\"}"))),
        @ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc đã hết hạn",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Invalid or expired reset token\"}")))  
    })
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token và mật khẩu mới",
            required = true,
            content = @Content(
                schema = @Schema(implementation = ResetPasswordRequest.class),
                examples = @ExampleObject(value = "{\"token\": \"abc123xyz789\", \"newPassword\": \"NewSecurePass123!\"}")
            )
        )
        @Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Gửi lại email xác thực",
        description = "Gửi lại email xác thực cho tài khoản chưa được xác thực"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email xác thực đã được gửi lại",
            content = @Content(schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Verification email resent successfully. Please check your email.\"}"))),
        @ApiResponse(responseCode = "400", description = "Email đã được xác thực hoặc không tồn tại",
            content = @Content(examples = @ExampleObject(value = "{\"error\": \"Email already verified or not found\"}")))  
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationEmail(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email cần gửi lại xác thực",
            required = true,
            content = @Content(
                schema = @Schema(implementation = ResendVerificationRequest.class),
                examples = @ExampleObject(value = "{\"email\": \"user@example.com\"}")
            )
        )
        @Valid @RequestBody ResendVerificationRequest request) {
        MessageResponse response = authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(response);
    }
}
