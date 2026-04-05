package com.example.ordersystem.config;

import com.example.ordersystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 起動時に BCrypt 照合が正常に機能するか検証する（デバッグ用・解決後に削除）
 */
@Component
public class PasswordEncoderDiagnostic {

    private static final Logger log = LoggerFactory.getLogger(PasswordEncoderDiagnostic.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public PasswordEncoderDiagnostic(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void diagnose() {
        log.info("=== PasswordEncoder Diagnostic ===");
        log.info("PasswordEncoder class: {}", passwordEncoder.getClass().getName());

        String freshHash = passwordEncoder.encode("sample");
        log.info("Fresh hash of 'sample': {}", freshHash);
        log.info("matches('sample', freshHash): {}", passwordEncoder.matches("sample", freshHash));

        userRepository.findByUsername("sample").ifPresentOrElse(user -> {
            String dbHash = user.getPassword();
            log.info("DB hash for 'sample': {}", dbHash);
            log.info("DB hash length: {}", dbHash != null ? dbHash.length() : 0);
            log.info("matches('sample', dbHash): {}", passwordEncoder.matches("sample", dbHash));
        }, () -> log.info("User 'sample' not found in DB"));

        log.info("=== End Diagnostic ===");
    }
}
