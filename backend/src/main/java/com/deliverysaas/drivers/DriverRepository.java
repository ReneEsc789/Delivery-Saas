package com.deliverysaas.drivers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.drivers.domain.Driver;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    List<Driver> findAllByUserOrganizationId(UUID organizationId);

    Optional<Driver> findByIdAndUserOrganizationId(UUID id, UUID organizationId);

    boolean existsByUserId(UUID userId);

    boolean existsByLicenseNumberAndOrganizationId(String licenseNumber, UUID organizationId);

    boolean existsByLicenseNumberAndOrganizationIdAndIdNot(String licenseNumber, UUID organizationId, UUID id);
}
