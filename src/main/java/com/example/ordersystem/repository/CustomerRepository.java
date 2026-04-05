package com.example.ordersystem.repository;

import com.example.ordersystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 顧客名の部分一致検索。
     * name が null の場合は IS NULL 条件が真になり全件返却される。
     * これにより「検索あり/なし」を1メソッドで処理できる。
     */
    @Query("SELECT c FROM Customer c WHERE (:name IS NULL OR c.customerName LIKE %:name%) ORDER BY c.id")
    List<Customer> searchByName(@Param("name") String name);
}
