package leancher.android.ui.components

import androidx.compose.foundation.Text
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AmbientContext
import leancher.android.R
import leancher.android.ui.util.TranslateString

@Composable
fun ActionDialog(
    title: String, text: String,
    showDialog: Boolean, setShowDialog: (Boolean) -> Unit,
    confirmAction: () -> Unit, confirmText: String = TranslateString(id = R.string.yes),
    dismissAction: (() -> Unit)? = null, dismissText: String = TranslateString(id = R.string.no)
) {

    if (showDialog) {
        if(dismissAction != null) {
            AlertDialog(
                    onDismissRequest = {},
                    title = { Text(text = title, style = MaterialTheme.typography.h1) },
                    text = { Text(text = text, style = MaterialTheme.typography.body1) },
                    confirmButton = {
                        Button(
                                onClick = {
                                    confirmAction()
                                    setShowDialog(false)
                                },
                        ) { Text(confirmText, style = MaterialTheme.typography.body1) }
                    },
                    dismissButton = {
                        Button(
                                onClick = {
                                    dismissAction
                                    setShowDialog(false)
                                },
                        ) {
                            Text(dismissText, style = MaterialTheme.typography.body1)
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
                        },
                        ) { Text(confirmText, style = MaterialTheme.typography.body1) }
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