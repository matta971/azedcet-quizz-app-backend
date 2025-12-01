package com.mindsoccer.api.controller;

import com.mindsoccer.api.entity.UserEntity;
import com.mindsoccer.api.repository.UserRepository;
import com.mindsoccer.api.security.CurrentUser;
import com.mindsoccer.api.security.UserPrincipal;
import com.mindsoccer.protocol.dto.common.ApiResponse;
import com.mindsoccer.protocol.dto.response.UserResponse;
import com.mindsoccer.shared.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Contrôleur utilisateurs.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestion des utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @Operation(summary = "Profil", description = "Obtenir le profil de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser UserPrincipal principal) {
        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow(NotFoundException::player);

        return ResponseEntity.ok(ApiResponse.success(toResponse(user)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Utilisateur par ID", description = "Obtenir un utilisateur par son ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(NotFoundException::player);

        return ResponseEntity.ok(ApiResponse.success(toResponse(user)));
    }

    @GetMapping("/handle/{handle}")
    @Operation(summary = "Utilisateur par pseudo", description = "Obtenir un utilisateur par son pseudo")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByHandle(@PathVariable String handle) {
        UserEntity user = userRepository.findByHandle(handle)
                .orElseThrow(NotFoundException::player);

        return ResponseEntity.ok(ApiResponse.success(toResponse(user)));
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getHandle(),
                user.getEmail(),
                user.getRole(),
                user.getRating(),
                user.getCountry(),
                user.getCreatedAt()
        );
    }
}
