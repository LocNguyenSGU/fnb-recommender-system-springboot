package com.example.demo.repository;

import com.example.demo.model.EmailVerificationToken;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    
    Optional<EmailVerificationToken> findByToken(String token);
    
    void deleteByUser(User user);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);
    
    Optional<EmailVerificationToken> findByUser(User user);
}
