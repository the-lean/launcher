package leancher.android.ui.pages

import android.appwidget.AppWidgetHostView
import android.widget.LinearLayout
import android.widget.ListPopupWindow.MATCH_PARENT
import android.widget.ListPopupWindow.WRAP_CONTENT
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import leancher.android.R
import leancher.android.ui.components.IconButton
import leancher.android.viewmodels.FeedViewModel
import leancher.android.viewmodels.Widget


@Composable
fun Feed(vm: FeedViewModel) {
    val context = AmbientContext.current

    Row(Modifier.padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Column {
            Image(
                imageResource(R.drawable.home),
                modifier = Modifier.preferredSize(35.dp, 35.dp),
                contentScale = ContentScale.Crop)
        }

        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = "Wanna add some widgets?", style = MaterialTheme.typography.h5)
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            IconButton(
                icon = Icons.Filled.Add,
                action = vm.actions.onSelectWidget
            )
        }
    }

    Row(Modifier.padding(top = 30.dp)) {
        WidgetHostView(
            widgets = vm.widgets,
            removeWidget = vm::removeWidget,
            createWidgetHostView = vm.actions.createWidgetHostView
        )
    }
}

@Composable
fun WidgetHostView(
    widgets: List<Widget>,
    removeWidget: (widget: Widget) -> Unit,
    createWidgetHostView: (widget: Widget) -> AppWidgetHostView
) {

    // TODO: show confirmation dialog before removing widget
    //    val context = AmbientContext.current
    //    val (showDialog, setShowDialog) = remember { mutableStateOf(show) }
    //    ActionDialog(
    //        title = context.getString(R.string.remove_widget_title), text = context.getString(R.string.remove_widget_text),
    //        showDialog = showDialog, setShowDialog = setShowDialog,
    //        confirmAction = { removeWidget(widget) },
    //        dismissAction = {  },
    //    )

    ScrollableColumn {
        widgets.forEach { widget ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = { },
                            onLongClick = {
                                removeWidget(widget)
                            }), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(Modifier.padding(horizontal = 15.dp)) {
                        AndroidView(
                            viewBlock = {
                                createWidgetHostView(widget).apply {
                                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                                    setOnLongClickListener {
                                        removeWidget(widget)
                                        true // <- set to true
                                    }
                                }
                            })
                    }
                }
            }
        }
    }
}