plugins {
    id("java")
    id("application")  // ← adds the `run` task
}

group = "org.vardinsdev.abyssnetwork"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("org.vardinsdev.abyssnetwork.Main")  // ← tells Gradle where to start
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

tasks.withType<JavaExec> {
    standardInput = System.`in`
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--add-modules=jdk.unsupported",
        "-XX:+IgnoreUnrecognizedVMOptions"
    )
}

dependencies {
    implementation("net.minestom:minestom:2026.07.12-26.2")
    implementation("com.github.AbyssalNetwork:minegun:1.0.3")
    implementation("rocks.minestom:placement:0.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("dev.hollowcube:polar:1.15.1")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("it.unimi.dsi:fastutil:8.5.12")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

tasks.test {
    useJUnitPlatform()
}