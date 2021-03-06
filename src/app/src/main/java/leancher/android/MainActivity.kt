package leancher.android

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.ComponentName
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import leancher.android.domain.models.Notification
import leancher.android.domain.models.Widget
import leancher.android.domain.services.NotificationService.Companion.CLEAR_NOTIFICATIONS
import leancher.android.domain.services.NotificationService.Companion.COMMAND_KEY
import leancher.android.domain.services.NotificationService.Companion.DISMISS_NOTIFICATION
import leancher.android.domain.services.NotificationService.Companion.GET_ACTIVE_NOTIFICATIONS
import leancher.android.domain.services.NotificationService.Companion.READ_COMMAND_ACTION
import leancher.android.domain.services.NotificationService.Companion.RESULT_KEY
import leancher.android.domain.services.NotificationService.Companion.RESULT_VALUE
import leancher.android.domain.services.NotificationService.Companion.UPDATE_UI_ACTION
import leancher.android.ui.layouts.Page
import leancher.android.ui.layouts.Pager
import leancher.android.ui.pages.Feed
import leancher.android.ui.pages.Home
import leancher.android.ui.pages.NotificationCenter
import leancher.android.ui.theme.LeancherTheme
import leancher.android.viewmodels.*
import java.lang.reflect.Type
import java.util.*

class MainActivity : ComponentActivity() {
    private val ACTION_NOTIFICATION_LISTENER_SETTINGS =
        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"

    private val APPWIDGET_HOST_ID = 1024
    private val REQUEST_CREATE_APPWIDGET = 5
    private val REQUEST_PICK_APPWIDGET = 9

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var viewModelStateManager: ViewModelStateManager

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("com.Leancher", MODE_PRIVATE)

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)
        appWidgetHost.startListening()

        requestLeancherPermissions()

        viewModelStateManager = ViewModelStateManager(this)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)

        setContent {
            LeancherTheme(
                content = {
                    Pager(
                        pages = listOf(
                            Page("YOUR DAY ...",  { Feed(feedVM) }),
                            Page("I WANNA ...",  { Home(homeVM) }),
                            Page("YOUR NOTIFS ...",  { NotificationCenter(notificationsVM) })
                        )
                    )
                }
            )
        }

        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }

