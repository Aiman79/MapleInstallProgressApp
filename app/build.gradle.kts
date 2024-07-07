plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rktechnohub.sugarbashprogressapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rktechnohub.sugarbashprogressapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
//        viewBinding = true
    }
    composeOptions{
        kotlinCompilerExtensionVersion = "1.5.0"
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage.ktx)
//    implementation(libs.compose.preview.renderer)
//    implementation(libs.compose.preview.renderer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui-graphics:1.6.7")
//    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))

    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("androidx.credentials:credentials:1.2.2")
    implementation ("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.0")

//    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
//    debugImplementation("androidx.compose.ui:ui-tooling")

    //emoji
//    implementation("com.rockerhieu.emojicon:library:1.4.5")
    implementation("androidx.emoji:emoji-appcompat:1.0.0")
    implementation("androidx.emoji:emoji-bundled:1.1.0")

    //image
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    //gson
    implementation("com.google.code.gson:gson:2.8.6")

    //color
    implementation("com.github.QuadFlask:colorpicker:0.0.15")
}