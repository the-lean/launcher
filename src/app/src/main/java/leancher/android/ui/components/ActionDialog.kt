package leancher.android.ui.components

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import leancher.android.R
import leancher.android.ui.theme.Black
import leancher.android.ui.theme.White
import leancher.android.ui.util.TranslateString

@Composable
fun ActionDialog(
    title: String, text: String,
    showDialog: Boolean, setShowDialog: (Boolean) -> Unit,
    confirmAction: () -> Unit, confirmText: String = TranslateString(id = R.string.yes),
    dismissAction: (() -> Unit)? = null, dismissText: String = TranslateString(id = R.string.no)
) {

    val buttonModifier = Modifier.width(100.dp).shadow(5.dp)
    val innerPadding = PaddingValues(5.dp)

    if (showDialog) {
        if(dismissAction != null) {
            AlertDialog(
                    onDismissRequest = {},
                    title = { Text(text = title, style = MaterialTheme.typography.h5, color = Black) },
                    text = { Text(text = text, style = MaterialTheme.typography.body1) },
                    confirmButton = {
                        Button(onClick = {
                                    confirmAction()
                                    setShowDialog(false)
                                }, buttonModifier, contentPadding = innerPadding
                        ) { Text(confirmText, Modifier.padding(0.dp), style = MaterialTheme.typography.body1, color = White) }
                    },
                    dismissButton = {
                        Button(onClick = {
                                    dismissAction()
                                    setShowDialog(false)
                                }, buttonModifier, contentPadding = innerPadding
                        ) {
                            Text(dismissText, Modifier.padding(0.dp), style = MaterialTheme.typography.body1, color = White)
                        }
                    }
            )
        } else {
            AlertDialog(
                    onDismissRequest = {},
                    title = { Text(text = title) },
                    text = { Text(text = text) },
                    confirmButton = {
                        Button(onClick = {
                            confirmAction()
                            setShowDialog(false)
                        }, buttonModifier, contentPadding = innerPadding
                        ) { Text(confirmText, style = MaterialTheme.typography.body1, color = White) }
                    }
            )
        }
    }
}

@Composable
@Deprecated("Do not use in prod - it is just a DEMO")
fun ActionDialogDemo() {
    // State to manage if the alert dialog is showing or not.
    // Default is false (not showing)
    val (showDialog, setShowDialog) =  remember { mutableStateOf(false) }

    Button(onClick = { setShowDialog(true) }) {
        Text("Show Dialog")
    }
    // Create alert dialog, pass the showDialog state to this Composable
    ActionDialog(
            "Testdialog", "Text",
            showDialog, setShowDialog,
            { println("Confirm") }, "Confirm",
            { println("Dismiss") }, "Dismiss")
}