package com.deliverysaas.vehicles;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.organizations.OrganizationRepository;
import com.deliverysaas.organizations.domain.Organization;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.vehicles.domain.Vehicle;
import com.deliverysaas.vehicles.domain.VehicleStatus;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final OrganizationRepository organizationRepository;

    public VehicleService(VehicleRepository vehicleRepository, OrganizationRepository organizationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public VehicleResponse create(UUID organizationId, CreateVehicleRequest request) {
        String plate = normalizedPlate(request.plate());
        ensurePlateAvailable(plate, null);
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new NotFoundException("Organization not found"));
        Vehicle vehicle = new Vehicle(organization, plate, request.type(), request.capacity());
        vehicle.setModel(blankToNull(request.model()));
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll(UUID organizationId) {
        return vehicleRepository.findAllByOrganizationId(organizationId).stream()
            .map(VehicleResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(UUID id, UUID organizationId) {
        return VehicleResponse.from(findVehicle(id, organizationId));
    }

    @Transactional
    public VehicleResponse update(UUID id, UUID organizationId, UpdateVehicleRequest request) {
        Vehicle vehicle = findVehicle(id, organizationId);
        String plate = normalizedPlate(request.plate());
        ensurePlateAvailable(plate, id);
        vehicle.setPlate(plate);
        vehicle.setModel(blankToNull(request.model()));
        vehicle.setType(request.type());
        vehicle.setCapacity(request.capacity());
        vehicle.setStatus(request.status());
        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public void delete(UUID id, UUID organizationId) {
        findVehicle(id, organizationId).setStatus(VehicleStatus.INACTIVE);
    }

    private Vehicle findVehicle(UUID id, UUID organizationId) {
        return vehicleRepository.findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    }

    private void ensurePlateAvailable(String plate, UUID vehicleId) {
        boolean exists = vehicleId == null
            ? vehicleRepository.existsByPlate(plate)
            : vehicleRepository.existsByPlateAndIdNot(plate, vehicleId);
        if (exists) {
            throw new ConflictException("A vehicle with this plate already exists");
        }
    }

    private String normalizedPlate(String plate) {
        return plate.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
