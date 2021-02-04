package leancher.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import leancher.android.ui.theme.White

@Composable
fun PagerCard(page: @Composable() (() -> Unit)) {
    val borderShape = RoundedCornerShape(topLeftPercent = 15, topRightPercent = 15, bottomLeftPercent = 0, bottomRightPercent = 0)
    Card(Modifier.fillMaxSize().shadow(elevation = 10.dp, shape = borderShape),
        backgroundColor = White,
        shape = borderShape) {
        Column(Modifier.fillMaxSize().padding(top = 30.dp).padding(horizontal = 5.dp)) {
            page()
        }
    }
}