import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.adr.opsc7312_poe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adr.opsc7312_poe"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        //load the values from .properties file
        val keystoreFile = project.rootProject.file("Client_Key.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        //return empty key in case something goes wrong
        val clientID = properties.getProperty("CLIENT_ID") ?: ""

        buildConfigField("String", "CLIENT_ID_KEY", "\"$clientID\"")

    }
    buildFeatures {
        buildConfig = true // Enable BuildConfig fields
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
    packaging {
        resources {
            excludes += "/META-INF/{DEPENDENCIES,LICENSE,LICENSE.txt,NOTICE,NOTICE.txt}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.googleid)
    implementation(libs.junit.junit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // coroutines/auth/fuel/jwt/gson
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.10"))
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")
    implementation("com.google.code.gson:gson:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.credentials:credentials:1.5.0-alpha05")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-alpha05")
    implementation("com.google.api-client:google-api-client:1.33.0")
    implementation("com.google.api-client:google-api-client-gson:1.33.0")
    implementation ("androidx.biometric:biometric:1.2.0-alpha04")

    // unit tests
    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
