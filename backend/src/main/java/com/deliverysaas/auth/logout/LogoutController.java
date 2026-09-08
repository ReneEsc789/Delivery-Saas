package com.deliverysaas.auth.logout;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverysaas.shared.security.AuthCookieFactory;

@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {

    private final AuthCookieFactory authCookieFactory;

    public LogoutController(AuthCookieFactory authCookieFactory) {
        this.authCookieFactory = authCookieFactory;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = authCookieFactory.clear();
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build();
    }
}