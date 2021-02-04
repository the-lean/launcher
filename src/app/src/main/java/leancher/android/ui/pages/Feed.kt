package leancher.android.ui.pages

import android.appwidget.AppWidgetHostView
import android.content.pm.ProviderInfo
import android.widget.LinearLayout
import android.widget.ListPopupWindow.MATCH_PARENT
import android.widget.ListPopupWindow.WRAP_CONTENT
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import leancher.android.R
import leancher.android.domain.models.PageTitle
import leancher.android.ui.components.ActionDialog
import leancher.android.ui.components.IconButton
import leancher.android.ui.components.TitleCard
import leancher.android.viewmodels.FeedViewModel
import leancher.android.viewmodels.Widget


@Composable
fun Feed(vm: FeedViewModel) {
    val context = AmbientContext.current

    val feedTitleModel = PageTitle(
        context.getString(R.string.page_widget_feed),
        context.getString(R.string.hows_your_day),
        R.drawable.home
    )

    Row {
        Column(Modifier.padding(10.dp)) {
            TitleCard(pageTitle = feedTitleModel) {
                IconButton(
                    icon = Icons.Filled.Add,
                    action = vm.actions.onSelectWidget,
                    text = context.getString(R.string.add)
                )
            }
        }
    }

    Row {
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