package com.loopy.footballvideoprocessor.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.loopy.footballvideoprocessor.user.model.User;
import com.loopy.footballvideoprocessor.user.model.VerificationToken;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    List<VerificationToken> findByUser(User user);

    @Query("SELECT v FROM VerificationToken v WHERE v.expiryDate < ?1")
    List<VerificationToken> findAllExpiredTokens(LocalDateTime now);
}