package com.example.ordersystem.service;

import com.example.ordersystem.entity.Customer;
import com.example.ordersystem.entity.Order;
import com.example.ordersystem.entity.OrderDetail;
import com.example.ordersystem.entity.Product;
import com.example.ordersystem.exception.ResourceNotFoundException;
import com.example.ordersystem.form.OrderDetailForm;
import com.example.ordersystem.form.OrderForm;
import com.example.ordersystem.repository.CustomerRepository;
import com.example.ordersystem.repository.OrderDetailRepository;
import com.example.ordersystem.repository.OrderRepository;
import com.example.ordersystem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    /**
     * ステータス・受注日範囲で絞り込む。
     * 各パラメータが null または空の場合はその条件を無視する。
     *
     * <p>PostgreSQL は型不明の null パラメータを拒否するため、日付が null の場合は
     * sentinel 値（最小日付 / 最大日付）に変換してリポジトリへ渡す。
     */
    public List<Order> findByFilters(String status, LocalDate fromDate, LocalDate toDate) {
        String statusParam = (status != null && status.isBlank()) ? null : status;
        LocalDate from = (fromDate != null) ? fromDate : LocalDate.of(1900, 1, 1);
        LocalDate to   = (toDate   != null) ? toDate   : LocalDate.of(9999, 12, 31);
        return orderRepository.findByFilters(statusParam, from, to);
    }

    /**
     * ID で受注（顧客情報含む）を取得する。存在しない場合は {@link ResourceNotFoundException} をスローする。
     */
    public Order findById(Long id) {
        return orderRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new ResourceNotFoundException("受注が見つかりません ID: " + id));
    }

    /**
     * 受注ヘッダと明細を1トランザクションで登録する。
     *
     * <ol>
     *   <li>orders テーブルに total_amount=0 で INSERT → order.id が確定する</li>
     *   <li>明細ループ: 商品マスタから単価をコピーして order_details に INSERT</li>
     *   <li>orders テーブルの total_amount を確定値で UPDATE</li>
     * </ol>
     */
    @Transactional
    public void create(OrderForm form) {
        // ① 顧客の存在確認
        Customer customer = customerRepository.findById(form.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("顧客が見つかりません"));

        // ② 受注ヘッダを先に保存（order.id が確定する）
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(form.getOrderDate());
        order.setStatus("PENDING");
        order = orderRepository.save(order);

        // ③ 明細の保存と合計金額の積算
        int totalAmount = 0;
        for (OrderDetailForm detailForm : form.getDetails()) {
            Product product = productRepository.findById(detailForm.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません"));

            int subtotal = product.getUnitPrice() * detailForm.getQuantity();

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(detailForm.getQuantity());
            detail.setUnitPrice(product.getUnitPrice()); // 受注時点の単価をコピー
            detail.setSubtotal(subtotal);
            orderDetailRepository.save(detail);

            totalAmount += subtotal;
        }

        // ④ 合計金額を確定値で更新
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }
}
