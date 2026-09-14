package com.deliverysaas.organizations;

import java.time.Instant;
import java.util.UUID;
import com.deliverysaas.organizations.domain.Organization;
import com.deliverysaas.organizations.domain.OrganizationStatus;

public record OrganizationResponse(UUID id, String name, String slug, OrganizationStatus status,
        Instant createdAt, Instant updatedAt) {
    public static OrganizationResponse from(Organization value) {
        return new OrganizationResponse(value.getId(), value.getName(), value.getSlug(), value.getStatus(),
            value.getCreatedAt(), value.getUpdatedAt());
    }
}
