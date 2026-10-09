package com.fishforandre.brainrotwatch

import com.fishforandre.brainrotwatch.ui.theme.*

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.fishforandre.brainrotwatch.data.supabase
import io.github.jan.supabase.postgrest.from
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        com.fishforandre.brainrotwatch.notifications.NotificationHelper.createNotificationChannels(this)
        setContent {
            BrainRotWatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrainRotWatchEntry(splashScreen = splashScreen)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrainRotWatchEntry(splashScreen: SplashScreen) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val appState = remember { BrainRotWatchAppState(context) }

    splashScreen.setKeepOnScreenCondition {
        appState.isCheckingAuth
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
        try {
            appState.checkAuthAndRoute()
            val response = supabase.from("communities").select()
            android.util.Log.d("SupabaseTest", "SUCCESS: Connected to Supabase! Response: ${response.data}")

            // Schedule 15-minute background screen-time sync worker
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
                AppScreen.Home -> BrainRotWatchHome(
                    appState = appState,
                    onOpenManageCommunity = { appState.navigateTo(AppScreen.ManageCommunity) },
                    onOpenSettings = { appState.navigateTo(AppScreen.Settings) },
                )

                AppScreen.ManageCommunity -> CommunityManageScreen(
                    communityName = appState.selectedCommunity.name,
                    notifications = appState.notificationPreferences,
                    onBack = { appState.navigateTo(AppScreen.Home) },
                    onInvitePeople = { },
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
                )

                AppScreen.Settings -> UserSettingsScreen(
                    initialName = appState.displayName,
                    initialAvatarUrl = appState.avatarUrl ?: appState.currentUser.avatarUrl,
                    initialMaxScreenTimeMinutes = appState.maxScreenTimeMinutes,
                    authRepository = appState.authRepository,
                    onBack = { appState.navigateTo(AppScreen.Home) },
                    onNameChange = appState::updateDisplayName,
                    onSaveProfile = { newName, newAvatarUrl ->
                        appState.updateUserProfile(newName, newAvatarUrl)
                    },
                    onMaxScreenTimeChange = appState::updateMaxScreenTime,
                    onDeleteAccount = { appState.deleteAccount() },
                )

                AppScreen.CreateCommunity -> CreateCommunityScreen(
                    onBack = { appState.navigateTo(AppScreen.Home) },
                    onCreateCommunity = { newCommunityName ->
                        appState.createCommunity(newCommunityName)
                    }
                )

                AppScreen.Events -> EventsScreen(
                    unreadCount = appState.unreadNotifications,
                    onBack = { appState.navigateTo(AppScreen.Home) }
                )

                else -> BrainRotWatchHome(
                    appState = appState,
                    onOpenManageCommunity = { appState.navigateTo(AppScreen.ManageCommunity) },
                    onOpenSettings = { appState.navigateTo(AppScreen.Settings) }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrainRotWatchHome(
    appState: BrainRotWatchAppState,
    onOpenManageCommunity: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    var showInviteOptionsSheet by remember { mutableStateOf(false) }
    var inviteOptionsTitle by remember { mutableStateOf("") }

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
            avatarUrl = appState.avatarUrl ?: appState.currentUser.avatarUrl
        )
        value = loaded
        appState.currentUser = loaded
    }

    val dashboard = SampleBrainRotWatchData.homeDashboard.copy(
        currentUser = userScreenTime
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = AppBackgroundGradient
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HomeTopBar(
                    dashboard = dashboard,
                    onOpenSettings = onOpenSettings,
                    onOpenEvents = { appState.navigateTo(AppScreen.Events) }
                )
                CommunityHeader(
                    communityName = appState.selectedCommunityName,
                    availableCommunities = appState.availableCommunities,
                    isInviteSheetOpen = showInviteOptionsSheet,
                    onSelectCommunity = { newName -> appState.selectCommunity(newName) },
                    onOpenManageCommunity = onOpenManageCommunity,
                    onOpenInviteMember = {
                        inviteOptionsTitle = "Invite to Community"
                        showInviteOptionsSheet = true
                    },
                    onOpenAddFriend = {
                        inviteOptionsTitle = "Add Friend"
                        showInviteOptionsSheet = true
                    },
                    onOpenCreateCommunity = {
                        appState.navigateTo(AppScreen.CreateCommunity)
                    }
                )
                UserScrollCard(dashboard.currentUser)
            }
        }

        if (showInviteOptionsSheet) {
            InviteOptionsBottomSheet(
                title = inviteOptionsTitle,
                onDismissRequest = { showInviteOptionsSheet = false },
                onShowQrCode = {
                    android.widget.Toast.makeText(context, "Show QR Code clicked", android.widget.Toast.LENGTH_SHORT).show()
                },
                onSendInvite = {
                    android.widget.Toast.makeText(context, "Send Invite clicked", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteOptionsBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    onShowQrCode: () -> Unit,
    onSendInvite: () -> Unit,
) {
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
                .padding(bottom = 36.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onShowQrCode()
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCode2,
                            contentDescription = "Show QR Code",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Show QR Code",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        onSendInvite()
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Send Invite",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Send Invite",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    dashboard: HomeDashboard,
    onOpenSettings: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.final_logo_icon_only),
                contentDescription = "BrainRotWatch Logo Icon",
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(id = R.drawable.final_logo_text_only),
                contentDescription = "BrainRotWatch Text Logo",
                modifier = Modifier
                    .width(140.dp)
                    .height(20.dp),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotificationBadge(
                count = dashboard.unreadNotifications,
                onClick = onOpenEvents
            )

            AvatarCircle(
                initials = dashboard.currentUser.initials,
                avatarUrl = dashboard.currentUser.avatarUrl,
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun ContextualActionButton(
    isExpanded: Boolean,
    isFriendsSelected: Boolean,
    isSelected: Boolean = false,
    onOpenAddFriend: () -> Unit = {},
    onOpenInviteMember: () -> Unit = {},
    onOpenCreateCommunity: () -> Unit = {},
) {
    val (icon, text, onClick) = when {
        isExpanded -> Triple(
            Icons.Default.Add,
            "New Community",
            onOpenCreateCommunity
        )
        isFriendsSelected -> Triple(
            Icons.Outlined.PersonAdd,
            "Add Friend",
            onOpenAddFriend
        )
        else -> Triple(
            Icons.Outlined.PersonAdd,
            "Invite Member",
            onOpenInviteMember
        )
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        AnimatedContent(
            targetState = Triple(icon, text, isExpanded),
            transitionSpec = {
                (fadeIn() + expandHorizontally()).togetherWith(fadeOut() + shrinkHorizontally())
            },
            label = "ContextualButtonContent"
        ) { (targetIcon, targetText, _) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = targetIcon,
                    contentDescription = targetText,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CommunityHeader(
    communityName: String,
    availableCommunities: List<String> = listOf("Friends", "The Family", "Weekend Doomscrollers"),
    isInviteSheetOpen: Boolean = false,
    onSelectCommunity: (String) -> Unit = {},
    onOpenManageCommunity: () -> Unit = {},
    onOpenInviteMember: () -> Unit = {},
    onOpenAddFriend: () -> Unit = {},
    onOpenCreateCommunity: () -> Unit = {},
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isFriendsSelected = communityName.equals("Friends", ignoreCase = true)
    val dropdownCommunities = availableCommunities.filter { !it.equals(communityName, ignoreCase = true) }

    BackHandler(enabled = isExpanded) {
        isExpanded = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) { // There's a propensy to add this spacing here even when removed, this should not be here and therefore I've left it in as 0dp.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = communityName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                )

                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Switch community",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !isFriendsSelected,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
            ) {
                FilledIconButton(
                    onClick = onOpenManageCommunity,
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Community settings",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Dropdown Communities List (Appears between community name and contextual button when expanded)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                dropdownCommunities.forEach { community ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectCommunity(community)
                                isExpanded = false
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = community,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Contextual Action Button (Below Community Name when collapsed, below Expanded List when expanded)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            ContextualActionButton(
                isExpanded = isExpanded,
                isFriendsSelected = isFriendsSelected,
                isSelected = isInviteSheetOpen,
                onOpenAddFriend = onOpenAddFriend,
                onOpenInviteMember = onOpenInviteMember,
                onOpenCreateCommunity = onOpenCreateCommunity
            )
        }
    }
}




@Composable
private fun UserScrollCard(user: UserScreenTime) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Top Row: Avatar + Display Name on Left, Info Icon on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AvatarIconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Avatar",
                                tint = AvatarTextDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        text = user.displayName.ifEmpty { "Placeholder Name" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Show/Hide details",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Bottom Row: Screen time text on Bottom Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formatUsageTime(user.totalMinutes),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Used apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    user.apps.forEach { usage ->
                        UsageRow(usage)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    BrainRotWatchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HomeTopBar(
                    dashboard = SampleBrainRotWatchData.homeDashboard
                )
                CommunityHeader(
                    communityName = "The Family"
                )
                UserScrollCard(user = SampleBrainRotWatchData.homeDashboard.currentUser)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendsTopBarPreview() {
    BrainRotWatchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HomeTopBar(
                    dashboard = SampleBrainRotWatchData.homeDashboard
                )
                CommunityHeader(
                    communityName = "Friends"
                )
                UserScrollCard(user = SampleBrainRotWatchData.homeDashboard.currentUser)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserScrollCardPreview() {
    BrainRotWatchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            UserScrollCard(user = SampleBrainRotWatchData.homeDashboard.currentUser)
        }
    }
}

@Composable
private fun UsageRow(usage: AppUsage) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(usage.accentColor)
                )
                Text(
                    text = usage.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Text(
                text = formatUsageTime(usage.minutes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SlateSecondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(usage.trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(usage.fillFraction)
                    .height(14.dp)
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
        (hours > 0) && (remainingMinutes > 0) -> "${hours}h ${remainingMinutes}m"
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
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
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
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
    Box(contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(26.dp)
            )
        }

        if (count > 0) {
            Badge(
                containerColor = BadgeNotificationRed,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
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
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AvatarBackground)
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
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AvatarTextDark,
                textAlign = TextAlign.Center
            )
        }
    }
}
