package leancher.android.domain.models

import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification

class Notification(
    key: String,
    packageName: String,
    title: String,
    text: String,
    icon: Icon,
    originalNotification: StatusBarNotification
) {
    val key = key
    val packageName = packageName
    val title = title
    val text = text
    val icon = icon
    val originalNotification = originalNotification
}