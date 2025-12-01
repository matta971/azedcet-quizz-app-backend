package com.mindsoccer.api.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Annotation pour injecter l'utilisateur courant dans un contrôleur.
 * Usage: @CurrentUser UserPrincipal user
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
