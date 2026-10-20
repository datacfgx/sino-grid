plugins { id("com.android.application") }

android {
    namespace = "br.eni.probe"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.eni.probe"
        minSdk = 23
        targetSdk = 23
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
