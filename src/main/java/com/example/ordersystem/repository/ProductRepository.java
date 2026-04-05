package com.example.ordersystem.repository;

import com.example.ordersystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * カテゴリによる絞り込み検索。
     * category が null の場合は IS NULL 条件が真になり全件返却される。
     */
    @Query("SELECT p FROM Product p WHERE (:category IS NULL OR p.category = :category) ORDER BY p.id")
    List<Product> findByCategory(@Param("category") String category);
}
