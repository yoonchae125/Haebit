plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
    }
}

dependencies {
    testImplementation(libs.junit)
}