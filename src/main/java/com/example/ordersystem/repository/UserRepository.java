package com.example.ordersystem.repository;

import com.example.ordersystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** ユーザー名でユーザーを検索する。Spring Security の認証処理から呼ばれる。 */
    Optional<User> findByUsername(String username);

    /** ユーザー名の重複チェック。アカウント登録時に使用する。 */
    boolean existsByUsername(String username);
}
