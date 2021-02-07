package leancher.android.ui.theme

import android.graphics.Color.rgb
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

val White = Color.White
val Gray = Color.DarkGray
val Black = Color.Black
val Orange = Color(rgb(224, 152, 36))

val LightColors = lightColors(
        primary = Color(rgb(0,153,188)),
        onPrimary = Color(rgb(0,153,188)),
        primaryVariant = Color(rgb(0,153,188)),

        secondary = Color(rgb(45,125,154)),
        onSecondary = Color(rgb(0,183,195)),
        secondaryVariant = Color(rgb(0,153,188)),

        background = Color(rgb(0,120,215)),
        onBackground = Color(rgb(0,99,177)),

        surface = White,
        onSurface = Color(rgb(3,131,135))

)

// TODO: cool dark theme
val DarkColors = LightColors