package leancher.android.viewmodels

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import leancher.android.domain.models.Notification

data class Widget (val id: Int, val providerInfo: AppWidgetProviderInfo)

class FeedViewModel(widgets: List<Widget>, val actions: Actions): ViewModel() {
    var widgets: List<Widget> by mutableStateOf(widgets)
        private set

    fun addWidget(widget: Widget) {
        widgets = widgets + widget
    }

    fun removeWidget(widget: Widget) {
        widgets = widgets - widget
    }

    data class Actions(
        val onSelectWidget: () -> Unit,
        val createWidgetHostView: (widget: Widget) -> AppWidgetHostView
    )
}