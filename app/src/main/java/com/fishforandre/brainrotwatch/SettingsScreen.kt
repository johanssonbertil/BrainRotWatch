package com.fishforandre.brainrotwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishforandre.brainrotwatch.ui.theme.AppBackgroundGradient

@Composable
fun UserSettingsPage(
    initialName: String = "Johan",
    initialMaxScreenTimeMinutes: String = "180",
    onBack: () -> Unit = {},
) {
    var name by remember { mutableStateOf(initialName) }
    var maxScreenTime by remember { mutableStateOf(initialMaxScreenTimeMinutes) }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = AppBackgroundGradient
                    )
                )
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
                Text(
                    text = "User settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Display name") },
                            shape = RoundedCornerShape(18.dp)
                        )
                        OutlinedTextField(
                            value = maxScreenTime,
                            onValueChange = { maxScreenTime = it.filter { char -> char.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Maximum screen time in minutes") },
                            shape = RoundedCornerShape(18.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { }, shape = RoundedCornerShape(999.dp)) {
                                Text("Change profile picture")
                            }
                            Button(onClick = { }, shape = RoundedCornerShape(999.dp)) {
                                Text("Save changes")
                            }
                        }
                    }
                }
            }
        }
    }
}
