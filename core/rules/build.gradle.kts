plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:model"))
    implementation(libs.jsoup)
    implementation(libs.rhino)
    implementation(libs.json.schema.validator)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}
