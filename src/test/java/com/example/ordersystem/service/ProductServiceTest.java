package com.example.ordersystem.service;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.exception.ResourceNotFoundException;
import com.example.ordersystem.form.ProductForm;
import com.example.ordersystem.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // -------------------------------------------------------------------------
    // findByCategory（UT-D-03・UT-D-04）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-D-03 findByCategory: categoryがnullの場合はfindByCategory(null)が呼ばれる（@QueryのIS NULL条件で全件返却）")
    void findByCategory_nullCategory_callsFindByCategoryWithNull() {
        when(productRepository.findByCategory(null)).thenReturn(List.of());

        productService.findByCategory(null);

        verify(productRepository).findByCategory(null);
        verify(productRepository, never()).findAll();
    }

    @Test
    @DisplayName("UT-D-03 findByCategory: categoryが空文字の場合もfindByCategory(null)が呼ばれる（@QueryのIS NULL条件で全件返却）")
    void findByCategory_blankCategory_callsFindByCategoryWithNull() {
        when(productRepository.findByCategory(null)).thenReturn(List.of());

        productService.findByCategory("  ");

        verify(productRepository).findByCategory(null);
    }

    @Test
    @DisplayName("UT-D-04 findByCategory: categoryがある場合はリポジトリのfindByCategoryを呼ぶ")
    void findByCategory_withCategory_callsRepository() {
        Product p = buildProduct(1L, "ノートPC", 150000, "電子機器");
        when(productRepository.findByCategory("電子機器")).thenReturn(List.of(p));

        List<Product> result = productService.findByCategory("電子機器");

        assertThat(result).containsExactly(p);
        verify(productRepository).findByCategory("電子機器");
    }

    // -------------------------------------------------------------------------
    // findById（UT-B-05）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-B-05 findById: 存在するIDで商品エンティティを返す")
    void findById_found_returnsProduct() {
        Product p = buildProduct(1L, "マウス", 3000, "周辺機器");
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        Product result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("マウス");
    }

    @Test
    @DisplayName("findById: 存在しないIDでResourceNotFoundExceptionをスロー")
    void findById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // create（UT-B-06）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-B-06 create: フォームの内容がProductエンティティにコピーされて保存される")
    void create_savesProductWithFormData() {
        ProductForm form = buildForm("キーボード", 8000, 20, "周辺機器");

        productService.create(form);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getProductName()).isEqualTo("キーボード");
        assertThat(saved.getUnitPrice()).isEqualTo(8000);
        assertThat(saved.getStockQuantity()).isEqualTo(20);
        assertThat(saved.getCategory()).isEqualTo("周辺機器");
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update: 既存商品のフィールドがフォームの内容で上書きされる")
    void update_updatesExistingProduct() {
        Product existing = buildProduct(1L, "旧商品名", 1000, "旧カテゴリ");
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        ProductForm form = buildForm("新商品名", 2000, 50, "新カテゴリ");
        productService.update(1L, form);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getProductName()).isEqualTo("新商品名");
        assertThat(saved.getUnitPrice()).isEqualTo(2000);
        assertThat(saved.getStockQuantity()).isEqualTo(50);
        assertThat(saved.getCategory()).isEqualTo("新カテゴリ");
    }

    @Test
    @DisplayName("update: 存在しないIDではResourceNotFoundExceptionをスロー")
    void update_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductForm form = buildForm("何か", 100, 1, null);
        assertThatThrownBy(() -> productService.update(99L, form))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete: リポジトリのdeleteByIdを呼ぶ")
    void delete_callsDeleteById() {
        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    // -------------------------------------------------------------------------
    // ヘルパーメソッド
    // -------------------------------------------------------------------------

    private Product buildProduct(Long id, String name, int price, String category) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setUnitPrice(price);
        p.setStockQuantity(0);
        p.setCategory(category);
        return p;
    }

    private ProductForm buildForm(String name, int price, int stock, String category) {
        ProductForm f = new ProductForm();
        f.setProductName(name);
        f.setUnitPrice(price);
        f.setStockQuantity(stock);
        f.setCategory(category);
        return f;
    }
}
