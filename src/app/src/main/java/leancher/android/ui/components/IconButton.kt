package leancher.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import leancher.android.ui.theme.White

@Composable
fun IconButton(icon: ImageVector, action: () -> Unit, text: String? = null) {
    if(icon != null && text != null) {
        Button(onClick = {
            action()
        }) {
            Icon(icon, Modifier.preferredSize(20.dp), tint = White)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "$text")
        }
    } else {
        Icon(icon, Modifier
                .preferredSize(30.dp)
                .clickable(onClick = { action() }), tint = White)
    }

}