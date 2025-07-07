package com.example.checklist.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.checklist.ui.components.AppLogo
import com.example.checklist.ui.theme.ChecklistTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
){
    LaunchedEffect(Unit) {
    delay(2000) // or 2000L for 2s delay
    onTimeout()
}
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ){
        AppLogo(
            fontSize = 44,
            iconSize = 44
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview(){
    ChecklistTheme  {
        SplashScreen(
            onTimeout = {}
        )
    }
}