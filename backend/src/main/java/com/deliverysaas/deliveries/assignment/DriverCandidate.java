package com.deliverysaas.deliveries.assignment;

import java.time.Instant;

import com.deliverysaas.drivers.domain.Driver;

public record DriverCandidate(Driver driver, double latitude, double longitude, Instant location, long deliveries) {}