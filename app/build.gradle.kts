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
        versionCode = 55
        versionName = "3.0.07"

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

            resValue("string", "csso_url", "https://demo.mutron.com.tw/teamwalk-fe-api/")
            resValue("string", "csso_forget_mima_url", "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk")
            resValue("string", "csso_sign_up_url", "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk")
            resValue("string", "tcav_url", "https://tcavuat.taiwanlife.com/")
            resValue("string", "api_url", "https://demo.mutron.com.tw/teamwalk-fe-api/")
            resValue("string", "web_url", "https://demo.mutron.com.tw/teamwalk/bridge")
            resValue("string", "origin", "http://teamwork-frontend.bestamina.net:8080")
            resValue("string", "taiwanlife_member_url", "https://uatnew.taiwanlife.com/member?service=https:%2F%2Ftcavuat.taiwanlife.com%2Flogin")
            resValue("string", "connect_google_client_id", "773524163063-6je6tkpag1qgcapsn7kuv6s0h85tkct4.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "22BZQG")
            resValue("string", "connect_fitbit_client_s", "565d1f1a0822d1fbf50cedae0041d3ee")
            resValue("string", "connect_garmin_consumer_key", "fee4d993-db0d-4e55-855f-b7ed099ce358")
            resValue("string", "connect_garmin_consumer_s", "4b3u2KpOwZdjymZSW9IqbfKXZ4g9MS4tmQb")
            resValue("string", "garmin_url", "https://connectapi.garmin.com/oauth-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")

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

            resValue("string", "csso_url", "https://cssouat.taiwanlife.com/csso/")
            resValue("string", "csso_forget_mima_url", "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk")
            resValue("string", "csso_sign_up_url", "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk")
            resValue("string", "tcav_url", "https://tcavuat.taiwanlife.com/")
            resValue("string", "api_url", "http://10.1.242.55:9080/frontend/api/")
            resValue("string", "web_url", "http://10.1.242.55:9080/frontend/")
            resValue("string", "origin", "http://10.1.242.55:9080")
            resValue("string", "taiwanlife_member_url", "https://uatnew.taiwanlife.com/member?service=https:%2F%2Ftcavuat.taiwanlife.com%2Flogin")
            resValue("string", "connect_google_client_id", "773524163063-6je6tkpag1qgcapsn7kuv6s0h85tkct4.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "23B3KR")
            resValue("string", "connect_fitbit_client_s", "c586771e4f9a4ae17ec0b1de15f38ef6")
            resValue("string", "connect_garmin_consumer_key", "0b3e7e2c-328a-4bc6-b713-1dd7774ff3d8")
            resValue("string", "connect_garmin_consumer_s", "l9FrmPYVSNgqTT5SOwPfOuT172Vrtmi2tV0")
            resValue("string", "garmin_url", "https://connectapi.garmin.com/oauth-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")

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

            resValue("string", "csso_url", "https://cssouat.taiwanlife.com/csso/")
            resValue("string", "csso_forget_mima_url", "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk")
            resValue("string", "csso_sign_up_url", "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk")
            resValue("string", "tcav_url", "https://tcavuat.taiwanlife.com/")
            resValue("string", "api_url", "https://teamwalk2uat.taiwanlife.com/teamwalk-fe-api/")
            resValue("string", "web_url", "https://teamwalk2uat.taiwanlife.com/bridge")
            resValue("string", "origin", "https://teamwalkuat.taiwanlife.com")
            resValue("string", "taiwanlife_member_url", "https://uatnew.taiwanlife.com/member?service=https:%2F%2Ftcavuat.taiwanlife.com%2Flogin")
            resValue("string", "connect_google_client_id", "29548848966-igadugv3n8h8htautt1lfmg5l6kt34vh.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "23B3KR")
            resValue("string", "connect_fitbit_client_s", "c586771e4f9a4ae17ec0b1de15f38ef6")
            resValue("string", "connect_garmin_consumer_key", "0b3e7e2c-328a-4bc6-b713-1dd7774ff3d8")
            resValue("string", "connect_garmin_consumer_s", "l9FrmPYVSNgqTT5SOwPfOuT172Vrtmi2tV0")
            resValue("string", "garmin_url", "https://connectapi.garmin.com/oauth-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")

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

            resValue("string", "csso_url", "https://csso.taiwanlife.com/csso/")
            resValue("string", "csso_forget_mima_url", "https://csso.taiwanlife.com/csso/mobileForget?outsite=teamwalk")
            resValue("string", "csso_sign_up_url", "https://csso.taiwanlife.com/csso/mobileRegister?outsite=teamwalk")
            resValue("string", "tcav_url", "https://tcav.taiwanlife.com/")
            resValue("string", "api_url", "https://teamwalk.taiwanlife.com/frontend/api/")
            resValue("string", "web_url", "https://teamwalk.taiwanlife.com/frontend/")
            resValue("string", "origin", "https://teamwalk.taiwanlife.com")
            resValue("string", "taiwanlife_member_url", "https://www.taiwanlife.com/member?service=https%3A%2F%2Ftcav.taiwanlife.com%2Flogin")
            resValue("string", "connect_google_client_id", "29548848966-igadugv3n8h8htautt1lfmg5l6kt34vh.apps.googleusercontent.com")
            resValue("string", "connect_fitbit_client_id", "22BVVM")
            resValue("string", "connect_fitbit_client_s", "00403c68f5025d238d022963dfab43dd")
            resValue("string", "connect_garmin_consumer_key", "4a08c804-fbd9-41f7-87f7-b7bcd44d430a")
            resValue("string", "connect_garmin_consumer_s", "DJ1wEi3uNk0rbBDdalerp6ZYqmFJ2D4kf4t")
            resValue("string", "garmin_url", "https://connectapi.garmin.com/oauth-service/oauth/")
            resValue("string", "fitbit_url", "https://api.fitbit.com/oauth2/")

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

//    implementation(libs.material)


    implementation(libs.facebook.sdk)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}