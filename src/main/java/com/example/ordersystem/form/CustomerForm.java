package com.example.ordersystem.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 顧客登録・編集フォームの入力値を受け取るクラス。
 */
public class CustomerForm {

    @NotBlank(message = "顧客名は必須です")
    @Size(max = 100, message = "顧客名は100文字以内で入力してください")
    private String customerName;

    @Size(max = 20, message = "電話番号は20文字以内で入力してください")
    private String phone;

    @Size(max = 10, message = "郵便番号は10文字以内で入力してください")
    private String postalCode;

    @Size(max = 200, message = "住所は200文字以内で入力してください")
    private String address;

    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
    private String email;

    // ---------- getters / setters ----------

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
