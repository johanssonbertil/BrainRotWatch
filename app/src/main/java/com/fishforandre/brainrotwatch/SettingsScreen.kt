package com.fishforandre.brainrotwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishforandre.brainrotwatch.domain.model.CardCosmetics
import com.fishforandre.brainrotwatch.domain.model.MasterCatalog
import com.fishforandre.brainrotwatch.domain.model.ThemeId
import com.fishforandre.brainrotwatch.domain.model.UserInventory
import com.fishforandre.brainrotwatch.ui.theme.AppBackgroundGradient

/**
 * Inventory & Loadout Manager Screen:
 * - Manage Global User Card Loadout (background, border, nameplate)
 * - Configure Per-Community Card Overrides (including earned Group Pact Trophy borders)
 * - Permanent Gamification Trophies & Milestones
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryAndLoadoutScreen(
    inventory: UserInventory,
    availableCommunities: List<String>,
    onBack: () -> Unit,
    onAssignPatronTheme: ((ThemeId, String) -> Result<Unit>)? = null,
    onUnequipPatronTheme: ((ThemeId) -> Unit)? = null,
    onUpdateGlobalLoadout: (CardCosmetics) -> Unit,
    onUpdateCommunityOverride: (String, CardCosmetics?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val nonFriendCommunities = remember(availableCommunities) {
        availableCommunities.filter { !it.equals("Friends", ignoreCase = true) }
    }
    var selectedOverrideCommunity by remember(nonFriendCommunities) {
        mutableStateOf(nonFriendCommunities.firstOrNull() ?: "No-Scroll Squad")
    }
    var statusFeedback by remember { mutableStateOf<String?>(null) }

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
                            text = "Inventory & Loadouts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Permanent Account Cosmetics & Custom Card Loadouts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                statusFeedback?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 1. Global Default User Card Loadout
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
                            text = "Global Default User Card Loadout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Shown on your Friends List and any Community without a custom override. Note: Active Progressive Brain Rot frames always override custom borders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        MasterCatalog.availableGlobalCardPresets.forEach { preset ->
                            val isSelected = inventory.loadout.globalDefaultLoadout.backgroundId == preset.backgroundId
                            LoadoutPresetRow(
                                preset = preset,
                                isSelected = isSelected,
                                onSelect = {
                                    onUpdateGlobalLoadout(preset)
                                    statusFeedback = "Updated Global User Card to '${preset.backgroundName}'."
                                    android.widget.Toast.makeText(context, "Global Card Loadout updated!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // 3. Per-Community Card Loadout Overrides & Trophy Cosmetics
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
                            text = "Per-Community Card Overrides & Trophies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Flex earned Group Pact Trophy borders in specific Communities or customize per-group card styles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            nonFriendCommunities.forEach { commName ->
                                FilterChip(
                                    selected = selectedOverrideCommunity == commName,
                                    onClick = { selectedOverrideCommunity = commName },
                                    label = { Text(commName) }
                                )
                            }
                        }

                        val currentOverride = inventory.loadout.communityOverrides[selectedOverrideCommunity]

                        Text(
                            text = if (currentOverride != null) {
                                "Active Override for $selectedOverrideCommunity: ${currentOverride.backgroundName} (${currentOverride.nameplateTitle ?: "No Title"})"
                            } else {
                                "Using Global Default Loadout for $selectedOverrideCommunity"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Group-Bound Earned Trophies for this Community
                        val boundTrophies = inventory.earnedCommunityTrophies.filter {
                            it.boundCommunityId.equals(selectedOverrideCommunity, ignoreCase = true)
                        }
                        if (boundTrophies.isNotEmpty()) {
                            Text(
                                text = "Earned Group Pact Trophies ($selectedOverrideCommunity only):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            boundTrophies.forEach { trophy ->
                                val trophyCard = CardCosmetics(
                                    backgroundId = "trophy_${trophy.cosmeticId}",
                                    backgroundName = trophy.name,
                                    backgroundGradientHex = listOf(0xFFFFF8E1, 0xFFFFECB3),
                                    borderId = trophy.cosmeticId,
                                    borderName = trophy.name,
                                    borderColorHex = trophy.borderColorHex,
                                    nameplateTitle = trophy.titleBadge
                                )
                                val isEquipped = currentOverride?.borderId == trophy.cosmeticId
                                LoadoutPresetRow(
                                    preset = trophyCard,
                                    isSelected = isEquipped,
                                    badgeText = trophy.requirement.label,
                                    onSelect = {
                                        onUpdateCommunityOverride(selectedOverrideCommunity, trophyCard)
                                        statusFeedback = "Equipped Trophy '${trophy.name}' in $selectedOverrideCommunity!"
                                    }
                                )
                            }
                        }

                        MasterCatalog.availableGlobalCardPresets.forEach { preset ->
                            val isSelected = currentOverride?.backgroundId == preset.backgroundId
                            LoadoutPresetRow(
                                preset = preset,
                                isSelected = isSelected,
                                onSelect = {
                                    onUpdateCommunityOverride(selectedOverrideCommunity, preset)
                                    statusFeedback = "Set $selectedOverrideCommunity override to '${preset.backgroundName}'."
                                }
                            )
                        }

                        if (currentOverride != null) {
                            OutlinedButton(
                                onClick = {
                                    onUpdateCommunityOverride(selectedOverrideCommunity, null)
                                    statusFeedback = "Cleared custom override for $selectedOverrideCommunity (reverted to Global Default)."
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape
                            ) {
                                Text("Clear Override (Use Global Default)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadoutPresetRow(
    preset: CardCosmetics,
    isSelected: Boolean,
    badgeText: String? = null,
    onSelect: () -> Unit
) {
    val borderColor = preset.borderColorHex?.let { Color(it) } ?: MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (preset.backgroundGradientHex.size >= 2) {
                    Brush.horizontalGradient(preset.backgroundGradientHex.map { Color(it) })
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            )
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) LogoTurquoise else borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = preset.backgroundName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2040)
                )
                preset.nameplateTitle?.let { title ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E2040).copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2040)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badgeText ?: "Border: ${preset.borderName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF35394F)
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Equipped",
                tint = LogoTurquoise
            )
        } else {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Select",
                tint = Color(0xFF6D7390).copy(alpha = 0.5f)
            )
        }
    }
}
