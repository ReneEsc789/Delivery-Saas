package com.deliverysaas.vehicles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.deliverysaas.vehicles.domain.Vehicle;

import jakarta.persistence.LockModeType;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findAllByOrganizationId(UUID organizationId);

    Optional<Vehicle> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByPlateAndOrganizationId(String plate, UUID organizationId);

    boolean existsByPlateAndOrganizationIdAndIdNot(String plate, UUID organizationId, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicle v where v.id = :id and v.organization.id = :organizationId")
    Optional<Vehicle> findForUpdate(@Param("id") UUID id, @Param("organizationId") UUID organizationId);
}