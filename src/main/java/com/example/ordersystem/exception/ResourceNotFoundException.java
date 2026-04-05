package com.example.ordersystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 指定されたリソースが見つからない場合にスローするカスタム例外。
 * {@code @ResponseStatus} により、Spring MVC が自動的に 404 エラー画面へ遷移する。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
