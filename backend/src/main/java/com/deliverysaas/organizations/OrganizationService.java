package com.deliverysaas.organizations;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.organizations.domain.Organization;
import com.deliverysaas.organizations.domain.OrganizationStatus;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.audit.AuditService;

@Service
public class OrganizationService {
    private final OrganizationRepository repository;
    private final AuditService audit;

    public OrganizationService(OrganizationRepository repository, AuditService audit) { this.repository = repository; this.audit = audit; }

    @Transactional(readOnly = true)
    public OrganizationResponse find(UUID id) { return OrganizationResponse.from(get(id)); }

    @Transactional
    public OrganizationResponse update(UUID id, UpdateOrganizationRequest request) {
        Organization organization = get(id);
        organization.setName(request.name().trim());
        audit.record("UPDATE", "ORGANIZATION", organization.getId(), "Organization updated");
        return OrganizationResponse.from(organization);
    }

    @Transactional
    public void delete(UUID id) { Organization o=get(id);o.setStatus(OrganizationStatus.INACTIVE);audit.record("DEACTIVATE", "ORGANIZATION", o.getId(), "Organization deactivated"); }

    private Organization get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Organization not found"));
    }
}
