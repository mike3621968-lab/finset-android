import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// local.properties(로컬 개발용, git에 커밋되지 않음) 또는 환경변수(GitHub Actions CI용)에서
// KIS App Key/Secret을 읽어와 소스코드에 평문으로 남기지 않고 BuildConfig 상수로 주입한다.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun localProp(key: String, envKey: String? = null, default: String = ""): String {
    val fromFile = localProps.getProperty(key)
    if (!fromFile.isNullOrBlank()) return fromFile
    if (envKey != null) {
        val fromEnv = System.getenv(envKey)
        if (!fromEnv.isNullOrBlank()) return fromEnv
    }
    return default
}

android {
    namespace = "com.finset.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.finset.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "KIS_APP_KEY", "\"${localProp("kis.appkey", "KIS_APP_KEY")}\"")
        buildConfigField("String", "KIS_APP_SECRET", "\"${localProp("kis.appsecret", "KIS_APP_SECRET")}\"")
        buildConfigField("String", "KIS_BASE_URL", "\"${localProp("kis.baseUrl", "KIS_BASE_URL", "https://openapi.koreainvestment.com:9443")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity / lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room (local DB)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // 한국투자증권 Open API 연동 (실시간 시세)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
