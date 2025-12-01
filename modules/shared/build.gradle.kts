plugins {
    `java-library`
}

dependencies {
    api("org.slf4j:slf4j-api")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("jakarta.validation:jakarta.validation-api")
    api("org.springframework:spring-web")

    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-redis")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}
