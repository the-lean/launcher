package leancher.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import leancher.android.ui.theme.White
import leancher.android.ui.util.TranslateString

@Composable
@Deprecated("Use SwipeActionList")
fun NotificationList(notifications: List<Notification>) {
    val context = AmbientContext.current
    val activity: MainActivity = context as MainActivity

    ScrollableColumn(Modifier.fillMaxHeight()) {
        if(notifications.isEmpty()) {
            Text(text = TranslateString(id = R.string.no_notifications))
        } else {
            notifications.forEach { notification ->
                if (notifications.indexOf(notification) == 0) {
                    Text(
                        modifier = Modifier.padding(15.dp),
                        text = TranslateString(id = R.string.all_notifications),
                        style = MaterialTheme.typography.h1
                    )
                }
                ListItem {
                    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.Center) {
                            Image(
                                imageResource(id = R.drawable.notification),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.preferredSize(20.dp, 20.dp)
                            )
                        }
                        Column(Modifier.padding(horizontal = 10.dp)) {
                            Row() {
                                Text(text = notification.title, style = MaterialTheme.typography.subtitle1)
                            }
                            Row() {
                                Text(text = notification.text, style = MaterialTheme.typography.body1)
                            }
                        }
                        Column(
                            Modifier
                                .padding(horizontal = 10.dp)
                                .fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            IconButton(icon = Icons.Filled.Delete, action = {
                                activity.dismissNotification(notification)
                            })
                        }
                    }
                }
                Divider(color = White)
            }   
        }
    }
}