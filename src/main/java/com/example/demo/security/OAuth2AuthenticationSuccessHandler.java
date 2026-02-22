package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        
        // Extract user info based on provider
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes, registrationId);
        String name = extractName(attributes, registrationId);
        String providerId = extractProviderId(attributes, registrationId);
        
        log.info("OAuth2 login attempt - Provider: {}, Email: {}", registrationId, email);
        
        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewOAuthUser(email, name, registrationId, providerId));
        
        // Update provider info if user exists but wasn't linked
        if (!user.getProvider().equals(registrationId)) {
            updateUserProvider(user, registrationId, providerId);
        }
        
        // Ensure user is verified (OAuth users are auto-verified)
        if (!user.getIsVerified()) {
            user.setIsVerified(true);
            userRepository.save(user);
        }
        
        // Generate JWT tokens
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        
        // Build redirect URL with tokens
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
        
        log.info("OAuth2 login successful - Redirecting to: {}", targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private String extractEmail(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("email");
        }
        throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
    }
    
    private String extractName(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("name");
        }
        return "OAuth User";
    }
    
    private String extractProviderId(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("id");
        }
        return null;
    }
    
    private User createNewOAuthUser(String email, String name, String provider, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(name);
        user.setProvider(provider);
        user.setIsVerified(true); // OAuth users are auto-verified
        user.setRole("user");
        user.setPassword(""); // No password for OAuth users
        
        user = userRepository.save(user);
        log.info("Created new OAuth user: {} via {} (providerId: {})", email, provider, providerId);
        return user;
    }
    
    private void updateUserProvider(User user, String provider, String providerId) {
        user.setProvider(provider);
        userRepository.save(user);
        log.info("Updated provider info for user: {} (providerId: {})", user.getEmail(), providerId);
    }
}
