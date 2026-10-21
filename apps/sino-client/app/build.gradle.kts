plugins { id("com.android.application") }
android {
    namespace = "br.eni.cora"
    compileSdk = 35
    defaultConfig { applicationId = "br.eni.cora"; minSdk = 23; targetSdk = 23; versionCode = 5; versionName = "0.6" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
