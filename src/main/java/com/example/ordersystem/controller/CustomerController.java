package com.example.ordersystem.controller;

import com.example.ordersystem.entity.Customer;
import com.example.ordersystem.form.CustomerForm;
import com.example.ordersystem.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String name, Model model) {
        model.addAttribute("customers", customerService.search(name));
        model.addAttribute("name", name);
        return "customer/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customerForm", new CustomerForm());
        return "customer/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CustomerForm customerForm,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "customer/form";
        }
        customerService.create(customerForm);
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.findById(id);
        CustomerForm form = new CustomerForm();
        form.setCustomerName(customer.getCustomerName());
        form.setPhone(customer.getPhone());
        form.setPostalCode(customer.getPostalCode());
        form.setAddress(customer.getAddress());
        form.setEmail(customer.getEmail());
        model.addAttribute("customerForm", form);
        model.addAttribute("customerId", id);
        return "customer/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute CustomerForm customerForm,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customerId", id);
            return "customer/form";
        }
        customerService.update(id, customerForm);
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        customerService.delete(id);
        return "redirect:/customers";
    }
}
