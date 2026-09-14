package com.deliverysaas.drivers;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.deliverysaas.drivers.domain.DriverLocation;
public interface DriverLocationRepository extends JpaRepository<DriverLocation,UUID>{List<DriverLocation> findAllByDriverIdOrderByRecordedAtDesc(UUID driverId);Optional<DriverLocation> findFirstByDriverIdOrderByRecordedAtDesc(UUID driverId);}
