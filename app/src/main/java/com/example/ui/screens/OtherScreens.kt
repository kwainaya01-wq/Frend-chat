package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.repository.FriendChatRepository
import com.example.ui.components.FriendChatTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.PrimaryIndigo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    repository: FriendChatRepository,
    onNavigateBack: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit
) {
    val notifications by repository.notifications.collectAsState()

    Scaffold(
        topBar = {
            FriendChatTopBar(
                title = "Notifications",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { repository.clearAllNotifications() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No notifications yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Friend requests, messages, and calls will show up here.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    NotificationItemCard(
                        notification = notif,
                        onClick = {
                            repository.markNotificationAsRead(notif.id)
                            onNotificationClick(notif)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        AppNotification.TYPE_FRIEND_REQUEST -> Icons.Default.PersonAdd
        AppNotification.TYPE_REQUEST_ACCEPTED -> Icons.Default.CheckCircle
        AppNotification.TYPE_NEW_MESSAGE -> Icons.Default.ChatBubble
        AppNotification.TYPE_VOICE_CALL -> Icons.Default.Call
        AppNotification.TYPE_VIDEO_CALL -> Icons.Default.Videocam
        else -> Icons.Default.Notifications
    }

    val iconColor = when (notification.type) {
        AppNotification.TYPE_FRIEND_REQUEST -> PrimaryIndigo
        AppNotification.TYPE_REQUEST_ACCEPTED -> Color(0xFF10B981)
        AppNotification.TYPE_NEW_MESSAGE -> AccentCyan
        AppNotification.TYPE_VOICE_CALL -> Color(0xFF38BDF8)
        AppNotification.TYPE_VIDEO_CALL -> AccentPink
        else -> PrimaryIndigo
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_item_${notification.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = notification.senderAvatar,
                displayName = notification.senderName,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = notification.body,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = formatNotificationTime(notification.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    repository: FriendChatRepository,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val blockedIds by repository.blockedUserIds.collectAsState()

    Scaffold(
        topBar = {
            FriendChatTopBar(
                title = "Settings",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Appearance
            Text("Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = PrimaryIndigo)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Dark Mode", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy & Safety
            Text("Privacy & Safety", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Blocked Users", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                        Text("${blockedIds.size} users", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryIndigo)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("End-to-End Privacy Protection", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Information
            Text("About Friend Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Version", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.0.0 (Sketchware Pro Compatible)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Backend", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Firebase & Agora RTC", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            OutlinedButton(
                onClick = {
                    repository.logout()
                    onLogout()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
