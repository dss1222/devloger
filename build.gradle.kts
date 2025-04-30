plugins {
    id("org.springframework.boot") version "3.2.3" apply false
    id("io.spring.dependency-management") version "1.1.3"
}

subprojects {
    apply(plugin = "org.springframework.boot")
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
