package com.example.ordersystem.controller;

import com.example.ordersystem.form.OrderDetailForm;
import com.example.ordersystem.form.OrderForm;
import com.example.ordersystem.service.CustomerService;
import com.example.ordersystem.service.OrderService;
import com.example.ordersystem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;

    public OrderController(OrderService orderService,
                           CustomerService customerService,
                           ProductService productService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model) {
        model.addAttribute("orders", orderService.findByFilters(status, from, to));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "order/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        OrderForm form = new OrderForm();
        form.setOrderDate(LocalDate.now());
        form.setDetails(new ArrayList<>());
        form.getDetails().add(new OrderDetailForm());
        model.addAttribute("orderForm", form);
        model.addAttribute("customers", customerService.search(null));
        model.addAttribute("products", productService.findAll());
        return "order/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute OrderForm orderForm,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.search(null));
            model.addAttribute("products", productService.findAll());
            return "order/form";
        }
        orderService.create(orderForm);
        return "redirect:/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        return "order/detail";
    }
}
