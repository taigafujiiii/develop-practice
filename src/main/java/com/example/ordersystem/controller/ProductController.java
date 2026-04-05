package com.example.ordersystem.controller;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.form.ProductForm;
import com.example.ordersystem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String category, Model model) {
        model.addAttribute("products", productService.findByCategory(category));
        model.addAttribute("selectedCategory", category);
        return "product/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        return "product/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ProductForm productForm,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "product/form";
        }
        productService.create(productForm);
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        ProductForm form = new ProductForm();
        form.setProductName(product.getProductName());
        form.setCategory(product.getCategory());
        form.setUnitPrice(product.getUnitPrice());
        form.setStockQuantity(product.getStockQuantity());
        model.addAttribute("productForm", form);
        model.addAttribute("productId", id);
        return "product/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ProductForm productForm,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("productId", id);
            return "product/form";
        }
        productService.update(id, productForm);
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/products";
    }
}
