import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

group = "io.github.crazyvibes"
version = "0.1.1"

android {
    namespace = "io.github.crazyvibes.backgroundaudit"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    // Every public declaration must carry an explicit visibility modifier and an
    // explicit return type. This is the cheapest way to stop something leaking into
    // the public API by accident.
    explicitApi()

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.androidx.annotation)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true,
        )
    )

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(project.group.toString(), "background-audit", project.version.toString())

    pom {
        name.set("BackgroundAudit")
        description.set("Reports at runtime why Android background work is likely to be killed on this device.")
        inceptionYear.set("2026")
        url.set("https://github.com/crazyvibes/background-audit")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("crazyvibes")
                name.set("Birju Kumar")
                email.set("bkm123r@gmail.com")
                url.set("https://github.com/crazyvibes")
            }
        }

        scm {
            url.set("https://github.com/crazyvibes/background-audit")
            connection.set("scm:git:git://github.com/crazyvibes/background-audit.git")
            developerConnection.set("scm:git:ssh://git@github.com/crazyvibes/background-audit.git")
        }
    }
}
