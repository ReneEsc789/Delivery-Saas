package com.deliverysaas.deliveries.prioritization;

import java.util.List;

import com.deliverysaas.deliveries.DeliveryResponse;

public record DispatchResponse(List<DeliveryResponse> assigned, List<SkippedDelivery> skipped) {
}
