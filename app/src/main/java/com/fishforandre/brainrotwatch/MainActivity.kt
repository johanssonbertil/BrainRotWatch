package com.fishforandre.brainrotwatch

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.fishforandre.brainrotwatch.data.SupabaseFriendRepository
import com.fishforandre.brainrotwatch.data.supabase
import com.fishforandre.brainrotwatch.domain.engine.InterventionType
import com.fishforandre.brainrotwatch.domain.engine.ShapedBreakdownDrawerState
import com.fishforandre.brainrotwatch.domain.engine.TrendDirection
import com.fishforandre.brainrotwatch.domain.engine.WeeklyGroupPactSummaryResult
import com.fishforandre.brainrotwatch.domain.model.CommunityLimitMode
import com.fishforandre.brainrotwatch.domain.model.CommunitySettings
import com.fishforandre.brainrotwatch.domain.model.EnabledBadgesConfig
import com.fishforandre.brainrotwatch.domain.model.MasterCatalog
import com.fishforandre.brainrotwatch.domain.model.MediaTrackingMode
import com.fishforandre.brainrotwatch.domain.model.SyncIntegrityState
import com.fishforandre.brainrotwatch.notifications.NotificationHelper
import com.fishforandre.brainrotwatch.ui.qr.AddFriendBottomSheet
import com.fishforandre.brainrotwatch.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var appStateRef: BrainRotWatchAppState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        setContent {
            BrainRotWatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrainRotWatchEntry(
                        splashScreen = splashScreen,
                        onAppStateCreated = { appState ->
                            appStateRef = appState
                            handleIncomingActionIntent(intent, appState)
                        }
                    )
                }
            }
        }

        intent?.data?.let { uri ->
            appStateRef?.handleDeepLink(uri)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            appStateRef?.handleDeepLink(uri)
        }
        appStateRef?.let { handleIncomingActionIntent(intent, it) }
    }

    private fun handleIncomingActionIntent(intent: Intent?, appState: BrainRotWatchAppState) {
        if (intent?.action == NotificationHelper.ACTION_TACTICAL_NUDGE) {
            val targetName = intent.getStringExtra(NotificationHelper.EXTRA_TARGET_USER_NAME) ?: "Friend"
            appState.activeTacticalNudgeOverlay = TacticalNudgePayload(
                senderName = "Accountability Squad",
                targetName = targetName,
                title = "Caught in 4K 📸",
                subtitle = "Put the phone down and go touch grass right now!",
                emoji = "🌿"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrainRotWatchEntry(
    splashScreen: SplashScreen,
    onAppStateCreated: (BrainRotWatchAppState) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val appState = remember { BrainRotWatchAppState(context) }

    splashScreen.setKeepOnScreenCondition {
        appState.isCheckingAuth
    }

    BackHandler(enabled = appState.canGoBack) {
        appState.popBack()
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                appState.refreshUsageAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        onAppStateCreated(appState)
        try {
            appState.checkAuthAndRoute()
            val response = supabase.from("communities").select()
            android.util.Log.d("SupabaseTest", "SUCCESS: Connected to Supabase! Response: ${response.data}")

            com.fishforandre.brainrotwatch.data.ScreenTimeSyncWorker.schedule(context)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseTest", "ERROR: Connection failed - ${e.message}", e)
        }
    }

    if (!appState.isCheckingAuth) {
        if (appState.currentScreen == AppScreen.Auth || appState.currentScreen == AppScreen.DeviceSetup) {
            when (appState.currentScreen) {
                AppScreen.Auth -> AuthScreen(
                    authRepository = appState.authRepository,
                    onSignedIn = appState::handleSignedIn,
                    onNavigateToDeviceSetup = { appState.navigateTo(AppScreen.DeviceSetup) }
                )

                AppScreen.DeviceSetup -> DeviceSetupScreen(
                    authRepository = appState.authRepository,
                    onBack = { appState.navigateTo(AppScreen.Auth) },
                    onSignedIn = appState::handleSignedIn
                )

                else -> AuthScreen(
                    authRepository = appState.authRepository,
                    onSignedIn = appState::handleSignedIn
                )
            }
        } else if (appState.usageAccessGranted) {
            when (appState.currentScreen) {
                AppScreen.Home -> HomeScreen(
                    appState = appState,
                    onOpenSettings = { appState.navigateTo(AppScreen.Settings) },
                    onOpenEvents = { appState.navigateTo(AppScreen.Events) }
                )

                AppScreen.CommunityDetail -> CommunityLeaderboardScreen(
                    appState = appState,
                    onBack = { appState.navigateTo(AppScreen.Home) },
                    onOpenManageCommunity = { appState.navigateTo(AppScreen.ManageCommunity) }
                )

                AppScreen.AllCommunities -> {
                    var showJoinOrCreateSheet by remember { mutableStateOf(false) }
                    AllCommunitiesScreen(
                        bentoPreviews = appState.getCommunityBentoPreviews(),
                        onSelectCommunity = { commName ->
                            appState.openCommunityDetail(commName)
                        },
                        onOpenJoinOrCreateSheet = { showJoinOrCreateSheet = true },
                        onBack = { appState.navigateTo(AppScreen.Home) }
                    )
                    if (showJoinOrCreateSheet) {
                        CommunityActionBottomSheet(
                            onDismissRequest = { showJoinOrCreateSheet = false },
                            onJoinWithCode = { codeOrName ->
                                showJoinOrCreateSheet = false
                                val resolvedName = if (codeOrName.length <= 6) "Squad ($codeOrName)" else codeOrName
                                appState.requestJoinCommunityWithConsent(resolvedName)
                            },
                            onCreateNewCommunity = {
                                showJoinOrCreateSheet = false
                                appState.navigateTo(AppScreen.CreateCommunity)
                            }
                        )
                    }
                }

                AppScreen.ManageCommunity -> CommunityManageScreen(
                    communityName = appState.selectedCommunityName,
                    isAdmin = true,
                    communitySettings = appState.getActiveCommunitySettings(appState.selectedCommunityName),
                    notifications = appState.notificationPreferences,
                    onBack = { appState.navigateTo(AppScreen.CommunityDetail) },
                    onInvitePeople = { },
                    onUpdateCommunitySettings = { updated ->
                        appState.updateCommunitySettings(appState.selectedCommunityName, updated)
                    },
                    onDailySummaryChange = { enabled ->
                        appState.notificationPreferences = appState.notificationPreferences.copy(
                            dailySummaryEnabled = enabled
                        )
                    },
                    onThresholdAlertsChange = { enabled ->
                        appState.notificationPreferences = appState.notificationPreferences.copy(
                            thresholdAlertsEnabled = enabled
                        )
                    },
                    onApprovalRequiredChange = { enabled ->
                        appState.notificationPreferences = appState.notificationPreferences.copy(
                            approvalRequiredEnabled = enabled
                        )
                    },
                    onLeaveCommunity = {
                        appState.leaveCommunity(appState.selectedCommunityName)
                    },
                    onDisbandCommunity = {
                        appState.disbandCommunity(appState.selectedCommunityName)
                    }
                )

                AppScreen.Settings -> UserSettingsScreen(
                    initialName = appState.displayName,
                    initialAvatarUrl = appState.avatarUrl ?: appState.currentUser.avatarUrl,
                    initialMaxScreenTimeMinutes = appState.maxScreenTimeMinutes,
                    personalSettings = appState.personalSettings,
                    friendsList = appState.addedFriends,
                    authRepository = appState.authRepository,
                    onBack = { appState.navigateTo(AppScreen.Home) },
                    onNameChange = appState::updateDisplayName,
                    onSaveProfile = { newName, newAvatarUrl ->
                        appState.updateUserProfile(newName, newAvatarUrl)
                    },
                    onMaxScreenTimeChange = appState::updateMaxScreenTime,
                    onUpdatePersonalSettings = { appState.personalSettings = it },
                    onToggleAccountabilityBuddy = appState::toggleAccountabilityBuddy,
                    onOpenInventoryAndLoadout = { appState.navigateTo(AppScreen.InventoryAndLoadout) },
                    onDeleteAccount = { appState.deleteAccount() },
                )

                AppScreen.InventoryAndLoadout -> InventoryAndLoadoutScreen(
                    inventory = appState.userInventory,
                    availableCommunities = appState.availableCommunities,
                    onBack = { appState.navigateTo(AppScreen.Settings) },
                    onAssignPatronTheme = appState::assignPatronThemeToCommunity,
                    onUnequipPatronTheme = appState::unequipPatronTheme,
                    onUpdateGlobalLoadout = appState::updateGlobalCardLoadout,
                    onUpdateCommunityOverride = appState::updateCommunityCardOverride
                )

                AppScreen.CreateCommunity -> CreateCommunityScreen(
                    onBack = { appState.navigateTo(AppScreen.Home) },
                    onCreateCommunity = { newCommunityName ->
                        appState.createCommunity(newCommunityName)
                    },
                    onCreateConfiguredCommunity = { newCommunityName, settings ->
                        appState.createCommunity(newCommunityName, settings)
                    }
                )

                AppScreen.Events -> EventsScreen(
                    unreadCount = appState.unreadNotifications,
                    activityFeed = appState.activityFeed,
                    lastInterventionBanner = appState.lastInterventionResultBanner,
                    onTriggerTacticalNudge = { target ->
                        appState.triggerTacticalNudge(target)
                    },
                    onTriggerIntervention = { target, type, action ->
                        appState.triggerInterventionAction(target, type, action)
                    },
                    onBack = { appState.navigateTo(AppScreen.Home) }
                )

                else -> HomeScreen(
                    appState = appState,
                    onOpenSettings = { appState.navigateTo(AppScreen.Settings) },
                    onOpenEvents = { appState.navigateTo(AppScreen.Events) }
                )
            }
        } else {
            UsageAccessLockScreen(
                onPermitUsageAccess = {
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            )
        }
    }

    // Global In-App Tactical Nudge Poke Overlay ("Caught in 4K / Go Touch Grass")
    appState.activeTacticalNudgeOverlay?.let { nudge ->
        TacticalNudgeOverlayDialog(
            payload = nudge,
            onDismiss = { appState.activeTacticalNudgeOverlay = null }
        )
    }

    // Global Community Entry Consent Modal when joining a Community whose rules override personal notification/privacy settings
    val pendingConsent = appState.pendingCommunityEntryConsent
    if (pendingConsent != null) {
        val context = LocalContext.current
        CommunityEntryConsentModal(
            communityName = pendingConsent.first,
            settings = pendingConsent.second,
            onConfirmJoin = {
                val joinedName = pendingConsent.first
                appState.confirmCommunityEntryConsent()
                android.widget.Toast.makeText(context, "Accepted rules & joined $joinedName!", android.widget.Toast.LENGTH_LONG).show()
            },
            onCancel = {
                appState.dismissCommunityEntryConsent()
            }
        )
    }
}

/**
 * Unified, Scrollable Dashboard (HomeScreen) using LazyColumn:
 * 1. Minimal Collapsible Top App Bar (Logo icon scrolls LazyColumn back to index 0; Bell & Profile on right; pushed away on scroll down & dragged back on scroll up).
 * 2. Section A: "Last Visited Communities" 2x2 Asymmetric Bento Container (Wide 60% / Compact 40% in Row 1, Compact 40% / Wide 60% in Row 2, dashed "+ Join or Create" slot, and "+ / Show all" footer).
 * 3. Section B: Friends Leaderboard Header & Sort Filter ("Friends", "+ Add" button, and "Best v" dropdown).
 * 4. Section C: Inline User + Friends List (UserCard items) + dashed "Invite a Friend +" card when < 3 friends.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appState: BrainRotWatchAppState,
    onOpenSettings: () -> Unit,
    onOpenEvents: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val topBarHeight = 56.dp
    val topBarHeightPx = with(LocalDensity.current) { topBarHeight.toPx() }
    var topBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(topBarHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (topBarOffsetHeightPx + delta).coerceIn(-topBarHeightPx, 0f)
                topBarOffsetHeightPx = newOffset
                // Return Offset.Zero so LazyColumn scrolls simultaneously at 1:1 speed with finger
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (topBarOffsetHeightPx < 0f && topBarOffsetHeightPx > -topBarHeightPx) {
                    val target = if (topBarOffsetHeightPx > -topBarHeightPx / 2f) 0f else -topBarHeightPx
                    Animatable(topBarOffsetHeightPx).animateTo(
                        targetValue = target,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) {
                        topBarOffsetHeightPx = value
                    }
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
            topBarOffsetHeightPx = 0f
        }
    }

    var showAddFriendSheet by remember { mutableStateOf(false) }
    var showCommunityActionSheet by remember { mutableStateOf(false) }

    val userScreenTime by produceState(
        initialValue = appState.currentUser,
        key1 = appState.displayName,
        key2 = appState.avatarUrl
    ) {
        val loaded = DeviceUsageRepository.loadTodayUserScreenTime(
            context = context,
            displayName = appState.displayName,
            initials = if (appState.displayName.isNotBlank()) appState.displayName.take(1).uppercase() else "J",
            fallbackSummary = appState.currentUser.summary,
            avatarUrl = appState.avatarUrl ?: appState.currentUser.avatarUrl,
            friendCode = appState.currentUser.friendCode,
            deviceCount = appState.currentUser.deviceCount
        )
        value = loaded
        appState.currentUser = loaded
    }

    val dashboard = DefaultBrainRotWatchData.emptyDashboard.copy(
        currentUser = userScreenTime,
        unreadNotifications = appState.unreadNotifications
    )

    val bentoPreviews = remember(
        appState.availableCommunities,
        appState.communitySettingsMap.toMap(),
        dashboard.currentUser,
        appState.addedFriends.toList()
    ) {
        appState.getCommunityBentoPreviews()
    }

    val sortedFriendsAndUser = remember(
        appState.activeFriendsSort,
        dashboard.currentUser,
        appState.addedFriends.toList(),
        appState.personalSettings,
        appState.userInventory
    ) {
        appState.getSortedFriendsAndUserLeaderboard()
    }

    // Handle Pending Incoming Friend Request from Deep Link / QR Code
    val pendingFriendRequest = appState.pendingIncomingFriendRequest
    if (pendingFriendRequest != null) {
        FriendRequestDialog(
            request = pendingFriendRequest,
            onAccept = {
                val reqName = pendingFriendRequest.displayName
                appState.addFriendByUsernameOrCode(reqName)
                appState.dismissIncomingFriendRequest()
                android.widget.Toast.makeText(context, "Added $reqName as a friend!", android.widget.Toast.LENGTH_LONG).show()
            },
            onDismiss = {
                appState.dismissIncomingFriendRequest()
            }
        )
    }

    // Handle Pending Incoming Community Invite
    val pendingCommunityInvite = appState.pendingIncomingCommunityInvite
    if (pendingCommunityInvite != null) {
        CommunityInviteDialog(
            invite = pendingCommunityInvite,
            onJoin = {
                val name = pendingCommunityInvite.communityName
                val settings = pendingCommunityInvite.communitySettings
                appState.dismissIncomingCommunityInvite()
                appState.requestJoinCommunityWithConsent(name, settings)
            },
            onDismiss = {
                appState.dismissIncomingCommunityInvite()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
            .nestedScroll(nestedScrollConnection)
    ) {
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val topContentPadding = statusBarTop + topBarHeight + 8.dp

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = topContentPadding, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Optional Life-Line Intervention Result Banner
            appState.lastInterventionResultBanner?.let { bannerMsg ->
                item(key = "intervention_banner") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { appState.lastInterventionResultBanner = null },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF6EA))
                    ) {
                        Text(
                            text = bannerMsg,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            // SECTION A: Communities Asymmetric Bento Container (expandable downwards)
            item(key = "bento_communities_container") {
                LastVisitedCommunitiesBentoContainer(
                    bentoPreviews = bentoPreviews,
                    onOpenCommunity = { communityName ->
                        appState.openCommunityDetail(communityName)
                    },
                    onOpenJoinOrCreateSheet = {
                        showCommunityActionSheet = true
                    }
                )
            }

            // SECTION B: Friends Leaderboard Header & Sort Filter
            item(key = "friends_leaderboard_header") {
                FriendsLeaderboardHeaderRow(
                    selectedSort = appState.activeFriendsSort,
                    onSelectSort = { appState.activeFriendsSort = it },
                    onOpenAddFriendSheet = { showAddFriendSheet = true }
                )
            }

            // SECTION C: Inline User + Friends Leaderboard Cards
            itemsIndexed(
                items = sortedFriendsAndUser,
                key = { _, member -> "friend_row_${member.userId}_${member.displayName}" }
            ) { index, member ->
                val isSelf = member.userId == "current_user" ||
                    member.displayName.equals(appState.displayName, ignoreCase = true)

                UserScrollCard(
                    user = member,
                    rankNumber = index + 1,
                    enabledBadges = EnabledBadgesConfig(),
                    isSelf = isSelf,
                    onTacticalNudge = {
                        appState.triggerTacticalNudge(member)
                    },
                    onCallLifeLine = {
                        appState.triggerInterventionAction(member, InterventionType.PHONE_CALL, "DIAL")
                    }
                )
            }

            // Onboarding Empty/Low State: If user has fewer than 3 friends, append dashed "Invite a Friend +" card
            if (appState.addedFriends.size < 3) {
                item(key = "invite_friend_dashed_onboarding") {
                    DashedInviteFriendCard(
                        onClick = { showAddFriendSheet = true }
                    )
                }
            }
        }

        // Minimal Collapsible Top App Bar with solid background (translates 1:1 with finger scroll)
        Column(modifier = Modifier.fillMaxWidth()) {
            // Solid background behind the system status bar so scrolling content never peeks through
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(MaterialTheme.colorScheme.background)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .clipToBounds()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = topBarOffsetHeightPx
                        }
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(topBarHeight)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Minimal Left Logo Icon ONLY (tapping scrolls LazyColumn back to index 0)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch {
                                            topBarOffsetHeightPx = 0f
                                            listState.animateScrollToItem(0)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.final_logo_icon_only),
                                    contentDescription = "BrainRotWatch Logo Icon (Scroll to Top)",
                                    modifier = Modifier.size(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Right Actions: Notification Bell + Avatar
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NotificationBadge(
                                    count = dashboard.unreadNotifications,
                                    onClick = onOpenEvents
                                )

                                AvatarCircle(
                                    initials = dashboard.currentUser.initials.ifBlank { "J" },
                                    avatarUrl = dashboard.currentUser.avatarUrl,
                                    onClick = onOpenSettings
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dedicated Community Action Bottom Sheet (Join via Code or Create New Community)
    if (showCommunityActionSheet) {
        CommunityActionBottomSheet(
            onDismissRequest = { showCommunityActionSheet = false },
            onJoinWithCode = { codeOrName ->
                showCommunityActionSheet = false
                val resolvedName = if (codeOrName.length <= 6) "Squad ($codeOrName)" else codeOrName
                appState.requestJoinCommunityWithConsent(resolvedName)
            },
            onCreateNewCommunity = {
                showCommunityActionSheet = false
                appState.navigateTo(AppScreen.CreateCommunity)
            }
        )
    }

    // Dedicated Add Friend Bottom Sheet (Search username, Share Invite Link, or Scan QR Code)
    if (showAddFriendSheet) {
        AddFriendBottomSheet(
            displayName = appState.displayName,
            friendCode = appState.currentUser.friendCode,
            avatarUrl = appState.avatarUrl ?: appState.currentUser.avatarUrl,
            title = "Add Friend",
            onDismissRequest = { showAddFriendSheet = false },
            onFriendTokenDetected = { token ->
                scope.launch {
                    val result = SupabaseFriendRepository().sendFriendRequestByCode(token)
                    if (result.isSuccess) {
                        appState.addFriendByUsernameOrCode(token)
                        android.widget.Toast.makeText(context, "Added $token!", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        appState.addFriendByUsernameOrCode(token)
                        android.widget.Toast.makeText(context, "Added $token to Friends!", android.widget.Toast.LENGTH_LONG).show()
                    }
                    showAddFriendSheet = false
                }
            }
        )
    }
}

/**
 * Section A: Communities Bento Container
 * - Rounded container card at the top of the LazyColumn displaying communities
 *   in an asymmetric Bento grid (collapsed: 2x2; expanded: expands downwards to show all rows).
 * - Footer: `+` Add button on the far left; `Show all` with downwards arrow on the right to expand/collapse.
 */
@Composable
private fun LastVisitedCommunitiesBentoContainer(
    bentoPreviews: List<CommunityBentoPreview>,
    onOpenCommunity: (String) -> Unit,
    onOpenJoinOrCreateSheet: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val expandedRowCount = maxOf(3, (bentoPreviews.size + 2) / 2)
    val tileHeight = 82.dp
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bentoExpandArrowRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 12.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Base 2x2 Bento rows (always visible)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (rowIndex in 0 until 2) {
                    BentoGridRow(
                        rowIndex = rowIndex,
                        bentoPreviews = bentoPreviews,
                        tileHeight = tileHeight,
                        onOpenCommunity = onOpenCommunity,
                        onOpenJoinOrCreateSheet = onOpenJoinOrCreateSheet
                    )
                }
            }

            // Smoothly expanding/collapsing additional Bento rows
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 260, easing = LinearOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (rowIndex in 2 until expandedRowCount) {
                        BentoGridRow(
                            rowIndex = rowIndex,
                            bentoPreviews = bentoPreviews,
                            tileHeight = tileHeight,
                            onOpenCommunity = onOpenCommunity,
                            onOpenJoinOrCreateSheet = onOpenJoinOrCreateSheet
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: subtle `+` Add button on the far left, `Show all` with animated downwards arrow on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onOpenJoinOrCreateSheet),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Join or Create Community",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Show less" else "Show all",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse communities" else "Expand communities",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = arrowRotation }
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoGridRow(
    rowIndex: Int,
    bentoPreviews: List<CommunityBentoPreview>,
    tileHeight: androidx.compose.ui.unit.Dp,
    onOpenCommunity: (String) -> Unit,
    onOpenJoinOrCreateSheet: () -> Unit
) {
    val leftSlotIndex = rowIndex * 2
    val rightSlotIndex = rowIndex * 2 + 1
    val leftIsWide = rowIndex % 2 == 0
    val leftWeight = if (leftIsWide) 0.60f else 0.40f
    val rightWeight = if (leftIsWide) 0.40f else 0.60f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BentoSlotView(
            preview = bentoPreviews.getOrNull(leftSlotIndex),
            isWideSlot = leftIsWide,
            slotIndex = leftSlotIndex,
            modifier = Modifier
                .weight(leftWeight)
                .height(tileHeight),
            onOpenCommunity = onOpenCommunity,
            onOpenJoinOrCreate = onOpenJoinOrCreateSheet
        )
        BentoSlotView(
            preview = bentoPreviews.getOrNull(rightSlotIndex),
            isWideSlot = !leftIsWide,
            slotIndex = rightSlotIndex,
            modifier = Modifier
                .weight(rightWeight)
                .height(tileHeight),
            onOpenCommunity = onOpenCommunity,
            onOpenJoinOrCreate = onOpenJoinOrCreateSheet
        )
    }
}

/** Flat pastel tile colors, assigned per Bento slot so neighbouring tiles never share a color. */
private val BentoTilePalette = listOf(AccentLime, AccentPeach, AccentLavender, AccentMint)

@Composable
private fun BentoSlotView(
    preview: CommunityBentoPreview?,
    isWideSlot: Boolean,
    slotIndex: Int,
    modifier: Modifier = Modifier,
    onOpenCommunity: (String) -> Unit,
    onOpenJoinOrCreate: () -> Unit
) {
    val tileColor = BentoTilePalette[slotIndex % BentoTilePalette.size]
    if (preview == null) {
        // Dashed "+ Join or Create" tile in remaining Bento slot
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .dashedBorder(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                    cornerRadius = 20.dp
                )
                .clickable(onClick = onOpenJoinOrCreate)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Join or Create",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Join or create",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else if (isWideSlot) {
        WideBentoCommunityTile(
            preview = preview,
            tileColor = tileColor,
            modifier = modifier,
            onClick = { onOpenCommunity(preview.communityName) }
        )
    } else {
        CompactBentoCommunityTile(
            preview = preview,
            tileColor = tileColor,
            modifier = modifier,
            onClick = { onOpenCommunity(preview.communityName) }
        )
    }
}

/**
 * Wide Bento Tile (~60% width):
 * Compact card showing Community Name and mini Shared Health Bar (no abbreviation, member count, or theme label).
 */
@Composable
private fun WideBentoCommunityTile(
    preview: CommunityBentoPreview,
    tileColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val pact = preview.settings.limitMode as? CommunityLimitMode.WeeklyGroupPact
    val hpFraction = (pact?.healthFraction ?: 0.72f).coerceIn(0f, 1f)

    PastelTileSurface(
        tileColor = tileColor,
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = preview.communityName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Mini Shared Health Bar (ink on translucent track)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pact?.let { "${formatUsageTime(it.remainingMinutes)} left" }
                        ?: preview.leaderOrAnchorLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = NavyPrimary.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(hpFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyPrimary.copy(alpha = 0.75f)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(NavyPrimary.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(NavyPrimary)
                )
            }
        }
    }
}

/**
 * Compact Bento Tile (~40% width):
 * Compact card showing Community Name, user's current rank, and an alert indicator if a member breached their limit.
 */
@Composable
private fun CompactBentoCommunityTile(
    preview: CommunityBentoPreview,
    tileColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PastelTileSurface(
        tileColor = tileColor,
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = preview.communityName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${preview.userRank}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            if (preview.hasLimitBreachAlert) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(BadgeNotificationRed)
                    )
                    Text(
                        text = "Breach",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TrendRed
                    )
                }
            }
        }
    }
}

