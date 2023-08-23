val jupiterVersion = "5.10.0"

group = "mendes.sutil.dyego"
version = "1.2-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

plugins {
    java
    idea
    id("org.sonarqube") version "4.3.0.3225"
    id ("maven-publish")
    id ("java-library")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api(platform("software.amazon.awssdk:bom:2.20.132"))
    api("software.amazon.awssdk:regions")
    api("software.amazon.awssdk:auth")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.7")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.11.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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