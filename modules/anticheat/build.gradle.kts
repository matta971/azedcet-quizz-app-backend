plugins {
    `java-library`
}

dependencies {
    api(project(":modules:protocol"))
    api(project(":modules:shared"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.bucket4j:bucket4j-core:8.10.1")
    implementation("com.bucket4j:bucket4j-redis:8.10.1")
    implementation("commons-codec:commons-codec:1.16.1")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
