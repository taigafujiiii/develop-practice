package com.example.ordersystem.service;

import com.example.ordersystem.entity.Customer;
import com.example.ordersystem.exception.ResourceNotFoundException;
import com.example.ordersystem.form.CustomerForm;
import com.example.ordersystem.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // -------------------------------------------------------------------------
    // search（UT-D-01・UT-D-02）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-D-01 search: nameがnullの場合はsearchByName(null)が呼ばれる（@QueryのIS NULL条件で全件返却）")
    void search_nullName_callsSearchByNameWithNull() {
        when(customerRepository.searchByName(null)).thenReturn(List.of());

        customerService.search(null);

        verify(customerRepository).searchByName(null);
        verify(customerRepository, never()).findAll();
    }

    @Test
    @DisplayName("UT-D-01 search: nameが空文字の場合もsearchByName(null)が呼ばれる（@QueryのIS NULL条件で全件返却）")
    void search_blankName_callsSearchByNameWithNull() {
        when(customerRepository.searchByName(null)).thenReturn(List.of());

        customerService.search("   ");

        verify(customerRepository).searchByName(null);
    }

    @Test
    @DisplayName("UT-D-02 search: nameがある場合はsearchByName(name)を呼ぶ")
    void search_withName_callsSearchByName() {
        Customer c = buildCustomer(1L, "田中 一郎");
        when(customerRepository.searchByName("田中")).thenReturn(List.of(c));

        List<Customer> result = customerService.search("田中");

        assertThat(result).containsExactly(c);
        verify(customerRepository).searchByName("田中");
    }

    // -------------------------------------------------------------------------
    // findById（UT-B-01）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-B-01 findById: 存在するIDで顧客エンティティを返す")
    void findById_found_returnsCustomer() {
        Customer c = buildCustomer(1L, "鈴木 花子");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));

        Customer result = customerService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerName()).isEqualTo("鈴木 花子");
    }

    @Test
    @DisplayName("findById: 存在しないIDでResourceNotFoundExceptionをスロー")
    void findById_notFound_throwsResourceNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // create（UT-B-02）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-B-02 create: フォームの内容がCustomerエンティティにコピーされて保存される")
    void create_savesCustomerWithFormData() {
        CustomerForm form = buildForm("佐藤 次郎", "090-0000-0001", "123-4567",
                "東京都渋谷区1-1", "sato@example.com");

        customerService.create(form);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer saved = captor.getValue();

        assertThat(saved.getCustomerName()).isEqualTo("佐藤 次郎");
        assertThat(saved.getPhone()).isEqualTo("090-0000-0001");
        assertThat(saved.getPostalCode()).isEqualTo("123-4567");
        assertThat(saved.getAddress()).isEqualTo("東京都渋谷区1-1");
        assertThat(saved.getEmail()).isEqualTo("sato@example.com");
    }

    // -------------------------------------------------------------------------
    // update（UT-B-03）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-B-03 update: 既存顧客のフィールドがフォームの内容で上書きされる")
    void update_updatesExistingCustomer() {
        Customer existing = buildCustomer(1L, "旧名前");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        CustomerForm form = buildForm("新名前", "080-1111-2222", "000-0000",
                "大阪府大阪市1-1", "new@example.com");
        customerService.update(1L, form);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer saved = captor.getValue();
        assertThat(saved.getCustomerName()).isEqualTo("新名前");
        assertThat(saved.getPhone()).isEqualTo("080-1111-2222");
    }

    @Test
    @DisplayName("update: 存在しないIDではResourceNotFoundExceptionをスロー")
    void update_notFound_throwsResourceNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        CustomerForm form = buildForm("誰か", null, null, null, null);
        assertThatThrownBy(() -> customerService.update(99L, form))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // delete（UT-B-04）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-B-04 delete: リポジトリのdeleteByIdを呼ぶ")
    void delete_callsDeleteById() {
        customerService.delete(1L);

        verify(customerRepository).deleteById(1L);
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

    private CustomerForm buildForm(String name, String phone, String postalCode,
                                   String address, String email) {
        CustomerForm f = new CustomerForm();
        f.setCustomerName(name);
        f.setPhone(phone);
        f.setPostalCode(postalCode);
        f.setAddress(address);
        f.setEmail(email);
        return f;
    }
}
