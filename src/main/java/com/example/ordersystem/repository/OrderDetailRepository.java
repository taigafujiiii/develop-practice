package com.example.ordersystem.repository;

import com.example.ordersystem.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    // 基本的な CRUD は JpaRepository が提供するため追加メソッド不要
}
