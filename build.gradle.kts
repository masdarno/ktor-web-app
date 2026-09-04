plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "id.darno"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.csrf)
    implementation(ktorLibs.server.rateLimit)
    implementation(ktorLibs.server.di)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.pebble)
    implementation(ktorLibs.server.requestValidation)
    implementation(ktorLibs.server.sessions)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.utils)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.logback.classic)

    // MariaDB
    implementation(libs.mariadb.java.client)
    // HikariCP - Database Pooling
    implementation(libs.hikari.cp)
    // Valiktor - validasi
    implementation(libs.valiktor.core)
    // BCrypt - hasing password
    implementation(libs.security.bcrypt)
    // Email
    implementation(libs.jakarta.mail)
    // Dotenv
    implementation(libs.dotenv.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
