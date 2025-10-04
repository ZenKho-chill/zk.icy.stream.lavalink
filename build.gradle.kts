plugins {
    java
    `maven-publish`
    alias(libs.plugins.lavalink)
    kotlin("jvm") version "1.9.22"
}

group = "com.zenkho"
version = "1.0.3"

lavalinkPlugin {
    name = "icy-stream-plugin"
    apiVersion = libs.versions.lavalink.api
    serverVersion = libs.versions.lavalink.server
    configurePublishing = true
    path = "com.zenkho.icy.IcyStreamPlugin"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
}
