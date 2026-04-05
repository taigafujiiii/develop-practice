# -------------------------------------------------------
# Stage 1: ビルド
# -------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle wrapper と依存関係定義を先にコピーしてレイヤーキャッシュを活用する
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon -q || true

# ソースをコピーしてビルド（テストはスキップ）
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# -------------------------------------------------------
# Stage 2: 実行
# -------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Railway は PORT 環境変数でポートを指定する
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
