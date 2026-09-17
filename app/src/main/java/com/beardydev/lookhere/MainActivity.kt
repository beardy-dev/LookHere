package com.beardydev.lookhere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.beardydev.lookhere.ui.navigation.LookHereRoot
import com.beardydev.lookhere.ui.splash.AppSplashScreen
import com.beardydev.lookhere.ui.theme.LookHereTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Dismisses as soon as this first Compose frame is drawn -- the Compose
        // AppSplashScreen below (same icon, same brand-red background) then takes
        // over for the full delay, since the system SplashScreen API has no slot
        // for the "Powered by Klipy" attribution text.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            LookHereTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showSplash by remember { mutableStateOf(true) }
                    LaunchedEffect(Unit) {
                        delay(1000)
                        showSplash = false
                    }
                    if (showSplash) {
                        AppSplashScreen()
                    } else {
                        LookHereRoot()
                    }
                }
            }
        }
    }
}
