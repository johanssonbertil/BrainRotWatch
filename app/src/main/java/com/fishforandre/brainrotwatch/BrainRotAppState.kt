package com.fishforandre.brainrotwatch

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.fishforandre.brainrotwatch.domain.engine.CosmeticLoadoutResolver
import com.fishforandre.brainrotwatch.domain.engine.InterventionEvaluationResult
import com.fishforandre.brainrotwatch.domain.engine.InterventionRebateManager
import com.fishforandre.brainrotwatch.domain.engine.InterventionType
import com.fishforandre.brainrotwatch.domain.engine.LeaderboardScoringEngine
import com.fishforandre.brainrotwatch.domain.engine.LimitAlertEvent
import com.fishforandre.brainrotwatch.domain.engine.ShapedBreakdownDrawerState
import com.fishforandre.brainrotwatch.domain.engine.TrendCalculator
import com.fishforandre.brainrotwatch.domain.engine.UsageFilterEngine
import com.fishforandre.brainrotwatch.domain.engine.ViewerContext
import com.fishforandre.brainrotwatch.domain.engine.WeeklyGroupPactSummaryResult
import com.fishforandre.brainrotwatch.domain.model.AppCategory
import com.fishforandre.brainrotwatch.domain.model.BreakdownVisibility
import com.fishforandre.brainrotwatch.domain.model.CardCosmetics
import com.fishforandre.brainrotwatch.domain.model.CommunityLimitMode
import com.fishforandre.brainrotwatch.domain.model.CommunitySettings
import com.fishforandre.brainrotwatch.domain.model.CommunityThemeItem
import com.fishforandre.brainrotwatch.domain.model.DailyUsageSnapshot
import com.fishforandre.brainrotwatch.domain.model.MasterCatalog
import com.fishforandre.brainrotwatch.domain.model.MediaTrackingMode
import com.fishforandre.brainrotwatch.domain.model.PersonalSettings
import com.fishforandre.brainrotwatch.domain.model.ProgressiveRotFrame
import com.fishforandre.brainrotwatch.domain.model.ProgressiveRotStage
import com.fishforandre.brainrotwatch.domain.model.ResolvedCardCosmetics
import com.fishforandre.brainrotwatch.domain.model.SyncIntegrityState
import com.fishforandre.brainrotwatch.domain.model.ThemeId
import com.fishforandre.brainrotwatch.domain.model.TrophyCosmetic
import com.fishforandre.brainrotwatch.domain.model.TrophySurvivalRequirement
import com.fishforandre.brainrotwatch.domain.model.UsageRecord
import com.fishforandre.brainrotwatch.domain.model.UserInventory
import kotlin.math.max

enum class AppScreen {
    Auth,
    DeviceSetup,
    Home,
    CommunityDetail,
    AllCommunities,
    ManageCommunity,
    Settings,
    CreateCommunity,
    Events,
    InventoryAndLoadout
}

enum class FriendsSortOption(val label: String) {
    BEST("Best"),
    WORST_ROTTING("Worst / Rotting"),
    MOST_IMPROVED("Most Improved"),
    ACCOUNTABILITY_BUDDIES("Accountability Buddies")
}

enum class ActivityFeedCategory(val sectionTitle: String) {
    LIMIT_BREACH_OR_SPIKE("Limit Breaches & Spikes"),
    INTEGRITY_ALERT("Integrity & Floor Alerts"),
    DAILY_OR_WEEKLY_SUMMARY("Daily & Weekly Summaries")
}

data class TacticalNudgePayload(
    val id: String = "nudge_${System.currentTimeMillis()}",
    val senderName: String,
    val targetName: String,
    val title: String,
    val subtitle: String,
    val emoji: String = "👀",
    val timestampMillis: Long = System.currentTimeMillis()
)

data class ActivityFeedItem(
    val id: String,
    val category: ActivityFeedCategory,
    val title: String,
    val subtitle: String,
    val timestampLabel: String,
    val strugglingUser: UserScreenTime? = null,
    val limitAlertEvent: LimitAlertEvent? = null,
    val summaryHighlight: String? = null
)

