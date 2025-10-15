package com.vergil.lottery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.vergil.lottery.presentation.MainScreen
import com.vergil.lottery.di.AppModule
import com.vergil.lottery.presentation.theme.LotteryTheme
import timber.log.Timber


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        WindowCompat.setDecorFitsSystemWindows(window, false)


        AppModule.init(this)


        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("MainActivity onCreate")

        setContent {
            val themeViewModel = AppModule.themeViewModel
            val themeMode by themeViewModel.themeMode.collectAsState()

            LotteryTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}