plugins { id("com.android.application") }

android {
    namespace = "br.eni.micgain"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.eni.micgain"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

dependencies { implementation("androidx.appcompat:appcompat:1.7.0") }
