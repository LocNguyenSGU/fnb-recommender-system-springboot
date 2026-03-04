package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.EmailNotVerifiedException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest validLoginRequest;
    private AuthResponse validAuthResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFullName("Test User");
        userResponse.setRole("user");

        validAuthResponse = new AuthResponse();
        validAuthResponse.setAccessToken("test-access-token");
        validAuthResponse.setRefreshToken("test-refresh-token");
        validAuthResponse.setUser(userResponse);
    }

    @Test
    @DisplayName("Login with valid credentials - should return 200 and auth tokens")
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(validAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Login with invalid email format - should return 400")
    void login_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with blank email - should return 400")
    void login_WithBlankEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("");
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with blank password - should return 400")
    void login_WithBlankPassword_ShouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with null email - should return 400")
    void login_WithNullEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail(null);
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with null password - should return 400")
    void login_WithNullPassword_ShouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with invalid credentials - should return 401")
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with unverified email - should return 403")
    void login_WithUnverifiedEmail_ShouldReturnForbidden() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new EmailNotVerifiedException("Email not verified. Please check your email."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Login with wrong HTTP method - should return 405")
    void login_WithWrongHttpMethod_ShouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with empty request body - should return 400")
    void login_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login endpoint exists at correct path")
    void login_EndpointPath_ShouldBeCorrect() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(validAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk());
    }
}
