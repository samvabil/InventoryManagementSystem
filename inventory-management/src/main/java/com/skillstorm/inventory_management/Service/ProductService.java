package com.skillstorm.inventory_management.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillstorm.inventory_management.Model.Product;
import com.skillstorm.inventory_management.Repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product findOrCreateProductBySku(String sku,
                                            String name,
                                            String description,
                                            String category) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        String trimmedSku = sku.trim();

        Optional<Product> existing = productRepository.findBySkuIgnoreCase(trimmedSku);
        if (existing.isPresent()) {
            return existing.get();
        }

        Product product = new Product();
        product.setSku(trimmedSku);
        product.setName(name.trim());
        product.setDescription(description);
        product.setCategory(category);

        return productRepository.save(product);
    }

    public Product findById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product findBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return null;
        }
        return productRepository.findBySkuIgnoreCase(sku.trim()).orElse(null);
    }
}
