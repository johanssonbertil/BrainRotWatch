package com.fishforandre.brainrotwatch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.fishforandre.brainrotwatch.ui.theme.AppBackgroundGradient
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CommunityManageScreen(
    communityName: String,
    notifications: CommunityNotificationPreferences,
    onBack: () -> Unit,
    onInvitePeople: () -> Unit,
    onDailySummaryChange: (Boolean) -> Unit,
    onThresholdAlertsChange: (Boolean) -> Unit,
    onApprovalRequiredChange: (Boolean) -> Unit,
) {
    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "Manage $communityName",
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
                        Text(
                            text = "Notification Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        PreferenceToggleRow(
                            title = "Daily Summary",
                            subtitle = "Notify when top and bottom scroll time is recorded",
                            checked = notifications.dailySummaryEnabled,
                            onCheckedChange = onDailySummaryChange
                        )

                        PreferenceToggleRow(
                            title = "Limit Threshold Alerts",
                            subtitle = "Notify when someone exceeds max screen time",
                            checked = notifications.thresholdAlertsEnabled,
                            onCheckedChange = onThresholdAlertsChange
                        )

                        PreferenceToggleRow(
                            title = "Require Admin Approval",
                            subtitle = "New members require admin approval to join",
                            checked = notifications.approvalRequiredEnabled,
                            onCheckedChange = onApprovalRequiredChange
                        )

                        Button(
                            onClick = onInvitePeople,
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape
                        ) {
                            Text("Invite People")
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
    onCreateCommunity: (String) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    // Automatically clear focus (and un-highlight text box) when soft keyboard is hidden
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (nameText.isNotBlank()) {
                                    onCreateCommunity(nameText.trim())
                                }
                            },
                            enabled = nameText.isNotBlank(),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Create", fontWeight = FontWeight.Bold)
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
        Column(modifier = Modifier.weight(1f)) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSettingsScreen(
    initialName: String = "Johan",
    initialAvatarUrl: String? = null,
    initialMaxScreenTimeMinutes: String = "180",
    authRepository: com.fishforandre.brainrotwatch.data.AuthRepository? = null,
    onBack: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onSaveProfile: (String, String?) -> Unit = { _, _ -> },
    onMaxScreenTimeChange: (String) -> Unit = {},
    onDeleteAccount: suspend () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    // Automatically clear focus (and un-highlight text box) when soft keyboard is hidden
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
            title = {
                Text(
                    text = "Delete Account?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
            },
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
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
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
                        text = "User Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                        // Avatar Photo Circle Preview
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

                        // Photo Buttons
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
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Maximum screen time (minutes)") },
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true
                        )

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
                                                statusMessage = "Failed to save profile changes."
                                            }
                                        }
                                    }
                                },
                                enabled = name.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkButtonContainer, contentColor = Color.White)
                            ) {
                                Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
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

@Composable
fun EventsScreen(
    unreadCount: Int,
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                        text = "Notifications & Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

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
                            text = "• Alex exceeded daily limit (185 min today)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Daily summary: Johan had the lowest screen time yesterday!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
