package leancher.android.ui.components.itemtemplates

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import leancher.android.R
import leancher.android.domain.models.Notification
import leancher.android.ui.theme.White

@Composable
fun NotificationItemTemplate(notification: Notification) {
    ListItem {
        Row(
            Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically
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
        }
    }
    Divider(color = White)
}