package leancher.android.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext


// Interim solution for assigning translated default values in constructors
@Composable
fun TranslateString(id: Int): String {
    val context = AmbientContext.current
    return context.getString(id)
}