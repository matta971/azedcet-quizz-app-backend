plugins {
    `java-library`
}

dependencies {
    api(project(":modules:protocol"))
    api(project(":modules:shared"))
    api(project(":modules:engine"))
    api(project(":modules:content"))
    api(project(":modules:scoring"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
