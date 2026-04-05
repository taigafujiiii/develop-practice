package com.example.ordersystem.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 受注明細1行分の入力値を受け取るクラス。
 * unit_price と subtotal はサーバーサイドで計算するためフォームには含まない。
 */
public class OrderDetailForm {

    @NotNull(message = "商品を選択してください")
    private Long productId;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上で入力してください")
    private Integer quantity;

    // ---------- getters / setters ----------

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
