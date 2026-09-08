package com.deliverysaas.vehicles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.vehicles.domain.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findAllByOrganizationId(UUID organizationId);

    Optional<Vehicle> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByPlate(String plate);

    boolean existsByPlateAndIdNot(String plate, UUID id);
}
