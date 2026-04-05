package com.example.ordersystem.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 受注登録フォームの入力値を受け取るクラス。
 * status と total_amount はサーバーサイドで自動設定するためフォームには含まない。
 */
public class OrderForm {

    @NotNull(message = "顧客を選択してください")
    private Long customerId;

    @NotNull(message = "受注日は必須です")
    private LocalDate orderDate;

    @Valid
    private List<OrderDetailForm> details = new ArrayList<>();

    // ---------- getters / setters ----------

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public List<OrderDetailForm> getDetails() { return details; }
    public void setDetails(List<OrderDetailForm> details) { this.details = details; }
}
