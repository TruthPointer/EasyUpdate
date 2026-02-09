import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.Node
import kotlin.collections.forEach

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.tpmobile.easyupdate"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "org.tpmobile.easyupdate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = getVersionNameFromResources()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug{
            resValue("string", "app_name", "EasyUpdate")
            buildConfigField ("boolean", "MY_DEBUG", "true")
        }
        release {
            resValue("string", "app_name", "易升级")
            buildConfigField( "boolean", "MY_DEBUG", "false")

            isDebuggable=false
            isMinifyEnabled=true
            isShrinkResources=true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig  = true
        resValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.preference.ktx)

    implementation(libs.gson)
    implementation(libs.zip4j)
}

fun getVersionNameFromResources() : String{
    val stringsFile = file("src/main/res/values/strings.xml")
    val xml = XmlSlurper().parse(stringsFile)
    var versionName: String? = null
    //println(xml.childNodes().forEach { node -> println((node as Node).attributes()) })
    xml.childNodes().forEach { node ->
        (node as Node).attributes().forEach { ss->
            if(ss.value == "version_name") {
                versionName = node.text().toString()
            }
        }
    }
    if (versionName == null) {
        throw GradleException("String resource 'version_name' not found in strings.xml!")
    }
    println("versionName: $versionName")
    return versionName
}