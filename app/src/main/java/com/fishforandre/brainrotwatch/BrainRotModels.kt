package com.fishforandre.brainrotwatch

import androidx.compose.ui.graphics.Color

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
    val deviceCount: Int = 1
)

data class Friendship(
    val id: String,
    val friendUserId: String,
    val friendDisplayName: String,
    val friendAvatarUrl: String? = null,
    val status: FriendStatus = FriendStatus.Accepted
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
    val inviterName: String? = null
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
    val memberCount: Int = 1
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
    val displayName: String,
    val initials: String,
    val avatarUrl: String? = null,
    val friendCode: String = "",
    val deviceCount: Int = 1,
    val totalMinutes: Int,
    val deltaMinutes: Int,
    val apps: List<AppUsage>,
    val summary: String
)

data class AppUsage(
    val name: String,
    val minutes: Int,
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
