plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.taiwanlife.teamwalk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.taiwanlife.teamwalk"
        minSdk = 26
        targetSdk = 36
        versionCode = 55
        versionName = "3.0.03"

//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("./teamwalk-uat.jks")
            storePassword = "Best2021"
            keyAlias = "teamwalk"
            keyPassword = "Best2021"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            signingConfig = signingConfigs.getByName("debug")

            // APP 名稱
            resValue("string", "app_name", "DebugTeamWalk")
            // 綁定成功時的domain(scheme)
            resValue("string", "redirect_scheme", "teamwalkdebug")

            //FB SDK
            resValue("string", "facebook_app_id", "1151503809061491")
            resValue("string", "fb_login_protocol_scheme", "fb1151503809061491")
            resValue("string", "facebook_client_token", "2183e7d396168d09c4799adb16c4f276")
        }
        create("sit") {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".sit"
            versionNameSuffix = "-SIT"

            signingConfig = signingConfigs.getByName("debug")

            // APP 名稱
            resValue("string", "app_name", "SITTeamWalk")
            // 綁定成功時的domain(scheme)
            resValue("string", "redirect_scheme", "teamwalksit")

            //FB SDK
            resValue("string", "facebook_app_id", "1151503809061491")
            resValue("string", "fb_login_protocol_scheme", "fb1151503809061491")
            resValue("string", "facebook_client_token", "2183e7d396168d09c4799adb16c4f276")
        }
        create("uat") {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".uat"
            versionNameSuffix = "-UAT"

            signingConfig = signingConfigs.getByName("debug")

            // APP 名稱
            resValue("string", "app_name", "UATTeamWalk")
            // 綁定成功時的domain(scheme)
            resValue("string", "redirect_scheme", "teamwalkuat")

            //FB SDK
            resValue("string", "facebook_app_id", "1151503809061491")
            resValue("string", "fb_login_protocol_scheme", "fb1151503809061491")
            resValue("string", "facebook_client_token", "2183e7d396168d09c4799adb16c4f276")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // APP 名稱
            resValue("string", "app_name", "TeamWalk")
            // 綁定成功時的domain(scheme)
            resValue("string", "redirect_scheme", "teamwalk")

            //FB SDK
            resValue("string", "facebook_app_id", "707814117610778")
            resValue("string", "fb_login_protocol_scheme", "fb707814117610778")
            resValue("string", "facebook_client_token", "accb9d67a13fc31412f9694e3bfe2e87")
        }
    }
    namespace = "com.taiwanlife.teamwalk"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(fileTree("libs") { include("*.jar", "*.aar") })
    implementation(libs.patternlockview)
    implementation(libs.recyclercoverflow)
    implementation(libs.jakewharton.timber)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(libs.gms.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.connect.client)


    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.glide)
    implementation(libs.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.material)


    implementation(libs.facebook.sdk)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}