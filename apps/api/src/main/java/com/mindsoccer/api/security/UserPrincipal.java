package com.mindsoccer.api.security;

import com.mindsoccer.api.entity.UserEntity;
import com.mindsoccer.protocol.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Principal utilisateur pour Spring Security.
 */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String handle;
    private final String email;
    private final String password;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String handle, String email, String password, UserRole role) {
        this.id = id;
        this.handle = handle;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public static UserPrincipal fromEntity(UserEntity user) {
        return new UserPrincipal(
                user.getId(),
                user.getHandle(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getHandle() {
        return handle;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return handle;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
