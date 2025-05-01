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
COPY api-gateway ./api-gateway

# 빌드
RUN ./gradlew :api-gateway:bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

# curl 설치
RUN apt-get update && apt-get install -y curl

# 빌드 결과물 복사
COPY --from=build /workspace/api-gateway/build/libs/*.jar app.jar

# 환경변수 기본 설정 (포트 오픈)
EXPOSE 8000

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"] 