package com.deliverysaas.warehouses;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.organizations.OrganizationRepository;
import com.deliverysaas.organizations.domain.Organization;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.warehouses.domain.Warehouse;
import com.deliverysaas.warehouses.domain.WarehouseStatus;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final OrganizationRepository organizationRepository;

    public WarehouseService(WarehouseRepository warehouseRepository, OrganizationRepository organizationRepository) {
        this.warehouseRepository = warehouseRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public WarehouseResponse create(UUID organizationId, CreateWarehouseRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new NotFoundException("Organization not found"));
        Warehouse warehouse = new Warehouse(organization, request.name().trim(), request.latitude(), request.longitude());
        applyAddress(warehouse, request.address(), request.city(), request.state(), request.postalCode());
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> findAll(UUID organizationId) {
        return warehouseRepository.findAllByOrganizationId(organizationId).stream()
            .map(WarehouseResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse findById(UUID id, UUID organizationId) {
        return WarehouseResponse.from(findWarehouse(id, organizationId));
    }

    @Transactional
    public WarehouseResponse update(UUID id, UUID organizationId, UpdateWarehouseRequest request) {
        Warehouse warehouse = findWarehouse(id, organizationId);
        warehouse.setName(request.name().trim());
        warehouse.setLatitude(request.latitude());
        warehouse.setLongitude(request.longitude());
        warehouse.setStatus(request.status());
        applyAddress(warehouse, request.address(), request.city(), request.state(), request.postalCode());
        return WarehouseResponse.from(warehouse);
    }

    @Transactional
    public void delete(UUID id, UUID organizationId) {
        findWarehouse(id, organizationId).setStatus(WarehouseStatus.INACTIVE);
    }

    private Warehouse findWarehouse(UUID id, UUID organizationId) {
        return warehouseRepository.findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new NotFoundException("Warehouse not found"));
    }

    private void applyAddress(Warehouse warehouse, String address, String city, String state, String postalCode) {
        warehouse.setAddress(blankToNull(address));
        warehouse.setCity(blankToNull(city));
        warehouse.setState(blankToNull(state));
        warehouse.setPostalCode(blankToNull(postalCode));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
