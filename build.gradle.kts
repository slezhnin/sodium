import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.0"
    id("com.github.johnrengelman.shadow") version "4.0.2"
    application
}

allprojects {
    group = "com.lezhnin.project"
    version = "1.0-SNAPSHOT"

    repositories {
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform {}
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "application")

    dependencies {
        compile(kotlin("stdlib-jdk8"))
        compile(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
        testCompile(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.3.1")
        testCompile(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.3.1")
        testCompile(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = "3.1.10")
        testCompile(group = "org.awaitility", name = "awaitility-kotlin", version = "3.1.2")
    }
}

project("sodium-store") {
    val vertxVersion = "3.5.3"

    dependencies {
        compile(group = "io.vertx", name = "vertx-core", version = vertxVersion)
        compile(group = "io.vertx", name = "vertx-lang-kotlin", version = vertxVersion)
        compile(group = "io.vertx", name = "vertx-config", version = vertxVersion)
        compile(group = "io.vertx", name = "vertx-config-git", version = vertxVersion)
        compile(group = "io.vertx", name = "vertx-web", version = vertxVersion)
        compile(group = "io.vertx", name = "vertx-web-client", version = vertxVersion)
        compile(group = "io.vertx", name = "vertx-lang-kotlin-coroutines", version = vertxVersion)
    }

    tasks.withType<ShadowJar> {
        mergeServiceFiles {
            include(
                "META-INF/services/io.vertex.config.spi.ConfigStoreFactory"
            )
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes["Main-Class"] = "io.vertx.core.Launcher"
            attributes["Main-Verticle"] = "com.lezhnin.project.sodium.store.start.SodiumVerticle"
            attributes["Class-Path"] = configurations.compile.joinToString(" ") { "lib/" + it.name }
        }
    }

    tasks.create<Copy>("dist") {
        dependsOn("jar")
        from(tasks["jar"])
        from(configurations.runtime)
        into("${project.buildDir}/dist/lib")
    }

    application {
        mainClassName = "io.vertx.core.Launcher"
        applicationDefaultJvmArgs = listOf(
            "-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
        )
    }
}
