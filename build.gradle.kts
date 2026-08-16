plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.binary.compatibility.validator)
}

// Public API surface is tracked in background-audit/api/*.api and enforced in CI.
// Run ./gradlew apiDump after any intentional public API change, and commit the result.
apiValidation {
    ignoredProjects.addAll(listOf())
}
