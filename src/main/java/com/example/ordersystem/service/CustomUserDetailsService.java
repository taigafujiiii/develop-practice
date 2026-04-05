package com.example.ordersystem.service;

import com.example.ordersystem.entity.User;
import com.example.ordersystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));

        String storedHash = user.getPassword();
        log.info("loadUser: username={}, hash_len={}, hash_prefix={}",
                user.getUsername(),
                storedHash != null ? storedHash.length() : 0,
                storedHash != null ? storedHash.substring(0, Math.min(7, storedHash.length())) : "null");

        // BCrypt 照合テスト（デバッグ用）
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean testMatch = encoder.matches("sample", storedHash);
        log.info("BCrypt test matches('sample', storedHash) = {}", testMatch);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(storedHash)
                .roles("USER")
                .build();
    }
}
