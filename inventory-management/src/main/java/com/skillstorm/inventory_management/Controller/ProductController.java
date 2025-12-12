package com.skillstorm.inventory_management.Controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillstorm.inventory_management.DTO.ProductCreateRequest;
import com.skillstorm.inventory_management.Model.Department;
import com.skillstorm.inventory_management.Model.Product;
import com.skillstorm.inventory_management.Service.ProductService;

@RestController
@RequestMapping("/products")
@CrossOrigin("*") // for development
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves all products
     * @return list of products with HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Product>> findAllProducts() {
        List<Product> products = productService.findAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Retrieves a product by its ID
     * @param id product ID
     * @return product with HTTP 200 or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> findProductById(@PathVariable int id) {
        Product product = productService.findById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    /**
     * Retrieves a product by its SKU
     * @param sku product SKU
     * @return product with HTTP 200 or HTTP 404 if not found
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> findProductBySku(@PathVariable String sku) {
        Product product = productService.findBySku(sku);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    /**
     * Creates or retrieves a product by SKU
     * If a product with this SKU already exists, that product is returned
     * @param request product data
     * @return existing or newly created product with HTTP 201
     */
    @PostMapping
    public ResponseEntity<Product> createOrGetProduct(@RequestBody ProductCreateRequest request) {
        Product product = productService.findOrCreateProductBySku(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getCategory()
        );
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    /**
     * Returns all available departments for populating dropdowns
     * @return list of department enum values with HTTP 200
     */
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return new ResponseEntity<>(Arrays.asList(Department.values()), HttpStatus.OK);
    }
}
