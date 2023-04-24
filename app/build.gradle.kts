plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.com.android.application)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
    namespace = "com.sunritel.applock"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.sunritel.applock"
        minSdk = 33
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("mtk.keystore")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }

        create("release") {
            storeFile = file("mtk.keystore")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
//                val outputFileName =
//                    "AppLock-${variant.baseName}-${variant.versionName}-${variant.versionCode}.apk"
                val outputFileName = "AppLock.apk"
                println("OutputFileName: $outputFileName")
                output.outputFileName = outputFileName
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}