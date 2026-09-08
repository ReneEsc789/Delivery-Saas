package com.deliverysaas.warehouses;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.products.ProductRepository;
import com.deliverysaas.products.domain.Product;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.warehouses.domain.Warehouse;
import com.deliverysaas.warehouses.domain.WarehouseInventory;
import com.deliverysaas.warehouses.domain.WarehouseStatus;

@Service
public class WarehouseInventoryService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final WarehouseInventoryRepository inventoryRepository;

    public WarehouseInventoryService(WarehouseRepository warehouseRepository, ProductRepository productRepository,
            WarehouseInventoryRepository inventoryRepository) {
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public WarehouseInventoryResponse create(UUID warehouseId, UUID organizationId,
            CreateWarehouseInventoryRequest request) {
        Warehouse warehouse = findWarehouse(warehouseId, organizationId);
        ensureWarehouseActive(warehouse);
        Product product = findProduct(request.productId(), organizationId);
        ensureProductActive(product);
        if (inventoryRepository.findByWarehouseIdAndProductId(warehouseId, product.getId()).isPresent()) {
            throw new ConflictException("This product is already registered in the warehouse inventory");
        }
        WarehouseInventory inventory = new WarehouseInventory(warehouse, product);
        inventory.setQuantity(request.quantity());
        return WarehouseInventoryResponse.from(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public List<WarehouseInventoryResponse> findAll(UUID warehouseId, UUID organizationId) {
        findWarehouse(warehouseId, organizationId);
        return inventoryRepository.findAllByWarehouseId(warehouseId).stream()
            .map(WarehouseInventoryResponse::from)
            .toList();
    }

    @Transactional
    public WarehouseInventoryResponse update(UUID warehouseId, UUID productId, UUID organizationId,
            UpdateWarehouseInventoryRequest request) {
        Warehouse warehouse = findWarehouse(warehouseId, organizationId);
        ensureWarehouseActive(warehouse);
        WarehouseInventory inventory = findInventory(warehouseId, productId);
        inventory.setQuantity(request.quantity());
        return WarehouseInventoryResponse.from(inventory);
    }

    @Transactional
    public void delete(UUID warehouseId, UUID productId, UUID organizationId) {
        findWarehouse(warehouseId, organizationId);
        inventoryRepository.delete(findInventory(warehouseId, productId));
    }

    private Warehouse findWarehouse(UUID id, UUID organizationId) {
        return warehouseRepository.findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new NotFoundException("Warehouse not found"));
    }

    private Product findProduct(UUID id, UUID organizationId) {
        return productRepository.findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private WarehouseInventory findInventory(UUID warehouseId, UUID productId) {
        return inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId)
            .orElseThrow(() -> new NotFoundException("Inventory record not found"));
    }

    private void ensureWarehouseActive(Warehouse warehouse) {
        if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new ConflictException("Cannot modify inventory in an inactive warehouse");
        }
    }

    private void ensureProductActive(Product product) {
        if (!product.isActive()) {
            throw new ConflictException("Cannot add an inactive product to inventory");
        }
    }
}
