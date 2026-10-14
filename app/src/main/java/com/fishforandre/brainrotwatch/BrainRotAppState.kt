package com.fishforandre.brainrotwatch

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppScreen {
    Auth,
    DeviceSetup,
    Home,
    ManageCommunity,
    Settings,
    CreateCommunity,
    Events,
}

class BrainRotWatchAppState(private val context: Context) {
    private val prefs = context.getSharedPreferences("brainrotwatch_prefs", Context.MODE_PRIVATE)

    val authRepository: com.fishforandre.brainrotwatch.data.AuthRepository =
        com.fishforandre.brainrotwatch.data.SupabaseAuthRepository(context)

    var isCheckingAuth by mutableStateOf(true)
    private val screenStack = mutableStateListOf<AppScreen>(AppScreen.Home)

    val currentScreen: AppScreen
        get() = screenStack.lastOrNull() ?: AppScreen.Home

    val canGoBack: Boolean
        get() = screenStack.size > 1
    var usageAccessGranted by mutableStateOf(DeviceUsageRepository.hasUsageAccess(context))
    var currentUser by mutableStateOf(DefaultBrainRotWatchData.emptyUserScreenTime)
    var selectedCommunity by mutableStateOf(DefaultBrainRotWatchData.emptyDashboard.community)
    var selectedCommunityName by mutableStateOf(prefs.getString("last_community", "Friends") ?: "Friends")
    var availableCommunities by mutableStateOf(listOf("Friends"))
    var unreadNotifications by mutableStateOf(0)
    var notificationPreferences by mutableStateOf(sampleCommunityNotificationPreferences)
    var displayName by mutableStateOf("")
    var avatarUrl by mutableStateOf<String?>(null)
    var maxScreenTimeMinutes by mutableStateOf("180")
    var pendingIncomingFriendRequest by mutableStateOf<IncomingFriendRequest?>(null)
    var pendingIncomingCommunityInvite by mutableStateOf<IncomingCommunityInvite?>(null)

    val addedFriends = mutableStateListOf<UserScreenTime>()
    val communityMembersMap = mutableMapOf<String, MutableList<UserScreenTime>>()

    fun getMembersForSelectedCommunity(): List<UserScreenTime> {
        val extraMembers = if (selectedCommunityName == "Friends") {
            addedFriends
        } else {
            communityMembersMap[selectedCommunityName].orEmpty()
        }
        return (listOf(currentUser) + extraMembers)
            .filter { it.displayName.isNotBlank() }
            .distinctBy { it.displayName }
            .sortedByDescending { it.totalMinutes }
    }

    fun handleDeepLink(uri: android.net.Uri?) {
        if (uri == null) return
        val path = uri.path ?: ""
        val isCommunityJoin = path.contains("join") || uri.getQueryParameter("community") != null || uri.getQueryParameter("community_id") != null

        if (isCommunityJoin) {
            val commName = uri.getQueryParameter("community") ?: uri.getQueryParameter("name") ?: "New Community"
            val commId = uri.getQueryParameter("community_id") ?: uri.getQueryParameter("id") ?: "comm_123"
            val invCode = uri.getQueryParameter("code") ?: "inv_123"
            val inviter = uri.getQueryParameter("inviter") ?: "A friend"

            pendingIncomingCommunityInvite = IncomingCommunityInvite(
                communityId = commId,
                communityName = commName,
                inviteCode = invCode,
                inviterName = inviter
            )
        } else {
            val friendCode = uri.getQueryParameter("code") ?: uri.getQueryParameter("friend_code")
            val reqName = uri.getQueryParameter("name") ?: uri.getQueryParameter("display_name") ?: "BrainRotWatch User"
            val reqAvatar = uri.getQueryParameter("avatar") ?: uri.getQueryParameter("avatar_url")

            if (!friendCode.isNullOrBlank()) {
                pendingIncomingFriendRequest = IncomingFriendRequest(
                    friendCode = friendCode,
                    displayName = reqName,
                    avatarUrl = reqAvatar
                )
            }
        }
    }