/**
 * Shared flat pastel tile shell used by both Wide and Compact Bento tiles.
 */
@Composable
private fun PastelTileSurface(
    tileColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(tileColor)
            .border(1.dp, NavyPrimary.copy(alpha = 0.06f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            content()
        }
    }
}

/**
 * Section B: Friends Leaderboard Header & Sort Filter
 * - Left: Bold `Friends` editorial title text.
 * - Right: Dark ink squircle `+` Add button alongside a crisp white outlined sort pill (`Best ↕`).
 */
@Composable
private fun FriendsLeaderboardHeaderRow(
    selectedSort: FriendsSortOption,
    onSelectSort: (FriendsSortOption) -> Unit,
    onOpenAddFriendSheet: () -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: "Friends" section title
        Text(
            text = "Friends",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Right: Dark squircle icon-only Add Button alongside Sort Dropdown (Best ↕)
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dark squircle icon-only + Add Friend button (matches sorting button height)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onOpenAddFriendSheet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Friend",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sort Dropdown Pill (Best ↕)
            Box(modifier = Modifier.fillMaxHeight()) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                        .clickable { sortMenuExpanded = true }
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedSort.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Sort Friends",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    FriendsSortOption.entries.forEach { option ->
                        val isCurrent = option == selectedSort
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = option.label,
                                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Medium
                                    )
                                    val subtitle = when (option) {
                                        FriendsSortOption.BEST -> "Lowest screen time today (Default)"
                                        FriendsSortOption.WORST_ROTTING -> "Highest screen time today"
                                        FriendsSortOption.MOST_IMPROVED -> "Steepest % drop vs. rolling average"
                                        FriendsSortOption.ACCOUNTABILITY_BUDDIES -> "Close friends only"
                                    }
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            trailingIcon = {
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                onSelectSort(option)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Onboarding Empty/Low State Card:
 * Displayed at the bottom of the Friends list when the user has fewer than 3 friends.
 */
@Composable
private fun DashedInviteFriendCard(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .dashedBorder(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                cornerRadius = 22.dp
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Invite a Friend",
                    tint = NavyPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Invite a friend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Dedicated Community Action Bottom Sheet:
 * Opened when tapping the dashed "+ Join or Create" Bento tile or the `+` button in the Bento footer.
 * Provides options to Join via Invite Code or Create a New Community.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityActionBottomSheet(
    onDismissRequest: () -> Unit,
    onJoinWithCode: (String) -> Unit,
    onCreateNewCommunity: () -> Unit
) {
    var inviteCodeInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Communities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Join an existing accountability squad with an invite code or launch a new Community with custom rules.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = inviteCodeInput,
                onValueChange = { inviteCodeInput = it },
                label = { Text("Enter Community Invite Code or Name") },
                placeholder = { Text("e.g. DETOX9 or Monk Mode Club") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (inviteCodeInput.isNotBlank()) {
                        onJoinWithCode(inviteCodeInput.trim())
                    }
                },
                enabled = inviteCodeInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButtonContainer,
                    contentColor = Color.White
                )
            ) {
                Text("Join via Code", fontWeight = FontWeight.Bold, color = LogoTurquoise)
            }

            OutlinedButton(
                onClick = onCreateNewCommunity,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create New Community", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Full Community Leaderboard Detail Screen:
 * Opened when tapping any Community tile in the Bento container or All Communities list.
 * Preserves the full Community Header (Theme provided by @Patron, Shared Health Bar, Trophy streak,
 * and Expandable UserCard leaderboard).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityLeaderboardScreen(
    appState: BrainRotWatchAppState,
    onBack: () -> Unit,
    onOpenManageCommunity: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showInviteSheet by remember { mutableStateOf(false) }

    val activeCommunitySettings = appState.getActiveCommunitySettings(appState.selectedCommunityName)
    val activeTheme = remember(activeCommunitySettings.activeThemeId) {
        MasterCatalog.availableThemes.firstOrNull { it.themeId == activeCommunitySettings.activeThemeId }
    }
    val backgroundColors = remember(activeTheme) {
        if (activeTheme != null && activeTheme.gradientColorsHex.size >= 2) {
            activeTheme.gradientColorsHex.map { Color(it) }
        } else {
            AppBackgroundGradient
        }
    }

    val communityMembers = remember(
        appState.selectedCommunityName,
        appState.currentUser,
        appState.personalSettings,
        appState.userInventory,
        activeCommunitySettings
    ) {
        appState.getMembersForCommunity(appState.selectedCommunityName)
    }

    val weeklyPactSummary = remember(appState.selectedCommunityName, activeCommunitySettings) {
        appState.evaluateActiveWeeklyPactSummary(appState.selectedCommunityName)
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = backgroundColors))
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home Dashboard"
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showInviteSheet = true },
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = "Invite",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Invite", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        FilledIconButton(
                            onClick = onOpenManageCommunity,
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Community Settings",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                CommunityHeader(
                    communityName = appState.selectedCommunityName,
                    availableCommunities = appState.availableCommunities,
                    communitySettings = activeCommunitySettings,
                    weeklyPactSummary = weeklyPactSummary,
                    members = communityMembers,
                    onSelectCommunity = { newName -> appState.selectCommunity(newName) },
                    onPreviewConsentModal = {
                        appState.requestJoinCommunityWithConsent(appState.selectedCommunityName, activeCommunitySettings)
                    }
                )

                val badgeConfig = activeCommunitySettings.enabledBadges
                communityMembers.forEachIndexed { index, member ->
                    UserScrollCard(
                        user = member,
                        rankNumber = index + 1,
                        enabledBadges = badgeConfig,
                        isSelf = member.userId == "current_user" || member.displayName.equals(appState.displayName, ignoreCase = true),
                        onTacticalNudge = {
                            appState.triggerTacticalNudge(member)
                        },
                        onCallLifeLine = {
                            appState.triggerInterventionAction(member, InterventionType.PHONE_CALL, "DIAL")
                        }
                    )
                }
            }
        }
    }

    if (showInviteSheet) {
        AddFriendBottomSheet(
            displayName = appState.displayName,
            friendCode = appState.currentUser.friendCode,
            avatarUrl = appState.avatarUrl ?: appState.currentUser.avatarUrl,
            title = "Invite to ${appState.selectedCommunityName}",
            onDismissRequest = { showInviteSheet = false },
            onFriendTokenDetected = { token ->
                scope.launch {
                    SupabaseFriendRepository().sendFriendRequestByCode(token)
                    appState.addFriendByUsernameOrCode(token)
                    android.widget.Toast.makeText(context, "Invited $token!", android.widget.Toast.LENGTH_LONG).show()
                    showInviteSheet = false
                }
            }
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f)
    )
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = stroke
    )
}

