package com.example.demo.scheduled;

import com.example.demo.repository.EmailVerificationTokenRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupJob {
    
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    
    /**
     * Cleanup expired tokens daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM every day
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting token cleanup job...");
        
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // Cleanup expired email verification tokens
            int emailTokensDeleted = emailVerificationTokenRepository.deleteExpiredTokens(now);
            log.info("Deleted {} expired email verification tokens", emailTokensDeleted);
            
            // Cleanup expired password reset tokens
            int resetTokensDeleted = passwordResetTokenRepository.deleteExpiredTokens(now);
            log.info("Deleted {} expired password reset tokens", resetTokensDeleted);
            
            // Cleanup expired refresh tokens
            int refreshTokensDeleted = refreshTokenRepository.deleteExpiredTokens(now);
            log.info("Deleted {} expired refresh tokens", refreshTokensDeleted);
            
            log.info("Token cleanup job completed successfully. Total tokens deleted: {}", 
                    emailTokensDeleted + resetTokensDeleted + refreshTokensDeleted);
            
        } catch (Exception e) {
            log.error("Error during token cleanup job", e);
        }
    }
    
    /**
     * Manual cleanup method for testing or manual trigger
     */
    @Transactional
    public void cleanupNow() {
        log.info("Manual token cleanup triggered");
        cleanupExpiredTokens();
    }
}
