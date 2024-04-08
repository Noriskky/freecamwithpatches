buildscript {
    repositories {
        maven("https://repo.spongepowered.org/maven")
        mavenCentral()
    }
}

plugins {
    java
}

repositories {
    maven("https://repo.spongepowered.org/maven")
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-tree:9.6")
    implementation("org.spongepowered:mixin:0.8.5")
}

sourceSets.main {
    java {
        srcDir("../common/mixin-plugin/java")
    }
}