data class CommunityBentoPreview(
    val communityName: String,
    val settings: CommunitySettings,
    val theme: CommunityThemeItem?,
    val isWideTile: Boolean,
    val userRank: Int,
    val totalMembers: Int,
    val hasLimitBreachAlert: Boolean,
    val pactSummary: WeeklyGroupPactSummaryResult?,
    val leaderOrAnchorLabel: String
)

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
    var selectedCommunityName by mutableStateOf(prefs.getString("last_community", "No-Scroll Squad") ?: "No-Scroll Squad")
    var availableCommunities by mutableStateOf(
        listOf(
            "No-Scroll Squad",
            "Earbud Detox Club",
            "TikTok Zero",
            "Sunday Offline",
            "Night Owl Curfew"
        )
    )
    var unreadNotifications by mutableStateOf(5)
    var notificationPreferences by mutableStateOf(sampleCommunityNotificationPreferences)
    var displayName by mutableStateOf("Johan")
    var avatarUrl by mutableStateOf<String?>(null)
    var maxScreenTimeMinutes by mutableStateOf("120")

    // Friends Leaderboard Sort Option
    var activeFriendsSort by mutableStateOf(FriendsSortOption.BEST)

    // Personal Settings & Inventory State
    var personalSettings by mutableStateOf(
        PersonalSettings(
            dailyLimitMinutes = 120,
            warningThresholdPercent = 80,
            breakdownVisibility = BreakdownVisibility.FULL_APPS,
            accountabilityBuddyIds = setOf("friend_maya", "friend_alex")
        )
    )

    var userInventory by mutableStateOf(
        UserInventory(
            userId = "current_user",
            ownedThemes = MasterCatalog.availableThemes,
            patronAssignments = mapOf("theme_zen_garden" to "No-Scroll Squad"),
            ownedGlobalCardCosmetics = MasterCatalog.availableGlobalCardPresets,
            earnedCommunityTrophies = listOf(
                TrophyCosmetic(
                    cosmeticId = "trophy_border_survivor",
                    name = "2-Week Pact Survivor Border",
                    description = "Earned by surviving 2 consecutive Weekly Group Pacts in No-Scroll Squad.",
                    requirement = TrophySurvivalRequirement.TWO_WEEK_RANDOM,
                    boundCommunityId = "No-Scroll Squad",
                    borderColorHex = 0xFFFFD700,
                    titleBadge = "🏆 Pact Survivor"
                )
            )
        )
    )

    // Community Settings Map
    val communitySettingsMap = mutableStateMapOf<String, CommunitySettings>()

    // Pending Modals & Overlays
    var pendingIncomingFriendRequest by mutableStateOf<IncomingFriendRequest?>(null)
    var pendingIncomingCommunityInvite by mutableStateOf<IncomingCommunityInvite?>(null)
    var pendingCommunityEntryConsent by mutableStateOf<Pair<String, CommunitySettings>?>(null)
    var activeTacticalNudgeOverlay by mutableStateOf<TacticalNudgePayload?>(null)
    var lastInterventionResultBanner by mutableStateOf<String?>(null)

    val addedFriends = mutableStateListOf<UserScreenTime>()
    val communityMembersMap = mutableMapOf<String, MutableList<UserScreenTime>>()
    val activityFeed = mutableStateListOf<ActivityFeedItem>()

    init {
        seedInitialSocialGraphAndSettings()
    }

    private fun seedInitialSocialGraphAndSettings() {
        val now = System.currentTimeMillis()

        // 1. No-Scroll Squad (Weekly Group Pact -> Wide Bento Tile)
        communitySettingsMap["No-Scroll Squad"] = CommunitySettings(
            trackedAppIds = setOf("com.zhiliaoapp.musically", "com.instagram.android", "com.google.android.youtube", "TikTok", "Instagram", "YouTube"),
            mediaMode = MediaTrackingMode.SCREEN_AND_AUDIO,
            limitMode = CommunityLimitMode.WeeklyGroupPact(
                weeklyPoolMinutes = 1200, // 20h shared weekly pool
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
            ),
            notifyOnMemberLimitReached = true,
            activeThemeId = "theme_zen_garden",
            patronUserId = "current_user",
            patronUsername = displayName.ifBlank { "Johan" }
        )

        // 2. Earbud Detox Club (Audio-Only Be Present Mode -> Compact Bento Tile)
        communitySettingsMap["Earbud Detox Club"] = CommunitySettings(
            trackedAppIds = emptySet(),
            mediaMode = MediaTrackingMode.AUDIO_ONLY_BE_PRESENT,
            limitMode = CommunityLimitMode.IndividualSharedLimit(dailyMinutes = 90),
            notifyOnMemberLimitReached = true,
            activeThemeId = "theme_alpine_peak",
            patronUserId = "friend_maya",
            patronUsername = "Maya Lin"
        )

        // 3. TikTok Zero (Weekly Group Pact -> Wide Bento Tile)
        communitySettingsMap["TikTok Zero"] = CommunitySettings(
            trackedAppIds = setOf("com.zhiliaoapp.musically", "TikTok"),
            mediaMode = MediaTrackingMode.SCREEN_ONLY,
            limitMode = CommunityLimitMode.WeeklyGroupPact(
                weeklyPoolMinutes = 600,
                currentUsedMinutes = 210,
                memberContributions = mapOf(
                    "current_user" to 45,
                    "friend_maya" to 35,
                    "friend_alex" to 130
                ),
                shieldedMinutes = 15,
                consecutiveWeeksSurvived = 3,
                targetedTrophyId = "trophy_touch_grass_sanctuary"
            ),
            notifyOnMemberLimitReached = true,
            activeThemeId = "trophy_touch_grass_sanctuary",
            patronUserId = "friend_alex",
            patronUsername = "Alex Rivera"
        )

        val mayaRecords = listOf(
            UsageRecord("com.instagram.android", "Instagram", AppCategory.SOCIAL_MEDIA, 28, 5, now, 2),
            UsageRecord("com.spotify.music", "Spotify", AppCategory.AUDIO_STREAMING, 10, 42, now, 2),
            UsageRecord("com.google.android.youtube", "YouTube", AppCategory.SHORT_FORM_VIDEO, 14, 0, now, 2)
        )
        val maya = UserScreenTime(
            userId = "friend_maya",
            displayName = "Maya Lin",
            initials = "M",
            phoneNumber = "5550192",
            deviceCount = 2,
            totalMinutes = 52,
            deltaMinutes = -22,
            yesterdayMinutes = 74,
            last7DaysMinutes = listOf(78, 72, 68, 74, 80, 65, 70),
            rolling30DayAvgMinutes = 85,
            apps = mayaRecords.map { r ->
                AppUsage(
                    appId = r.appId,
                    name = r.appName,
                    category = r.category,
                    minutes = r.screenTimeMinutes,
                    audioMinutes = r.audioTimeMinutes,
                    fillFraction = (r.screenTimeMinutes / 28f).coerceIn(0f, 1f),
                    accentColor = Color(r.category.accentColorHex),
                    trackColor = Color(0xFFD9E7F5)
                )
            },
            rawRecords = mayaRecords,
            summary = "52 min of screen time today.",
            meetsMinimumActivityFloor = true,
            syncState = SyncIntegrityState.Synced(lastSyncMillis = now - 120_000L, deviceCount = 2),
            breakdownVisibility = BreakdownVisibility.FULL_APPS,
            firstResponderPoints = 30,
            isAccountabilityBuddy = true,
            resolvedLoadout = ResolvedCardCosmetics(
                baseCosmetics = MasterCatalog.availableGlobalCardPresets[1], // Aurora Mist
                activeRotFrame = ProgressiveRotFrame(ProgressiveRotStage.NONE, 0)
            )
        )

        val alexRecords = listOf(
            UsageRecord("com.zhiliaoapp.musically", "TikTok", AppCategory.SHORT_FORM_VIDEO, 64, 0, now, 1),
            UsageRecord("com.reddit.frontpage", "Reddit", AppCategory.SOCIAL_MEDIA, 31, 0, now, 1),
            UsageRecord("com.supercell.clashroyale", "Clash Royale", AppCategory.GAMES, 22, 0, now, 1)
        )
        val alex = UserScreenTime(
            userId = "friend_alex",
            displayName = "Alex Rivera",
            initials = "A",
            phoneNumber = "5550148",
            deviceCount = 1,
            totalMinutes = 117,
            deltaMinutes = 29,
            yesterdayMinutes = 88,
            last7DaysMinutes = listOf(90, 95, 84, 88, 92, 100, 89),
            rolling30DayAvgMinutes = 95,
            apps = alexRecords.map { r ->
                AppUsage(
                    appId = r.appId,
                    name = r.appName,
                    category = r.category,
                    minutes = r.screenTimeMinutes,
                    audioMinutes = r.audioTimeMinutes,
                    fillFraction = (r.screenTimeMinutes / 64f).coerceIn(0f, 1f),
                    accentColor = Color(r.category.accentColorHex),
                    trackColor = Color(0xFFD9E7F5)
                )
            },
            rawRecords = alexRecords,
            summary = "117 min of screen time today (Categories Only).",
            meetsMinimumActivityFloor = true,
            syncState = SyncIntegrityState.Synced(lastSyncMillis = now - 300_000L, deviceCount = 1),
            breakdownVisibility = BreakdownVisibility.CATEGORIES_ONLY,
            firstResponderPoints = 10,
            isAccountabilityBuddy = true,
            resolvedLoadout = ResolvedCardCosmetics(
                baseCosmetics = MasterCatalog.availableGlobalCardPresets[2],
                activeRotFrame = ProgressiveRotFrame(ProgressiveRotStage.TV_STATIC_GLASSES, 2)
            )
        )

        val liamRecords = listOf(
            UsageRecord("com.zhiliaoapp.musically", "TikTok", AppCategory.SHORT_FORM_VIDEO, 95, 0, now - 7_200_000L, 1),
            UsageRecord("tv.twitch.android.app", "Twitch", AppCategory.SHORT_FORM_VIDEO, 50, 20, now - 7_200_000L, 1)
        )
        val liamGoneDark = UserScreenTime(
            userId = "friend_liam",
            displayName = "Liam Vance",
            initials = "L",
            phoneNumber = "5550177",
            deviceCount = 1,
            totalMinutes = 145,
            deltaMinutes = 40,
            yesterdayMinutes = 105,
            last7DaysMinutes = listOf(110, 115, 108, 120, 105),
            rolling30DayAvgMinutes = 112,
            apps = emptyList(),
            rawRecords = liamRecords,
            summary = "Sync interrupted — Gone Dark",
            meetsMinimumActivityFloor = false,
            syncState = SyncIntegrityState.GoneDark(failedSinceMillis = now - 3 * 3600_000L),
            breakdownVisibility = BreakdownVisibility.TOTAL_TIME_ONLY,
            resolvedLoadout = ResolvedCardCosmetics(
                baseCosmetics = MasterCatalog.availableGlobalCardPresets[0],
                activeRotFrame = ProgressiveRotFrame(ProgressiveRotStage.SPROUTING_MUSHROOM, 1)
            )
        )

        addedFriends.clear()
        addedFriends.addAll(listOf(maya, alex, liamGoneDark))

        communityMembersMap["No-Scroll Squad"] = mutableListOf(maya, alex, liamGoneDark)
        communityMembersMap["Earbud Detox Club"] = mutableListOf(maya, alex)
        communityMembersMap["TikTok Zero"] = mutableListOf(maya, alex)
        communityMembersMap["Sunday Offline"] = mutableListOf(maya, alex)
        communityMembersMap["Night Owl Curfew"] = mutableListOf(maya, liamGoneDark)

        // Seed Activity & Integrity Feed with all 3 required categories:
        // 1. Limit Breaches & Spikes
        // 2. Integrity & Floor Alerts (GoneDark, FledTheScene, and 2+ days below Touch Grass Floor)
        // 3. Daily & Weekly Summaries (Crown winners & Weekly Group Pact Carry vs. Anchor)
        val alertEvent = LimitAlertEvent(
            alertId = "alert_alex_1",
            strugglingUserId = alex.userId,
            strugglingUserName = alex.displayName,
            triggeredAtEpochMillis = now - 120_000L,
            currentMinutes = 117,
            limitMinutes = 90
        )
        activityFeed.clear()
        activityFeed.addAll(
            listOf(
                ActivityFeedItem(
                    id = "feed_breach_1",
                    category = ActivityFeedCategory.LIMIT_BREACH_OR_SPIKE,
                    title = "🚨 ${alex.displayName} breached their daily limit (117m / 90m)",
                    subtitle = "Spiked +33% on TikTok in No-Scroll Squad. Call within 5m for a +15m Life-Line Rebate & 30m Pool Shield!",
                    timestampLabel = "2m ago",
                    strugglingUser = alex,
                    limitAlertEvent = alertEvent
                ),
                ActivityFeedItem(
                    id = "feed_integrity_1",
                    category = ActivityFeedCategory.INTEGRITY_ALERT,
                    title = "🕵️ ${liamGoneDark.displayName} is GoneDark (Not Syncing)",
                    subtitle = "Background screen-time sync stopped 3h ago. Card is locked in static-grey state.",
                    timestampLabel = "1h ago",
                    strugglingUser = liamGoneDark
                ),
                ActivityFeedItem(
                    id = "feed_integrity_2",
                    category = ActivityFeedCategory.INTEGRITY_ALERT,
                    title = "🌱 Touch Grass Floor Alert: ${liamGoneDark.displayName}",
                    subtitle = "Hasn't met the minimum baseline phone activity floor in 2+ days (ineligible for Crown wins).",
                    timestampLabel = "4h ago",
                    strugglingUser = liamGoneDark
                ),
                ActivityFeedItem(
                    id = "feed_integrity_3",
                    category = ActivityFeedCategory.INTEGRITY_ALERT,
                    title = "🏃‍♂️ Devon K. FledTheScene (App Uninstalled)",
                    subtitle = "End-of-day silent push check detected an unregistered token without reinstall.",
                    timestampLabel = "Yesterday"
                ),
                ActivityFeedItem(
                    id = "feed_summary_1",
                    category = ActivityFeedCategory.DAILY_OR_WEEKLY_SUMMARY,
                    title = "👑 Daily Wrap-Up: Maya Lin took the Crown!",
                    subtitle = "Recorded the lowest valid screen time (52m, -39% vs 30d avg) and earned +3 Crown pts.",
                    timestampLabel = "Today, 8:00 AM",
                    summaryHighlight = "🥇 Maya Lin (52m) • 🥈 Johan (68m) • 🥉 Alex Rivera (117m)"
                ),
                ActivityFeedItem(
                    id = "feed_summary_2",
                    category = ActivityFeedCategory.DAILY_OR_WEEKLY_SUMMARY,
                    title = "⚡ Weekly Group Pact Summary: No-Scroll Squad",
                    subtitle = "Pact Survived (Week 2/3)! Unlocked a randomized Trophy Border drop.",
                    timestampLabel = "Sun, 9:00 PM",
                    summaryHighlight = "🏅 Carry: Maya Lin (95m used) • ⚓ Anchor: Liam Vance (245m drained)"
                )
            )
        )
    }

    fun getActiveCommunitySettings(communityName: String = selectedCommunityName): CommunitySettings {
        return communitySettingsMap.getOrPut(communityName) {
            CommunitySettings()
        }
    }

    fun updateCommunitySettings(communityName: String, updated: CommunitySettings) {
        communitySettingsMap[communityName] = updated
    }

    /**
     * Builds up to 4 Bento Previews for the "Last Visited Communities" container,
     * prioritizing active / Weekly Group Pact communities for the Wide tiles.
     */
    fun getCommunityBentoPreviews(): List<CommunityBentoPreview> {
        val nonFriendCommunities = availableCommunities.filter { !it.equals("Friends", ignoreCase = true) }
        val sortedByPriority = nonFriendCommunities.sortedByDescending { name ->
            getActiveCommunitySettings(name).limitMode is CommunityLimitMode.WeeklyGroupPact
        }

        return sortedByPriority.mapIndexed { index, name ->
            val settings = getActiveCommunitySettings(name)
            val theme = MasterCatalog.availableThemes.firstOrNull { it.themeId == settings.activeThemeId }
            val members = getMembersForCommunity(name)
            val selfIdx = members.indexOfFirst {
                it.userId == "current_user" || it.displayName.equals(displayName, ignoreCase = true)
            }
            val rank = if (selfIdx >= 0) selfIdx + 1 else 1
            val pact = settings.limitMode as? CommunityLimitMode.WeeklyGroupPact
            val pactSummary = pact?.let { LeaderboardScoringEngine.evaluateWeeklyGroupPactSummary(it) }

            val dailyCap = (settings.limitMode as? CommunityLimitMode.IndividualSharedLimit)?.dailyMinutes ?: 90
            val hasBreach = members.any { it.totalMinutes > dailyCap }

            val nameMap = members.associate { it.userId to it.displayName }
            val leaderOrAnchor = if (pactSummary != null) {
                val carry = pactSummary.carryUserId?.let { nameMap[it] ?: "Maya" } ?: "Maya"
                val anchor = pactSummary.anchorUserId?.let { nameMap[it] ?: "Liam" } ?: "Liam"
                "🏅 $carry • ⚓ $anchor"
            } else {
                val leader = members.firstOrNull()?.displayName ?: displayName.ifBlank { "Johan" }
                "👑 Leader: $leader"
            }

            // Even rows (0, 2, ...): Left = Wide, Right = Compact. Odd rows (1, 3, ...): Left = Compact, Right = Wide.
            val rowIndex = index / 2
            val colIndex = index % 2
            val isWide = if (rowIndex % 2 == 0) colIndex == 0 else colIndex == 1
            CommunityBentoPreview(
                communityName = name,
                settings = settings,
                theme = theme,
                isWideTile = isWide,
                userRank = rank,
                totalMembers = members.size.coerceAtLeast(1),
                hasLimitBreachAlert = hasBreach,
                pactSummary = pactSummary,
                leaderOrAnchorLabel = leaderOrAnchor
            )
        }
    }

    /**
     * Returns the inline User + Friends Leaderboard sorted/filtered by [activeFriendsSort].
     */
    fun getSortedFriendsAndUserLeaderboard(): List<UserScreenTime> {
        val allShaped = getMembersForCommunity("Friends")
        return when (activeFriendsSort) {
            FriendsSortOption.BEST -> allShaped.sortedBy { it.totalMinutes }
            FriendsSortOption.WORST_ROTTING -> allShaped.sortedByDescending { it.totalMinutes }
            FriendsSortOption.MOST_IMPROVED -> allShaped.sortedWith(
                compareByDescending<UserScreenTime> { it.sparkResult?.dropPercentage ?: 0 }
                    .thenBy { it.trendBadge.deltaPercentVs7DayAvg }
            )
            FriendsSortOption.ACCOUNTABILITY_BUDDIES -> allShaped.filter { member ->
                val isSelf = member.userId == "current_user" || member.displayName.equals(displayName, ignoreCase = true)
                isSelf || member.isAccountabilityBuddy || member.userId in personalSettings.accountabilityBuddyIds
            }.sortedBy { it.totalMinutes }
        }
    }

    fun getMembersForSelectedCommunity(): List<UserScreenTime> {
        return getMembersForCommunity(selectedCommunityName)
    }

    fun getMembersForCommunity(targetCommunityName: String): List<UserScreenTime> {
        val isFriendsView = targetCommunityName.equals("Friends", ignoreCase = true)
        val commSettings = if (!isFriendsView) getActiveCommunitySettings(targetCommunityName) else null

        val selfEntry = if (currentUser.displayName.isBlank()) {
            currentUser.copy(
                userId = "current_user",
                displayName = displayName.ifBlank { "Johan (You)" },
                initials = (displayName.firstOrNull()?.uppercase() ?: "J"),
                totalMinutes = 68,
                deltaMinutes = -14,
                yesterdayMinutes = 82,
                last7DaysMinutes = listOf(85, 80, 78, 82, 90, 75, 72),
                rolling30DayAvgMinutes = 88
            )
        } else {
            currentUser
        }

        val rawMembers = if (isFriendsView) {
            listOf(selfEntry) + addedFriends
        } else {
            listOf(selfEntry) + (communityMembersMap[targetCommunityName] ?: addedFriends)
        }.filter { it.displayName.isNotBlank() }
            .distinctBy { it.displayName }

        val snapshotsByDate = buildSnapshotsForLeaderboard(rawMembers, commSettings)
        val medalsMap = LeaderboardScoringEngine.calculateCrownScores(
            historicalDailySnapshotsByDate = snapshotsByDate,
            recent30DayDates = snapshotsByDate.keys,
            mediaMode = commSettings?.mediaMode ?: MediaTrackingMode.SCREEN_ONLY,
            trackedAppIds = commSettings?.trackedAppIds ?: emptySet()
        )

        val todayMinutesMap = mutableMapOf<String, Int>()
        val baselineMap = mutableMapOf<String, Int>()
        val eligibleIds = mutableSetOf<String>()

        val shapedMembers = rawMembers.map { member ->
            val isSelf = member.userId == "current_user" || member.displayName == selfEntry.displayName
            val effectiveVisibility = if (isSelf) {
                personalSettings.breakdownVisibility
            } else {
                member.breakdownVisibility
            }

            val viewerContext = if (isFriendsView || commSettings == null) {
                ViewerContext.FriendsList(ownerBreakdownVisibility = effectiveVisibility)
            } else {
                ViewerContext.CommunityView(
                    communitySettings = commSettings,
                    memberBreakdownVisibility = effectiveVisibility
                )
            }

            val recordsToShape = if (member.rawRecords.isNotEmpty()) {
                member.rawRecords
            } else {
                member.apps.map { app ->
                    UsageRecord(
                        appId = app.appId.ifBlank { app.name },
                        appName = app.name,
                        category = app.category,
                        screenTimeMinutes = app.minutes,
                        audioTimeMinutes = app.audioMinutes,
                        lastSyncedEpochMillis = System.currentTimeMillis(),
                        connectedDeviceCount = member.deviceCount
                    )
                }
            }

            val shaped = UsageFilterEngine.shapeUsageForViewer(recordsToShape, viewerContext)
            val effectiveMinutes = if (recordsToShape.isNotEmpty()) shaped.totalMinutes else member.totalMinutes

            todayMinutesMap[member.userId] = effectiveMinutes
            baselineMap[member.userId] = max(member.rolling30DayAvgMinutes, effectiveMinutes + 15)
            if (member.meetsMinimumActivityFloor && member.syncState is SyncIntegrityState.Synced) {
                eligibleIds.add(member.userId)
            }

            val trend = TrendCalculator.calculateTrend(
                todayMinutes = effectiveMinutes,
                yesterdayMinutes = member.yesterdayMinutes,
                last7DaysMinutes = member.last7DaysMinutes
            )

            val resolvedLoadout = if (isSelf) {
                CosmeticLoadoutResolver.resolveUserCardLoadout(
                    inventory = userInventory,
                    communityId = if (isFriendsView) null else targetCommunityName,
                    activeRotFrame = member.resolvedLoadout.activeRotFrame
                )
            } else {
                member.resolvedLoadout
            }

            val mappedApps = when (val d = shaped.drawerState) {
                is ShapedBreakdownDrawerState.FullAppBreakdown -> d.items.map { item ->
                    AppUsage(
                        appId = item.label,
                        name = item.label,
                        category = item.category,
                        minutes = item.minutes,
                        fillFraction = item.fillFraction,
                        accentColor = Color(item.accentColorHex),
                        trackColor = Color(0xFFD9E7F5)
                    )
                }
                is ShapedBreakdownDrawerState.CategoryGroupedBreakdown -> d.categories.map { item ->
                    AppUsage(
                        appId = item.label,
                        name = item.label,
                        category = item.category,
                        minutes = item.minutes,
                        fillFraction = item.fillFraction,
                        accentColor = Color(item.accentColorHex),
                        trackColor = Color(0xFFD9E7F5)
                    )
                }
                is ShapedBreakdownDrawerState.LockedTotalTimeOnly -> emptyList()
            }

            member.copy(
                totalMinutes = effectiveMinutes,
                apps = mappedApps,
                shapedDrawer = shaped.drawerState,
                trendBadge = trend,
                medalBreakdown = medalsMap[member.userId],
                isAccountabilityBuddy = member.userId in personalSettings.accountabilityBuddyIds,
                resolvedLoadout = resolvedLoadout
            )
        }

        val sparkMap = LeaderboardScoringEngine.calculateRelativeReductionSpark(
            todayMinutesByUser = todayMinutesMap,
            rolling30DayAverageByUser = baselineMap,
            eligibleUserIds = eligibleIds
        )

        return shapedMembers
            .map { m -> m.copy(sparkResult = sparkMap[m.userId]) }
            .sortedBy { it.totalMinutes }
    }

    private fun buildSnapshotsForLeaderboard(
        members: List<UserScreenTime>,
        commSettings: CommunitySettings?
    ): Map<String, List<DailyUsageSnapshot>> {
        val result = mutableMapOf<String, List<DailyUsageSnapshot>>()
        for (dayIdx in 0..6) {
            val dateKey = "2026-11-%02d".format(10 - dayIdx)
            result[dateKey] = members.map { member ->
                val dayMins = if (dayIdx == 0) {
                    member.totalMinutes
                } else {
                    member.last7DaysMinutes.getOrNull(dayIdx - 1) ?: member.yesterdayMinutes
                }
                DailyUsageSnapshot(
                    userId = member.userId,
                    dateIso = dateKey,
                    records = listOf(
                        UsageRecord(
                            appId = "summary_app",
                            appName = "Tracked Usage",
                            category = AppCategory.SHORT_FORM_VIDEO,
                            screenTimeMinutes = dayMins,
                            audioTimeMinutes = if (commSettings?.mediaMode == MediaTrackingMode.AUDIO_ONLY_BE_PRESENT) dayMins else 0,
                            lastSyncedEpochMillis = System.currentTimeMillis(),
                            connectedDeviceCount = member.deviceCount
                        )
                    ),
                    meetsMinimumActivityFloor = member.meetsMinimumActivityFloor,
                    syncState = member.syncState
                )
            }
        }
        return result
    }

    fun evaluateActiveWeeklyPactSummary(communityName: String = selectedCommunityName): WeeklyGroupPactSummaryResult? {
        val settings = getActiveCommunitySettings(communityName)
        val pact = settings.limitMode as? CommunityLimitMode.WeeklyGroupPact ?: return null
        return LeaderboardScoringEngine.evaluateWeeklyGroupPactSummary(pact)
    }

    fun triggerTacticalNudge(targetUser: UserScreenTime, presetTitle: String = "Caught in 4K 📸", presetSubtitle: String = "Go touch some grass right now!") {
        val payload = TacticalNudgePayload(
            senderName = displayName.ifBlank { "Friend" },
            targetName = targetUser.displayName,
            title = presetTitle,
            subtitle = presetSubtitle,
            emoji = "🌿"
        )
        activeTacticalNudgeOverlay = payload
        com.fishforandre.brainrotwatch.notifications.NotificationHelper.showInteractiveLimitAlert(
            context = context,
            strugglingUserName = targetUser.displayName,
            strugglingUserId = targetUser.userId,
            phoneNumber = targetUser.phoneNumber,
            currentMinutes = targetUser.totalMinutes,
            limitMinutes = personalSettings.dailyLimitMinutes
        )
    }

    fun triggerInterventionAction(
        targetUser: UserScreenTime,
        interventionType: InterventionType,
        launchDialerOrSms: String? = null
    ): InterventionEvaluationResult {
        val now = System.currentTimeMillis()
        val matchingAlert = activityFeed.firstOrNull { it.strugglingUser?.userId == targetUser.userId }?.limitAlertEvent
            ?: LimitAlertEvent(
                alertId = "adhoc_${now}",
                strugglingUserId = targetUser.userId,
                strugglingUserName = targetUser.displayName,
                triggeredAtEpochMillis = now - 60_000L,
                currentMinutes = targetUser.totalMinutes,
                limitMinutes = personalSettings.dailyLimitMinutes
            )

        val currentCommSettings = getActiveCommunitySettings(selectedCommunityName)
        val currentPact = currentCommSettings.limitMode as? CommunityLimitMode.WeeklyGroupPact

        val eval = InterventionRebateManager.evaluateIntervention(
            alertEvent = matchingAlert,
            responderUserId = "current_user",
            interventionType = interventionType,
            initiatedAtEpochMillis = now,
            callDurationSeconds = 180,
            activeGroupPact = currentPact
        )

        if (eval.updatedGroupPact != null && !selectedCommunityName.equals("Friends", ignoreCase = true)) {
            communitySettingsMap[selectedCommunityName] = currentCommSettings.copy(
                limitMode = eval.updatedGroupPact
            )
        }

        if (eval.awardedFirstResponderBadge) {
            currentUser = currentUser.copy(
                firstResponderPoints = currentUser.firstResponderPoints + eval.firstResponderPointsEarned
            )
        }

        lastInterventionResultBanner = eval.summaryMessage

        if (launchDialerOrSms == "DIAL") {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${targetUser.phoneNumber.ifBlank { "5550199" }}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
            } catch (_: Exception) {}
        } else if (launchDialerOrSms == "SMS") {
            try {
                val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:${targetUser.phoneNumber.ifBlank { "5550199" }}")
                    putExtra("sms_body", "Hey ${targetUser.displayName}! Saw your BrainRotWatch alert — take a break and touch grass! 🌿")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(smsIntent)
            } catch (_: Exception) {}
        }

        return eval
    }

    fun assignPatronThemeToCommunity(themeId: ThemeId, communityName: String): Result<Unit> {
        val result = CosmeticLoadoutResolver.assignPatronTheme(userInventory, themeId, communityName)
        return result.map { updatedInv ->
            userInventory = updatedInv
            val settings = getActiveCommunitySettings(communityName)
            communitySettingsMap[communityName] = settings.copy(
                activeThemeId = themeId,
                patronUserId = "current_user",
                patronUsername = displayName.ifBlank { "Johan" }
            )
        }
    }

    fun unequipPatronTheme(themeId: ThemeId) {
        val prevComm = userInventory.patronAssignments[themeId]
        userInventory = CosmeticLoadoutResolver.unequipPatronTheme(userInventory, themeId)
        if (prevComm != null) {
            val settings = getActiveCommunitySettings(prevComm)
            if (settings.activeThemeId == themeId) {
                communitySettingsMap[prevComm] = settings.copy(
                    activeThemeId = null,
                    patronUserId = null,
                    patronUsername = null
                )
            }
        }
    }

    fun updateGlobalCardLoadout(cosmetics: CardCosmetics) {
        userInventory = CosmeticLoadoutResolver.updateGlobalCardLoadout(userInventory, cosmetics)
    }

    fun updateCommunityCardOverride(communityName: String, cosmetics: CardCosmetics?) {
        userInventory = CosmeticLoadoutResolver.updateCommunityCardOverride(userInventory, communityName, cosmetics)
    }

    fun toggleAccountabilityBuddy(friendUserId: String) {
        val currentSet = personalSettings.accountabilityBuddyIds.toMutableSet()
        if (friendUserId in currentSet) {
            currentSet.remove(friendUserId)
        } else {
            currentSet.add(friendUserId)
        }
        personalSettings = personalSettings.copy(accountabilityBuddyIds = currentSet)
    }

    fun addFriendByUsernameOrCode(query: String) {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return
        if (addedFriends.any { it.displayName.equals(cleaned, ignoreCase = true) || it.friendCode.equals(cleaned, ignoreCase = true) }) {
            return
        }
        val now = System.currentTimeMillis()
        val newFriend = UserScreenTime(
            userId = "friend_${cleaned.lowercase().replace(" ", "_")}",
            displayName = cleaned.replaceFirstChar { it.uppercase() },
            initials = cleaned.take(1).uppercase(),
            friendCode = cleaned.uppercase().take(6),
            phoneNumber = "5550188",
            deviceCount = 1,
            totalMinutes = 64,
            deltaMinutes = -12,
            yesterdayMinutes = 76,
            last7DaysMinutes = listOf(80, 74, 78, 76, 70),
            rolling30DayAvgMinutes = 82,
            apps = listOf(
                AppUsage(
                    appId = "Instagram",
                    name = "Instagram",
                    category = AppCategory.SOCIAL_MEDIA,
                    minutes = 40,
                    fillFraction = 1f,
                    accentColor = Color(AppCategory.SOCIAL_MEDIA.accentColorHex),
                    trackColor = Color(0xFFD9E7F5)
                ),
                AppUsage(
                    appId = "YouTube",
                    name = "YouTube",
                    category = AppCategory.SHORT_FORM_VIDEO,
                    minutes = 24,
                    fillFraction = 0.6f,
                    accentColor = Color(AppCategory.SHORT_FORM_VIDEO.accentColorHex),
                    trackColor = Color(0xFFD9E7F5)
                )
            ),
            rawRecords = listOf(
                UsageRecord("com.instagram.android", "Instagram", AppCategory.SOCIAL_MEDIA, 40, 0, now, 1),
                UsageRecord("com.google.android.youtube", "YouTube", AppCategory.SHORT_FORM_VIDEO, 24, 0, now, 1)
            ),
            summary = "64 min of screen time today."
        )
        addedFriends.add(newFriend)
    }

    fun handleDeepLink(uri: Uri?) {
        if (uri == null) return
        val parsed = com.fishforandre.brainrotwatch.ui.qr.DeepLinkParser.parseUri(uri)

        if (parsed != null && parsed.isCommunityInvite) {
            val commName = parsed.communityName ?: uri.getQueryParameter("name") ?: "New Community"
            val commId = uri.getQueryParameter("community_id") ?: uri.getQueryParameter("id") ?: "comm_123"
            val invCode = parsed.token
            val inviter = uri.getQueryParameter("inviter") ?: "A friend"

            pendingIncomingCommunityInvite = IncomingCommunityInvite(
                communityId = commId,
                communityName = commName,
                inviteCode = invCode,
                inviterName = inviter,
                communitySettings = getActiveCommunitySettings(commName)
            )
        } else if (parsed != null && parsed.token.isNotBlank()) {
            val reqName = uri.getQueryParameter("name") ?: uri.getQueryParameter("display_name") ?: "BrainRotWatch User"
            val reqAvatar = uri.getQueryParameter("avatar") ?: uri.getQueryParameter("avatar_url")

            pendingIncomingFriendRequest = IncomingFriendRequest(
                friendCode = parsed.token,
                displayName = reqName,
                avatarUrl = reqAvatar
            )
        }
    }

    fun dismissIncomingFriendRequest() {
        pendingIncomingFriendRequest = null
    }

    fun dismissIncomingCommunityInvite() {
        pendingIncomingCommunityInvite = null
    }

    fun requestJoinCommunityWithConsent(communityName: String, settings: CommunitySettings = getActiveCommunitySettings(communityName)) {
        pendingCommunityEntryConsent = communityName to settings
    }

    fun confirmCommunityEntryConsent() {
        val pending = pendingCommunityEntryConsent ?: return
        val (commName, settings) = pending
        communitySettingsMap[commName] = settings
        joinCommunity(commName)
        pendingCommunityEntryConsent = null
    }

    fun dismissCommunityEntryConsent() {
        pendingCommunityEntryConsent = null
    }

    fun joinCommunity(communityName: String) {
        val trimmed = communityName.trim()
        if (trimmed.isNotBlank() && !availableCommunities.contains(trimmed)) {
            availableCommunities = availableCommunities + trimmed
        }
        if (!communityMembersMap.containsKey(trimmed)) {
            communityMembersMap[trimmed] = addedFriends.toMutableList()
        }
        openCommunityDetail(if (trimmed.isNotBlank()) trimmed else communityName)
        dismissIncomingCommunityInvite()
    }

    fun openCommunityDetail(communityName: String) {
        selectCommunity(communityName)
        navigateTo(AppScreen.CommunityDetail)
    }

    fun leaveCommunity(communityName: String) {
        userInventory = CosmeticLoadoutResolver.releaseThemesForCommunity(userInventory, communityName)
        availableCommunities = availableCommunities.filter { !it.equals(communityName, ignoreCase = true) }
        navigateTo(AppScreen.Home)
    }

    fun disbandCommunity(communityName: String) {
        userInventory = CosmeticLoadoutResolver.releaseThemesForCommunity(userInventory, communityName)
        availableCommunities = availableCommunities.filter { !it.equals(communityName, ignoreCase = true) }
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

    fun createCommunity(name: String, initialSettings: CommunitySettings = CommunitySettings()) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !availableCommunities.contains(trimmed)) {
            availableCommunities = availableCommunities + trimmed
        }
        communitySettingsMap[trimmed] = initialSettings
        communityMembersMap[trimmed] = addedFriends.toMutableList()
        openCommunityDetail(if (trimmed.isNotBlank()) trimmed else name)
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
        val mins = minutes.toIntOrNull() ?: 120
        personalSettings = personalSettings.copy(dailyLimitMinutes = mins)
    }
}
