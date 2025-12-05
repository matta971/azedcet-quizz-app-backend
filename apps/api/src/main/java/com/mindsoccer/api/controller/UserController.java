package com.mindsoccer.api.controller;

import com.mindsoccer.api.entity.UserEntity;
import com.mindsoccer.api.repository.UserRepository;
import com.mindsoccer.api.security.CurrentUser;
import com.mindsoccer.api.security.UserPrincipal;
import com.mindsoccer.protocol.dto.common.ApiResponse;
import com.mindsoccer.protocol.dto.request.UpdateProfileRequest;
import com.mindsoccer.protocol.dto.response.UserResponse;
import com.mindsoccer.protocol.enums.Language;
import com.mindsoccer.shared.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
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

    @PutMapping("/me")
    @Operation(summary = "Mettre à jour le profil", description = "Mettre à jour le profil de l'utilisateur connecté")
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow(NotFoundException::player);

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.birthDate() != null) {
            user.setBirthDate(request.birthDate());
        }
        if (request.country() != null) {
            user.setCountry(request.country());
        }
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(toResponse(user)));
    }

    @GetMapping("/languages")
    @Operation(summary = "Langues disponibles", description = "Obtenir la liste des langues supportées")
    public ResponseEntity<ApiResponse<List<LanguageOption>>> getAvailableLanguages() {
        List<LanguageOption> languages = Arrays.stream(Language.values())
                .map(lang -> new LanguageOption(lang.name(), lang.getDisplayName()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(languages));
    }

    public record LanguageOption(String code, String displayName) {}

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getHandle(),
                user.getEmail(),
                user.getRole(),
                user.getRating(),
                user.getFirstName(),
                user.getLastName(),
                user.getBirthDate(),
                user.getCountry(),
                user.getPreferredLanguage(),
                user.getCreatedAt()
        );
    }
}
