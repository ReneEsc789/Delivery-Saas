package com.deliverysaas.auth.login;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.error.UnauthorizedException;
import com.deliverysaas.users.UserRepository;
import com.deliverysaas.users.domain.User;
import com.deliverysaas.users.domain.UserStatus;

@Service 
public class LoginService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Account is not active");
        }

        return user;
    }
}
