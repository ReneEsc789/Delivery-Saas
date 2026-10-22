package com.deliverysaas.deliveries.prioritization;

import com.deliverysaas.deliveries.domain.Delivery;

public record PrioritizedDelivery(Delivery delivery, double score) {}