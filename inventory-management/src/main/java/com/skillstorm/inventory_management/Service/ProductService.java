package com.skillstorm.inventory_management.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillstorm.inventory_management.Model.Department;
import com.skillstorm.inventory_management.Model.Product;
import com.skillstorm.inventory_management.Repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Finds existing product by SKU or creates new one if it doesn't exist
     * @param sku         product SKU
     * @param name        product name 
     * @param description product description
     * @param category    product category or department
     * @return existing product if found by SKU, otherwise a newly created product
     * @throws IllegalArgumentException if SKU or name are null or blank
     */
    @Transactional
    public Product findOrCreateProductBySku(String sku,
                                            String name,
                                            String description,
                                            Department category) {
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

    /**
     * Retrieves all products 
     * @return list of products 
     */
    public List<Product> findAllProducts() {
    return productRepository.findAll();
    }

    /**
     * Finds a product by primary key
     * @param id product id
     * @return product if found, null if doesn't exist 
     */
    public Product findById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    /**
     * Finds a product by SKU
     * @param sku SKU value
     * @return Product if found, null if it doesn't exist 
     */
    public Product findBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return null;
        }
        return productRepository.findBySkuIgnoreCase(sku.trim()).orElse(null);
    }

}
