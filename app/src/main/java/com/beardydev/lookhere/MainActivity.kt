package com.beardydev.lookhere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.beardydev.lookhere.ui.navigation.LookHereRoot
import com.beardydev.lookhere.ui.theme.LookHereTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var keepSplashOnScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        // A tad longer than the default (which dismisses as soon as the first
        // frame is drawn -- often barely visible for a screen this simple).
        lifecycleScope.launch {
            delay(500)
            keepSplashOnScreen = false
        }

        enableEdgeToEdge()
        setContent {
            LookHereTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LookHereRoot()
                }
            }
        }
    }
}
