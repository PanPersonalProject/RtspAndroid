plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}
android {
    namespace = "com.pedro.rtspserver"
    compileSdk = 34

    defaultConfig {
        minSdk = 16
        lint.targetSdk = 34
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }


}



dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.rootencoder.library)
}
