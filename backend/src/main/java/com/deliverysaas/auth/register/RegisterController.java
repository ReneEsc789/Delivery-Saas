package com.deliverysaas.auth.register;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
public class RegisterController {

    private final RegisterService registerService;
    private final AuthCookieFactory authCookieFactory;

    public RegisterController(RegisterService registerService, AuthCookieFactory authCookieFactory) {
        this.registerService = registerService;
        this.authCookieFactory = authCookieFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

        User user = registerService.register(request);

        ResponseCookie cookie = authCookieFactory.create(user);

        RegisterResponse body = new RegisterResponse(
            user.getId(),
            user.getOrganization().getId(),
            user.getEmail(),
            user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(body);
    }
}