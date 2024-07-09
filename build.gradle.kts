plugins {
    id("java")
    application
}

group = "org.AniSocial"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("org.AniSocial.AniSocial")
}

tasks.test {
    useJUnitPlatform()
}