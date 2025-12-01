package com.mindsoccer.api.service;

import com.mindsoccer.api.entity.UserEntity;
import com.mindsoccer.api.repository.UserRepository;
import com.mindsoccer.api.security.JwtService;
import com.mindsoccer.protocol.dto.request.LoginRequest;
import com.mindsoccer.protocol.dto.request.RefreshTokenRequest;
import com.mindsoccer.protocol.dto.request.RegisterRequest;
import com.mindsoccer.protocol.dto.response.AuthResponse;
import com.mindsoccer.shared.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service d'authentification.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Inscription d'un nouvel utilisateur.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.handle());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.email())) {
            throw AuthenticationException.emailExists();
        }

        // Vérifier si le pseudo existe déjà
        if (userRepository.existsByHandle(request.handle())) {
            throw AuthenticationException.handleExists();
        }

        // Créer l'utilisateur
        UserEntity user = new UserEntity(
                request.handle(),
                request.email().toLowerCase(),
                passwordEncoder.encode(request.password())
        );
        user.setCountry(request.country());

        user = userRepository.save(user);
        log.info("User registered: {} ({})", user.getHandle(), user.getId());

        return generateAuthResponse(user);
    }

    /**
     * Connexion d'un utilisateur.
     */
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for: {}", request.identifier());

        UserEntity user = userRepository.findByEmailOrHandle(request.identifier().toLowerCase())
                .orElseThrow(AuthenticationException::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.debug("Invalid password for user: {}", user.getHandle());
            throw AuthenticationException.invalidCredentials();
        }

        log.info("User logged in: {} ({})", user.getHandle(), user.getId());
        return generateAuthResponse(user);
    }

    /**
     * Rafraîchissement du token.
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw AuthenticationException.tokenInvalid();
        }

        UUID userId = jwtService.extractUserId(refreshToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(AuthenticationException::tokenInvalid);

        log.debug("Token refreshed for user: {}", user.getHandle());
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getHandle(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getHandle(),
                        user.getEmail(),
                        user.getRole(),
                        user.getRating(),
                        user.getCountry()
                )
        );
    }
}
