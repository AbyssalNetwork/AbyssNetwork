plugins {
    id("java")
}

group = "org.vardinsdev.abyssnetwork"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

tasks.withType<JavaExec> {
    // Allows the terminal to accept your keyboard input (for commands)
    standardInput = System.`in`

    // These arguments silence the Java 25 / Minestom warnings
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--add-modules=jdk.unsupported",
        "-XX:+IgnoreUnrecognizedVMOptions"
    )
}

dependencies {
    implementation("net.minestom:minestom:2026.03.03-1.21.11")
    implementation("com.github.AbyssalNetwork:minegun:1.0.1")
    implementation("rocks.minestom:placement:0.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("dev.hollowcube:polar:1.15.1")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("it.unimi.dsi:fastutil:8.5.12")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
}

tasks.test {
    useJUnitPlatform()
}