package com.example.ordersystem.controller;

import com.example.ordersystem.entity.User;
import com.example.ordersystem.form.RegisterForm;
import com.example.ordersystem.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 認証・アカウント管理コントローラー。
 * /login の POST は Spring Security が処理するため、このクラスでは /register のみ担当する。
 */
@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterForm registerForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        // ユーザー名の重複チェック
        if (userRepository.existsByUsername(registerForm.getUsername())) {
            model.addAttribute("usernameError", "このユーザー名はすでに使われています");
            return "auth/register";
        }
        User user = new User();
        user.setUsername(registerForm.getUsername());
        user.setPassword(passwordEncoder.encode(registerForm.getPassword())); // BCrypt ハッシュ化
        userRepository.save(user);
        return "redirect:/login?registered";
    }
}
