package com.mindsoccer.api.security;

import com.mindsoccer.api.entity.UserEntity;
import com.mindsoccer.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service de chargement des utilisateurs pour Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailOrHandle(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        return UserPrincipal.fromEntity(user);
    }

    public UserDetails loadUserById(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return UserPrincipal.fromEntity(user);
    }
}
