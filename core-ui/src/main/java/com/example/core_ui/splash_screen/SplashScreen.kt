package com.example.core_ui.splash_screen

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.example.core_ui.R
import kotlinx.coroutines.delay
@Composable
fun SplashScreen(
    onShowSplashScreenTimeEnd: () -> Unit
) {
    val scale = remember {
        Animatable(0f)
    }
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.9f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = {
                    OvershootInterpolator(8f).getInterpolation(it)
                }
            )
        )
        delay(3000L)
        onShowSplashScreenTimeEnd()

    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center

    ) {
        Image(
            painter = painterResource(id = R.drawable.dr_hassan_logo),
            contentDescription = "image logo",
            modifier = Modifier.scale(scale.value)
        )
    }



}