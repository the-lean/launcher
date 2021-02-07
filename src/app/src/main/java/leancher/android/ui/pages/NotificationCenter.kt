package leancher.android.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import leancher.android.MainActivity
import leancher.android.R
import leancher.android.domain.models.Notification
import leancher.android.domain.models.PageTitle
import leancher.android.ui.components.*
import leancher.android.ui.components.itemtemplates.NotificationItemTemplate
import leancher.android.viewmodels.NotificationCenterViewModel


@Composable
fun NotificationCenter(vm: NotificationCenterViewModel) {
    val context = AmbientContext.current

    Row(Modifier.padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Column {
            Image(
                imageResource(R.drawable.notification),
                modifier = Modifier.preferredSize(35.dp, 35.dp),
                contentScale = ContentScale.Crop)
        }

        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = "Swipe right to dismiss ...", style = MaterialTheme.typography.h5)
        }
    }

    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        ActionSwitch(
            onAction = { vm.showStatusBar() },
            offAction = { vm.hideStatusBar() },
            text = "Show notifications"
        )
        Spacer(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .background(color = MaterialTheme.colors.secondary)
                .defaultMinSizeConstraints(minHeight = 30.dp, minWidth = 2.dp)
        )
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            IconButton(icon = Icons.Filled.Delete, action = vm::clearNotifications, "Clear")
        }
    }

    Row(Modifier.padding(top = 30.dp)) {
        SwipeActionList(
            innerPadding = PaddingValues(),
            items = vm.notifications,
            itemTemplate = { notification -> NotificationItemTemplate(notification) },
            onSwipe = { notification -> vm.dismissNotification(notification) },
            onClick = { notification ->
                val launchIntent = context.packageManager?.getLaunchIntentForPackage(notification.packageName)
                if(launchIntent != null) {
                    context.startActivity(launchIntent)
                }
            },
            Modifier.fillMaxWidth()
        )
    }
}