/**
 * Community Entry Consent Modal:
 * Shown when joining a Community whose rules override personal privacy & notification settings.
 */
@Composable
private fun CommunityEntryConsentModal(
    communityName: String,
    settings: CommunitySettings,
    onConfirmJoin: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Community Rules & Consent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Joining '$communityName' applies group rules that override your personal Friends List privacy settings:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "• Media Mode: ${settings.mediaMode.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        val limitDesc = when (val mode = settings.limitMode) {
                            is CommunityLimitMode.IndividualSharedLimit -> "Individual Shared Daily Cap (${mode.dailyMinutes}m/day)"
                            is CommunityLimitMode.WeeklyGroupPact -> "Weekly Group Pact Shared Pool (${mode.weeklyPoolMinutes / 60}h/week)"
                        }
                        Text(
                            text = "• Limit Mode: $limitDesc",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Mandatory Limit Alerts: ${if (settings.notifyOnMemberLimitReached) "Enabled (Notifies all members on breach)" else "Disabled"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Anti-Cheat Pipeline: 'Gone Dark' & Uninstall alerts are shared with the group if syncing stops.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = CircleShape
                    ) {
                        Text("Decline", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirmJoin,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkButtonContainer,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Accept & Join", fontWeight = FontWeight.Bold, color = LogoTurquoise)
                    }
                }
            }
        }
    }
}

