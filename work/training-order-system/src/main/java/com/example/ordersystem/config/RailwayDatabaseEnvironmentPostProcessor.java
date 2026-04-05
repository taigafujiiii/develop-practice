package com.example.ordersystem.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Railway デプロイ時に DATABASE_URL (postgresql://...) を
 * Spring Boot が要求する jdbc:postgresql:// 形式に変換する。
 */
public class RailwayDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            return;
        }

        Map<String, Object> props = new HashMap<>();
        // Railway: "postgresql://user:pass@host:port/db" → "jdbc:postgresql://user:pass@host:port/db"
        props.put("spring.datasource.url", "jdbc:" + databaseUrl);

        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");
        if (pgUser != null && !pgUser.isEmpty()) {
            props.put("spring.datasource.username", pgUser);
        }
        if (pgPassword != null && !pgPassword.isEmpty()) {
            props.put("spring.datasource.password", pgPassword);
        }

        environment.getPropertySources().addFirst(new MapPropertySource("railway-database", props));
    }
}
