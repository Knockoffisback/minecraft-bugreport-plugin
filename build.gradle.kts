plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val plugin = "minecraft-bugreporting-plugin"
val author = "Knockoff"
val desc = "A bugreporting plugin for AstralClub"
val main = "com.yourname.bugreport"
val version = "1.0"


repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")

}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("net.dv8tion:JDA:5.0.0-alpha.21")
    implementation("com.google.code.gson:gson:2.10.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    named<ProcessResources>("processResources") {
        val props = mapOf(
            "id" to plugin.lowercase(),
            "name" to plugin,
            "author" to author,
            "main" to main,
            "description" to desc,
            "version" to version
        )

        inputs.properties(props)

        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    // Keep your other tasks unchanged
    named<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
    }

    register<Copy>("copy") {
        from(named("shadowJar"))
        rename("(.*)-all.jar", "$plugin-$version.jar")
        into(file("jars"))
    }

    register("delete") {
        doLast { file("jars").deleteRecursively() }
    }
}