plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.AniSocial"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.12")

    runtimeOnly("org.postgresql:postgresql:42.7.3")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}

application {
    mainClass.set("org.AniSocial.Main")
}

tasks.test {
    useJUnitPlatform()
}