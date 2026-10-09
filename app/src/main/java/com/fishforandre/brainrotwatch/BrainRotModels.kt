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
    val maxScreenTimeMinutes: Int = 120
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

object SampleBrainRotWatchData {
    val homeDashboard = HomeDashboard(
        community = CommunitySummary(
            name = "The Family",
            visibility = CommunityAccessType.Closed,
            previewMembers = listOf(
                CommunityPreviewMember(
                    displayName = "Member A",
                    note = "Placeholder community data for now",
                    placeholderLabel = "Pending"
                ),
                CommunityPreviewMember(
                    displayName = "Member B",
                    note = "Placeholder community data for now",
                    placeholderLabel = "Pending"
                ),
                CommunityPreviewMember(
                    displayName = "Member C",
                    note = "Placeholder community data for now",
                    placeholderLabel = "Pending"
                )
            )
        ),
        currentUser = UserScreenTime(
            displayName = "Johan",
            initials = "J",
            totalMinutes = 142,
            deltaMinutes = -12,
            apps = listOf(
                AppUsage(
                    name = "Instagram Reels",
                    minutes = 48,
                    fillFraction = 0.79f,
                    accentColor = Color(0xFF6460FF),
                    trackColor = Color(0xFFD9E7F5)
                ),
                AppUsage(
                    name = "TikTok",
                    minutes = 61,
                    fillFraction = 1.0f,
                    accentColor = Color(0xFF23C8D7),
                    trackColor = Color(0xFFD9E7F5)
                ),
                AppUsage(
                    name = "YouTube Shorts",
                    minutes = 33,
                    fillFraction = 0.54f,
                    accentColor = Color(0xFFFF6D84),
                    trackColor = Color(0xFFD9E7F5)
                )
            ),
            summary = "142 min of screen time today: 61 on TikTok, 48 on Instagram Reels, and 33 on YouTube Shorts."
        ),
        unreadNotifications = 4
    )
}
