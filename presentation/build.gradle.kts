import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlinx-serialization")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.hiendao.presentation"
    compileSdk = 35

    val file = rootProject.file("local.properties")
    val properties = Properties()
    properties.load(FileInputStream(file))

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "SERVER_CLIENT_ID", properties.getProperty("server_client_id"))

        resourceConfigurations += listOf("en", "vi")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":navigation"))
    implementation(project(":domain"))
    implementation(project(":coreui"))
    implementation(project(":data"))

    // Compose BOM
    implementation(platform(libs.compose.bom))
    
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.auth)

    // Testing
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso.core)
    
    // Compose
    implementation(libs.compose.androidx.ui)
    implementation(libs.compose.androidx.ui.tooling)
    implementation(libs.compose.androidx.material3)
    implementation(libs.compose.androidx.activity)
    implementation(libs.compose.androidx.lifecycle.viewmodel)
    implementation(libs.compose.androidx.lifecycle.compose)
    implementation(libs.compose.androidx.material.icons.extended)
    implementation(libs.compose.landscapist.glide)
    implementation(libs.compose.androidx.activity)
    implementation(libs.compose.material3.android)
    implementation(libs.compose.androidx.constraintlayout)
    implementation(libs.compose.accompanist.systemuicontroller)
    implementation(libs.compose.accompanist.insets)
    implementation(libs.compose.accompanist.pager)
    implementation(libs.compose.accompanist.pager.indicators)
    implementation(libs.compose.coil)
    implementation(libs.compose.lazyColumnScrollbar)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    
    // Media
    implementation(libs.androidx.media)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.compiler)
    implementation(libs.hilt.androidx.compiler)
    implementation(libs.hilt.navigation.compose)
    ksp("com.google.dagger:hilt-android-compiler:2.49")

    implementation(libs.gson)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)

    implementation(libs.compose.coil)
    implementation(libs.jsoup)

    // Room components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    androidTestImplementation(libs.androidx.room.testing)

    implementation(libs.compose.lazyColumnScrollbar)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.timber)
    implementation(libs.kotlinx.datetime)
}