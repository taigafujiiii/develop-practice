package com.example.ordersystem.entity;

import jakarta.persistence.*;

/**
 * ユーザーエンティティ。
 * users テーブルに対応する。
 * password は BCrypt ハッシュ化済みの値を保存する（要件定義書 §3）。
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt でハッシュ化されたパスワード。平文での保存は禁止。 */
    @Column(nullable = false, length = 255)
    private String password;

    // ---------- getters / setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
