package com.example.ordersystem.service;

import com.example.ordersystem.entity.User;
import com.example.ordersystem.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // -------------------------------------------------------------------------
    // loadUserByUsername（UT-E-01・UT-E-02）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("UT-E-01 loadUserByUsername: 存在するユーザー名でUserDetailsを返す")
    void loadUserByUsername_found_returnsUserDetails() {
        User user = buildUser(1L, "admin", "{bcrypt}hashedpassword");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPassword()).isEqualTo("{bcrypt}hashedpassword");
        // roles("USER") により ROLE_USER 権限が付与される
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("UT-E-02 loadUserByUsername: 存在しないユーザー名でUsernameNotFoundExceptionをスロー")
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    // -------------------------------------------------------------------------
    // ヘルパーメソッド
    // -------------------------------------------------------------------------

    private User buildUser(Long id, String username, String password) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword(password);
        return u;
    }
}
