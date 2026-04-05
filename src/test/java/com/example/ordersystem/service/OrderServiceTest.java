package com.example.ordersystem.service;

import com.example.ordersystem.entity.Customer;
import com.example.ordersystem.entity.Order;
import com.example.ordersystem.entity.Product;
import com.example.ordersystem.exception.ResourceNotFoundException;
import com.example.ordersystem.form.OrderDetailForm;
import com.example.ordersystem.form.OrderForm;
import com.example.ordersystem.repository.CustomerRepository;
import com.example.ordersystem.repository.OrderDetailRepository;
import com.example.ordersystem.repository.OrderRepository;
import com.example.ordersystem.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    // -------------------------------------------------------------------------
    // findByFilters（UT-D-05・UT-D-06・UT-F-01〜UT-F-03）
    // -------------------------------------------------------------------------

    // sentinel 日付（null の代わりに渡される境界値）
    private static final LocalDate DATE_MIN = LocalDate.of(1900,  1,  1);
    private static final LocalDate DATE_MAX = LocalDate.of(9999, 12, 31);

    @Test
    @DisplayName("UT-D-05 findByFilters: statusのみ指定 → 日付はsentinel値に変換してfindByFiltersへ委譲する")
    void findByFilters_statusOnly_callsRepository() {
        Order o = new Order();
        when(orderRepository.findByFilters("PENDING", DATE_MIN, DATE_MAX)).thenReturn(List.of(o));

        List<Order> result = orderService.findByFilters("PENDING", null, null);

        assertThat(result).containsExactly(o);
        verify(orderRepository).findByFilters("PENDING", DATE_MIN, DATE_MAX);
    }

    @Test
    @DisplayName("UT-D-06 findByFilters: statusが空文字の場合はnullに変換してfindByFiltersを呼ぶ（@QueryのIS NULL条件で全件返却）")
    void findByFilters_blankStatus_convertsToNull() {
        when(orderRepository.findByFilters(null, DATE_MIN, DATE_MAX)).thenReturn(List.of());

        orderService.findByFilters("   ", null, null);

        verify(orderRepository).findByFilters(null, DATE_MIN, DATE_MAX);
    }

    @Test
    @DisplayName("UT-F-01 findByFilters: status + fromDate + toDate を指定 → 指定条件で絞り込まれる")
    void findByFilters_allFilters_callsRepository() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to   = LocalDate.of(2026, 3, 31);
        Order o = new Order();
        when(orderRepository.findByFilters("PENDING", from, to)).thenReturn(List.of(o));

        List<Order> result = orderService.findByFilters("PENDING", from, to);

        assertThat(result).containsExactly(o);
        verify(orderRepository).findByFilters("PENDING", from, to);
    }

    @Test
    @DisplayName("UT-F-02 findByFilters: fromDate + toDate のみ指定（status=null）→ 日付範囲で絞り込まれる")
    void findByFilters_dateRangeOnly_callsRepository() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to   = LocalDate.of(2026, 3, 31);
        Order o = new Order();
        when(orderRepository.findByFilters(null, from, to)).thenReturn(List.of(o));

        List<Order> result = orderService.findByFilters(null, from, to);

        assertThat(result).containsExactly(o);
        verify(orderRepository).findByFilters(null, from, to);
    }

    @Test
    @DisplayName("UT-F-03 findByFilters: 引数がすべてnull → 日付はsentinel値に変換してリポジトリを呼ぶ（全件返却）")
    void findByFilters_allNull_returnsAllOrders() {
        Order o1 = new Order();
        Order o2 = new Order();
        when(orderRepository.findByFilters(null, DATE_MIN, DATE_MAX)).thenReturn(List.of(o1, o2));

        List<Order> result = orderService.findByFilters(null, null, null);

        assertThat(result).containsExactly(o1, o2);
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById: 存在するIDで受注エンティティを返す")
    void findById_found_returnsOrder() {
        Order o = new Order();
        when(orderRepository.findByIdWithCustomer(1L)).thenReturn(Optional.of(o));

        assertThat(orderService.findById(1L)).isEqualTo(o);
    }

    @Test
    @DisplayName("UT-C-05 findById: 存在しないIDでResourceNotFoundExceptionをスロー")
    void findById_notFound_throwsResourceNotFoundException() {
        when(orderRepository.findByIdWithCustomer(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // create（UT-C-01・UT-C-06・UT-C-07）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-C-01 create: 受注ヘッダと明細が保存され、total_amountが設定される（save 2回）")
    void create_savesOrderWithDetailsAndTotalAmount() {
        Customer customer = buildCustomer(1L, "山田 太郎");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Product productA = buildProduct(10L, "ノートPC", 100000);
        Product productB = buildProduct(11L, "マウス", 3000);
        when(productRepository.findById(10L)).thenReturn(Optional.of(productA));
        when(productRepository.findById(11L)).thenReturn(Optional.of(productB));

        // thenAnswer で引数をそのまま返す（別インスタンスが返ると後続の setTotalAmount が反映されない）
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderForm form = buildOrderForm(1L,
                buildDetailForm(10L, 2),   // 100000 × 2 = 200000
                buildDetailForm(11L, 3));  // 3000   × 3 =   9000

        orderService.create(form);

        // save(Order) は2回: ①ヘッダ保存、②totalAmount更新
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getAllValues().get(1);
        assertThat(savedOrder.getTotalAmount()).isEqualTo(209000);

        verify(orderDetailRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("UT-C-06 create: 存在しない顧客IDではResourceNotFoundExceptionをスロー")
    void create_customerNotFound_throwsResourceNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        OrderForm form = buildOrderForm(99L, buildDetailForm(1L, 1));

        assertThatThrownBy(() -> orderService.create(form))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("顧客");
    }

    @Test
    @DisplayName("UT-C-07 create: 明細2件の合計金額が正しく計算される（1000+1500=2500）")
    void create_twoDetails_correctTotalAmount() {
        Customer customer = buildCustomer(1L, "田中 花子");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Product productA = buildProduct(1L, "商品A", 1000);
        Product productB = buildProduct(2L, "商品B", 1500);
        when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
        when(productRepository.findById(2L)).thenReturn(Optional.of(productB));

        OrderForm form = buildOrderForm(1L,
                buildDetailForm(1L, 1),   // 1000 × 1 = 1000
                buildDetailForm(2L, 1));  // 1500 × 1 = 1500

        orderService.create(form);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getTotalAmount()).isEqualTo(2500);
    }

    @Test
    @DisplayName("create: 存在しない商品IDではResourceNotFoundExceptionをスロー")
    void create_productNotFound_throwsResourceNotFoundException() {
        Customer customer = buildCustomer(1L, "鈴木 次郎");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        OrderForm form = buildOrderForm(1L, buildDetailForm(99L, 1));

        assertThatThrownBy(() -> orderService.create(form))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("商品");
    }

    @Test
    @DisplayName("create: ステータスはPENDINGで初期化される")
    void create_initialStatusIsPending() {
        Customer customer = buildCustomer(1L, "鈴木 一郎");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Product product = buildProduct(1L, "消しゴム", 100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderForm form = buildOrderForm(1L, buildDetailForm(1L, 1));
        orderService.create(form);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(captor.capture());
        // 1回目のsaveでステータスがPENDINGに設定されている
        assertThat(captor.getAllValues().get(0).getStatus()).isEqualTo("PENDING");
    }

    // -------------------------------------------------------------------------
    // ヘルパーメソッド
    // -------------------------------------------------------------------------

    private Customer buildCustomer(Long id, String name) {
        Customer c = new Customer();
        c.setId(id);
        c.setCustomerName(name);
        return c;
    }

    private Product buildProduct(Long id, String name, int unitPrice) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setUnitPrice(unitPrice);
        return p;
    }

    private OrderDetailForm buildDetailForm(Long productId, int quantity) {
        OrderDetailForm df = new OrderDetailForm();
        df.setProductId(productId);
        df.setQuantity(quantity);
        return df;
    }

    private OrderForm buildOrderForm(Long customerId, OrderDetailForm... details) {
        OrderForm form = new OrderForm();
        form.setCustomerId(customerId);
        form.setOrderDate(LocalDate.of(2026, 3, 29));
        form.setDetails(List.of(details));
        return form;
    }
}
