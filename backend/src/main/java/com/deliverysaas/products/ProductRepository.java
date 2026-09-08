package com.deliverysaas.products;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.products.domain.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByOrganizationId(UUID organizationId);

    Optional<Product> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsBySkuAndOrganizationId(String sku, UUID organizationId);

    boolean existsBySkuAndOrganizationIdAndIdNot(String sku, UUID organizationId, UUID id);
}
