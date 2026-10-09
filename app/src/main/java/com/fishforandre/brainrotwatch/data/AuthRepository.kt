package com.fishforandre.brainrotwatch.data

import android.content.Context
import android.net.Uri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fishforandre.brainrotwatch.UserProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object Loading : AuthState()
    object SignedOut : AuthState()
    data class SignedIn(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun signOut()
    suspend fun deleteAccount(): Boolean
    suspend fun getCurrentUserProfile(): UserProfile?
    suspend fun hasDeviceAccount(): Boolean
    suspend fun signInWithDevice(): Boolean
    suspend fun registerDeviceAccount(displayName: String, avatarUrl: String?): UserProfile?
}

class SupabaseAuthRepository(private val context: Context? = null) : AuthRepository {

    private val PREFS_NAME = "device_auth_prefs"
    private val KEY_DEVICE_TOKEN = "device_login_token"
    private val KEY_DEVICE_USER_ID = "device_user_id"
    private val KEY_DEVICE_USER_NAME = "device_user_name"
    private val KEY_DEVICE_AVATAR_URL = "device_avatar_url"
    private val KEY_DEVICE_FRIEND_CODE = "device_friend_code"
    private val KEY_DEVICE_SIGNED_IN = "device_signed_in"

    private fun getPrefs(): android.content.SharedPreferences? {
        if (context == null) return null
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secure_device_auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private suspend fun uploadAvatarToStorage(userId: String, localAvatarUriString: String): String? {
        if (context == null) return null
        return try {
            val uri = Uri.parse(localAvatarUriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.use { it.readBytes() }
            if (bytes.isEmpty()) return null

            val filename = "avatar_${userId}_${System.currentTimeMillis()}.jpg"
            val bucket = supabase.storage.from("avatars")

            bucket.upload(filename, bytes, upsert = true)
            bucket.publicUrl(filename)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error uploading avatar to Supabase Storage: ${e.message}", e)
            null
        }
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    suspend fun initialize() {
        try {
            if (hasDeviceAccount()) {
                val success = signInWithDevice()
                if (success) return
            }

            _authState.value = AuthState.SignedOut
        } catch (_: Exception) {
            _authState.value = AuthState.SignedOut
        }
    }

    override suspend fun hasDeviceAccount(): Boolean {
        val prefs = getPrefs() ?: return false
        val token = prefs.getString(KEY_DEVICE_TOKEN, null)
        val userId = prefs.getString(KEY_DEVICE_USER_ID, null)
        return !token.isNullOrEmpty() || !userId.isNullOrEmpty()
    }

    override suspend fun signInWithDevice(): Boolean {
        _authState.value = AuthState.Loading
        val prefs = getPrefs()
        val userId = prefs?.getString(KEY_DEVICE_USER_ID, null)
        val deviceToken = prefs?.getString(KEY_DEVICE_TOKEN, null)

        if (userId.isNullOrEmpty() && deviceToken.isNullOrEmpty()) {
            _authState.value = AuthState.SignedOut
            return false
        }

        val effectiveUserId = userId ?: deviceToken ?: "device_user"
        val cachedName = prefs?.getString(KEY_DEVICE_USER_NAME, "Device User") ?: "Device User"
        val cachedAvatar = prefs?.getString(KEY_DEVICE_AVATAR_URL, null)
        val cachedFriendCode = prefs?.getString(KEY_DEVICE_FRIEND_CODE, null) ?: generateFriendCode()

        val profile = fetchOrCreateUserProfile(
            userId = effectiveUserId,
            fallbackName = cachedName,
            avatarUrl = cachedAvatar
        ) ?: UserProfile(
            id = effectiveUserId,
            displayName = cachedName,
            avatarUrl = cachedAvatar,
            friendCode = cachedFriendCode,
            maxScreenTimeMinutes = 120
        )

        prefs?.edit()?.putBoolean(KEY_DEVICE_SIGNED_IN, true)?.apply()
        _authState.value = AuthState.SignedIn(profile)
        return true
    }

    override suspend fun registerDeviceAccount(displayName: String, avatarUrl: String?): UserProfile? {
        _authState.value = AuthState.Loading
        val prefs = getPrefs()
        var userId = prefs?.getString(KEY_DEVICE_USER_ID, null)
        if (userId.isNullOrEmpty()) {
            userId = "device_${java.util.UUID.randomUUID().toString().take(8)}"
        }

        var deviceToken = prefs?.getString(KEY_DEVICE_TOKEN, null)
        if (deviceToken.isNullOrEmpty()) {
            deviceToken = "brw_tok_${java.util.UUID.randomUUID()}"
        }

        val friendCode = prefs?.getString(KEY_DEVICE_FRIEND_CODE, null) ?: generateFriendCode()

        var publicAvatarUrl: String? = avatarUrl
        if (!avatarUrl.isNullOrEmpty() && (avatarUrl.startsWith("file:") || avatarUrl.startsWith("content:"))) {
            val uploadedUrl = uploadAvatarToStorage(userId, avatarUrl)
            if (!uploadedUrl.isNullOrEmpty()) {
                publicAvatarUrl = uploadedUrl
            }
        }

        prefs?.edit()
            ?.putString(KEY_DEVICE_TOKEN, deviceToken)
            ?.putString(KEY_DEVICE_USER_ID, userId)
            ?.putString(KEY_DEVICE_USER_NAME, displayName)
            ?.putString(KEY_DEVICE_AVATAR_URL, publicAvatarUrl ?: avatarUrl)
            ?.putString(KEY_DEVICE_FRIEND_CODE, friendCode)
            ?.putBoolean(KEY_DEVICE_SIGNED_IN, true)
            ?.apply()

        val profile = fetchOrCreateUserProfile(
            userId = userId,
            fallbackName = displayName,
            avatarUrl = publicAvatarUrl ?: avatarUrl
        ) ?: UserProfile(
            id = userId,
            displayName = displayName,
            avatarUrl = publicAvatarUrl ?: avatarUrl,
            friendCode = friendCode,
            maxScreenTimeMinutes = 120
        )

        _authState.value = AuthState.SignedIn(profile)
        return profile
    }

    override suspend fun signOut() {
        getPrefs()?.edit()?.putBoolean(KEY_DEVICE_SIGNED_IN, false)?.apply()
        _authState.value = AuthState.SignedOut
    }

    override suspend fun deleteAccount(): Boolean {
        _authState.value = AuthState.Loading
        return try {
            val prefs = getPrefs()
            val userId = prefs?.getString(KEY_DEVICE_USER_ID, null)

            if (!userId.isNullOrEmpty()) {
                try {
                    supabase.from("users").delete {
                        filter {
                            eq("id", userId)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AuthRepository", "Error deleting user row from Supabase: ${e.message}", e)
                }
            }

            prefs?.edit()?.clear()?.apply()
            _authState.value = AuthState.SignedOut
            true
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to delete account")
            false
        }
    }

    override suspend fun getCurrentUserProfile(): UserProfile? {
        val prefs = getPrefs() ?: return null
        val userId = prefs.getString(KEY_DEVICE_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_DEVICE_USER_NAME, "User") ?: "User"
        val avatar = prefs.getString(KEY_DEVICE_AVATAR_URL, null)
        return fetchOrCreateUserProfile(userId, name, avatar)
    }

    private suspend fun fetchOrCreateUserProfile(
        userId: String,
        fallbackName: String,
        avatarUrl: String?
    ): UserProfile? {
        if (userId.isEmpty()) return null
        return try {
            val existing = supabase.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeList<UserProfileDto>()

            if (existing.isNotEmpty()) {
                val dto = existing.first()
                return UserProfile(
                    id = dto.id,
                    displayName = dto.display_name,
                    avatarUrl = dto.avatar_url,
                    friendCode = dto.friend_code,
                    maxScreenTimeMinutes = dto.max_screen_time_minutes
                )
            }

            // Create new profile if first login
            val newDto = UserProfileDto(
                id = userId,
                display_name = fallbackName,
                avatar_url = avatarUrl,
                friend_code = generateFriendCode(),
                max_screen_time_minutes = 120
            )

            supabase.from("users").insert(newDto)

            UserProfile(
                id = newDto.id,
                displayName = newDto.display_name,
                avatarUrl = newDto.avatar_url,
                friendCode = newDto.friend_code,
                maxScreenTimeMinutes = newDto.max_screen_time_minutes
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun generateFriendCode(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { allowedChars.random() }.joinToString("")
    }
}

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@kotlinx.serialization.Serializable
private data class UserProfileDto(
    val id: String,
    val display_name: String,
    val avatar_url: String? = null,
    val friend_code: String = "",
    val max_screen_time_minutes: Int = 120
)
