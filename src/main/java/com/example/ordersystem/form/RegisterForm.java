package com.example.ordersystem.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * アカウント登録フォームの入力値を受け取るクラス。
 * ユーザー名重複チェックはアノテーションではなく Controller で行う（詳細設計書 §4）。
 */
public class RegisterForm {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(max = 50, message = "ユーザー名は50文字以内で入力してください")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    private String password;

    // ---------- getters / setters ----------

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
