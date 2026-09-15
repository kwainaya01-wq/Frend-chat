package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.User
import com.example.data.repository.FriendChatRepository
import com.example.ui.components.FriendChatTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.BubbleReceivedDark
import com.example.ui.theme.BubbleReceivedLight
import com.example.ui.theme.BubbleSent
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryIndigo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    repository: FriendChatRepository,
    onNavigateToChat: (User) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val conversations by repository.conversations.collectAsState()
    val users by repository.users.collectAsState()
    val friendIds by repository.friendIds.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredConversations = conversations.filter { conv ->
        val name = conv.otherUser?.fullName ?: ""
        searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            FriendChatTopBar(
                title = "Messages",
                onNotificationsClick = onNavigateToNotifications
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("chat_search_input")
            )

            if (filteredConversations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No chats yet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Connect with friends to start chatting!", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredConversations) { conversation ->
                        val target = conversation.otherUser ?: users.firstOrNull()
                        if (target != null) {
                            ChatConversationItem(
                                conversation = conversation,
                                user = target,
                                onClick = { onNavigateToChat(target) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatConversationItem(
    conversation: ChatConversation,
    user: User,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("conversation_${user.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = user.avatarUrl,
                displayName = user.fullName,
                size = 52.dp,
                isOnline = user.isOnline
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTime(conversation.lastTimestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        fontSize = 13.sp,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    repository: FriendChatRepository,
    targetUser: User,
    onNavigateBack: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onUserProfileClick: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val messages by repository.currentChatMessages.collectAsState()
    val isTypingMap by repository.isTypingMap.collectAsState()
    val chatId = "chat_${targetUser.id}"
    val isFriendTyping = isTypingMap[chatId] == true

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedMessageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        repository.loadMessagesForChat(chatId, targetUser)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onUserProfileClick() }
                    ) {
                        UserAvatar(
                            avatarUrl = targetUser.avatarUrl,
                            displayName = targetUser.fullName,
                            size = 38.dp,
                            isOnline = targetUser.isOnline
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = targetUser.fullName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isFriendTyping) "typing..." else if (targetUser.isOnline) "Online" else "Offline",
                                fontSize = 12.sp,
                                color = if (isFriendTyping) PrimaryIndigo else if (targetUser.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onStartVoiceCall,
                        modifier = Modifier.testTag("chat_voice_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = PrimaryIndigo)
                    }
                    IconButton(
                        onClick = onStartVideoCall,
                        modifier = Modifier.testTag("chat_video_call_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = PrimaryIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .imePadding()
            ) {
                // Reply Preview Bar
                AnimatedVisibility(visible = replyingToMessage != null) {
                    replyingToMessage?.let { reply ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Replying to ${reply.senderName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                                Text(reply.text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { replyingToMessage = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Chat Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            // Send sample photo in chat
                            val sampleImg = "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=800&auto=format&fit=crop&q=80"
                            repository.sendMessage(
                                chatId = chatId,
                                text = "Shared a photo 📸",
                                mediaUrl = sampleImg,
                                mediaType = ChatMessage.TYPE_IMAGE,
                                targetUser = targetUser
                            )
                        },
                        modifier = Modifier.testTag("chat_send_image_button")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Send Image", tint = PrimaryIndigo)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message...") },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                repository.sendMessage(
                                    chatId = chatId,
                                    text = inputText.trim(),
                                    replyToId = replyingToMessage?.id,
                                    replyToText = replyingToMessage?.text,
                                    targetUser = targetUser
                                )
                                inputText = ""
                                replyingToMessage = null
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) PrimaryIndigo else Color.LightGray)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isMe = message.senderId == currentUser?.id
                MessageBubbleItem(
                    message = message,
                    isMe = isMe,
                    onLongClick = { selectedMessageToDelete = message },
                    onReplyClick = { replyingToMessage = message }
                )
            }

            if (isFriendTyping) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                        Text(
                            text = "${targetUser.fullName.substringBefore(" ")} is typing...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Delete Message Confirmation Dialog
    if (selectedMessageToDelete != null) {
        AlertDialog(
            onDismissRequest = { selectedMessageToDelete = null },
            title = { Text("Message Options") },
            text = { Text("What would you like to do with this message?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMessageToDelete?.let { repository.deleteMessage(it.id) }
                        selectedMessageToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        replyingToMessage = selectedMessageToDelete
                        selectedMessageToDelete = null
                    }
                ) {
                    Text("Reply")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
    message: ChatMessage,
    isMe: Boolean,
    onLongClick: () -> Unit,
    onReplyClick: () -> Unit
) {
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val bubbleBg = if (isMe) {
        BubbleSent
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Quoted message preview if replied
        if (!message.replyToText.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .widthIn(max = 240.dp)
            ) {
                Text(
                    text = "↩ ${message.replyToText}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Surface(
            shape = bubbleShape,
            color = bubbleBg,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Media (Image/Video)
                if (message.mediaUrl.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = "Shared media",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (message.mediaType == ChatMessage.TYPE_VIDEO) {
                            Icon(
                                Icons.Default.PlayCircleFilled,
                                contentDescription = "Play video",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp).align(Alignment.Center)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Time and Delivery Checkmarks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(3.dp))
                        when (message.status) {
                            ChatMessage.STATUS_SEEN -> {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Seen",
                                    tint = Color(0xFF67E8F9), // bright cyan
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            ChatMessage.STATUS_DELIVERED -> {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
