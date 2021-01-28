package leancher.android

import android.Manifest
import android.app.NotificationManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.ComponentName
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.setContent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import leancher.android.domain.models.Notification
import leancher.android.domain.services.NotificationService.Companion.CLEAR_NOTIFICATIONS
import leancher.android.domain.services.NotificationService.Companion.COMMAND_KEY
import leancher.android.domain.services.NotificationService.Companion.DISMISS_NOTIFICATION
import leancher.android.domain.services.NotificationService.Companion.GET_ACTIVE_NOTIFICATIONS
import leancher.android.domain.services.NotificationService.Companion.READ_COMMAND_ACTION
import leancher.android.domain.services.NotificationService.Companion.RESULT_KEY
import leancher.android.domain.services.NotificationService.Companion.RESULT_VALUE
import leancher.android.domain.services.NotificationService.Companion.UPDATE_UI_ACTION
import leancher.android.ui.layouts.Pager
import leancher.android.ui.pages.Feed
import leancher.android.ui.pages.Home
import leancher.android.ui.pages.NotificationCenter
import leancher.android.ui.theme.LeancherTheme
import leancher.android.viewmodels.*
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {
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
                            { Feed(feedVM) },
                            { Home(homeVM) },
                            { NotificationCenter(notificationsVM) }
                        )
                    )
                }
            )
        }

        throw Exception("typically, a nice video is better than a crappy demo")
    }

    private val homeModel = HomeModel(ScopedStateStore("home"))

    private val feedVM by viewModels<FeedViewModel>() {
        ViewModelFactory(FeedViewModel::class.java) {
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
        ViewModelFactory(HomeViewModel::class.java) {
            HomeViewModel(
                model = homeModel,
                inputRenderers = mapOf(
                    "AppList" to { setResult -> ApplicationList(setResult) } // TODO: convert to reference, once possible
                ),
                outputRenderers = mapOf(),
                HomeViewModel.Actions(
                    executeIntent = ::startActivity,
                    isIntentCallable = ::isIntentCallable
                )
            )
        }
    }
    private val notificationsVM by viewModels<NotificationCenterViewModel> {
        ViewModelFactory(NotificationCenterViewModel::class.java) {
            NotificationCenterViewModel(
                NotificationCenterViewModel.Actions(
                    clearNotifications = ::clearNotifications,
                    showStatusBar = ::showStatusBar,
                    hideStatusBar = ::hideStatusBar,
                    dismissNotification = ::dismissNotification
                )
            )
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

    private fun test(){
//        val editText = EditText(applicationContext)
//        editText.focusable = View.FOCUSABLE
//
//        editText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: Editable?) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                println(editText.text)
//            }
//        })
//
//        val inputMethodManager: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)


    }

    private fun getApplicationsList(): List<LauncherActivityInfo> {
        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        return launcherApps.getActivityList(null, Process.myUserHandle())
    }

    @Composable
    fun ApplicationList(setResult: (Any) -> Unit) {
        for (info in getApplicationsList()) {
            Text(text = info.label as String, style = MaterialTheme.typography.body1)
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
        if (allNames != null && allNames?.isNotEmpty()) {
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
        // val hostView = appWidgetHost.createView(this, appWidgetId, appWidgetInfo)
        // hostView.setAppWidget(appWidgetId, appWidgetInfo)

        feedVM.addWidget(Widget(appWidgetId, appWidgetInfo))
    }

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).setInterruptionFilter(
            NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }

    private fun showStatusBar() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).setInterruptionFilter(
            NotificationManager.INTERRUPTION_FILTER_NONE
        )
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