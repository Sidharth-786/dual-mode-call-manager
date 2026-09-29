package com.example.dualmodecallmanager.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.dualmodecallmanager.data.model.AppMode

private val WorkLightScheme = lightColorScheme(
    primary = WorkPrimary,
    onPrimary = WorkOnPrimary,
    primaryContainer = WorkContainer,
    onPrimaryContainer = WorkOnContainer
)

private val WorkDarkScheme = darkColorScheme(
    primary = WorkPrimaryDark,
    onPrimary = WorkOnPrimaryDark,
    primaryContainer = WorkContainerDark,
    onPrimaryContainer = WorkOnContainerDark
)

private val PersonalLightScheme = lightColorScheme(
    primary = PersonalPrimary,
    onPrimary = PersonalOnPrimary,
    primaryContainer = PersonalContainer,
    onPrimaryContainer = PersonalOnContainer
)

private val PersonalDarkScheme = darkColorScheme(
    primary = PersonalPrimaryDark,
    onPrimary = PersonalOnPrimaryDark,
    primaryContainer = PersonalContainerDark,
    onPrimaryContainer = PersonalOnContainerDark
)

@Composable
fun DualModeCallManagerTheme(
    activeMode: AppMode = AppMode.WORK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        activeMode == AppMode.WORK && darkTheme -> WorkDarkScheme
        activeMode == AppMode.WORK -> WorkLightScheme
        activeMode == AppMode.PERSONAL && darkTheme -> PersonalDarkScheme
        else -> PersonalLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
