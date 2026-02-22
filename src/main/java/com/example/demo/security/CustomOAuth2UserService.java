package com.example.demo.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("Loading OAuth2 user from provider: {}", registrationId);
        
        // Process user attributes based on provider
        processOAuth2User(registrationId, oAuth2User);
        
        return oAuth2User;
    }
    
    private void processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        if ("google".equals(registrationId)) {
            String email = oAuth2User.getAttribute("email");
            Boolean emailVerified = oAuth2User.getAttribute("email_verified");
            log.info("Google user: email={}, verified={}", email, emailVerified);
        } else if ("facebook".equals(registrationId)) {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            log.info("Facebook user: email={}, name={}", email, name);
        }
    }
}
