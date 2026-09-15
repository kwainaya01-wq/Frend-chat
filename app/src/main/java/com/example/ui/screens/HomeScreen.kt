package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.FriendChatRepository
import com.example.ui.components.FriendChatTopBar
import com.example.ui.components.SocialPostCard
import com.example.ui.components.SuggestedUserCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.PrimaryIndigo

@Composable
fun HomeScreen(
    repository: FriendChatRepository,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToChat: (User) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val users by repository.users.collectAsState()
    val friendIds by repository.friendIds.collectAsState()
    val posts by repository.posts.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var newPostCaption by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            FriendChatTopBar(
                title = "Friend Chat",
                unreadNotificationsCount = unreadNotifs,
                onNotificationsClick = onNavigateToNotifications,
                onSearchClick = onNavigateToSearch
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = PrimaryIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("create_post_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Stories & Active Friends Carousel
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        text = "Stories & Active Friends",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // User's own story prompt
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { showCreatePostDialog = true }
                                    .testTag("add_story_item")
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    UserAvatar(
                                        avatarUrl = currentUser?.avatarUrl,
                                        displayName = currentUser?.fullName ?: "You",
                                        size = 56.dp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryIndigo),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add story",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your Story", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Friends online/stories
                        items(users) { user ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onNavigateToUserProfile(user.id) }
                                    .testTag("story_user_${user.id}")
                            ) {
                                UserAvatar(
                                    avatarUrl = user.avatarUrl,
                                    displayName = user.fullName,
                                    size = 56.dp,
                                    isOnline = user.isOnline,
                                    hasStoryRing = true
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user.fullName.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Suggested Friends Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suggested Friends",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "See All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryIndigo,
                            modifier = Modifier.clickable { onNavigateToSearch() }
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suggested = users.filter { it.id != currentUser?.id }
                        items(suggested) { user ->
                            val status = repository.getFriendshipStatus(user.id)
                            SuggestedUserCard(
                                user = user,
                                friendshipStatus = status,
                                onAddClick = {
                                    if (status == "none") {
                                        repository.sendFriendRequest(user)
                                    } else if (status == "pending_sent") {
                                        repository.cancelFriendRequest(user.id)
                                    }
                                },
                                onProfileClick = { onNavigateToUserProfile(user.id) }
                            )
                        }
                    }
                }
            }

            // Recent Activity / Social Feed
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recent Activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            items(posts) { post ->
                SocialPostCard(
                    post = post,
                    onLikeClick = { repository.toggleLikePost(post.id) },
                    onAuthorClick = { onNavigateToUserProfile(post.authorId) },
                    onCommentClick = {
                        val targetUser = users.find { it.id == post.authorId } ?: currentUser
                        if (targetUser != null && targetUser.id != currentUser?.id) {
                            onNavigateToChat(targetUser)
                        }
                    }
                )
            }
        }
    }

    // Create Post Dialog
    if (showCreatePostDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            title = { Text("Share on Friend Chat", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPostCaption,
                        onValueChange = { newPostCaption = it },
                        placeholder = { Text("What's on your mind?") },
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_caption_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPostCaption.isNotBlank()) {
                            val demoMedia = "https://images.unsplash.com/photo-1519741497674-611481863552?w=800&auto=format&fit=crop&q=80"
                            repository.createPost(newPostCaption.trim(), demoMedia)
                            newPostCaption = ""
                            showCreatePostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.testTag("post_submit_button")
                ) {
                    Text("Post", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
