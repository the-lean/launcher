package leancher.android.viewmodels

data class MainActivityViewModel(
    val homeViewModel: HomeViewModel,
    val feedViewModel: FeedViewModel,
    val notificationCenterViewModel: NotificationCenterViewModel
)