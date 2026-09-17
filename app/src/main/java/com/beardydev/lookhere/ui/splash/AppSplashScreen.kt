package com.beardydev.lookhere.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.beardydev.lookhere.R

/**
 * Takes over from the system SplashScreen (same icon, same brand-red
 * background) once the first Compose frame is drawn, so we can show the
 * official Klipy attribution lockup at the bottom -- the system SplashScreen
 * API has no slot for arbitrary extra content like this.
 */
@Composable
fun AppSplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.splash_background)),
    ) {
        Image(
            painter = painterResource(R.drawable.lookhere_icon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp),
        )
        Image(
            painter = painterResource(R.drawable.powered_by_klipy),
            contentDescription = "Powered by Klipy",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .width(180.dp),
        )
    }
}
