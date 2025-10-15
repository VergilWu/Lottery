package com.vergil.lottery.di

import android.content.Context
import com.vergil.lottery.BuildConfig
import com.vergil.lottery.data.local.LotteryDatabase
import com.vergil.lottery.data.local.preferences.PreferencesManager
import com.vergil.lottery.data.remote.LotteryApiService
import com.vergil.lottery.presentation.theme.ThemeViewModel
import com.vergil.lottery.data.repository.LotteryRepository
import com.vergil.lottery.data.cache.CachedLotteryRepository
import com.vergil.lottery.data.cache.CacheManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber


object AppModule {

    lateinit var applicationContext: Context


    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            prettyPrint = BuildConfig.DEBUG
        }
    }

    val httpClient: HttpClient by lazy {
        HttpClient(Android) {

            defaultRequest {
                url("https://www.szxk365.com/api/openapi.lottery/")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }


            install(ContentNegotiation) {
                json(json)
            }


            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Timber.tag("Ktor").d(message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }


            engine {
                connectTimeout = 10_000
                socketTimeout = 30_000
            }
        }
    }

    val database: LotteryDatabase by lazy {
        LotteryDatabase.getInstance(applicationContext)
    }

    val drawHistoryDao by lazy {
        database.drawHistoryDao()
    }

    val preferencesManager by lazy {
        PreferencesManager(applicationContext)
    }

    val cacheManager: CacheManager by lazy {
        CacheManager(applicationContext)
    }

    val apiService: LotteryApiService by lazy {
        LotteryApiService(httpClient)
    }


    private val originalRepository: LotteryRepository by lazy {
        LotteryRepository(apiService, drawHistoryDao)
    }


    val cachedRepository: CachedLotteryRepository by lazy {
        CachedLotteryRepository(applicationContext, originalRepository)
    }


    val lotteryRepository: LotteryRepository by lazy {
        originalRepository
    }

    val themeViewModel: ThemeViewModel by lazy {
        ThemeViewModel(preferencesManager)
    }


    fun createPredictionViewModel(): com.vergil.lottery.presentation.screens.prediction.PredictionViewModel {
        return com.vergil.lottery.presentation.screens.prediction.PredictionViewModel(
            repository = cachedRepository,
            context = applicationContext  
        )
    }
}

