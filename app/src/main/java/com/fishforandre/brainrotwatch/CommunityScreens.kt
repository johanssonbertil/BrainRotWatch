package com.fishforandre.brainrotwatch

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fishforandre.brainrotwatch.domain.engine.InterventionType
import com.fishforandre.brainrotwatch.domain.model.BreakdownVisibility
import com.fishforandre.brainrotwatch.domain.model.CommunityLimitMode
import com.fishforandre.brainrotwatch.domain.model.CommunitySettings
import com.fishforandre.brainrotwatch.domain.model.CurfewWindow
import com.fishforandre.brainrotwatch.domain.model.LimitBreachTarget
import com.fishforandre.brainrotwatch.domain.model.MasterCatalog
import com.fishforandre.brainrotwatch.domain.model.MediaTrackingMode
import com.fishforandre.brainrotwatch.domain.model.NotificationAudience
import com.fishforandre.brainrotwatch.domain.model.PersonalSettings
import com.fishforandre.brainrotwatch.domain.model.SimpleLocalTime
import com.fishforandre.brainrotwatch.domain.model.WrapUpSchedule
import com.fishforandre.brainrotwatch.ui.theme.AppBackgroundGradient
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

/**
 * Community Creator / Admin Settings Screen:
 * - Tracked Apps Filter (from master global Brain Rot list or all)
 * - MediaTrackingMode (SCREEN_ONLY, SCREEN_AND_AUDIO, AUDIO_ONLY_BE_PRESENT)
 * - Limit Mode (IndividualSharedLimit vs. WeeklyGroupPact with Shared Pool & Targeted Trophy)
 * - Curfew Blackout Window (start/end hours)
 * - Notification Triggers & Wrap-Up Schedule (DAILY / WEEKLY / NONE)
 * - Leaderboard Badge Toggles (Crown, 30-day Rolling Wins, Spark, Progressive Rot Frames)
 * - True Majority Vote toggle for rule changes
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityManageScreen(
    communityName: String,
    isAdmin: Boolean = true,
    communitySettings: CommunitySettings = CommunitySettings(),
    notifications: CommunityNotificationPreferences = sampleCommunityNotificationPreferences,
    onBack: () -> Unit,
    onInvitePeople: () -> Unit,
    onUpdateCommunitySettings: (CommunitySettings) -> Unit = {},
    onDailySummaryChange: (Boolean) -> Unit = {},
    onThresholdAlertsChange: (Boolean) -> Unit = {},
    onApprovalRequiredChange: (Boolean) -> Unit = {},
    onLeaveCommunity: () -> Unit = {},
    onDisbandCommunity: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }
    var showDisbandConfirmDialog by remember { mutableStateOf(false) }

    var settingsState by remember(communityName, communitySettings) {
        mutableStateOf(communitySettings)
    }

    val isGroupPact = settingsState.limitMode is CommunityLimitMode.WeeklyGroupPact
    val individualCapText = remember(settingsState.limitMode) {
        (settingsState.limitMode as? CommunityLimitMode.IndividualSharedLimit)?.dailyMinutes?.toString() ?: "90"
    }
    var dailyCapInput by remember(individualCapText) { mutableStateOf(individualCapText) }

    val weeklyPoolText = remember(settingsState.limitMode) {
        (settingsState.limitMode as? CommunityLimitMode.WeeklyGroupPact)?.weeklyPoolMinutes?.toString() ?: "1200"
    }
    var weeklyPoolInput by remember(weeklyPoolText) { mutableStateOf(weeklyPoolText) }

    val curfew = settingsState.curfewWindow ?: CurfewWindow(enabled = false)
    var curfewStartHour by remember(curfew) { mutableStateOf(curfew.start.hour.toString()) }
    var curfewEndHour by remember(curfew) { mutableStateOf(curfew.end.hour.toString()) }

    fun pushSettings(updated: CommunitySettings) {
        settingsState = updated
        onUpdateCommunitySettings(updated)
    }

    if (showLeaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            title = { Text(text = "Leave $communityName?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to leave $communityName? Any Patron Theme you slotted into this group will automatically return to your inventory.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveConfirmDialog = false
                        onLeaveCommunity()
                        android.widget.Toast.makeText(context, "Left $communityName (Patron Themes returned to inventory)", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Leave Group", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDisbandConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDisbandConfirmDialog = false },
            title = { Text(text = "Disband $communityName?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to disband $communityName? All members will be removed and any equipped Patron Themes will return to their owners' inventories.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisbandConfirmDialog = false
                        onDisbandCommunity()
                        android.widget.Toast.makeText(context, "Disbanded $communityName", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Disband Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisbandConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column {
                        Text(
                            text = "Manage $communityName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Community Ruleset, Media Mode & Group Pacts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. Media Tracking Mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Media Tracking Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose how screen and background headphone/speaker audio sessions count toward this Community's leaderboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        MediaTrackingMode.entries.forEach { mode ->
                            val isSelected = settingsState.mediaMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable {
                                        pushSettings(settingsState.copy(mediaMode = mode))
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Tracked Apps Filter (Lens over Master Dataset)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tracked Apps Filter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (settingsState.trackedAppIds.isEmpty()) {
                                        "Tracking ALL global Brain Rot apps"
                                    } else {
                                        "Filtering ${settingsState.trackedAppIds.size} selected vices"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (settingsState.trackedAppIds.isNotEmpty()) {
                                TextButton(onClick = { pushSettings(settingsState.copy(trackedAppIds = emptySet())) }) {
                                    Text("Track All")
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MasterCatalog.curatedApps.forEach { app ->
                                val isTracked = settingsState.trackedAppIds.isEmpty() ||
                                    app.appId in settingsState.trackedAppIds ||
                                    app.appName in settingsState.trackedAppIds

                                FilterChip(
                                    selected = isTracked,
                                    onClick = {
                                        val currentSet = if (settingsState.trackedAppIds.isEmpty()) {
                                            MasterCatalog.curatedApps.map { it.appName }.toMutableSet()
                                        } else {
                                            settingsState.trackedAppIds.toMutableSet()
                                        }
                                        if (app.appName in currentSet || app.appId in currentSet) {
                                            currentSet.remove(app.appName)
                                            currentSet.remove(app.appId)
                                        } else {
                                            currentSet.add(app.appName)
                                        }
                                        pushSettings(settingsState.copy(trackedAppIds = currentSet))
                                    },
                                    label = { Text(app.appName) }
                                )
                            }
                        }
                    }
                }

                // 3. Limit Modes: Individual Shared Cap vs. Weekly Group Pact
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Limit Mode & Weekly Group Pact",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = !isGroupPact,
                                onClick = {
                                    val mins = dailyCapInput.toIntOrNull() ?: 90
                                    pushSettings(
                                        settingsState.copy(
                                            limitMode = CommunityLimitMode.IndividualSharedLimit(mins)
                                        )
                                    )
                                },
                                label = { Text("Individual Shared Limit") }
                            )
                            FilterChip(
                                selected = isGroupPact,
                                onClick = {
                                    val poolMins = weeklyPoolInput.toIntOrNull() ?: 1200
                                    pushSettings(
                                        settingsState.copy(
                                            limitMode = CommunityLimitMode.WeeklyGroupPact(
                                                weeklyPoolMinutes = poolMins,
                                                currentUsedMinutes = 685,
                                                memberContributions = mapOf(
                                                    "current_user" to 135,
                                                    "friend_maya" to 95,
                                                    "friend_alex" to 210,
                                                    "friend_liam" to 245
                                                ),
                                                shieldedMinutes = 30,
                                                consecutiveWeeksSurvived = 2,
                                                targetedTrophyId = "trophy_touch_grass_sanctuary"
                                            )
                                        )
                                    )
                                },
                                label = { Text("Weekly Group Pact (Shared Pool)") }
                            )
                        }

                        if (!isGroupPact) {
                            OutlinedTextField(
                                value = dailyCapInput,
                                onValueChange = { text ->
                                    val digits = text.filter { it.isDigit() }
                                    dailyCapInput = digits
                                    val mins = digits.toIntOrNull() ?: 90
                                    pushSettings(
                                        settingsState.copy(
                                            limitMode = CommunityLimitMode.IndividualSharedLimit(mins)
                                        )
                                    )
                                },
                                label = { Text("Shared Daily Cap per Member (minutes)") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            val pact = settingsState.limitMode as CommunityLimitMode.WeeklyGroupPact
                            OutlinedTextField(
                                value = weeklyPoolInput,
                                onValueChange = { text ->
                                    val digits = text.filter { it.isDigit() }
                                    weeklyPoolInput = digits
                                    val pool = digits.toIntOrNull() ?: 1200
                                    pushSettings(
                                        settingsState.copy(
                                            limitMode = pact.copy(weeklyPoolMinutes = pool)
                                        )
                                    )
                                },
                                label = { Text("Weekly Shared Pool Budget (minutes)") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "3-Week Targeted Trophy Unlock: Touch Grass Sanctuary (Current Streak: ${pact.consecutiveWeeksSurvived}/3 weeks)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 4. Curfew Blackout Window & Notification Schedule
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Curfew Blackout & Notification Triggers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        PreferenceToggleRow(
                            title = "Enable Curfew Blackout Window",
                            subtitle = "Any tracked usage during curfew immediately alerts the group",
                            checked = curfew.enabled,
                            onCheckedChange = { enabled ->
                                pushSettings(
                                    settingsState.copy(
                                        curfewWindow = curfew.copy(enabled = enabled)
                                    )
                                )
                            }
                        )

                        if (curfew.enabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = curfewStartHour,
                                    onValueChange = { v ->
                                        curfewStartHour = v.filter { it.isDigit() }
                                        val h = curfewStartHour.toIntOrNull()?.coerceIn(0, 23) ?: 23
                                        pushSettings(
                                            settingsState.copy(
                                                curfewWindow = curfew.copy(start = SimpleLocalTime(h, 0))
                                            )
                                        )
                                    },
                                    label = { Text("Start Hour (0-23)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = curfewEndHour,
                                    onValueChange = { v ->
                                        curfewEndHour = v.filter { it.isDigit() }
                                        val h = curfewEndHour.toIntOrNull()?.coerceIn(0, 23) ?: 7
                                        pushSettings(
                                            settingsState.copy(
                                                curfewWindow = curfew.copy(end = SimpleLocalTime(h, 0))
                                            )
                                        )
                                    },
                                    label = { Text("End Hour (0-23)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        PreferenceToggleRow(
                            title = "Alert Group on Member Limit Reached",
                            subtitle = "Notify all members with Tactical Nudge & Life-Line Call actions",
                            checked = settingsState.notifyOnMemberLimitReached,
                            onCheckedChange = { enabled ->
                                pushSettings(settingsState.copy(notifyOnMemberLimitReached = enabled))
                                onThresholdAlertsChange(enabled)
                            }
                        )

                        Text(
                            text = "Automated Wrap-Up Schedule",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WrapUpSchedule.entries.forEach { schedule ->
                                FilterChip(
                                    selected = settingsState.wrapUpSchedule == schedule,
                                    onClick = {
                                        pushSettings(settingsState.copy(wrapUpSchedule = schedule))
                                        onDailySummaryChange(schedule != WrapUpSchedule.NONE)
                                    },
                                    label = { Text(schedule.displayName) }
                                )
                            }
                        }

                        PreferenceToggleRow(
                            title = "Require True Majority Vote for Rule Changes",
                            subtitle = "Changes to Community settings require >50% member approval",
                            checked = settingsState.requireMajorityVoteForSettingsChanges,
                            onCheckedChange = { enabled ->
                                pushSettings(settingsState.copy(requireMajorityVoteForSettingsChanges = enabled))
                                onApprovalRequiredChange(enabled)
                            }
                        )
                    }
                }

                // 5. Leaderboard & Badge Toggles
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Leaderboard & Gamification Badges",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val badges = settingsState.enabledBadges

                        PreferenceToggleRow(
                            title = "The Crown (Consistency Leader)",
                            subtitle = "Awards 3/2/1 pts for lowest daily screen time (with Touch Grass Floor)",
                            checked = badges.showCrown,
                            onCheckedChange = {
                                pushSettings(settingsState.copy(enabledBadges = badges.copy(showCrown = it)))
                            }
                        )

                        PreferenceToggleRow(
                            title = "30-Day Rolling Win Counter",
                            subtitle = "Displays 'X medals in the last 30 days' alongside the Crown",
                            checked = badges.show30DayRollingWins,
                            onCheckedChange = {
                                pushSettings(settingsState.copy(enabledBadges = badges.copy(show30DayRollingWins = it)))
                            }
                        )

                        PreferenceToggleRow(
                            title = "Relative Reduction Badge ('The Spark')",
                            subtitle = "Highlights steepest % drop vs. member's own 30-day rolling baseline",
                            checked = badges.showRelativeReductionSpark,
                            onCheckedChange = {
                                pushSettings(settingsState.copy(enabledBadges = badges.copy(showRelativeReductionSpark = it)))
                            }
                        )

                        PreferenceToggleRow(
                            title = "Progressive 'Brain Rot' Avatar Frames",
                            subtitle = "Playful frames (🍄 Mushroom, 📺 Static, 🧟 Zombie, 🪰 Flies) that override paid borders",
                            checked = badges.showProgressiveRotFrames,
                            onCheckedChange = {
                                pushSettings(settingsState.copy(enabledBadges = badges.copy(showProgressiveRotFrames = it)))
                            }
                        )
                    }
                }

                // 6. Invite & Membership Actions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onInvitePeople,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkButtonContainer,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Invite Members", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LogoTurquoise)
                        }

                        OutlinedButton(
                            onClick = { showLeaveConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CircleShape
                        ) {
                            Text("Leave Community", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }

                        if (isAdmin) {
                            Button(
                                onClick = { showDisbandConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Disband Community", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCommunityScreen(
    onBack: () -> Unit,
    onCreateCommunity: (String) -> Unit,
    onCreateConfiguredCommunity: (String, CommunitySettings) -> Unit = { name, _ -> onCreateCommunity(name) }
) {
    var nameText by remember { mutableStateOf("") }
    var selectedMediaMode by remember { mutableStateOf(MediaTrackingMode.SCREEN_ONLY) }
    var useWeeklyGroupPact by remember { mutableStateOf(false) }
    var limitMinutesText by remember { mutableStateOf("90") }

    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "Create Community",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Community Name") },
                            placeholder = { Text("e.g. TikTok Detox or Earbud Free") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Media Tracking Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MediaTrackingMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = selectedMediaMode == mode,
                                    onClick = { selectedMediaMode = mode },
                                    label = { Text(mode.displayName) }
                                )
                            }
                        }

                        Text(
                            text = "Limit Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !useWeeklyGroupPact,
                                onClick = {
                                    useWeeklyGroupPact = false
                                    limitMinutesText = "90"
                                },
                                label = { Text("Individual Cap") }
                            )
                            FilterChip(
                                selected = useWeeklyGroupPact,
                                onClick = {
                                    useWeeklyGroupPact = true
                                    limitMinutesText = "1200"
                                },
                                label = { Text("Weekly Group Pact Pool") }
                            )
                        }

                        OutlinedTextField(
                            value = limitMinutesText,
                            onValueChange = { limitMinutesText = it.filter { c -> c.isDigit() } },
                            label = {
                                Text(if (useWeeklyGroupPact) "Weekly Pool Budget (minutes)" else "Daily Member Cap (minutes)")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (nameText.isNotBlank()) {
                                    val mins = limitMinutesText.toIntOrNull() ?: if (useWeeklyGroupPact) 1200 else 90
                                    val limitMode = if (useWeeklyGroupPact) {
                                        CommunityLimitMode.WeeklyGroupPact(
                                            weeklyPoolMinutes = mins,
                                            currentUsedMinutes = 240,
                                            memberContributions = mapOf("current_user" to 120, "friend_maya" to 120),
                                            consecutiveWeeksSurvived = 1
                                        )
                                    } else {
                                        CommunityLimitMode.IndividualSharedLimit(mins)
                                    }
                                    val settings = CommunitySettings(
                                        mediaMode = selectedMediaMode,
                                        limitMode = limitMode
                                    )
                                    onCreateConfiguredCommunity(nameText.trim(), settings)
                                }
                            },
                            enabled = nameText.isNotBlank(),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Create Community", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Personal Settings Screen:
 * - Profile Avatar & Display Name
 * - Daily Limit & Warning Threshold %
 * - Limit Breach Notification Target (ALL_FRIENDS, ACCOUNTABILITY_BUDDIES_ONLY, PRIVATE)
 * - Allow Friends to receive notifications about me (ALL_FRIENDS, ACCOUNTABILITY_BUDDIES_ONLY, NONE)
 * - Friends List App-Breakdown Visibility (FULL_APPS, CATEGORIES_ONLY, TOTAL_TIME_ONLY)
 * - Accountability Buddies (Close Friends) selection
 * - Link to Inventory & Loadout Manager
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSettingsScreen(
    initialName: String = "Johan",
    initialAvatarUrl: String? = null,
    initialMaxScreenTimeMinutes: String = "120",
    personalSettings: PersonalSettings = PersonalSettings(),
    friendsList: List<UserScreenTime> = emptyList(),
    authRepository: com.fishforandre.brainrotwatch.data.AuthRepository? = null,
    onBack: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onSaveProfile: (String, String?) -> Unit = { _, _ -> },
    onMaxScreenTimeChange: (String) -> Unit = {},
    onUpdatePersonalSettings: (PersonalSettings) -> Unit = {},
    onToggleAccountabilityBuddy: (String) -> Unit = {},
    onOpenInventoryAndLoadout: () -> Unit = {},
    onDeleteAccount: suspend () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSyncDevicesDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(initialName) }
    var maxScreenTime by remember { mutableStateOf(initialMaxScreenTimeMinutes) }
    var avatarUrl by remember { mutableStateOf(initialAvatarUrl) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var tempUncroppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            val loadedBitmap = loadBitmapFromUri(context, cameraImageUri!!)
            if (loadedBitmap != null) {
                tempUncroppedBitmap = loadedBitmap
                showCropDialog = true
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val loadedBitmap = loadBitmapFromUri(context, uri)
            if (loadedBitmap != null) {
                tempUncroppedBitmap = loadedBitmap
                showCropDialog = true
            }
        }
    }

    if (showCropDialog && tempUncroppedBitmap != null) {
        FaceCropDialog(
            sourceBitmap = tempUncroppedBitmap!!,
            onCropped = { croppedBitmap, _, _ ->
                avatarBitmap = croppedBitmap
                val savedUri = saveBitmapToLocalFile(context, croppedBitmap)
                avatarUri = savedUri
                avatarUrl = savedUri?.toString()
                showCropDialog = false
            },
            onDismiss = { showCropDialog = false }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Delete Account?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete your account? This action is permanent and cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        isDeletingAccount = true
                        scope.launch {
                            onDeleteAccount()
                            isDeletingAccount = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "Personal Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }

                // 0. Quick Link to Inventory & Loadout Manager
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenInventoryAndLoadout() }
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Inventory",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Column {
                                Text(
                                    text = "Inventory & Loadout Manager",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Manage Global User Card, Community Overrides & 1-to-1 Patron Themes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Text(
                            text = "Open →",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 1. Profile & Daily Limit Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .border(2.5.dp, LogoTurquoise, CircleShape)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                .clickable {
                                    if (tempUncroppedBitmap != null || avatarBitmap != null) {
                                        if (tempUncroppedBitmap == null && avatarBitmap != null) {
                                            tempUncroppedBitmap = avatarBitmap
                                        }
                                        showCropDialog = true
                                    } else {
                                        galleryLauncher.launch("image/*")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap!!.asImageBitmap(),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else if (!avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "Add photo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val file = File(context.cacheDir, "camera_settings_${System.currentTimeMillis()}.jpg")
                                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        cameraImageUri = uri
                                        cameraLauncher.launch(uri)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkButtonContainer, contentColor = Color.White)
                            ) {
                                Text("Take Photo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Text("Select Photo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Display name") },
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = maxScreenTime,
                            onValueChange = {
                                val sanitized = it.filter { char -> char.isDigit() }
                                maxScreenTime = sanitized
                                onMaxScreenTimeChange(sanitized)
                                val mins = sanitized.toIntOrNull() ?: 120
                                onUpdatePersonalSettings(personalSettings.copy(dailyLimitMinutes = mins))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Personal Daily Screen Time Limit (minutes)") },
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Warning Threshold: ${personalSettings.warningThresholdPercent}% of daily cap",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = personalSettings.warningThresholdPercent.toFloat(),
                                onValueChange = { v ->
                                    onUpdatePersonalSettings(
                                        personalSettings.copy(warningThresholdPercent = v.roundToInt().coerceIn(50, 95))
                                    )
                                },
                                valueRange = 50f..95f
                            )
                        }

                        statusMessage?.let { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        } else {
                            Button(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        isLoading = true
                                        statusMessage = null
                                        scope.launch {
                                            val finalAvatar = avatarUri?.toString() ?: avatarUrl
                                            val updated = authRepository?.registerDeviceAccount(
                                                displayName = name.trim(),
                                                avatarUrl = finalAvatar
                                            )
                                            isLoading = false
                                            if (updated != null) {
                                                onSaveProfile(updated.displayName, updated.avatarUrl)
                                                onNameChange(updated.displayName)
                                                statusMessage = "Profile saved successfully!"
                                                android.widget.Toast.makeText(context, "Profile saved!", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                onSaveProfile(name.trim(), finalAvatar)
                                                onNameChange(name.trim())
                                                statusMessage = "Saved locally!"
                                            }
                                        }
                                    }
                                },
                                enabled = name.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkButtonContainer, contentColor = Color.White)
                            ) {
                                Text("Save Profile Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. Friends List Privacy & Breakdown Visibility
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Friends List Privacy & Limit Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personal settings strictly govern your Friends List. Community rules may override these inside a Community after explicit entry consent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "App Breakdown Visibility (Friends List)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        BreakdownVisibility.entries.forEach { mode ->
                            val isSelected = personalSettings.breakdownVisibility == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable {
                                        onUpdatePersonalSettings(personalSettings.copy(breakdownVisibility = mode))
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mode.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Text(
                            text = "When I Breach My Daily Limit, Notify:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LimitBreachTarget.entries.forEach { target ->
                                FilterChip(
                                    selected = personalSettings.limitBreachTarget == target,
                                    onClick = {
                                        onUpdatePersonalSettings(personalSettings.copy(limitBreachTarget = target))
                                    },
                                    label = { Text(target.displayName) }
                                )
                            }
                        }

                        Text(
                            text = "Allow Friends to Receive Notifications About Me?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NotificationAudience.entries.forEach { aud ->
                                FilterChip(
                                    selected = personalSettings.allowNotificationsAboutMe == aud,
                                    onClick = {
                                        onUpdatePersonalSettings(personalSettings.copy(allowNotificationsAboutMe = aud))
                                    },
                                    label = { Text(aud.displayName) }
                                )
                            }
                        }
                    }
                }

                // 3. Accountability Buddies (Close Friends) Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Accountability Buddies (Close Friends)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Designate close friends who receive high-priority alerts and Life-Line call prompts when you hit personal limits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        friendsList.forEach { friend ->
                            val isBuddy = friend.userId in personalSettings.accountabilityBuddyIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { onToggleAccountabilityBuddy(friend.userId) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(friend.displayName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (isBuddy) "★ Accountability Buddy" else "Standard Friend",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isBuddy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isBuddy,
                                    onCheckedChange = { onToggleAccountabilityBuddy(friend.userId) }
                                )
                            }
                        }
                    }
                }

                // 4. Sync Devices & Account Merge Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sync Devices & Merge Profiles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Link multiple devices to share screen time data under one profile, or merge two separately created profiles into one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { showSyncDevicesDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CircleShape
                        ) {
                            Text("Sync Devices / Merge Account", fontWeight = FontWeight.Bold)
                        }

                        if (isDeletingAccount) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        } else {
                            TextButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Delete account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSyncDevicesDialog) {
        val currentCode = authRepository?.authState?.value?.let {
            if (it is com.fishforandre.brainrotwatch.data.AuthState.SignedIn) it.user.friendCode else ""
        } ?: ""

        SyncDevicesDialog(
            currentSyncCode = currentCode,
            authRepository = authRepository,
            onSaveProfile = onSaveProfile,
            onDismiss = { showSyncDevicesDialog = false }
        )
    }
}

@Composable
private fun SyncDevicesDialog(
    currentSyncCode: String,
    authRepository: com.fishforandre.brainrotwatch.data.AuthRepository?,
    onSaveProfile: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var targetCodeText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sync & Link Devices",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Current Device Sync Code:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSyncCode.ifEmpty { "X7K2M9" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = LogoTurquoise
                    )
                }

                Text(
                    text = "To merge or link with another profile, enter the 6-character Sync Code from your other device:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = targetCodeText,
                    onValueChange = { targetCodeText = it.uppercase() },
                    label = { Text("Target Device Sync Code") },
                    placeholder = { Text("e.g. A2B3C4") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                successMsg?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (targetCodeText.isNotBlank()) {
                        isLoading = true
                        errorMsg = null
                        scope.launch {
                            val merged = authRepository?.mergeProfiles(targetCodeText.trim())
                            isLoading = false
                            if (merged != null) {
                                onSaveProfile(merged.displayName, merged.avatarUrl)
                                successMsg = "Devices linked successfully!"
                                android.widget.Toast.makeText(context, "Devices linked successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                errorMsg = "Could not find account with sync code: ${targetCodeText.trim()}"
                            }
                        }
                    }
                },
                enabled = targetCodeText.isNotBlank() && !isLoading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButtonContainer,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Link & Merge Account", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

data class CommunityNotificationPreferences(
    val dailySummaryEnabled: Boolean,
    val thresholdAlertsEnabled: Boolean,
    val approvalRequiredEnabled: Boolean,
)

val sampleCommunityNotificationPreferences = CommunityNotificationPreferences(
    dailySummaryEnabled = true,
    thresholdAlertsEnabled = true,
    approvalRequiredEnabled = false,
)

/**
 * Activity & Integrity Feed (Notification Bell Sheet/Screen):
 * Grouped by:
 * 1. Limit Breaches & Spikes: Actionable cards with inline [Call for Rebate] (Intent.ACTION_DIAL), [Text], and [Tactical Nudge] buttons.
 * 2. Integrity Alerts: Warnings for users in GoneDark (not syncing screen time) or FledTheScene (uninstalled),
 *    plus notices for users who haven't met the minimum phone activity floor in 2+ days.
 * 3. Daily & Weekly Summaries: Stored wrap-up cards showing Crown winners and Weekly Group Pact Carry vs. Anchor results.
 */
