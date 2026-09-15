package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.data.repository.FriendChatRepository
import com.example.ui.components.BrandGradient
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentPink
import com.example.ui.theme.PrimaryIndigo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    repository: FriendChatRepository,
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToChat: (User) -> Unit,
    onStartVoiceCall: (User) -> Unit,
    onStartVideoCall: (User) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val allUsers by repository.users.collectAsState()
    val allPosts by repository.posts.collectAsState()

    val isSelf = userId == currentUser?.id || userId == "self"
    val user = if (isSelf) currentUser else allUsers.find { it.id == userId }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User not found", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val userPosts = allPosts.filter { it.authorId == user.id }
    val friendshipStatus = if (!isSelf) repository.getFriendshipStatus(user.id) else "self"

    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "@${user.username}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    if (!isSelf) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("profile_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isSelf) {
                        IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("profile_settings_button")) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    } else {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Report User") },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Block User", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showBlockConfirmDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Profile Info Header spanning all 3 columns
            item(span = { GridItemSpan(3) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    // Avatar & Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UserAvatar(
                            avatarUrl = user.avatarUrl,
                            displayName = user.fullName,
                            size = 80.dp,
                            isOnline = user.isOnline,
                            hasStoryRing = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatItem(count = userPosts.size, label = "Posts")
                            ProfileStatItem(
                                count = user.friendsCount,
                                label = "Friends",
                                modifier = Modifier.clickable { onNavigateToFriends() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name, Status & Bio
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (user.isOnline) Color(0xFF10B981) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (user.isOnline) "Active Now" else "Last seen ${formatLastSeen(user.lastSeen)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (user.bio.isNotBlank()) {
                        Text(
                            text = user.bio,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = "Gender: ${user.gender}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons Row
                    if (isSelf) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = onNavigateToEditProfile,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("edit_profile_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Friend Status Button
                            when (friendshipStatus) {
                                "friends" -> {
                                    OutlinedButton(
                                        onClick = { repository.removeFriend(user.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Friends")
                                    }
                                }
                                "pending_sent" -> {
                                    Button(
                                        onClick = { repository.cancelFriendRequest(user.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Text("Requested", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { repository.sendFriendRequest(user) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        modifier = Modifier.weight(1f).height(42.dp).testTag("profile_add_friend_button")
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Friend")
                                    }
                                }
                            }

                            // Message
                            Button(
                                onClick = { onNavigateToChat(user) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.weight(1f).height(42.dp).testTag("profile_message_button")
                            ) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                            }

                            // Voice Call
                            FilledTonalIconButton(
                                onClick = { onStartVoiceCall(user) },
                                modifier = Modifier.size(42.dp).testTag("profile_voice_call_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Voice Call", modifier = Modifier.size(20.dp))
                            }

                            // Video Call
                            FilledTonalIconButton(
                                onClick = { onStartVideoCall(user) },
                                modifier = Modifier.size(42.dp).testTag("profile_video_call_button")
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = "Video Call", modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = PrimaryIndigo)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Posts & Media", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Grid items for posts
            items(userPosts) { post ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (post.mediaUrl.isNotBlank()) {
                        AsyncImage(
                            model = post.mediaUrl,
                            contentDescription = post.caption,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report @${user.username}") },
            text = {
                Column {
                    Text("Please specify why you are reporting this user:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Spam, harassment, inappropriate content...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            repository.reportUser(user.id, reportReason)
                            showReportDialog = false
                            reportReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Block Confirmation Dialog
    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = { Text("Block @${user.username}?") },
            text = {
                Text("They will not be able to message you, call you, or see your posts. You will be removed from each other's friends list.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.blockUser(user.id)
                        showBlockConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Block", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun EditProfileScreen(
    repository: FriendChatRepository,
    onNavigateBack: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val user = currentUser ?: return

    var fullName by remember { mutableStateOf(user.fullName) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio) }
    var gender by remember { mutableStateOf(user.gender) }
    var avatarUrl by remember { mutableStateOf(user.avatarUrl) }

    val sampleAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80"
    )

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    TextButton(
                        onClick = {
                            repository.updateProfile(fullName.trim(), username.trim(), bio.trim(), gender, avatarUrl)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryIndigo)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar with photo change icon
            Box(contentAlignment = Alignment.BottomEnd) {
                UserAvatar(avatarUrl = avatarUrl, displayName = fullName, size = 96.dp)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo)
                        .clickable {
                            // Cycle through sample avatars or pick new
                            val next = sampleAvatars[(sampleAvatars.indexOf(avatarUrl) + 1).coerceAtLeast(0) % sampleAvatars.size]
                            avatarUrl = next
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap camera icon to change avatar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("edit_fullname_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                prefix = { Text("@") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("edit_username_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("edit_bio_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender / Pronouns (Optional)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileStatItem(count: Int, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatLastSeen(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
