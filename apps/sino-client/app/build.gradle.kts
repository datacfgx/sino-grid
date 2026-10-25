plugins { id("com.android.application") }
android {
    namespace = "br.eni.cora"
    compileSdk = 35
    defaultConfig { applicationId = "br.eni.cora"; minSdk = 21; targetSdk = 23; versionCode = 49; versionName = "2.5.1" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
