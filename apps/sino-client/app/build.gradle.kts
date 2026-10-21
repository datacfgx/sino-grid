plugins { id("com.android.application") }
android {
    namespace = "br.eni.client"
    compileSdk = 35
    defaultConfig { applicationId = "br.eni.client"; minSdk = 23; targetSdk = 23; versionCode = 1; versionName = "0.1" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
