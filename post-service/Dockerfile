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
COPY post-service ./post-service

# 빌드
RUN ./gradlew :post-service:bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

# curl 설치
RUN apt-get update && apt-get install -y curl

# 대기 스크립트 추가
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# 빌드 결과물 복사
COPY --from=build /workspace/post-service/build/libs/*.jar app.jar

# 환경변수 기본 설정 (포트 오픈)
EXPOSE 8082

# 실행
ENTRYPOINT ["/wait-for-it.sh", "mysql-post:3306", "--", "java", "-jar", "app.jar"]
