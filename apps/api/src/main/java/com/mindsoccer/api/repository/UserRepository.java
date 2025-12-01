package com.mindsoccer.api.repository;

import com.mindsoccer.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository utilisateurs.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByHandle(String handle);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :identifier OR u.handle = :identifier")
    Optional<UserEntity> findByEmailOrHandle(String identifier);

    boolean existsByEmail(String email);

    boolean existsByHandle(String handle);
}
