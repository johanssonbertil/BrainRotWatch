package com.fishforandre.brainrotwatch.data

import com.fishforandre.brainrotwatch.FriendStatus
import com.fishforandre.brainrotwatch.Friendship
import com.fishforandre.brainrotwatch.UserScreenTime
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface FriendRepository {
    suspend fun sendFriendRequestByCode(friendCode: String): Result<Unit>
    suspend fun acceptFriendRequest(friendshipId: String): Result<Unit>
    suspend fun rejectFriendRequest(friendshipId: String): Result<Unit>
    fun getFriendshipsFlow(): Flow<List<Friendship>>
    suspend fun getFriendsLeaderboard(): List<UserScreenTime>
}

class SupabaseFriendRepository : FriendRepository {

    override suspend fun sendFriendRequestByCode(friendCode: String): Result<Unit> {
        val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val targetUsers = supabase.from("users")
                .select {
                    filter {
                        eq("friend_code", friendCode.uppercase().trim())
                    }
                }.decodeList<UserProfileDataDto>()

            if (targetUsers.isEmpty()) {
                return Result.failure(IllegalArgumentException("Invalid friend code"))
            }

            val targetUser = targetUsers.first()
            if (targetUser.id == currentUserId) {
                return Result.failure(IllegalArgumentException("You cannot add yourself as a friend"))
            }

            val dto = FriendRequestDto(
                userId = currentUserId,
                friendId = targetUser.id,
                status = "PENDING"
            )

            supabase.from("user_friends").insert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> {
        return try {
            supabase.from("user_friends").update(
                mapOf("status" to "ACCEPTED")
            ) {
                filter {
                    eq("id", friendshipId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectFriendRequest(friendshipId: String): Result<Unit> {
        return try {
            supabase.from("user_friends").delete {
                filter {
                    eq("id", friendshipId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFriendshipsFlow(): Flow<List<Friendship>> = flow {
        val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: return@flow
        try {
            val list = supabase.from("user_friends")
                .select {
                    filter {
                        or {
                            eq("user_id", currentUserId)
                            eq("friend_id", currentUserId)
                        }
                    }
                }.decodeList<FriendshipDto>()

            val result = list.map { dto ->
                val otherUserId = if (dto.userId == currentUserId) dto.friendId else dto.userId
                Friendship(
                    id = dto.id ?: "",
                    friendUserId = otherUserId,
                    friendDisplayName = "Friend ($otherUserId)",
                    status = when (dto.status) {
                        "ACCEPTED" -> FriendStatus.Accepted
                        "PENDING" -> FriendStatus.Pending
                        else -> FriendStatus.Blocked
                    }
                )
            }
            emit(result)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFriendsLeaderboard(): List<UserScreenTime> {
        return emptyList()
    }
}

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class FriendRequestDto(
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String,
    val status: String
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class FriendshipDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String,
    val status: String
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class UserProfileDataDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("friend_code") val friendCode: String = ""
)
