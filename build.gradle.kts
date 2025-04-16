import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application


}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application{
    mainClass.set("chatAppServer.server.ApplicationKt")
}


dependencies {
    // Ktor Server Core
    implementation("io.ktor:ktor-server-core:3.1.2")
    implementation("io.ktor:ktor-server-netty:3.1.2")

    // Routing and JSON
    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.4.3")

    // Ktor HTTP client (for sending FCM HTTP POST)
    implementation("io.ktor:ktor-client-core:3.1.2")
    implementation("io.ktor:ktor-client-cio:3.1.2")

    // Logging (optional)
    implementation("io.ktor:ktor-server-call-logging:3.1.2")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation("ch.qos.logback:logback-classic:1.5.18")


}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

tasks.named<Jar>("jar") {
    enabled = false
}

// Configure the shadowJar to produce the desired output
tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("ChatAppKtorServer")
    archiveClassifier.set("")
    archiveVersion.set("1.0-SNAPSHOT")
}

// Ensure build depends on shadowJar
tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}





