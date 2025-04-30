# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /workspace

# 루트 프로젝트의 gradle 설정 복사
COPY gradle ./gradle
COPY gradlew .
COPY gradlew.bat .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 서비스별 소스 복사
COPY comment-service ./comment-service

# 빌드
RUN ./gradlew :comment-service:bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /workspace/comment-service/build/libs/*.jar app.jar

# 환경변수 기본 설정 (포트 오픈)
EXPOSE 8083

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"] 