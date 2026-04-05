package com.example.ordersystem.service;

import com.example.ordersystem.entity.Customer;
import com.example.ordersystem.exception.ResourceNotFoundException;
import com.example.ordersystem.form.CustomerForm;
import com.example.ordersystem.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * 顧客名で部分一致検索する。
     * name が null または空文字の場合は全件返却する。
     */
    public List<Customer> search(String name) {
        if (name == null || name.isBlank()) {
            return customerRepository.searchByName(null);
        }
        return customerRepository.searchByName(name);
    }

    /**
     * ID で顧客を取得する。存在しない場合は {@link ResourceNotFoundException} をスローする。
     */
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("顧客が見つかりません ID: " + id));
    }

    @Transactional
    public void create(CustomerForm form) {
        Customer customer = new Customer();
        copyFormToEntity(form, customer);
        customerRepository.save(customer);
    }

    @Transactional
    public void update(Long id, CustomerForm form) {
        Customer customer = findById(id);
        copyFormToEntity(form, customer);
        customerRepository.save(customer);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.deleteById(id);
    }

    private void copyFormToEntity(CustomerForm form, Customer customer) {
        customer.setCustomerName(form.getCustomerName());
        customer.setPhone(form.getPhone());
        customer.setPostalCode(form.getPostalCode());
        customer.setAddress(form.getAddress());
        customer.setEmail(form.getEmail());
    }
}
