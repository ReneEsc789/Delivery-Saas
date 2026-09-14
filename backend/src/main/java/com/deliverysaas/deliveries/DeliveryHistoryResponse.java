package com.deliverysaas.deliveries;
import java.time.Instant;import java.util.UUID;import com.deliverysaas.deliveries.domain.*;
public record DeliveryHistoryResponse(UUID id,DeliveryStatus previousStatus,DeliveryStatus newStatus,UUID changedBy,String notes,Instant createdAt){public static DeliveryHistoryResponse from(DeliveryStatusHistory h){return new DeliveryHistoryResponse(h.getId(),h.getPreviousStatus(),h.getNewStatus(),h.getChangedBy().getId(),h.getNotes(),h.getCreatedAt());}}
