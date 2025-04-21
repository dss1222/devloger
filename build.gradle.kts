plugins {
    id("io.spring.dependency-management") version "1.1.3"
}

subprojects {
    apply(plugin = "io.spring.dependency-management")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.1")
        }
    }

    repositories {
        mavenCentral()
    }
}