    fun dismissIncomingFriendRequest() {
        pendingIncomingFriendRequest = null
    }

    fun dismissIncomingCommunityInvite() {
        pendingIncomingCommunityInvite = null
    }

    fun joinCommunity(communityName: String) {
        val trimmed = communityName.trim()
        if (trimmed.isNotBlank() && !availableCommunities.contains(trimmed)) {
            availableCommunities = availableCommunities + trimmed
        }
        selectCommunity(if (trimmed.isNotBlank()) trimmed else communityName)
        dismissIncomingCommunityInvite()
    }

    fun leaveCommunity(communityName: String) {
        availableCommunities = availableCommunities.filter { !it.equals(communityName, ignoreCase = true) }
        selectCommunity("Friends")
        navigateTo(AppScreen.Home)
    }

    fun disbandCommunity(communityName: String) {
        availableCommunities = availableCommunities.filter { !it.equals(communityName, ignoreCase = true) }
        selectCommunity("Friends")
        navigateTo(AppScreen.Home)
    }

    suspend fun checkAuthAndRoute() {
        try {
            (authRepository as? com.fishforandre.brainrotwatch.data.SupabaseAuthRepository)?.initialize()
            when (val state = authRepository.authState.value) {
                is com.fishforandre.brainrotwatch.data.AuthState.SignedIn -> {
                    handleSignedIn(state.user)
                }
                else -> {
                    navigateTo(AppScreen.Auth)
                }
            }
        } finally {
            isCheckingAuth = false
        }
    }

    fun refreshUsageAccess() {
        usageAccessGranted = DeviceUsageRepository.hasUsageAccess(context)
    }

    fun handleSignedIn(profile: UserProfile) {
        displayName = profile.displayName
        avatarUrl = profile.avatarUrl
        val initials = if (profile.displayName.isNotBlank()) profile.displayName.take(1).uppercase() else "J"
        currentUser = currentUser.copy(
            displayName = profile.displayName,
            initials = initials,
            avatarUrl = profile.avatarUrl,
            deviceCount = profile.deviceCount
        )
        navigateTo(AppScreen.Home)
    }

    fun signOut() {
        navigateTo(AppScreen.Auth)
    }

    suspend fun deleteAccount() {
        authRepository.deleteAccount()
        navigateTo(AppScreen.Auth)
    }

    fun selectCommunity(name: String) {
        selectedCommunityName = name
        prefs.edit().putString("last_community", name).apply()
        selectedCommunity = selectedCommunity.copy(name = name)
    }

    fun createCommunity(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !availableCommunities.contains(trimmed)) {
            availableCommunities = availableCommunities + trimmed
        }
        selectCommunity(if (trimmed.isNotBlank()) trimmed else name)
        navigateTo(AppScreen.Home)
    }

    fun refreshCurrentUserScreenTime() {
        currentUser = DeviceUsageRepository.loadTodayUserScreenTime(
            context = context,
            displayName = displayName,
            initials = currentUser.initials,
            fallbackSummary = currentUser.summary,
        )
    }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.Home) {
            screenStack.clear()
            screenStack.add(AppScreen.Home)
        } else if (screen == AppScreen.Auth) {
            screenStack.clear()
            screenStack.add(AppScreen.Auth)
        } else {
            if (currentScreen != screen) {
                screenStack.add(screen)
            }
        }
    }

    fun popBack(): Boolean {
        if (canGoBack) {
            screenStack.removeAt(screenStack.lastIndex)
            return true
        }
        return false
    }

    fun updateDisplayName(newName: String) {
        displayName = newName
        currentUser = currentUser.copy(displayName = newName)
    }

    fun updateUserProfile(newName: String, newAvatarUrl: String?) {
        displayName = newName
        avatarUrl = newAvatarUrl
        val initials = if (newName.isNotBlank()) newName.take(1).uppercase() else "J"
        currentUser = currentUser.copy(
            displayName = newName,
            initials = initials,
            avatarUrl = newAvatarUrl
        )
    }

    fun updateMaxScreenTime(minutes: String) {
        maxScreenTimeMinutes = minutes
    }
}
