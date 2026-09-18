package com.deliverysaas.users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.users.domain.User;
import com.deliverysaas.users.domain.UserRole;
import com.deliverysaas.users.domain.UserStatus;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByOrganizationId(UUID organizationId);
    @EntityGraph(attributePaths = "organization")
    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);
    long countByOrganizationIdAndRoleAndStatus(UUID organizationId, UserRole role, UserStatus status);
}
