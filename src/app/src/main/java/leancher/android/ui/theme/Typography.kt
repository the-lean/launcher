package leancher.android.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.sp
import leancher.android.R

// Example: define custom fonts
 val SegoeUI = fontFamily(
        font(R.font.segoeui),
        font(R.font.segoeuil, FontWeight.Light),
        font(R.font.seguisb, FontWeight.Bold)
 )

val LeancherTypography = Typography (
        h1 = TextStyle(
                fontFamily = SegoeUI,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = White
        ),
        h2 = TextStyle(
                fontFamily = SegoeUI,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = White
        ),
        h5 = TextStyle(
                fontFamily = SegoeUI,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray
        ),
        subtitle1 = TextStyle(
                fontFamily = SegoeUI,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = White
        ),
        body1 = TextStyle(
                fontFamily = SegoeUI,
                fontSize = 15.sp,
                color = Gray
        ),
        body2 = TextStyle(
                fontFamily = SegoeUI,
                fontSize = 12.sp,
                color = Gray
        )
)