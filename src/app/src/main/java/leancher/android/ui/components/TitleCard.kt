package leancher.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import leancher.android.domain.models.PageTitle

@Composable
fun TitleCard(pageTitle: PageTitle, actionButton: @Composable() (() -> Unit)?) {
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = CenterVertically,
            modifier = Modifier.padding(16.dp)) {

            Image(imageResource(id = pageTitle.imageResource),
                    modifier = Modifier.preferredSize(50.dp, 50.dp),
                    contentScale = ContentScale.Crop)

            Column(modifier = Modifier.padding(10.dp)) {
                Text(pageTitle.title, style = MaterialTheme.typography.h1)
                Text(pageTitle.longText, style = MaterialTheme.typography.body1)
            }

            if(actionButton != null) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                    actionButton()
                }
            }
        }
    }
}