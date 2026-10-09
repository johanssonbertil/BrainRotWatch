package com.fishforandre.brainrotwatch.data

import com.fishforandre.brainrotwatch.Community
import com.fishforandre.brainrotwatch.CommunityAccessType
import com.fishforandre.brainrotwatch.CommunityJoinRequest
import com.fishforandre.brainrotwatch.RequestStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CommunityRepository {
    suspend fun createCommunity(name: String, accessType: CommunityAccessType): Result<Community>
    suspend fun joinCommunityByInviteCode(inviteCode: String): Result<String>
    suspend fun getUserCommunities(): List<Community>
    suspend fun getCommunityJoinRequests(communityId: String): List<CommunityJoinRequest>
    suspend fun castDemocraticVote(requestId: String, approve: Boolean): Result<Unit>
}

class SupabaseCommunityRepository : CommunityRepository {

    override suspend fun createCommunity(name: String, accessType: CommunityAccessType): Result<Community> {
        val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val dto = CreateCommunityDto(
                name = name,
                accessType = when (accessType) {
                    CommunityAccessType.Open -> "OPEN"
                    CommunityAccessType.Closed -> "CLOSED"
                    CommunityAccessType.Democratic -> "DEMOCRATIC"
                },
                createdBy = currentUserId
            )

            val inserted = supabase.from("communities")
                .insert(dto) { select() }
                .decodeSingle<CommunityResponseDto>()

            // Add creator as ADMIN member
            val memberDto = CommunityMemberDto(
                communityId = inserted.id,
                userId = currentUserId,
                role = "ADMIN"
            )
            supabase.from("community_members").insert(memberDto)

            Result.success(
                Community(
                    id = inserted.id,
                    name = inserted.name,
                    inviteCode = inserted.inviteCode,
                    accessType = accessType,
                    memberCount = 1
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinCommunityByInviteCode(inviteCode: String): Result<String> {
        val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val communities = supabase.from("communities")
                .select {
                    filter {
                        eq("invite_code", inviteCode.trim())
                    }
                }.decodeList<CommunityResponseDto>()

            if (communities.isEmpty()) {
                return Result.failure(IllegalArgumentException("Invalid community invite code"))
            }

            val targetCommunity = communities.first()

            when (targetCommunity.accessType) {
                "OPEN" -> {
                    val memberDto = CommunityMemberDto(
                        communityId = targetCommunity.id,
                        userId = currentUserId,
                        role = "MEMBER"
                    )
                    supabase.from("community_members").insert(memberDto)
                    Result.success("Joined community ${targetCommunity.name}!")
                }
                "CLOSED", "DEMOCRATIC" -> {
                    val requestDto = JoinRequestDto(
                        communityId = targetCommunity.id,
                        applicantId = currentUserId,
                        status = "PENDING"
                    )
                    supabase.from("community_requests").insert(requestDto)
                    Result.success("Request submitted! Waiting for admission approval.")
                }
                else -> Result.failure(IllegalStateException("Unknown access type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserCommunities(): List<Community> {
        val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: return emptyList()
        return try {
            val memberships = supabase.from("community_members")
                .select {
                    filter {
                        eq("user_id", currentUserId)
                    }
                }.decodeList<CommunityMemberDto>()

            val communityIds = memberships.map { it.communityId }
            if (communityIds.isEmpty()) return emptyList()

            val list = supabase.from("communities")
                .select {
                    filter {
                        isIn("id", communityIds)
                    }
                }.decodeList<CommunityResponseDto>()

            list.map { dto ->
                Community(
                    id = dto.id,
                    name = dto.name,
                    inviteCode = dto.inviteCode,
                    accessType = when (dto.accessType) {
                        "OPEN" -> CommunityAccessType.Open
                        "CLOSED" -> CommunityAccessType.Closed
                        else -> CommunityAccessType.Democratic
                    }
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getCommunityJoinRequests(communityId: String): List<CommunityJoinRequest> {
        return try {
            val list = supabase.from("community_requests")
                .select {
                    filter {
                        eq("community_id", communityId)
                        eq("status", "PENDING")
                    }
                }.decodeList<JoinRequestDto>()

            list.map { dto ->
                CommunityJoinRequest(
                    id = dto.id ?: "",
                    communityId = dto.communityId,
                    applicantId = dto.applicantId,
                    applicantName = "Applicant (${dto.applicantId.take(6)})",
                    status = RequestStatus.Pending
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun castDemocraticVote(requestId: String, approve: Boolean): Result<Unit> {
        val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val voteDto = DemocraticVoteDto(
                requestId = requestId,
                voterId = currentUserId,
                vote = if (approve) "APPROVE" else "REJECT"
            )
            supabase.from("community_votes").insert(voteDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class CreateCommunityDto(
    val name: String,
    @SerialName("access_type") val accessType: String,
    @SerialName("created_by") val createdBy: String
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class CommunityResponseDto(
    val id: String,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("access_type") val accessType: String
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class CommunityMemberDto(
    @SerialName("community_id") val communityId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "MEMBER"
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class JoinRequestDto(
    val id: String? = null,
    @SerialName("community_id") val communityId: String,
    @SerialName("applicant_id") val applicantId: String,
    val status: String = "PENDING"
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class DemocraticVoteDto(
    @SerialName("request_id") val requestId: String,
    @SerialName("voter_id") val voterId: String,
    val vote: String
)
