import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "xyz.emlyn.pulsar"
    compileSdk = 34

    buildFeatures.buildConfig = true

    val props = Properties()
	if (rootProject.file("local.properties").exists()) {
	    properties.load(rootProject.file("local.properties").newDataInputStream())
	}
    val XMPP_USER = props.getProperty("XMPP_USER", "")
    val XMPP_PASS = props.getProperty("XMPP_PASS", "")
	val XMPP_ADDR = props.getProperty("XMPP_ADDR", "")
	val XMPP_PORT = props.getProperty("XMPP_PORT", "")


    defaultConfig {
        applicationId = "xyz.emlyn.pulsar"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField("String","XMPP_USER", XMPP_USER)
        buildConfigField("String","XMPP_PASS", XMPP_PASS)
		buildConfigField("int", "XMPP_PORT", XMPP_PORT)
		buildConfigField("String", "XMPP_ADDR", XMPP_ADDR)
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("org.igniterealtime.smack:smack-android-extensions:4.4.8") {
        exclude(group="xpp3", module="xpp3")
        exclude(group="xpp3", module="xpp3_min")
    }
    implementation("org.igniterealtime.smack:smack-tcp:4.4.8"){
        exclude(group="xpp3", module="xpp3")
        exclude(group="xpp3", module="xpp3_min")
    }

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}
