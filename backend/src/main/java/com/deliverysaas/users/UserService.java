package com.deliverysaas.users;

import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.audit.AuditService;
import com.deliverysaas.organizations.OrganizationRepository;
import com.deliverysaas.organizations.domain.Organization;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.domain.User;
import com.deliverysaas.users.domain.UserRole;
import com.deliverysaas.users.domain.UserStatus;

@Service
public class UserService {

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public UserService(UserRepository users, OrganizationRepository organizations, PasswordEncoder encoder, AuditService audit) {
        this.users = users;
        this.organizations = organizations;
        this.encoder = encoder;
        this.audit = audit;
    }

    @Transactional
    public UserResponse create(AuthPrincipal actor, CreateUserRequest r) {
        authorizeRole(actor, null, r.role());
        if (users.existsByEmail(r.email().trim().toLowerCase())) {
            throw new ConflictException("Email already registered");
        }
        Organization org = organizations.findById(actor.organizationId())
            .orElseThrow(() -> new NotFoundException("Organization not found"));
        User u = users.save(new User(org, r.name().trim(), r.email().trim().toLowerCase(),
            encoder.encode(r.password()), r.role()));
        audit.record("CREATE", "USER", u.getId(), "User created");
        return UserResponse.from(u);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> all(AuthPrincipal actor) {
        return users.findAllByOrganizationId(actor.organizationId()).stream()
            .filter(u -> canView(actor, u))
            .map(UserResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse one(AuthPrincipal actor, UUID id) {
        User u = get(actor.organizationId(), id);
        requireView(actor, u);
        return UserResponse.from(u);
    }

    @Transactional
    public UserResponse update(AuthPrincipal actor, UUID id, UpdateUserRequest r) {
        User u = get(actor.organizationId(), id);
        requireView(actor, u);
        authorizeRole(actor, id, r.role());
        if (actor.userId().equals(id) && r.role() != u.getRole()) {
            throw new ConflictException("You cannot change your own role");
        }
        if (!u.getEmail().equalsIgnoreCase(r.email()) && users.existsByEmail(r.email().trim().toLowerCase())) {
            throw new ConflictException("Email already registered");
        }
        protectLastAdmin(u, r.role(), r.status());
        if (actor.userId().equals(id) && r.status() != UserStatus.ACTIVE) {
            throw new ConflictException("You cannot deactivate yourself");
        }
        u.setName(r.name().trim());
        u.setEmail(r.email().trim().toLowerCase());
        u.setRole(r.role());
        u.setStatus(r.status());
        audit.record("UPDATE", "USER", u.getId(), "User updated");
        return UserResponse.from(u);
    }

    @Transactional
    public void delete(AuthPrincipal actor, UUID id) {
        User u = get(actor.organizationId(), id);
        requireView(actor, u);
        if (actor.userId().equals(id)) {
            throw new ConflictException("You cannot deactivate yourself");
        }
        protectLastAdmin(u, u.getRole(), UserStatus.INACTIVE);
        u.setStatus(UserStatus.INACTIVE);
        audit.record("DEACTIVATE", "USER", u.getId(), "User deactivated");
    }

    private User get(UUID org, UUID id) {
        return users.findByIdAndOrganizationId(id, org)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private boolean canView(AuthPrincipal a, User u) {
        return a.role() == UserRole.ADMIN
            || a.userId().equals(u.getId())
            || (a.role() == UserRole.MANAGER && (u.getRole() == UserRole.CUSTOMER || u.getRole() == UserRole.DRIVER));
    }

    private void requireView(AuthPrincipal a, User u) {
        if (!canView(a, u)) {
            throw new ForbiddenException("You do not have permission to manage this user");
        }
    }

    private void authorizeRole(AuthPrincipal a, UUID targetId, UserRole role) {
        if (a.role() == UserRole.ADMIN) {
            return;
        }
        if (a.userId().equals(targetId) && role == a.role()) {
            return;
        }
        if (a.role() != UserRole.MANAGER || (role != UserRole.CUSTOMER && role != UserRole.DRIVER)) {
            throw new ForbiddenException("You cannot manage this role");
        }
    }

    private void protectLastAdmin(User u, UserRole role, UserStatus status) {
        if (u.getRole() == UserRole.ADMIN && (role != UserRole.ADMIN || status != UserStatus.ACTIVE)
                && users.countByOrganizationIdAndRoleAndStatus(u.getOrganization().getId(), UserRole.ADMIN,
                    UserStatus.ACTIVE) <= 1) {
            throw new ConflictException("The organization must have an active admin");
        }
    }
}