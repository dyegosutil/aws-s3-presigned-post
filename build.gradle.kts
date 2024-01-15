import java.net.URI

group = "io.github.dyegosutil"
version = "0.1.0-beta.3"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val jupiterVersion = "5.10.1"

plugins {
    java
    idea
    signing
    id("org.sonarqube") version "4.4.1.3373"
    id ("maven-publish")
    id ("java-library")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api(platform("software.amazon.awssdk:bom:2.21.43"))
    api("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.9")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.14")

    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.9.0")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.1.5")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("MavenPublication") {
            pom {
                name.set("AWS S3 Pre Signed Post")
                description.set("A Java library to generate pre-signed post data to be used to upload files to S3")
                url.set("https://github.com/dyegosutil/aws-s3-presigned-post")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://www.opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("dyego.sutil")
                        name.set("Dyego Sutil Mendes")
                        email.set("aws.s3.presigned.post@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/dyegosutil/aws-s3-presigned-post.git")
                    developerConnection.set("scm:git:ssh://github.com:dyegosutil/aws-s3-presigned-post.git")
                    url.set("github.com/dyegosutil/aws-s3-presigned-post/tree/main")
                }
            }
            from(
                components.getByName("java")
            )
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USER_NAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["MavenPublication"])
}