plugins {
    `java-library`
    id("xyz.jpenilla.run-velocity") version "2.3.1"
}

group = "me.liwk"
version = "1.1.0-SNAPSHOT"
description = "KarhuVelocity"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")
    }
}
