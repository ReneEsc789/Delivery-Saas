package com.deliverysaas.organizations;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverysaas.organizations.domain.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsBySlug(String slug);
}
