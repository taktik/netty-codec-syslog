import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks

val repoUsername: String by project
val repoPassword: String by project
val mavenReleasesRepository: String by project

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.4.21"
}

repositories {
    mavenLocal()
    jcenter()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-reactor", version = "1.4.2")

    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.11.3")
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.11.3")
    implementation(group = "com.fasterxml.jackson.dataformat", name = "jackson-dataformat-avro", version = "2.11.3")
    implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310", version = "2.11.3")

    implementation(group = "javax.annotation", name = "javax.annotation-api", version = "1.3.2")

    compileOnly(group = "io.netty", name = "netty-all", version = "4.1.54.Final")
    compileOnly(group = "org.slf4j", name = "slf4j-api", version = "1.7.21")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.0.0")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.0.0")
    testImplementation(group = "org.mockito", name = "mockito-core", version = "2.18.3")
    testImplementation(group = "org.mockito", name = "mockito-inline", version = "2.18.3")
    testImplementation(group = "ch.qos.logback", name = "logback-classic", version = "1.1.8")
    testImplementation(group = "org.graylog2", name = "syslog4j", version = "0.9.60")
    testCompileOnly(group = "io.netty", name = "netty-all", version = "4.1.54.Final")
    testCompileOnly(group = "org.slf4j", name = "slf4j-api", version = "1.7.21")
}

group = "com.github.jcustenborder.netty"
description = "netty-codec-syslog"
version = "0.4"
java.sourceCompatibility = JavaVersion.VERSION_11

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}

compileTestKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "Taktik"
            url = uri(mavenReleasesRepository)
            credentials {
                username = repoUsername
                password = repoPassword
            }
        }
    }
}
