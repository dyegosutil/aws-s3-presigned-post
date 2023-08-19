val jupiterVersion = "5.10.0"

plugins {
    java
    idea
}

group = "org.example"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.20.129"))
    implementation("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.7")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.11.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    implementation("org.mockito:mockito-core:5.4.0")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.11")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
