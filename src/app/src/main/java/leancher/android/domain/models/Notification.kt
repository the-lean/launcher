package leancher.android.domain.models

import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification

data class Notification(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val icon: Icon,
    val originalNotification: StatusBarNotification
)