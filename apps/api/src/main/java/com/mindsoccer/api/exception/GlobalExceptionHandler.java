package com.mindsoccer.api.exception;

import com.mindsoccer.protocol.dto.common.ApiResponse;
import com.mindsoccer.shared.exception.*;
import com.mindsoccer.shared.exception.MatchException;
import com.mindsoccer.shared.i18n.I18nService;
import com.mindsoccer.shared.i18n.SupportedLocale;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gestionnaire global des exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final I18nService i18nService;

    public GlobalExceptionHandler(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        HttpStatus status = switch (ex.getErrorCode()) {
            case UNAUTHORIZED, AUTH_TOKEN_EXPIRED, AUTH_TOKEN_INVALID, AUTH_INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case AUTH_EMAIL_EXISTS, AUTH_HANDLE_EXISTS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), message, ex.getFieldErrors()));
    }

    @ExceptionHandler(MatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMatchException(MatchException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        HttpStatus status = switch (ex.getErrorCode()) {
            case MATCH_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case MATCH_FULL, MATCH_ALREADY_STARTED, MATCH_ALREADY_FINISHED, MATCH_ALREADY_PARTICIPANT -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(RoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoundException(RoundException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(PlayerException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlayerException(PlayerException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(RateLimitException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<ApiResponse<Void>> handleIdempotencyException(IdempotencyException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(GameException.class)
    public ResponseEntity<ApiResponse<Void>> handleGameException(GameException ex, WebRequest request) {
        String message = i18nService.getMessage(ex.getMessageKey(), getLocale(request), ex.getArgs());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        SupportedLocale locale = getLocale(request);

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String message = i18nService.getMessage(error.getDefaultMessage(), locale);
            errors.put(error.getField(), message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(),
                        i18nService.getMessage("error.bad_request", locale), errors));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        String message = i18nService.getMessage("error.auth.token_expired", getLocale(request));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_TOKEN_EXPIRED.getCode(), message));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException ex, WebRequest request) {
        String message = i18nService.getMessage("error.auth.token_invalid", getLocale(request));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_TOKEN_INVALID.getCode(), message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String message = i18nService.getMessage("error.forbidden", getLocale(request));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        String message = i18nService.getMessage("error.internal", getLocale(request));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), message));
    }

    private SupportedLocale getLocale(WebRequest request) {
        Locale locale = request.getLocale();
        return SupportedLocale.fromCode(locale.getLanguage());
    }
}
