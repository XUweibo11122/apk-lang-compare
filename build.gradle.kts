plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.osdetector") version "1.7.3"
}

group = "com.apkcompare"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.aayushatharva.brotli4j:brotli4j:1.16.0")
    runtimeOnly("com.aayushatharva.brotli4j:native-${osdetector.classifier}:1.16.0")

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.apkcompare.lang.ApkLangCompareApp")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("apk-lang-compare")
    archiveClassifier.set("all")
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
