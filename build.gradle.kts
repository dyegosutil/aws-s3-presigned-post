val jupiterVersion = "5.8.2"

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
    implementation(platform("software.amazon.awssdk:bom:2.17.220"))
    implementation("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
    implementation("com.google.code.gson:gson:2.9.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion") // TODO is it really needed?
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion") // TODO is it really needed?
}

//dependencyManagement {
//    imports {
//        mavenBom("software.amazon.awssdk:bom:2.17.220")
//    }
//}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
