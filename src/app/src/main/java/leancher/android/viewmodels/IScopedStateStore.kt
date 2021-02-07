package leancher.android.viewmodels

interface IScopedStateStore {
    fun <TState> saveState(key: String, state: TState)
    fun <TState> loadState(key: String): TState?
}