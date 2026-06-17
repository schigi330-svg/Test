// NOTE for Claude Code: these plugin versions reflect a stable AGP 8.x /
// Kotlin 2.0 toolchain. Android tooling changed in 2026 (AGP 9 introduced
// built-in Kotlin support with a new DSL). If your installed Android
// Studio / command-line tools require AGP 9+, update the versions below
// (and possibly remove the separate kotlin-android plugin id in
// app/build.gradle.kts in favor of AGP's built-in Kotlin support) using
// the AGP Upgrade Assistant or current official docs. None of the Kotlin
// application code in this project needs to change for that migration —
// only these plugin declarations.

plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
}
