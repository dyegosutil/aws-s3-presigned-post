val jupiterVersion = "5.8.2"

plugins {
    id("java")
    idea
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.17.220")) // TODO add bom
    implementation("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
//    <dependency>
//    <groupId>software.amazon.awssdk</groupId>
//    <artifactId>bom</artifactId>
//    <version>2.17.190</version>
//    <type>pom</type>
//    <scope>import</scope>
//    </dependency>

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
