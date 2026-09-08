package com.deliverysaas.auth.login;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverysaas.shared.security.AuthCookieFactory;
import com.deliverysaas.users.domain.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LoginService loginService;
    private final AuthCookieFactory authCookieFactory;

    public LoginController(LoginService loginService, AuthCookieFactory authCookieFactory) {
        this.loginService = loginService;
        this.authCookieFactory = authCookieFactory;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        User user = loginService.authenticate(request);

        ResponseCookie cookie = authCookieFactory.create(user);

        LoginResponse body = new LoginResponse(
            user.getId(),
            user.getOrganization().getId(),
            user.getEmail(),
            user.getName(),
            user.getRole()
        );

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(body);
    }
}