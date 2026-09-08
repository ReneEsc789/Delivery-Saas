package com.deliverysaas.products;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.domain.UserRole;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateProductRequest request) {
        requireCatalogManager(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(principal.organizationId(), request));
    }

    @GetMapping
    public List<ProductResponse> findAll(@AuthenticationPrincipal AuthPrincipal principal) {
        return productService.findAll(principal.organizationId());
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        return productService.findById(id, principal.organizationId());
    }

    @PutMapping("/{id}")
    public ProductResponse update(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        requireCatalogManager(principal);
        return productService.update(id, principal.organizationId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        requireCatalogManager(principal);
        productService.delete(id, principal.organizationId());
        return ResponseEntity.noContent().build();
    }

    private void requireCatalogManager(AuthPrincipal principal) {
        if (principal.role() != UserRole.ADMIN && principal.role() != UserRole.MANAGER) {
            throw new ForbiddenException("You do not have permission to manage products");
        }
    }
}