@Composable
fun EventsScreen(
    unreadCount: Int,
    activityFeed: List<ActivityFeedItem> = emptyList(),
    lastInterventionBanner: String? = null,
    onTriggerTacticalNudge: (UserScreenTime) -> Unit = {},
    onTriggerIntervention: (UserScreenTime, InterventionType, String?) -> Unit = { _, _, _ -> },
    onBack: () -> Unit
) {
    val groupedFeed = remember(activityFeed) {
        ActivityFeedCategory.entries.mapNotNull { category ->
            val items = activityFeed.filter { it.category == category }
            if (items.isEmpty()) null else category to items
        }
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "Activity & Integrity Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                lastInterventionBanner?.let { banner ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDFF6EA)
                        )
                    ) {
                        Text(
                            text = banner,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                if (groupedFeed.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Recent Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No new notifications.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    groupedFeed.forEach { (category, items) ->
                        Text(
                            text = category.sectionTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )

                        items.forEach { item ->
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
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = item.timestampLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    item.summaryHighlight?.let { highlight ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = highlight,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    item.strugglingUser?.let { target ->
                                        if (item.category == ActivityFeedCategory.LIMIT_BREACH_OR_SPIKE) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        onTriggerIntervention(target, InterventionType.PHONE_CALL, "DIAL")
                                                    },
                                                    modifier = Modifier.weight(1.2f),
                                                    shape = CircleShape
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Call,
                                                        contentDescription = "Call for Rebate",
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Call for Rebate",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        onTriggerIntervention(target, InterventionType.IN_APP_NUDGE_OR_VOICE, "SMS")
                                                    },
                                                    shape = CircleShape
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Sms,
                                                        contentDescription = "Text",
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Text",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Button(
                                                    onClick = { onTriggerTacticalNudge(target) },
                                                    modifier = Modifier.weight(1.1f),
                                                    shape = CircleShape,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = DarkButtonContainer,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.NotificationsActive,
                                                        contentDescription = "Tactical Nudge",
                                                        tint = LogoTurquoise,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Tactical Nudge",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = LogoTurquoise,
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
            }
        }
    }
}

/**
 * Full Communities List Screen (opened via "Show all" in the Bento container):
 * Lists all communities with theme previews, health bars, ranks, and a "+ Join or Create" button.
 */
@Composable
fun AllCommunitiesScreen(
    bentoPreviews: List<CommunityBentoPreview>,
    onSelectCommunity: (String) -> Unit,
    onOpenJoinOrCreateSheet: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Text(
                            text = "All Communities",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = onOpenJoinOrCreateSheet,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkButtonContainer,
                            contentColor = Color.White
                        )
                    ) {
                        Text("+ Join or Create", color = LogoTurquoise, fontWeight = FontWeight.Bold)
                    }
                }

                bentoPreviews.forEach { preview ->
                    val accent = Color(preview.theme?.accentColorHex ?: 0xFF1E2040)
                    val bgBrush = if (preview.theme != null && preview.theme.gradientColorsHex.size >= 2) {
                        Brush.horizontalGradient(preview.theme.gradientColorsHex.map { Color(it) })
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                            .clickable { onSelectCommunity(preview.communityName) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgBrush)
                                .padding(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preview.communityName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E2040)
                                    )
                                    Text(
                                        text = "Rank #${preview.userRank} of ${preview.totalMembers}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = accent
                                    )
                                }

                                preview.theme?.let { th ->
                                    Text(
                                        text = "🎨 ${th.name}" + (preview.settings.patronUsername?.let { " • by @$it" } ?: ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF35394F)
                                    )
                                }

                                Text(
                                    text = preview.leaderOrAnchorLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E2040)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

