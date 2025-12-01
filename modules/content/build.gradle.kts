plugins {
    `java-library`
}

dependencies {
    api(project(":modules:protocol"))
    api(project(":modules:shared"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.opencsv:opencsv:5.9")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
