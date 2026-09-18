package com.deliverysaas.drivers;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.audit.AuditService;
import com.deliverysaas.drivers.domain.Driver;
import com.deliverysaas.drivers.domain.DriverStatus;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.users.UserRepository;
import com.deliverysaas.users.domain.User;
import com.deliverysaas.users.domain.UserRole;
import com.deliverysaas.users.domain.UserStatus;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public DriverService(DriverRepository driverRepository, UserRepository userRepository, AuditService auditService) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public DriverResponse create(UUID organizationId, CreateDriverRequest request) {
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));
        ensureEligibleDriverUser(user, organizationId);
        if (driverRepository.existsByUserId(user.getId())) {
            throw new ConflictException("This user already has a driver profile");
        }
        String licenseNumber = normalizedLicense(request.licenseNumber());
        ensureLicenseAvailable(licenseNumber, organizationId, null);
        Driver driver = new Driver(user, user.getOrganization(), licenseNumber, request.phone().trim());
        driverRepository.save(driver);
        auditService.record("CREATE", "DRIVER", driver.getId(), "Driver profile created");
        return DriverResponse.from(driver);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll(UUID organizationId) {
        return driverRepository.findAllByUserOrganizationId(organizationId).stream()
            .map(DriverResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(UUID id, UUID organizationId) {
        return DriverResponse.from(findDriver(id, organizationId));
    }

    @Transactional
    public DriverResponse update(UUID id, UUID organizationId, UpdateDriverRequest request) {
        Driver driver = findDriver(id, organizationId);
        String licenseNumber = normalizedLicense(request.licenseNumber());
        ensureLicenseAvailable(licenseNumber, organizationId, id);
        driver.setLicenseNumber(licenseNumber);
        driver.setPhone(request.phone().trim());
        driver.setStatus(request.status());
        auditService.record("UPDATE", "DRIVER", driver.getId(), "Driver profile updated");
        return DriverResponse.from(driver);
    }

    @Transactional
    public void delete(UUID id, UUID organizationId) {
        Driver driver = findDriver(id, organizationId);
        driver.setStatus(DriverStatus.SUSPENDED);
        auditService.record("DEACTIVATE", "DRIVER", driver.getId(), "Driver suspended");
    }

    private Driver findDriver(UUID id, UUID organizationId) {
        return driverRepository.findByIdAndUserOrganizationId(id, organizationId)
            .orElseThrow(() -> new NotFoundException("Driver not found"));
    }

    private void ensureEligibleDriverUser(User user, UUID organizationId) {
        if (!user.getOrganization().getId().equals(organizationId)) {
            throw new NotFoundException("User not found");
        }
        if (user.getRole() != UserRole.DRIVER) {
            throw new ConflictException("The user must have the DRIVER role");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ConflictException("The user must be active to become a driver");
        }
    }

    private void ensureLicenseAvailable(String licenseNumber, UUID organizationId, UUID driverId) {
        boolean exists = driverId == null
            ? driverRepository.existsByLicenseNumberAndOrganizationId(licenseNumber, organizationId)
            : driverRepository.existsByLicenseNumberAndOrganizationIdAndIdNot(licenseNumber, organizationId, driverId);
        if (exists) {
            throw new ConflictException("A driver with this license number already exists");
        }
    }

    private String normalizedLicense(String licenseNumber) {
        return licenseNumber.trim().toUpperCase(Locale.ROOT);
    }
}
