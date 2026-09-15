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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FriendRequest
import com.example.data.model.User
import com.example.data.repository.FriendChatRepository
import com.example.ui.components.FriendChatTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentPink
import com.example.ui.theme.PrimaryIndigo

@Composable
fun FriendsScreen(
    repository: FriendChatRepository,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToChat: (User) -> Unit,
    onStartVoiceCall: (User) -> Unit,
    onStartVideoCall: (User) -> Unit
) {
    val users by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val friendIds by repository.friendIds.collectAsState()
    val friendRequests by repository.friendRequests.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Friends, 1: Requests
    var searchQuery by remember { mutableStateOf("") }

    val myFriends = users.filter { friendIds.contains(it.id) }.filter {
        searchQuery.isBlank() ||
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true)
    }

    val incomingRequests = friendRequests.filter { it.receiverId == currentUser?.id }

    Scaffold(
        topBar = {
            FriendChatTopBar(
                title = "Friends",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: Friends vs Friend Requests
            TabRow(
                selectedTabIndex = selectedTabIndex,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryIndigo
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "My Friends (${myFriends.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 0) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Requests",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == 1) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (incomingRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = AccentPink) {
                                    Text(incomingRequests.size.toString(), color = Color.White)
                                }
                            }
                        }
                    }
                )
            }

            if (selectedTabIndex == 0) {
                // Search friends
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your friends...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("friends_search_input")
                )

                if (myFriends.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No friends to display yet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Head over to Discover to connect with new friends!", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(myFriends) { friend ->
                            FriendItemCard(
                                friend = friend,
                                onProfileClick = { onNavigateToUserProfile(friend.id) },
                                onMessageClick = { onNavigateToChat(friend) },
                                onVoiceCallClick = { onStartVoiceCall(friend) },
                                onVideoCallClick = { onStartVideoCall(friend) }
                            )
                        }
                    }
                }
            } else {
                // Requests Tab
                if (incomingRequests.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No pending friend requests", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("When people want to be friends, they will appear here.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(incomingRequests) { req ->
                            FriendRequestItemCard(
                                request = req,
                                onAccept = { repository.acceptFriendRequest(req) },
                                onReject = { repository.rejectFriendRequest(req) },
                                onProfileClick = { onNavigateToUserProfile(req.senderId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItemCard(
    friend: User,
    onProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .testTag("friend_card_${friend.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = friend.avatarUrl,
                displayName = friend.fullName,
                size = 50.dp,
                isOnline = friend.isOnline
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (friend.isOnline) "Active now" else "@${friend.username}",
                    fontSize = 12.sp,
                    color = if (friend.isOnline) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (friend.isOnline) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Quick Actions: Message, Voice Call, Video Call
            FilledTonalIconButton(
                onClick = onMessageClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.size(38.dp).testTag("message_button_${friend.id}")
            ) {
                Icon(Icons.Default.ChatBubble, contentDescription = "Chat", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            FilledTonalIconButton(
                onClick = onVoiceCallClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(38.dp).testTag("voice_call_button_${friend.id}")
            ) {
                Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            FilledTonalIconButton(
                onClick = onVideoCallClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(38.dp).testTag("video_call_button_${friend.id}")
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FriendRequestItemCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("request_item_${request.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onProfileClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    avatarUrl = request.senderAvatar,
                    displayName = request.senderName,
                    size = 48.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = request.senderName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "@${request.senderUsername}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("accept_request_${request.id}")
                ) {
                    Text("Accept", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("reject_request_${request.id}")
                ) {
                    Text("Decline")
                }
            }
        }
    }
}
