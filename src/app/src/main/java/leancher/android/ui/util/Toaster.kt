package leancher.android.ui.util

import android.content.Context
import android.widget.Toast

fun Toast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, length)
}