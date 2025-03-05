package com.github.adrjo.snowcloud.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM cloud_user u WHERE u.username = :nameOrEmail OR u.email = :nameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("nameOrEmail")String nameOrEmail);

    Optional<User> findByOidcId(String oidcId);
}
