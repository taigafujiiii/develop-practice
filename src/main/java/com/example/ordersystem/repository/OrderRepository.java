package com.example.ordersystem.repository;

import com.example.ordersystem.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * ステータス・受注日範囲による絞り込み検索。
     *
     * <p>PostgreSQL は null パラメータの型を推論できないため、日付条件には
     * sentinel 値（fromDate=1900-01-01、toDate=9999-12-31）を使用する。
     * null の場合は呼び出し元（{@link com.example.ordersystem.service.OrderService}）で
     * sentinel 値に変換してから渡すこと。
     *
     * <p>ステータスは文字列なので PostgreSQL が型を推論できるため IS NULL 判定が使える。
     *
     * <p>JOIN FETCH で Customer を一緒に取得することで N+1 問題を防ぐ。
     */
    @Query("""
            SELECT o FROM Order o JOIN FETCH o.customer
            WHERE (:status IS NULL OR o.status = :status)
              AND o.orderDate >= :fromDate
              AND o.orderDate <= :toDate
            ORDER BY o.id DESC
            """)
    List<Order> findByFilters(@Param("status")   String status,
                              @Param("fromDate")  LocalDate fromDate,
                              @Param("toDate")    LocalDate toDate);

    /**
     * 受注詳細画面用。Customer を JOIN FETCH して取得する。
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithCustomer(@Param("id") Long id);
}
