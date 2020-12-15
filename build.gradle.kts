import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.4.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-avro:2.11.3")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.0")
    testImplementation("org.mockito:mockito-core:2.18.3")
    testImplementation("ch.qos.logback:logback-classic:1.1.8")
    testImplementation("org.graylog2:syslog4j:0.9.60")
    compileOnly("io.netty:netty-all:4.1.54.Final")
    testCompileOnly("io.netty:netty-all:4.1.54.Final")
    compileOnly("org.slf4j:slf4j-api:1.7.21")
    testCompileOnly("org.slf4j:slf4j-api:1.7.21")
    implementation(kotlin("stdlib-jdk8"))
}

group = "com.github.jcustenborder.netty"
version = "0.3-SNAPSHOT"
description = "netty-codec-syslog"
java.sourceCompatibility = JavaVersion.VERSION_11

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}
