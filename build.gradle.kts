
plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
}

group = "me.deprilula28"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

kotlin {
    sourceSets {
        val main by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0-RC2")
                implementation("io.ktor:ktor-client-apache:1.4.1")
                implementation("org.apache.httpcomponents:httpasyncclient:4.1.4")
                implementation("com.rabbitmq:amqp-client:5.9.0")
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.4.0-M1")
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.0-M1")
                implementation("org.mockito:mockito-core:3.3.3")
            }
        }
    }
}
