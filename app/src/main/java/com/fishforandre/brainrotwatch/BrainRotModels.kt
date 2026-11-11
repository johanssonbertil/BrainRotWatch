package com.fishforandre.brainrotwatch

import androidx.compose.ui.graphics.Color
import com.fishforandre.brainrotwatch.domain.engine.BehavioralTrendBadge
import com.fishforandre.brainrotwatch.domain.engine.RelativeReductionSparkResult
import com.fishforandre.brainrotwatch.domain.engine.ShapedBreakdownDrawerState
import com.fishforandre.brainrotwatch.domain.engine.TrendDirection
import com.fishforandre.brainrotwatch.domain.engine.UserMedalBreakdown
import com.fishforandre.brainrotwatch.domain.model.AppCategory
import com.fishforandre.brainrotwatch.domain.model.BreakdownVisibility
import com.fishforandre.brainrotwatch.domain.model.CommunitySettings
import com.fishforandre.brainrotwatch.domain.model.MasterCatalog
import com.fishforandre.brainrotwatch.domain.model.PersonalSettings
import com.fishforandre.brainrotwatch.domain.model.ProgressiveRotFrame
import com.fishforandre.brainrotwatch.domain.model.ProgressiveRotStage
import com.fishforandre.brainrotwatch.domain.model.ResolvedCardCosmetics
import com.fishforandre.brainrotwatch.domain.model.SyncIntegrityState
import com.fishforandre.brainrotwatch.domain.model.UsageRecord
import com.fishforandre.brainrotwatch.domain.model.UserInventory

enum class CommunityAccessType {
    Open,       // Instant join via invite code
    Closed,     // Requires group admin approval
    Democratic  // Requires member voting threshold
}

data class UserProfile(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val friendCode: String = "",
    val maxScreenTimeMinutes: Int = 120,
    val deviceCount: Int = 1,
    val personalSettings: PersonalSettings = PersonalSettings(dailyLimitMinutes = maxScreenTimeMinutes),
    val syncState: SyncIntegrityState = SyncIntegrityState.Synced(
        lastSyncMillis = System.currentTimeMillis(),
        deviceCount = deviceCount
    ),
    val inventory: UserInventory = UserInventory(
        userId = id,
        ownedThemes = MasterCatalog.availableThemes,
        ownedGlobalCardCosmetics = MasterCatalog.availableGlobalCardPresets
    )
)

data class Friendship(
    val id: String,
    val friendUserId: String,
    val friendDisplayName: String,
    val friendAvatarUrl: String? = null,
    val status: FriendStatus = FriendStatus.Accepted,
    val isAccountabilityBuddy: Boolean = false
)

data class IncomingFriendRequest(
    val friendCode: String,
    val displayName: String,
    val avatarUrl: String? = null
)

data class IncomingCommunityInvite(
    val communityId: String,
    val communityName: String,
    val inviteCode: String,
    val inviterName: String? = null,
    val communitySettings: CommunitySettings = CommunitySettings()
)

enum class FriendStatus {
    Pending,
    Accepted,
    Blocked
}

data class Community(
    val id: String,
    val name: String,
    val inviteCode: String,
    val accessType: CommunityAccessType,
    val votingThresholdPercent: Int = 50,
    val memberCount: Int = 1,
    val settings: CommunitySettings = CommunitySettings(),
    val patronUserId: String? = settings.patronUserId,
    val patronUsername: String? = settings.patronUsername
)

data class CommunityJoinRequest(
    val id: String,
    val communityId: String,
    val applicantId: String,
    val applicantName: String,
    val applicantAvatarUrl: String? = null,
    val status: RequestStatus = RequestStatus.Pending,
    val votesApproved: Int = 0,
    val votesRequired: Int = 1
)

enum class RequestStatus {
    Pending,
    Approved,
    Rejected
}

data class HomeDashboard(
    val community: CommunitySummary,
    val currentUser: UserScreenTime,
    val unreadNotifications: Int,
)

data class CommunitySummary(
    val name: String,
    val visibility: CommunityVisibility,
    val previewMembers: List<CommunityPreviewMember>
)

typealias CommunityVisibility = CommunityAccessType

data class CommunityPreviewMember(
    val displayName: String,
    val note: String,
    val placeholderLabel: String
)

data class UserScreenTime(
    val userId: String = "user_default",
    val displayName: String,
    val initials: String,
    val avatarUrl: String? = null,
    val friendCode: String = "",
    val phoneNumber: String = "",
    val deviceCount: Int = 1,
    val totalMinutes: Int,
    val deltaMinutes: Int,
    val yesterdayMinutes: Int = (totalMinutes - deltaMinutes).coerceAtLeast(0),
    val last7DaysMinutes: List<Int> = emptyList(),
    val rolling30DayAvgMinutes: Int = 0,
    val apps: List<AppUsage>,
    val rawRecords: List<UsageRecord> = emptyList(),
    val summary: String,
    val meetsMinimumActivityFloor: Boolean = true,
    val syncState: SyncIntegrityState = SyncIntegrityState.Synced(
        lastSyncMillis = System.currentTimeMillis(),
        deviceCount = deviceCount
    ),
    val breakdownVisibility: BreakdownVisibility = BreakdownVisibility.FULL_APPS,
    val shapedDrawer: ShapedBreakdownDrawerState? = null,
    val trendBadge: BehavioralTrendBadge = BehavioralTrendBadge(
        label = "Steady pace today",
        direction = TrendDirection.NEUTRAL,
        deltaPercentVsYesterday = 0,
        deltaPercentVs7DayAvg = 0
    ),
    val medalBreakdown: UserMedalBreakdown? = null,
    val sparkResult: RelativeReductionSparkResult? = null,
    val firstResponderPoints: Int = 0,
    val isAccountabilityBuddy: Boolean = false,
    val resolvedLoadout: ResolvedCardCosmetics = ResolvedCardCosmetics(
        baseCosmetics = MasterCatalog.availableGlobalCardPresets.first(),
        activeRotFrame = ProgressiveRotFrame(ProgressiveRotStage.NONE, 0)
    ),
    val personalExtensionMinutesGranted: Int = 0
)

data class AppUsage(
    val appId: String = "",
    val name: String,
    val category: AppCategory = AppCategory.OTHER_BRAIN_ROT,
    val minutes: Int,
    val audioMinutes: Int = 0,
    val fillFraction: Float,
    val accentColor: Color,
    val trackColor: Color
)

object DefaultBrainRotWatchData {
    val emptyUserScreenTime = UserScreenTime(
        displayName = "",
        initials = "",
        avatarUrl = null,
        friendCode = "",
        deviceCount = 1,
        totalMinutes = 0,
        deltaMinutes = 0,
        apps = emptyList(),
        summary = "No screen time recorded yet today."
    )

    val emptyDashboard = HomeDashboard(
        community = CommunitySummary(
            name = "Friends",
            visibility = CommunityAccessType.Open,
            previewMembers = emptyList()
        ),
        currentUser = emptyUserScreenTime,
        unreadNotifications = 0
    )
}
