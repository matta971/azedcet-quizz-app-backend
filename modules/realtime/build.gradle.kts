plugins {
    `java-library`
}

dependencies {
    api(project(":modules:protocol"))
    api(project(":modules:shared"))
    api(project(":modules:match"))
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
