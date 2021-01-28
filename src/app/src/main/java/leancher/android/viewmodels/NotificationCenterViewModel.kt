package leancher.android.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.annotations.Expose
import leancher.android.domain.intents.LeancherIntent
import leancher.android.domain.models.Notification

class NotificationCenterViewModel(
    private val actions: Actions
) : ViewModel() {

    var notifications: List<Notification> by mutableStateOf(listOf())
        internal set

    fun clearNotifications() = actions.clearNotifications()
    fun showStatusBar() = actions.showStatusBar()
    fun hideStatusBar() = actions.hideStatusBar()
    fun dismissNotification(notification: Notification) = actions.dismissNotification(notification)

    data class Actions(
        val clearNotifications: () -> Unit,
        val showStatusBar: () -> Unit,
        val hideStatusBar: () -> Unit,
        val dismissNotification: (notification: Notification) -> Unit
    )
}