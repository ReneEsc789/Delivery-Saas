package com.deliverysaas.drivers;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.deliverysaas.drivers.domain.Driver;
import jakarta.persistence.LockModeType;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    List<Driver> findAllByUserOrganizationId(UUID organizationId);

    Optional<Driver> findByIdAndUserOrganizationId(UUID id, UUID organizationId);
    Optional<Driver> findByUserIdAndUserOrganizationId(UUID userId, UUID organizationId);

    boolean existsByUserId(UUID userId);

    boolean existsByLicenseNumberAndOrganizationId(String licenseNumber, UUID organizationId);

    boolean existsByLicenseNumberAndOrganizationIdAndIdNot(String licenseNumber, UUID organizationId, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Driver d where d.id = :id and d.user.organization.id = :organizationId")
    Optional<Driver> findForUpdate(@Param("id") UUID id, @Param("organizationId") UUID organizationId);
}