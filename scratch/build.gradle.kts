plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

group = "com.github.uditbhaskar"
version = "0.1.2"

android {
    namespace = "com.skretch.scratch"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk {
            version = release(31)
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                // Must match the GitHub repo name so JitPack keeps sources metadata intact.
                groupId = "com.github.uditbhaskar"
                artifactId = "SKRETCH"
                version = project.version.toString()

                pom {
                    name.set("SKRETCH")
                    description.set("Scratch cards for Jetpack Compose")
                    url.set("https://github.com/uditbhaskar/SKRETCH")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("uditbhaskar")
                            name.set("uditbhaskar")
                            url.set("https://github.com/uditbhaskar")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/uditbhaskar/SKRETCH.git")
                        developerConnection.set("scm:git:ssh://github.com/uditbhaskar/SKRETCH.git")
                        url.set("https://github.com/uditbhaskar/SKRETCH")
                    }
                }
            }
        }
    }
}

// JitPack rewrites *.module and points sources at SKRETCH-x.y.z.jar (404).
// Disable module metadata so Gradle uses Maven layout and fetches *-sources.jar.
tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}
