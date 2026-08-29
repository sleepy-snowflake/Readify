plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:model"))
    implementation(libs.jsoup)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}
