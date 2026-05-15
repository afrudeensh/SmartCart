package com.smartcart.auth.repository;

import com.smartcart.auth.entity.RefreshToken;
import com.smartcart.auth.entity.Users;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // revoke all tokens for a user on logout
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllByUser(Users user);
}