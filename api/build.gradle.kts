plugins {
    kotlin("jvm")
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
}

group = "com.tomtruyen.tcgtrends"
version = "1.0.0"

application {
    mainClass.set("com.tomtruyen.tcgtrends.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.ktor.serialization.gson.jvm)
    implementation(libs.ktor.server.auth.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.logback)

    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.junit)

    implementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.assemble {
    dependsOn(":common:assemble")
}