plugins {
    id("io.spring.dependency-management") version "1.1.3"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.4")
    }
}
