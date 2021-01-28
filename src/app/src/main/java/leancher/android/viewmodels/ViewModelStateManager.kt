package leancher.android.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class ViewModelStateManager {
    private val PREFERENCES_FILE_NAME = "com.Leancher"
    private val PREFERENCES_KEY = "mainActivityViewModel"

    private val context: Context
    private val sharedPreferences: SharedPreferences

    constructor(context: Context) {
        this.context = context
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, AppCompatActivity.MODE_PRIVATE);
    }

    fun persistViewState(viewModel: MainActivityViewModel) {
        val gson = Gson()
        val json = gson.toJson(viewModel)

        val editor = sharedPreferences.edit();
        editor.putString(PREFERENCES_KEY, json)
        editor.commit();
    }

    fun restoreViewState(): MainActivityViewModel? {
        val json = sharedPreferences.getString(PREFERENCES_KEY, "")
        if (!json!!.isEmpty()) {
            val gson = Gson()
            return gson.fromJson(json, MainActivityViewModel::class.java)
        } else {
            return null
        }
    }

}