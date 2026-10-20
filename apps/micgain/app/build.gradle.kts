plugins { id("com.android.application") }

android {
    namespace = "br.eni.micgain"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.eni.micgain"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "3.9d"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
}
