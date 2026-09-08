package com.deliverysaas.auth.me;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverysaas.shared.error.UnauthorizedException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.UserRepository;
import com.deliverysaas.users.domain.User;

@RestController
@RequestMapping("/api/v1/auth")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AuthPrincipal principal) {

        User user = userRepository.findById(principal.userId())
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        return new MeResponse(
            user.getId(),
            user.getOrganization().getId(),
            user.getEmail(),
            user.getName(),
            user.getRole()
        );
    }
}