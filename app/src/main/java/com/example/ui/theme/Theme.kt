package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BraveOrange,
    onPrimary = BraveTextPrimary,
    primaryContainer = BraveOrangeContainer,
    onPrimaryContainer = BraveOrangeLight,
    secondary = BraveOrangeLight,
    onSecondary = BraveTextPrimary,
    tertiary = BraveShieldGreen,
    onTertiary = BraveTextPrimary,
    tertiaryContainer = BraveShieldGreenContainer,
    background = BraveDarkBackground,
    onBackground = BraveTextPrimary,
    surface = BraveDarkSurface,
    onSurface = BraveTextPrimary,
    surfaceVariant = BraveDarkSurfaceVariant,
    onSurfaceVariant = BraveTextSecondary,
    outline = BraveDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BraveOrange,
    onPrimary = BraveTextPrimary,
    primaryContainer = BraveOrangeContainer,
    onPrimaryContainer = BraveOrangeDark,
    secondary = BraveOrangeLight,
    onSecondary = BraveTextPrimary,
    tertiary = BraveShieldGreen,
    onTertiary = BraveTextPrimary,
    background = BraveLightBackground,
    onBackground = BraveLightTextPrimary,
    surface = BraveLightSurface,
    onSurface = BraveLightTextPrimary,
    surfaceVariant = BraveLightSurfaceVariant,
    onSurfaceVariant = BraveLightTextSecondary,
    outline = BraveDarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Brave uses sleek dark theme by default, but respects system theme
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // default dark for authentic Brave look

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
