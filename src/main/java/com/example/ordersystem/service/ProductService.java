package com.example.ordersystem.service;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.exception.ResourceNotFoundException;
import com.example.ordersystem.form.ProductForm;
import com.example.ordersystem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * カテゴリで絞り込む。category が null または空文字の場合は全件返却する。
     */
    public List<Product> findByCategory(String category) {
        if (category == null || category.isBlank()) {
            return productRepository.findByCategory(null);
        }
        return productRepository.findByCategory(category);
    }

    /**
     * 全商品を返す（受注登録フォームのプルダウン用）。
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * ID で商品を取得する。存在しない場合は {@link ResourceNotFoundException} をスローする。
     */
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません ID: " + id));
    }

    @Transactional
    public void create(ProductForm form) {
        Product product = new Product();
        copyFormToEntity(form, product);
        productRepository.save(product);
    }

    @Transactional
    public void update(Long id, ProductForm form) {
        Product product = findById(id);
        copyFormToEntity(form, product);
        productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    private void copyFormToEntity(ProductForm form, Product product) {
        product.setProductName(form.getProductName());
        product.setCategory(form.getCategory());
        product.setUnitPrice(form.getUnitPrice());
        product.setStockQuantity(form.getStockQuantity());
    }
}
