package leancher.android.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientAnimationClock
import androidx.compose.ui.unit.dp
import leancher.android.ui.components.Pager
import leancher.android.ui.components.PagerCard
import leancher.android.ui.components.PagerState
import leancher.android.ui.components.Paginator

data class Page (
    val name: String,
    val component: @Composable() () -> Unit
)

@Composable
fun Pager(
    pages: List<Page>
) {
    val clock = AmbientAnimationClock.current
    val pagerState = remember(clock) { PagerState(clock, 1, 0, 2) }
    val currentPage = pagerState.currentPage

    run {
        val clock = AmbientAnimationClock.current
        remember(clock) { PagerState(clock, 1, 0, 2) }
    }

    Pager(state = pagerState) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(Modifier.padding(top = 50.dp)) {
                Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.Start) {
                    Text(text = pages.getOrNull(page)?.name.toString(), style = MaterialTheme.typography.h1)
                }

                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.Center) {
                    PagerCard { Column { pages.getOrNull(page)?.component?.invoke() } }
                }
            }
        }
    }
    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom) {
        Column { Paginator(pageAmount = 3, currentPage = currentPage) }
    }
}