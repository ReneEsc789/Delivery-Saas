package com.deliverysaas.products;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.audit.AuditService;
import com.deliverysaas.organizations.OrganizationRepository;
import com.deliverysaas.organizations.domain.Organization;
import com.deliverysaas.products.domain.Product;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.NotFoundException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository, OrganizationRepository organizationRepository,
            AuditService auditService) {
        this.productRepository = productRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ProductResponse create(UUID organizationId, CreateProductRequest request) {
        String sku = normalizedSku(request.sku());
        ensureSkuAvailable(sku, organizationId, null);
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new NotFoundException("Organization not found"));
        Product product = new Product(organization, request.name().trim(), sku,
            blankToNull(request.description()), request.price(), request.weight());
        productRepository.save(product);
        auditService.record("CREATE", "PRODUCT", product.getId(), "Product created");
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(UUID organizationId) {
        return productRepository.findAllByOrganizationId(organizationId).stream()
            .map(ProductResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id, UUID organizationId) {
        return ProductResponse.from(findProduct(id, organizationId));
    }

    @Transactional
    public ProductResponse update(UUID id, UUID organizationId, UpdateProductRequest request) {
        Product product = findProduct(id, organizationId);
        String sku = normalizedSku(request.sku());
        ensureSkuAvailable(sku, organizationId, id);
        product.setName(request.name().trim());
        product.setSku(sku);
        product.setDescription(blankToNull(request.description()));
        product.setPrice(request.price());
        product.setWeight(request.weight());
        product.setActive(request.active());
        auditService.record("UPDATE", "PRODUCT", product.getId(), "Product updated");
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(UUID id, UUID organizationId) {
        Product product = findProduct(id, organizationId);
        product.setActive(false);
        auditService.record("DEACTIVATE", "PRODUCT", product.getId(), "Product deactivated");
    }

    private Product findProduct(UUID id, UUID organizationId) {
        return productRepository.findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private void ensureSkuAvailable(String sku, UUID organizationId, UUID productId) {
        boolean exists = productId == null
            ? productRepository.existsBySkuAndOrganizationId(sku, organizationId)
            : productRepository.existsBySkuAndOrganizationIdAndIdNot(sku, organizationId, productId);
        if (exists) {
            throw new ConflictException("A product with this SKU already exists");
        }
    }

    private String normalizedSku(String sku) {
        return sku.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
