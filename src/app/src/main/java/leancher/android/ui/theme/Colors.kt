package leancher.android.ui.theme

import android.graphics.Color.rgb
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

private val Blue200 = Color(0xff91a4fc)

val White = Color.White
val Gray = Color.Gray

// TODO: cool dark theme
val DarkColors = darkColors(
        primary = Color(rgb(0,99,177)),
        onPrimary = Color.Blue,
        primaryVariant = Color.Cyan,

        secondary = Blue200,
        onSecondary = Color.Red,

        background = Color.DarkGray,
        onBackground = Color.LightGray,

        surface = Color.Green,
        onSurface = Color.Magenta
)

val LightColors = lightColors(
        primary = Color(rgb(0,153,188)),
        onPrimary = Color(rgb(0,153,188)),
        primaryVariant = Color(rgb(0,153,188)),

        secondary = Color(rgb(45,125,154)),
        onSecondary = Color(rgb(0,183,195)),
        secondaryVariant = Color(rgb(0,153,188)),

        background = Color(rgb(0,120,215)),
        onBackground = Color(rgb(0,99,177)),

        surface = Color(rgb(0,183,195)),
        onSurface = Color(rgb(3,131,135))

)