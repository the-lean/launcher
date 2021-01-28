package leancher.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

// THEME DOC:
// https://developer.android.com/jetpack/compose/themes

@Composable
fun LeancherTheme(
        content: @Composable () -> Unit,
        darkTheme: Boolean = isSystemInDarkTheme()
) {
    MaterialTheme(
            colors = LightColors,
            // TODO: change colors to (when implemented dark theme)
            // colors = if (darkTheme) DarkColors else LightColors,
            typography = LeancherTypography,
            content = content
    )
}