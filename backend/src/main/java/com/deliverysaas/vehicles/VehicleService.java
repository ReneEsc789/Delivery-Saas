package com.deliverysaas.vehicles;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.audit.AuditService;
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
    private final AuditService auditService;

    public VehicleService(VehicleRepository vehicleRepository, OrganizationRepository organizationRepository,
            AuditService auditService) {
        this.vehicleRepository = vehicleRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public VehicleResponse create(UUID organizationId, CreateVehicleRequest request) {
        String plate = normalizedPlate(request.plate());
        ensurePlateAvailable(plate, organizationId, null);
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new NotFoundException("Organization not found"));
        Vehicle vehicle = new Vehicle(organization, plate, request.type(), request.capacity());
        vehicle.setModel(blankToNull(request.model()));
        vehicleRepository.save(vehicle);
        auditService.record("CREATE", "VEHICLE", vehicle.getId(), "Vehicle created");
        return VehicleResponse.from(vehicle);
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
        ensurePlateAvailable(plate, organizationId, id);
        vehicle.setPlate(plate);
        vehicle.setModel(blankToNull(request.model()));
        vehicle.setType(request.type());
        vehicle.setCapacity(request.capacity());
        vehicle.setStatus(request.status());
        auditService.record("UPDATE", "VEHICLE", vehicle.getId(), "Vehicle updated");
        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public void delete(UUID id, UUID organizationId) {
        Vehicle vehicle = findVehicle(id, organizationId);
        vehicle.setStatus(VehicleStatus.INACTIVE);
        auditService.record("DEACTIVATE", "VEHICLE", vehicle.getId(), "Vehicle deactivated");
    }

    private Vehicle findVehicle(UUID id, UUID organizationId) {
        return vehicleRepository.findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    }

    private void ensurePlateAvailable(String plate, UUID organizationId, UUID vehicleId) {
        boolean exists = vehicleId == null
            ? vehicleRepository.existsByPlateAndOrganizationId(plate, organizationId)
            : vehicleRepository.existsByPlateAndOrganizationIdAndIdNot(plate, organizationId, vehicleId);
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
