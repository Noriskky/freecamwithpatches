buildscript {
    repositories {
        maven("https://repo.spongepowered.org/maven")
        mavenCentral()
    }
}

plugins {
    java
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

repositories {
    maven("https://repo.spongepowered.org/maven")
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-tree:9.6")
    implementation("org.spongepowered:mixin:0.8.5")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

sourceSets.main {
    java {
        srcDir("../common/mixin-plugin/java")
    }
}