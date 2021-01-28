package leancher.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onCommit
import androidx.compose.ui.Modifier

@Composable
fun <T> SwipeActionList(
    innerPadding: PaddingValues,
    items: List<T>,
    itemTemplate: @Composable() ((T) -> Unit),
    onSwipe: (T) -> Unit,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumnFor(
        modifier = modifier.padding(innerPadding),
        items = items,
    ) { item ->
        AnimatedSwipe(
            item = item,
            background = { isDismissed ->
                // basic component structure from: https://gist.github.com/bmc08gt/fca95db3bf9fcf255d76f03ec10ea3f9
                /** define your background delete view here
                 * possibly:
                Box(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = Color.Red,
                paddingStart = 20.dp,
                paddingEnd = 20.dp,
                gravity = ContentGravity.CenterEnd
                ) {
                val alpha = animate( if (isDismissed) 0f else 1f)
                Icon(Icons.Filled.Delete, tint = Color.White.copy(alpha = alpha))
                }

                using isDismissed to control alpha of the icon or content in the box
                 */
            },
            content = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                onClick(item)
                            })) {
                    itemTemplate(item)
                }
            },
            onDismiss = { onSwipe(it) },
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun <T> AnimatedSwipe(
    modifier: Modifier = Modifier,
    item: T,
    background: @Composable (isDismissed: Boolean) -> Unit,
    content: @Composable (isDismissed: Boolean) -> Unit,
    directions: Set<DismissDirection> = setOf(DismissDirection.StartToEnd),
    enter: EnterTransition = expandVertically(),
    exit: ExitTransition = shrinkVertically(
        animSpec = tween(
            durationMillis = 500,
        )
    ),
    onDismiss: (T) -> Unit
) {
    val dismissState = rememberDismissState()
    val isDismissed = dismissState.isDismissed(DismissDirection.StartToEnd)

    onCommit(dismissState.value) {
        if (dismissState.value == DismissValue.DismissedToEnd) {
            onDismiss(item)
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = !isDismissed,
        enter = enter,
        exit = exit
    ) {
        SwipeToDismiss(
            modifier = modifier,
            state = dismissState,
            directions = directions,
            background = { background(isDismissed) },
            dismissContent = { content(isDismissed) }
        )
    }
}