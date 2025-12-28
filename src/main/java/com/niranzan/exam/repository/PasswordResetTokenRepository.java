package com.niranzan.exam.repository;

import com.niranzan.exam.entity.PasswordResetToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.userId = :userId")
    void markAllTokensAsUsedForUser(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.userId = :userId")
    void deleteAllTokensForUser(@Param("userId") Long userId);
}


