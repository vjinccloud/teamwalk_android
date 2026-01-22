package com.taiwanlife.teamwalk.di

import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.remote.ApiRepository
import com.taiwanlife.teamwalk.remote.CssoRepository
import com.taiwanlife.teamwalk.remote.FitbitRepository
import com.taiwanlife.teamwalk.remote.GarminRepository
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.interceptor.ApiLoggingInterceptor
import com.taiwanlife.teamwalk.remote.interceptor.TokenInterceptor
import com.taiwanlife.teamwalk.remote.service.APIService
import com.taiwanlife.teamwalk.remote.service.CssoService
import com.taiwanlife.teamwalk.remote.service.FitBitService
import com.taiwanlife.teamwalk.remote.service.GarminService
import com.taiwanlife.teamwalk.ui.common.FitbitViewModel
import com.taiwanlife.teamwalk.ui.common.GarminViewModel
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import com.taiwanlife.teamwalk.ui.login.LoginViewModel
import com.taiwanlife.teamwalk.ui.main.MainViewModel
import com.taiwanlife.teamwalk.ui.onboarding.OnBoardingViewModel
import com.taiwanlife.teamwalk.ui.pattern.PatternSetupViewModel
import com.taiwanlife.teamwalk.ui.test.TestViewModel
import com.taiwanlife.teamwalk.utils.getGson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single { getGson() }

    single { SharedEventViewModel() }

    single<OkHttpClient> {
        val logger = HttpLoggingInterceptor()
        logger.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }

        val tokenInterceptor = TokenInterceptor()

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(tokenInterceptor)

        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(ApiLoggingInterceptor())
        }

        clientBuilder.build()
    }

    // 建立屬於API的Retrofit
    single<Retrofit>(named("api")) {
        Retrofit.Builder()
            .baseUrl(EnvironmentManager.getEnvironmentConfig().apiUrl)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    // 建立屬於CSSO的Retrofit
    single<Retrofit>(named("csso")) {
        Retrofit.Builder()
            .baseUrl(EnvironmentManager.getEnvironmentConfig().cssoUrl)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    // 建立屬於Garmin的Retrofit
    single<Retrofit>(named("garmin")) {
        Retrofit.Builder()
            .baseUrl(EnvironmentManager.getEnvironmentConfig().garminUrl)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }
    // 建立屬於Google Fitbit的Retrofit
    single<Retrofit>(named("fitbit")) {
        Retrofit.Builder()
            .baseUrl(EnvironmentManager.getEnvironmentConfig().googleFitbitUrl)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    single<APIService> {
        get<Retrofit>(named("api")).create(APIService::class.java)
    }

    single<CssoService> {
        get<Retrofit>(named("csso")).create(CssoService::class.java)
    }

    single<GarminService> {
        get<Retrofit>(named("garmin")).create(GarminService::class.java)
    }

    single<FitBitService> {
        get<Retrofit>(named("fitbit")).create(FitBitService::class.java)
    }

    single { ApiRepository(get()) }
    single { CssoRepository(get()) }
    single { GarminRepository(get()) }
    single { FitbitRepository(get()) }

    single<Repository> {
        Repository(get(), get(), get(), get())
    }

    viewModelOf(::MainViewModel)
    viewModelOf(::GarminViewModel)
    viewModelOf(::FitbitViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::OnBoardingViewModel)
    viewModelOf(::TestViewModel)
    viewModelOf(::PatternSetupViewModel)
}