package com.deliverysaas.shared.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.deliverysaas.users.domain.User;

@Component
public class AuthCookieFactory {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthProperties authProperties;

    public AuthCookieFactory(JwtService jwtService, JwtProperties jwtProperties, AuthProperties authProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.authProperties = authProperties;
    }

    public ResponseCookie create(User user) {
        String token = jwtService.generate(user);
        return ResponseCookie.from(authProperties.cookieName(), token)
            .httpOnly(true)
            .secure(authProperties.cookieSecure())
            .sameSite("Strict")
            .path("/")
            .maxAge(jwtProperties.expiration())
            .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(authProperties.cookieName(), "")
            .httpOnly(true)
            .secure(authProperties.cookieSecure())
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build();
    }
}