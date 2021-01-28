package leancher.android.domain.services

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson


// https://gist.github.com/paulo-raca/471680c0fe4d8f91b8cde486039b0dcd
// https://www.javacodegeeks.com/2013/10/android-notificationlistenerservice-example.html
// https://github.com/Chagall/notification-listener-service-example
// https://github.com/hiteshsahu/Android-Notification-Demo

class NotificationService : NotificationListenerService() {

    private lateinit var gson: Gson
    private var commandFromUIReceiver: CommandFromUIReceiver? = null

    override fun onCreate() {
        super.onCreate()

        // Register broadcast from UI
        commandFromUIReceiver = CommandFromUIReceiver()
        val filter = IntentFilter()
        filter.addAction(READ_COMMAND_ACTION)
        registerReceiver(commandFromUIReceiver, filter)

        gson = Gson()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(commandFromUIReceiver)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        fetchCurrentNotifications()
    }

    override fun onNotificationPosted(newNotification: StatusBarNotification) {
        fetchCurrentNotifications()
    }

    override fun onNotificationRemoved(removedNotification: StatusBarNotification) {
        fetchCurrentNotifications()
    }

    private fun dismissNotification(intent: Intent) {
        if(intent.getIntExtra(RESULT_KEY, AppCompatActivity.RESULT_CANCELED) == Activity.RESULT_OK) {
            val resultValue = intent.getStringExtra(RESULT_VALUE)
            cancelNotification(resultValue)
        }
    }

    private fun fetchCurrentNotifications() {
        var notifications = mutableListOf<leancher.android.domain.models.Notification>()
        this@NotificationService.activeNotifications.forEach { statusBarNotification ->
            val extras: Bundle = statusBarNotification.notification.extras
            val notification = leancher.android.domain.models.Notification(
                key = statusBarNotification.key,
                packageName = statusBarNotification.packageName,
                title = extras[Notification.EXTRA_TITLE].toString(),
                text = extras[Notification.EXTRA_TEXT].toString(),
                icon = statusBarNotification.notification.smallIcon,
                originalNotification = statusBarNotification
            )
            notifications.add(notification)
        }

        sendResultOnUI(gson.toJson(notifications))
    }

    private fun sendResultOnUI(result: String?) {
        val resultIntent = Intent(UPDATE_UI_ACTION)
        resultIntent.putExtra(RESULT_KEY, Activity.RESULT_OK)
        resultIntent.putExtra(RESULT_VALUE, result)
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent)
    }

    internal inner class CommandFromUIReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra(COMMAND_KEY) == CLEAR_NOTIFICATIONS) {
                cancelAllNotifications()
            }
            else if (intent.getStringExtra(COMMAND_KEY) == GET_ACTIVE_NOTIFICATIONS) {
                fetchCurrentNotifications()
            }
            else if (intent.getStringExtra(COMMAND_KEY) == DISMISS_NOTIFICATION) {
                dismissNotification(intent = intent)
            }
        }
    }

    companion object {
        const val TAG = "NotificationListener"

        //Update UI action
        const val UPDATE_UI_ACTION =   "ACTION_UPDATE_UI"
        const val READ_COMMAND_ACTION = "ACTION_READ_COMMAND"


        // Bundle Key Value Pair
        const val RESULT_KEY = "readResultKey"
        const val RESULT_VALUE = "readResultValue"


        //Actions sent from UI
        const val COMMAND_KEY = "READ_COMMAND"
        const val CLEAR_NOTIFICATIONS = "clearall"
        const val GET_ACTIVE_NOTIFICATIONS = "list"
        const val DISMISS_NOTIFICATION = "dismiss"
    }
}
