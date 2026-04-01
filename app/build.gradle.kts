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
        minSdk = 29
        targetSdk = 36
        versionCode = 74
        versionName = "3.0.35"

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
            applicationIdSuffix = ".debug"

            signingConfig = signingConfigs.getByName("debug")

            // APP 名稱
            resValue("string", "app_name", "DebugTeamWalk")
            // 綁定成功時的domain(scheme)
            resValue("string", "redirect_scheme", "teamwalkdebug")

            resValue("string", "csso_url", "https://demo.mutron.com.tw/teamwalk-fe-api/")
            resValue("string", "csso_forget_mima_url", "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk2")
            resValue("string", "csso_sign_up_url", "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk2")
            resValue("string", "tcav_url", "https://tcavuat.taiwanlife.com/")
            resValue("string", "api_url", "https://demo.mutron.com.tw/teamwalk-fe-api/")
            resValue("string", "web_url_base", "https://demo.mutron.com.tw/teamwalk/")
            resValue("string", "web_url", "https://demo.mutron.com.tw/teamwalk/bridge/")
            resValue("string", "connect_google_client_id", "773524163063-6je6tkpag1qgcapsn7kuv6s0h85tkct4.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "22BZQG")
            resValue("string", "connect_fitbit_client_s", "565d1f1a0822d1fbf50cedae0041d3ee")
            resValue("string", "connect_garmin_consumer_key", "0fe5a7c0-bc5b-41f5-aa05-97978415d272")
            resValue("string", "connect_garmin_consumer_s", "nsQkfD0nwCq2SHAwiIlhQatmAqYtWV/bRjQewaPZ+vM")
            resValue("string", "connect_garmin_portal", "https://connect.garmin.com/oauth2Confirm")
            resValue("string", "connect_garmin_redirect_url", "https://demo.mutron.com.tw/webconnectgarmin")
            resValue("string", "app_link_host", "demo.mutron.com.tw")
            resValue("string", "login_success_redirect_url", "https://demo.mutron.com.tw/loginsuccess")
            resValue("string", "garmin_api_url", "https://diauth.garmin.com/di-oauth2-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")
        }
        create("sit") {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".sit"

            signingConfig = signingConfigs.getByName("debug")

            // APP 名稱
            resValue("string", "app_name", "SITTeamWalk")
            // 綁定成功時的domain(scheme)
            resValue("string", "redirect_scheme", "teamwalksit")

            resValue("string", "csso_url", "https://demo.mutron.com.tw/teamwalk-fe-api/")
            resValue("string", "csso_forget_mima_url", "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk2")
            resValue("string", "csso_sign_up_url", "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk2")
            resValue("string", "tcav_url", "https://tcavuat.taiwanlife.com/")
            resValue("string", "api_url", "https://demo.mutron.com.tw/teamwalk-fe-api/")
            resValue("string", "web_url_base", "https://demo.mutron.com.tw/teamwalk/")
            resValue("string", "web_url", "https://demo.mutron.com.tw/teamwalk/bridge/")
            resValue("string", "connect_google_client_id", "773524163063-6je6tkpag1qgcapsn7kuv6s0h85tkct4.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "22BZQG")
            resValue("string", "connect_fitbit_client_s", "565d1f1a0822d1fbf50cedae0041d3ee")
            resValue("string", "connect_garmin_consumer_key", "0fe5a7c0-bc5b-41f5-aa05-97978415d272")
            resValue("string", "connect_garmin_consumer_s", "nsQkfD0nwCq2SHAwiIlhQatmAqYtWV/bRjQewaPZ+vM")
            resValue("string", "connect_garmin_portal", "https://connect.garmin.com/oauth2Confirm")
            resValue("string", "connect_garmin_redirect_url", "https://demo.mutron.com.tw/webconnectgarmin")
            resValue("string", "app_link_host", "demo.mutron.com.tw")
            resValue("string", "login_success_redirect_url", "https://demo.mutron.com.tw/loginsuccess")
            resValue("string", "garmin_api_url", "https://diauth.garmin.com/di-oauth2-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")
        }
        create("uat") {
            isDebuggable = true
            isMinifyEnabled = false
            // 如要做UAT資安檢測 請將下面註解部分打開
//            isDebuggable = false
//            isMinifyEnabled = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
            applicationIdSuffix = ".uat"

            signingConfig = signingConfigs.getByName("debug")

            // APP 名稱
            resValue("string", "app_name", "UATTeamWalk")
            // 綁定成功時的domain(scheme)
            resValue("string", "redirect_scheme", "teamwalkuat")

            resValue("string", "csso_url", "https://cssouat.taiwanlife.com/csso/")
            resValue("string", "csso_forget_mima_url", "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk2")
            resValue("string", "csso_sign_up_url", "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk2")
            resValue("string", "tcav_url", "https://tcavuat.taiwanlife.com/")
            resValue("string", "api_url", "https://teamwalk2uat.taiwanlife.com/teamwalk-fe-api/")
            resValue("string", "web_url_base", "https://teamwalk2uat.taiwanlife.com/")
            resValue("string", "web_url", "https://teamwalk2uat.taiwanlife.com/bridge/")
            resValue("string", "connect_google_client_id", "29548848966-igadugv3n8h8htautt1lfmg5l6kt34vh.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "23B3KR")
            resValue("string", "connect_fitbit_client_s", "c586771e4f9a4ae17ec0b1de15f38ef6")
            resValue("string", "connect_garmin_consumer_key", "beae74e2-1896-49c0-b475-6d7ce94fa8c6")
            resValue("string", "connect_garmin_consumer_s", "Icm0qiNW+HFFFbt/A3mjZXuLn4Ak3rbkjheOjdAr+d0")
            resValue("string", "connect_garmin_portal", "https://connect.garmin.com/oauth2Confirm")
            resValue("string", "connect_garmin_redirect_url", "https://teamwalk2uat.taiwanlife.com/webconnectgarmin")
            resValue("string", "app_link_host", "teamwalk2uat.taiwanlife.com")
            resValue("string", "login_success_redirect_url", "https://teamwalk2uat.taiwanlife.com/loginsuccess")
            resValue("string", "garmin_api_url", "https://diauth.garmin.com/di-oauth2-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")
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
            resValue("string", "redirect_scheme", "https://teamwalk2.taiwanlife.com/loginsuccess")

            resValue("string", "csso_url", "https://csso.taiwanlife.com/csso/")
            resValue("string", "csso_forget_mima_url", "https://csso.taiwanlife.com/csso/mobileForget?outsite=teamwalk2")
            resValue("string", "csso_sign_up_url", "https://csso.taiwanlife.com/csso/mobileRegister?outsite=teamwalk2")
            resValue("string", "tcav_url", "https://tcav.taiwanlife.com/")
            resValue("string", "api_url", "https://teamwalk2.taiwanlife.com/teamwalk-fe-api/")
            resValue("string", "web_url_base", "https://teamwalk2.taiwanlife.com/")
            resValue("string", "web_url", "https://teamwalk2.taiwanlife.com/bridge/")
            resValue("string", "connect_google_client_id", "29548848966-igadugv3n8h8htautt1lfmg5l6kt34vh.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "22BVVM")
            resValue("string", "connect_fitbit_client_s", "00403c68f5025d238d022963dfab43dd")
            resValue("string", "connect_garmin_consumer_key", "5932dde5-77a2-41de-ac2b-f2774f14e0de")
            resValue("string", "connect_garmin_consumer_s", "J+7jHnYNawnKwAr6/tv3crfGuTgBcsMyne27QdERvXA")
            resValue("string", "connect_garmin_portal", "https://connect.garmin.com/oauth2Confirm")
            resValue("string", "connect_garmin_redirect_url", "https://teamwalk2.taiwanlife.com/webconnectgarmin")
            resValue("string", "app_link_host", "teamwalk2.taiwanlife.com")
            resValue("string", "login_success_redirect_url", "https://teamwalk2.taiwanlife.com/loginsuccess")
            resValue("string", "garmin_api_url", "https://diauth.garmin.com/di-oauth2-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")
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
    implementation(libs.tink.android)
    implementation(libs.androidx.datastore.preferences)

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

//    implementation(libs.material)


//    implementation(libs.facebook.sdk)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}