//    private val homeModel = HomeModel(ScopedStateStore("home"))

    private val feedVM by viewModels<FeedViewModel> {
        ViewModelFactory {
            FeedViewModel(
                widgets = mutableListOf(),
                FeedViewModel.Actions(
                    onSelectWidget = ::selectWidget,
                    createWidgetHostView = ::createWidgetHostView
                )
            )
        }
    }
    private val homeVM by viewModels<HomeViewModel> {
        ViewModelFactory {
            HomeViewModel(
                inputRenderers = mapOf(
                    "AppList" to { setResult -> ApplicationList(setResult) } // TODO: convert to reference, once possible
                ),
                outputRenderers = mapOf(),
                HomeViewModel.Actions(
                    executeIntent = ::startActivity,
                    executeIntentForResult = ::startForResult,
                    isIntentCallable = ::isIntentCallable
                )
            )
        }
    }
    private val notificationsVM by viewModels<NotificationCenterViewModel> {
        ViewModelFactory {
            NotificationCenterViewModel(
                NotificationCenterViewModel.Actions(
                    clearNotifications = ::clearNotifications,
                    dismissNotification = ::dismissNotification,
                    showStatusBar = ::showStatusBar,
                    hideStatusBar = ::hideStatusBar,
                    getNextAlarm = ::getNextAlarm
                )
            )
        }
    }

    private fun startForResult(intent: Intent, setResult: (Intent) -> Unit) =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null)
                setResult(result.data!!)
            else
                TODO("handle failure case")
        }.launch(intent)

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showStatusBar() {
        window.insetsController?.let { controller ->
            controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideStatusBar() {
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }

    private fun createWidgetHostView(widget: Widget) =
        appWidgetHost.createView(applicationContext, widget.id, widget.providerInfo)

    private val clientLooperReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val resultCode = intent.getIntExtra(RESULT_KEY, RESULT_CANCELED)
            if (resultCode == RESULT_OK) {
                val resultValue = intent.getStringExtra(RESULT_VALUE)
                val gson = Gson()
                val type: Type = object : TypeToken<MutableList<Notification>>() {}.type

                val notifications: List<Notification> = gson.fromJson(resultValue, type) as List<Notification>
                notificationsVM.notifications = notifications
//                println("Notifications Count: ${notifications.size}")
//                notifications.forEach { n -> println("${n.key}, ${n.packageName}, ${n.title}, ${n.text}") }
            }
        }
    }

    private fun isIntentCallable(intent: Intent) =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty() // TODO: check whether this flag is needed

    private fun getNextAlarm(): Optional<Date> =
        Optional
            .ofNullable((getSystemService(ALARM_SERVICE) as AlarmManager).nextAlarmClock)
            .map { alarm -> Date(alarm.triggerTime) }

    private fun getApplicationsList(): List<LauncherActivityInfo> {
        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        return launcherApps.getActivityList(null, Process.myUserHandle())
    }

    @Composable
    fun ApplicationList(setResult: (Any) -> Unit) =
        Column(Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())) {
            for (info in getApplicationsList()) {
                val intent = packageManager.getLaunchIntentForPackage(info.applicationInfo.packageName)!!
                Text(
                    modifier = Modifier.clickable(onClick = { setResult(intent) }),
                    text = info.label as String,
                    style = MaterialTheme.typography.body1
                )
            }
        }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onResume() {
        super.onResume()

        if (sharedPreferences.getBoolean("firstRun", true)) {
            editor = sharedPreferences.edit()
            with(editor) {
                putBoolean("firstRun", false)
                apply()
            }

            requestLeancherPermissions()
        }

        //Register to Broadcast for Updating UI
        LocalBroadcastManager.getInstance(this).registerReceiver(
            clientLooperReceiver,
            IntentFilter(UPDATE_UI_ACTION)
        )

        readNotifications()
    }

    override fun onPause() {
        super.onPause()
//        TODO: viewModelStateManager.persistViewState(mainActivityViewModel)
    }

    override fun onStop() {
        super.onStop()
        // TODO: fix bug -> should stop listening on widget changes while app is not in foreground
        // but call throws null pointer when attempting to read from field ->
        // 'com.android.server.appwidget.AppWidgetServiceImpl$ProviderId com.android.server.appwidget.AppWidgetServiceImpl$Provider.id'
        // appWidgetHost.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()

        //Unregister to Broadcast for Updating UI
        LocalBroadcastManager.getInstance(this).unregisterReceiver(clientLooperReceiver)
    }

    private fun requestLeancherPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY), 1)
        }
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun readNotifications() =
        sendBroadcast(Intent(READ_COMMAND_ACTION).apply {
            putExtra(
                COMMAND_KEY,
                GET_ACTIVE_NOTIFICATIONS
            )
        })

    private fun clearNotifications() =
        sendBroadcast(Intent(READ_COMMAND_ACTION).apply {
            putExtra(
                COMMAND_KEY,
                CLEAR_NOTIFICATIONS
            )
        })

    fun dismissNotification(notification: Notification) =
        sendBroadcast(Intent(READ_COMMAND_ACTION).apply {
            putExtra(COMMAND_KEY, DISMISS_NOTIFICATION)
            putExtra(RESULT_KEY, RESULT_OK)
            putExtra(RESULT_VALUE, notification.key)
        })

    private fun isNotificationServiceEnabled(): Boolean {
        val allNames = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (allNames?.isNotEmpty() == true) {
            for (name in allNames.split(":").toTypedArray()) {
                if (packageName == ComponentName.unflattenFromString(name)!!.packageName) {
                    return true
                }
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_APPWIDGET -> configureWidget(data)
                REQUEST_CREATE_APPWIDGET -> data?.run { createWidget(data) }
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    private fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    private fun configureWidget(data: Intent?) {
        val extras = data!!.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (appWidgetInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
        } else {
            createWidget(data)
        }
    }

    private fun createWidget(data: Intent) {
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

        feedVM.addWidget(Widget(appWidgetId, appWidgetInfo))
    }
}

class ScopedStateStore(val scope: String) : IScopedStateStore {
    override fun <TState> saveState(key: String, state: TState) {
        println("storing at $key")
//        TODO("Not yet implemented")
    }

    override fun <TState> loadState(key: String): TState? {
        return null
//        TODO("Not yet implemented")
    }
}

/*

String action !including Namespace // https://developer.android.com/reference/android/content/Intent#Intent(java.lang.String)
Uri data | setDataAndNormalize // https://developer.android.com/reference/android/content/Intent#setData(android.net.Uri)
String type | setTypeAndNormalize // https://developer.android.com/reference/android/content/Intent#setTypeAndNormalize(java.lang.String)

Boolean showChooser // https://developer.android.com/training/basics/intents/sending#AppChooser
String? chooserTitle


 */