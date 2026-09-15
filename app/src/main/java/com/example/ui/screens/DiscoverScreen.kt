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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.FriendChatRepository
import com.example.ui.components.FriendChatTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.PrimaryIndigo

@Composable
fun DiscoverScreen(
    repository: FriendChatRepository,
    onNavigateToNotifications: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit
) {
    val users by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val friendIds by repository.friendIds.collectAsState()
    val friendRequests by repository.friendRequests.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Online", "Suggested"

    val filteredUsers = users.filter { user ->
        if (user.id == currentUser?.id) return@filter false
        val matchesQuery = searchQuery.isBlank() ||
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.username.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Online" -> user.isOnline
            "Suggested" -> !friendIds.contains(user.id)
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            FriendChatTopBar(
                title = "Discover Friends",
                onNotificationsClick = onNavigateToNotifications
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or @username...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryIndigo)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("discover_search_input")
            )

            // Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Online", "Suggested")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Users List
            if (filteredUsers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No users found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Try searching for another name or handle.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredUsers) { user ->
                        val status = repository.getFriendshipStatus(user.id)
                        DiscoverUserItemCard(
                            user = user,
                            status = status,
                            onUserClick = { onNavigateToUserProfile(user.id) },
                            onActionClick = {
                                when (status) {
                                    "none" -> repository.sendFriendRequest(user)
                                    "pending_sent" -> repository.cancelFriendRequest(user.id)
                                    "friends" -> repository.removeFriend(user.id)
                                    "pending_received" -> {
                                        val req = friendRequests.find { it.senderId == user.id }
                                        if (req != null) repository.acceptFriendRequest(req)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverUserItemCard(
    user: User,
    status: String,
    onUserClick: () -> Unit,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
            .testTag("discover_user_${user.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = user.avatarUrl,
                displayName = user.fullName,
                size = 50.dp,
                isOnline = user.isOnline,
                hasStoryRing = true
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.bio.isNotBlank()) {
                    Text(
                        text = user.bio,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (status) {
                "friends" -> {
                    OutlinedButton(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Friends", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "pending_sent" -> {
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Requested", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                "pending_received" -> {
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
