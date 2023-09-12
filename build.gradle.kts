val jupiterVersion = "5.10.0"

group = "io.github.dyegosutil"
version = "0.1.0-beta"
java.sourceCompatibility = JavaVersion.VERSION_1_8

plugins {
    java
    idea
    id("org.sonarqube") version "4.3.1.3277"
    id ("maven-publish")
    id ("java-library")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api(platform("software.amazon.awssdk:bom:2.20.144"))
    api("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.9")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.11.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.1.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("aws-s3-presigned-post") {
            from(
                components.getByName("java")
            )
        }
    }
}