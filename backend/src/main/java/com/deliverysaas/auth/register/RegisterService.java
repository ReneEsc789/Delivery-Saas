package com.deliverysaas.auth.register;

import java.text.Normalizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.organizations.OrganizationRepository;
import com.deliverysaas.organizations.domain.Organization;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.users.UserRepository;
import com.deliverysaas.users.domain.User;
import com.deliverysaas.users.domain.UserRole;

@Service 
public class RegisterService {
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional 
    public User register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }

        String slug = generateUniqueSlug(request.organizationName());

        Organization organization = new Organization(request.organizationName(), slug);
        organizationRepository.save(organization);

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(
            organization,
            request.name(),
            request.email(),
            passwordHash,
            UserRole.ADMIN
        );
        userRepository.save(user);

        return user;
    }

    private String generateUniqueSlug(String organizationName) {
        String base = Normalizer.normalize(organizationName, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");

        String slug = base;
        int suffix = 2;
        while (organizationRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix;
            suffix ++;
        }
        return slug;
    }
}
