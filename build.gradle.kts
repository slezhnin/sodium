import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.0"
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
    }

    tasks.create<Jar>("fatJar") {
        baseName = "${project.name}-fat"
        manifest {
            attributes["Main-Class"] = "io.vertx.core.Launcher"
            attributes["Main-Verticle"] = "SodiumVerticle"
        }
        from(configurations.compileClasspath.map {
            if (it.isDirectory) fileTree(it) else zipTree(it)
        })
        with(rootProject.tasks["jar"] as CopySpec)
    }
}
