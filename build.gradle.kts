plugins {
    // Android & Kotlin base plugins
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false

    // Dagger Hilt for dependency injection
    id("com.google.dagger.hilt.android") version "2.51" apply false

    // Kotlin Serialization (for Retrofit or Ktor)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10" apply false

    // Google Services for Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false

    // ✅ Detekt static analysis plugin (used by CI)
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
}

subprojects {
    // Only apply Detekt if this is a Kotlin or Android module
    if (project.name != "buildSrc") {
        apply(plugin = "io.gitlab.arturbosch.detekt")

        configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            // Reference the reusable Detekt config
            config.setFrom(files("$rootDir/tool/detekt.yml"))
            buildUponDefaultConfig = true
            ignoreFailures = false
        }

        tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            reports {
                html.required.set(true)
                xml.required.set(true)
                txt.required.set(false)
                sarif.required.set(false)
            }
        }
    }
}
