package leancher.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import leancher.android.ui.theme.Gray
import leancher.android.ui.theme.White

@Composable
fun PageIndicator(shape: Shape, color: Color) {
    Column(modifier = Modifier
        .wrapContentSize(Alignment.Center)
        .padding(horizontal = 10.dp)) {
        Box(
            modifier = Modifier
                .preferredSize(10.dp)
                .clip(shape)
                .background(color)
        )
    }
}

@Composable
fun Paginator(pageAmount: Int, currentPage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        for(i in 0 until pageAmount) {
            val color = if(i == currentPage) White else Gray
            PageIndicator(shape = CircleShape, color = color)
        }
    }
}

@Preview
@Composable
fun PaginatorPreview() { Paginator(pageAmount = 3, currentPage = 1) }