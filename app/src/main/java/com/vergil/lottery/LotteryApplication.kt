package com.vergil.lottery

import android.app.Application
import android.content.Context
import com.vergil.lottery.di.AppModule
import timber.log.Timber

class LotteryApplication : Application() {


    override fun onCreate() {
        super.onCreate()


        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }


        AppModule.init(this)


        Timber.d("LotteryApplication initialized")
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

}




