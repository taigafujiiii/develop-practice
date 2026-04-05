package com.example.ordersystem.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Railway デプロイ時の DB 接続設定を自動解決する。
 * 優先順位:
 *   1. DATABASE_URL (postgresql://...) → jdbc:postgresql:// に変換
 *   2. PGHOST / PGPORT / PGDATABASE / PGUSER / PGPASSWORD から構築
 */
public class RailwayDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> props = new HashMap<>();

        String databaseUrl = System.getenv("DATABASE_URL");
        if (isPresent(databaseUrl)) {
            // "postgresql://user:pass@host:port/db" → "jdbc:postgresql://user:pass@host:port/db"
            props.put("spring.datasource.url", "jdbc:" + databaseUrl);
            String pgUser = System.getenv("PGUSER");
            String pgPassword = System.getenv("PGPASSWORD");
            if (isPresent(pgUser)) props.put("spring.datasource.username", pgUser);
            if (isPresent(pgPassword)) props.put("spring.datasource.password", pgPassword);

        } else {
            // DATABASE_URL がなければ個別の PG* 変数から構築
            String pgHost = System.getenv("PGHOST");
            String pgPort = System.getenv("PGPORT");
            String pgDb   = System.getenv("PGDATABASE");
            String pgUser = System.getenv("PGUSER");
            String pgPass = System.getenv("PGPASSWORD");

            if (isPresent(pgHost) && isPresent(pgPort) && isPresent(pgDb)) {
                props.put("spring.datasource.url",
                        "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDb);
                if (isPresent(pgUser)) props.put("spring.datasource.username", pgUser);
                if (isPresent(pgPass))  props.put("spring.datasource.password", pgPass);
            }
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("railway-database", props));
        }
    }

    private boolean isPresent(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
