plugins {
    `java-library`
}

dependencies {
    api(project(":modules:shared"))
    api("jakarta.validation:jakarta.validation-api")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("io.swagger.core.v3:swagger-annotations:2.2.21")
    api("org.springframework.data:spring-data-commons")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
