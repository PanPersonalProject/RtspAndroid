@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "pan.project.fastrtsplive"
    compileSdk = 34

    defaultConfig {
        applicationId = "pan.project.fastrtsplive"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters.add("arm64-v8a")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("debug") {
            java.srcDirs(
                "src/main/java",
                "build/generated/data_binding_base_class_source_out/debug/out"
            )
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.bundles.essential)
    implementation(libs.bundles.camerax)
    implementation(libs.cameraRecord)
//    implementation(project(":camera_record"))

    implementation(libs.rootencoder.library)
    implementation(project(":rtspserver"))
}