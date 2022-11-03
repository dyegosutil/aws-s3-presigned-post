val jupiterVersion = "5.9.0"

plugins {
    id("java")
    idea
    id("io.freefair.lombok") version "6.5.0.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.17.285"))
    implementation("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.1")
    implementation("org.slf4j:slf4j-api:2.0.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion") // TODO is it really needed?
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion") // TODO is it really needed?
    testImplementation("com.squareup.okhttp3:okhttp:4.10.0") // TODO test other levels.
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0") // TODO double check
    testImplementation("ch.qos.logback:logback-classic:1.4.4") // TODO make sure this dependency is not being shipped in the jar
}

//dependencyManagement {
//    imports {
//        mavenBom("software.amazon.awssdk:bom:2.17.220")
//    }
//}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
