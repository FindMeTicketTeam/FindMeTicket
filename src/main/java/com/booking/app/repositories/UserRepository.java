package com.booking.app.repositories;

import com.booking.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :userId")
    void deleteUserById(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User uc SET uc.notification = :notification WHERE uc.id = :userId")
    void updateByNotification(@Param("userId") UUID userId, @Param("notification") Boolean notification);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrUsername(String email, String username);

    @Modifying
    @Query(value = "UPDATE User u SET u.password = :password WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("password") String password);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :pid")
    void deleteByPid(@Param("pid") UUID theId);

    @Modifying
    @Query(value = "UPDATE User u SET u.enabled = true, u.accountNonExpired = true, u.accountNonLocked = true, u.credentialsNonExpired = true WHERE u.id = :userId")
    void enableUser(@Param("userId") UUID userId);
}
