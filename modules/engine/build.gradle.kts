plugins {
    `java-library`
}

dependencies {
    api(project(":modules:protocol"))
    api(project(":modules:shared"))
    api(project(":modules:scoring"))
    implementation("org.springframework:spring-context")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