/**
 * In-App Visual/Audio Tactical Nudge Overlay Dialog ("Caught in 4K / Go Touch Grass").
 */
@Composable
private fun TacticalNudgeOverlayDialog(
    payload: TacticalNudgePayload,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, LogoTurquoise, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2040)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = payload.emoji,
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "TACTICAL NUDGE SENT!",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = LogoTurquoise
                )
                Text(
                    text = payload.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${payload.senderName} nudged ${payload.targetName}: \"${payload.subtitle}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LogoTurquoise,
                        contentColor = Color(0xFF1E2040)
                    )
                ) {
                    Text("Touch Grass Now 🌿", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun CommunityInviteDialog(
    invite: IncomingCommunityInvite,
    onJoin: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(LogoTurquoise.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "Community Invite",
                        tint = LogoTurquoise,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "Join ${invite.communityName}?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${invite.inviterName ?: "A friend"} invited you to join ${invite.communityName}. Review the Community Ruleset to join.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = CircleShape
                    ) {
                        Text("Not Right Now", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onJoin,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkButtonContainer,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Review Rules", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = LogoTurquoise)
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRequestDialog(
    request: IncomingFriendRequest,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .border(2.5.dp, LogoTurquoise, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!request.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = request.avatarUrl,
                            contentDescription = "Friend Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = LogoTurquoise,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Text(
                    text = "${request.displayName} wants to add you as a friend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "They will be able to see your daily screen time and you will see theirs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = CircleShape
                    ) {
                        Text("Not Right Now", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkButtonContainer,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Add Friend", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = LogoTurquoise)
                    }
                }
            }
        }
    }
}

/**
 * Upgraded Community Screen Header:
 * - Renders active Community Theme & "Theme provided by @Patron" badge if a Patron slot is active.
 * - Shows active MediaTrackingMode chip (e.g., Audio-Only "Be Present" mode or Screen + Audio).
 * - When WeeklyGroupPact is enabled, pins the Shared Health Bar to the top of the screen showing
 *   remaining weekly pool hours, segmented by member avatar colors, alongside survival streak progress
 *   (2-week random / 3-week targeted Trophy tracker) and Carry vs. Anchor highlights.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunityHeader(
    communityName: String,
    availableCommunities: List<String> = emptyList(),
    communitySettings: CommunitySettings? = null,
    weeklyPactSummary: WeeklyGroupPactSummaryResult? = null,
    members: List<UserScreenTime> = emptyList(),
    onSelectCommunity: (String) -> Unit = {},
    onPreviewConsentModal: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dropdownCommunities = availableCommunities.filter { !it.equals(communityName, ignoreCase = true) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = communityName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Switch community",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(26.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                dropdownCommunities.forEach { community ->
                    Text(
                        text = community,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectCommunity(community)
                                isExpanded = false
                            }
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    )
                }
            }
        }

        if (communitySettings != null) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                communitySettings.patronUsername?.let { patron ->
                    val themeObj = MasterCatalog.availableThemes.firstOrNull { it.themeId == communitySettings.activeThemeId }
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(themeObj?.accentColorHex ?: 0xFF2E7D32),
                                shape = CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎨 ${themeObj?.name ?: "Custom Theme"} • Theme provided by @$patron",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(themeObj?.accentColorHex ?: 0xFF1E2040)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                        .clickable { onPreviewConsentModal() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val modeIcon = when (communitySettings.mediaMode) {
                        MediaTrackingMode.SCREEN_ONLY -> "📱"
                        MediaTrackingMode.SCREEN_AND_AUDIO -> "📱+🎧"
                        MediaTrackingMode.AUDIO_ONLY_BE_PRESENT -> "🎧"
                    }
                    Text(
                        text = "$modeIcon ${communitySettings.mediaMode.displayName} • Rules",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Pinned Weekly Group Pact Shared Health Bar
        val pact = communitySettings?.limitMode as? CommunityLimitMode.WeeklyGroupPact
        if (pact != null) {
            WeeklyGroupPactSharedHealthBar(
                pact = pact,
                summary = weeklyPactSummary,
                members = members
            )
        }
    }
}

@Composable
private fun WeeklyGroupPactSharedHealthBar(
    pact: CommunityLimitMode.WeeklyGroupPact,
    summary: WeeklyGroupPactSummaryResult?,
    members: List<UserScreenTime>
) {
    val segmentPalette = listOf(
        Color(0xFF6460FF),
        Color(0xFF23C8D7),
        Color(0xFFFF6D84),
        Color(0xFFFFA24B),
        Color(0xFF9B6BFF)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⚡ Weekly Group Pact Pool",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${formatUsageTime(pact.remainingMinutes)} left of ${formatUsageTime(pact.weeklyPoolMinutes)} (${(pact.healthFraction * 100).toInt()}% HP)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (pact.shieldedMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDFF6EA), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🛡️ ${pact.shieldedMinutes}m Shielded",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            val totalPool = pact.weeklyPoolMinutes.coerceAtLeast(1).toFloat()
            val entries = pact.memberContributions.entries.toList()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFD9E7F5))
            ) {
                entries.forEachIndexed { idx, (_, mins) ->
                    val frac = (mins.toFloat() / totalPool).coerceIn(0.02f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(frac)
                            .fillMaxHeight()
                            .background(segmentPalette[idx % segmentPalette.size])
                    )
                }
                val remFrac = pact.healthFraction.coerceIn(0.01f, 1f)
                Box(
                    modifier = Modifier
                        .weight(remFrac)
                        .fillMaxHeight()
                        .background(Color(0xFF2FC37F))
                )
            }

            val memberNameById = members.associate { it.userId to it.displayName }
            val carryName = summary?.carryUserId?.let { memberNameById[it] ?: it }
            val anchorName = summary?.anchorUserId?.let { memberNameById[it] ?: it }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (carryName != null) {
                    Text(
                        text = "🏅 Carry: $carryName (${summary.carryMinutesUsed}m)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                if (anchorName != null) {
                    Text(
                        text = "⚓ Anchor: $anchorName (${summary.anchorMinutesUsed}m)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                val streak = pact.consecutiveWeeksSurvived
                Text(
                    text = "🏆 Pact Streak: $streak wk • 2-Wk Random Drop (${if (streak >= 2) "UNLOCKED ✓" else "$streak/2"}) • 3-Wk Targeted Trophy (${streak.coerceAtMost(3)}/3)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Upgraded Expandable UserCard Component (Friends & Community Lists):
 * - Clean editorial row inspired by modern wellness leaderboards:
 *   `01` rank index on the left, rounded-square pastel initials avatar with a live sync dot,
 *   Name + compact icon/pill badges on top with colored trend arrow subtitle (`↓ 18% vs last week`)
 *   underneath, and bold total time (`42m`) + device count (`📱 2`) + chevron on the right.
 * - Preserves full resolved UserCardLoadout (background gradient, border, nameplate) and
 *   ProgressiveRotFrame override (Brain Rot Override Rule).
 * - Smoothly expands on tap to reveal the privacy-filtered app/category breakdown and quick actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserScrollCard(
    user: UserScreenTime,
    rankNumber: Int = 1,
    enabledBadges: EnabledBadgesConfig = EnabledBadgesConfig(),
    isSelf: Boolean = false,
    onTacticalNudge: () -> Unit = {},
    onCallLifeLine: () -> Unit = {}
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val isGoneDark = user.syncState is SyncIntegrityState.GoneDark
    val isFledTheScene = user.syncState is SyncIntegrityState.FledTheScene
    val isWarningPending = user.syncState is SyncIntegrityState.WarningPending
    val isStaticGrey = isGoneDark || isFledTheScene

    val loadout = user.resolvedLoadout
    val rotFrame = loadout.activeRotFrame
    val showRotFrame = enabledBadges.showProgressiveRotFrames && rotFrame?.isActive == true

    val cardShape = RoundedCornerShape(22.dp)

    val cardBrush = when {
        isStaticGrey -> Brush.horizontalGradient(listOf(Color(0xFFECEAE4), Color(0xFFF3F1EC)))
        loadout.baseCosmetics.backgroundGradientHex.size >= 2 -> {
            Brush.horizontalGradient(loadout.baseCosmetics.backgroundGradientHex.map { Color(it) })
        }
        else -> null
    }

    val avatarPastel = remember(user.displayName, isSelf) {
        if (isSelf) AccentLime else pastelFor(user.displayName.ifBlank { user.userId })
    }
    val initialsText = remember(user.initials, user.displayName) {
        val parts = user.displayName.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            user.initials.isNotBlank() -> user.initials.take(2).uppercase()
            else -> "U"
        }
    }

    val syncDotColor = when (user.syncState) {
        is SyncIntegrityState.Synced -> OnlineGreen
        is SyncIntegrityState.WarningPending -> WarningAmber
        is SyncIntegrityState.GoneDark, is SyncIntegrityState.FledTheScene -> OfflineGrey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isStaticGrey) 0.78f else 1f)
            .clickable { expanded = !expanded },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (cardBrush != null) Modifier.background(cardBrush) else Modifier)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showRotFrame && rotFrame != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(rotFrame.stage.borderColorHex).copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${rotFrame.stage.badgeEmoji} Brain Rot Frame: ${rotFrame.stage.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }

                if (isGoneDark || isFledTheScene || isWarningPending) {
                    val bannerText = when {
                        isFledTheScene -> "🚨 Fled the Scene • App uninstalled / token unregistered"
                        isGoneDark -> "🕵️ Gone Dark • Screen time sync stopped updating"
                        else -> "⚠️ Sync Warning Pending • Waiting for background update"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PaperSunken, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateSecondary
                        )
                    }
                }

                // Primary compact leaderboard row: Rank (01) + Squircle Avatar + Name/Trend + Time/Devices + Chevron
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 2-digit rank number ("01", "02", ...)
                    Text(
                        text = rankNumber.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(26.dp)
                    )

                    // Rounded-square pastel avatar with corner online sync status dot
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(avatarPastel)
                                .then(
                                    if (showRotFrame && rotFrame != null) {
                                        Modifier.border(
                                            2.dp,
                                            Color(rotFrame.stage.borderColorHex),
                                            RoundedCornerShape(14.dp)
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            } else {
                                Text(
                                    text = initialsText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }

                        // Sync integrity status dot with crisp surface ring
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(syncDotColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Center column: Name + compact inline badges on top, colored trend subtitle below
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val displayLabel = when {
                                isSelf -> "You"
                                user.displayName.isNotBlank() -> user.displayName
                                else -> "Member"
                            }
                            Text(
                                text = displayLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (enabledBadges.showCrown && user.medalBreakdown?.isCrownHolder == true) {
                                CompactIconBadge(
                                    symbol = "🏆",
                                    backgroundColor = AccentButter
                                )
                            } else if (enabledBadges.showRelativeReductionSpark && user.sparkResult?.isSparkHolder == true) {
                                CompactIconBadge(
                                    symbol = "✦",
                                    backgroundColor = AccentLavender
                                )
                            } else if (enabledBadges.show30DayRollingWins && (user.medalBreakdown?.totalMedalsLast30Days ?: 0) > 0) {
                                CompactIconBadge(
                                    symbol = "🏅",
                                    backgroundColor = AccentMint
                                )
                            }

                            if (user.isAccountabilityBuddy) {
                                CompactIconBadge(
                                    symbol = "★",
                                    backgroundColor = AccentPeach.copy(alpha = 0.55f)
                                )
                            }

                            loadout.baseCosmetics.nameplateTitle?.let { title ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = PaperSunken,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = title.trim(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SlateSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Subtitle: compact arrow trend ("↓ 18% vs last week") or floor warning
                        val trendText = remember(user.trendBadge, user.meetsMinimumActivityFloor) {
                            if (!user.meetsMinimumActivityFloor) {
                                "⚠️ Below Touch Grass floor"
                            } else {
                                formatCompactTrendSubtitle(user.trendBadge.label, user.trendBadge.direction)
                            }
                        }
                        val trendColor = when {
                            !user.meetsMinimumActivityFloor -> WarningAmber
                            user.trendBadge.direction == TrendDirection.IMPROVED -> TrendGreen
                            user.trendBadge.direction == TrendDirection.SPIKED -> TrendRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Text(
                            text = trendText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = trendColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right column: Total time ("42m" / "1h 08m") on top, device count ("📱 2") below
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = formatUsageTime(user.totalMinutes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "Connected devices",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = user.deviceCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (expanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.AutoMirrored.Filled.KeyboardArrowRight
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Toggle breakdown",
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Secondary achievement badges row when expanded
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (enabledBadges.showCrown && user.medalBreakdown?.isCrownHolder == true) {
                                BadgePill(
                                    text = "👑 Crown (${user.medalBreakdown.crownPoints} pts)",
                                    backgroundColor = AccentButter.copy(alpha = 0.65f),
                                    textColor = NavyPrimary
                                )
                            }
                            if (enabledBadges.show30DayRollingWins && (user.medalBreakdown?.totalMedalsLast30Days ?: 0) > 0) {
                                BadgePill(
                                    text = "🏅 ${user.medalBreakdown?.totalMedalsLast30Days} medals in 30d",
                                    backgroundColor = AccentLavender.copy(alpha = 0.55f),
                                    textColor = NavyPrimary
                                )
                            }
                            if (enabledBadges.showRelativeReductionSpark && user.sparkResult?.isSparkHolder == true) {
                                BadgePill(
                                    text = "✦ Spark (-${user.sparkResult.dropPercentage}% vs 30d)",
                                    backgroundColor = AccentMint.copy(alpha = 0.65f),
                                    textColor = NavyPrimary
                                )
                            }
                            if (user.firstResponderPoints > 0) {
                                BadgePill(
                                    text = "🚑 First Responder (${user.firstResponderPoints} pts)",
                                    backgroundColor = AccentPeach.copy(alpha = 0.55f),
                                    textColor = NavyPrimary
                                )
                            }
                        }

                        user.medalBreakdown?.let { medals ->
                            if (medals.totalMedalsLast30Days > 0) {
                                Text(
                                    text = "30-Day Medals: 🥇 ${medals.goldMedals30d} Gold • 🥈 ${medals.silverMedals30d} Silver • 🥉 ${medals.bronzeMedals30d} Bronze",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        when (user.shapedDrawer) {
                            is ShapedBreakdownDrawerState.LockedTotalTimeOnly -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Column {
                                        Text(
                                            text = "Total Time Only (Privacy Locked)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${user.displayName} shares their daily total without per-app breakdown.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            is ShapedBreakdownDrawerState.CategoryGroupedBreakdown -> {
                                Text(
                                    text = "Category Breakdown (App Names Masked)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (user.apps.isEmpty()) {
                                    Text(
                                        text = "No category usage recorded yet today.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    user.apps.forEach { usage ->
                                        UsageRow(usage)
                                    }
                                }
                            }

                            else -> {
                                Text(
                                    text = "Used Brain Rot Apps",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (user.apps.isEmpty()) {
                                    Text(
                                        text = "No Brain Rot apps recorded yet today.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    user.apps.forEach { usage ->
                                        UsageRow(usage)
                                    }
                                }
                            }
                        }

                        if (!isSelf) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onTacticalNudge,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Tactical Nudge",
                                        tint = AccentLime,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tactical Nudge",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = onCallLifeLine,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Life-Line",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Call (Life-Line)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactIconBadge(
    symbol: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
    }
}

private fun formatCompactTrendSubtitle(label: String, direction: TrendDirection): String {
    val normalized = label
        .replace("Down ", "↓ ", ignoreCase = true)
        .replace("Up ", "↑ ", ignoreCase = true)
        .replace("from last week's avg", "vs last week", ignoreCase = true)
        .replace("from last week", "vs last week", ignoreCase = true)
        .replace("from yesterday", "vs yesterday", ignoreCase = true)
    return when {
        normalized.startsWith("↓") || normalized.startsWith("↑") -> normalized
        direction == TrendDirection.IMPROVED -> "↓ $normalized"
        direction == TrendDirection.SPIKED -> "↑ $normalized"
        else -> normalized
    }
}

@Composable
private fun BadgePill(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeDashboardPreview() {
    BrainRotWatchTheme {
        val context = LocalContext.current
        val sampleState = remember { BrainRotWatchAppState(context) }
        HomeScreen(
            appState = sampleState,
            onOpenSettings = {},
            onOpenEvents = {}
        )
    }
}

@Composable
private fun UsageRow(usage: AppUsage) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(usage.accentColor)
                )
                Text(
                    text = usage.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Text(
                text = formatUsageTime(usage.minutes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SlateSecondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(UsageTrackDefault)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(usage.fillFraction.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(usage.accentColor)
            )
        }
    }
}

private fun formatUsageTime(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return when {
        (hours > 0) && (remainingMinutes > 0) -> "${hours}h ${remainingMinutes.toString().padStart(2, '0')}m"
        hours > 0 -> "${hours}h"
        else -> "${remainingMinutes}m"
    }
}

@Composable
private fun UsageAccessLockScreen(
    onPermitUsageAccess: () -> Unit,
) {
    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = AppBackgroundGradient
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Usage Access Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "To enter BrainRotWatch, allow Usage Access so we can read your actual screen time and app usage from this device.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onPermitUsageAccess,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(
                            text = "Permit usage access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationBadge(
    count: Int,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )

        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-7).dp, y = 7.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(BadgeNotificationRed)
            )
        }
    }
}

@Composable
private fun AvatarCircle(
    initials: String,
    avatarUrl: String? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ProfilePurple)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = initials.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Online sync status dot in bottom-right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 1.dp, y = 1.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(2.dp)
                .clip(CircleShape)
                .background(OnlineGreen)
        )
    }
}
