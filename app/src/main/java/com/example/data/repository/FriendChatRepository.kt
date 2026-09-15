package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.model.AppNotification
import com.example.data.model.BlockedUser
import com.example.data.model.CallSession
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.FriendRequest
import com.example.data.model.Post
import com.example.data.model.ReportUser
import com.example.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FriendChatRepository private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: FriendChatRepository? = null

        fun getInstance(context: Context): FriendChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FriendChatRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Firebase instances (safely checked)
    private val isFirebaseAvailable: Boolean = try {
        FirebaseApp.getApps(context).isNotEmpty()
    } catch (_: Exception) {
        false
    }

    private val auth: FirebaseAuth? = if (isFirebaseAvailable) {
        try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    } else null

    private val firestore: FirebaseFirestore? = if (isFirebaseAvailable) {
        try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
    } else null

    private val storage: FirebaseStorage? = if (isFirebaseAvailable) {
        try { FirebaseStorage.getInstance() } catch (_: Exception) { null }
    } else null

    // State flows
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    private val _friendIds = MutableStateFlow<Set<String>>(emptySet())
    val friendIds: StateFlow<Set<String>> = _friendIds.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _currentChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentChatMessages: StateFlow<List<ChatMessage>> = _currentChatMessages.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    private val _isTypingMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isTypingMap: StateFlow<Map<String, Boolean>> = _isTypingMap.asStateFlow()

    init {
        initInitialData()
    }

    private fun initInitialData() {
        val defaultCurrent = User(
            id = "user_me",
            fullName = "Alex Morgan",
            username = "alex_morgan",
            email = "alex@example.com",
            bio = "Photography enthusiast 📸 | Travel & Coffee lover ☕️ | Building Friend Chat!",
            gender = "Non-binary",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
            isOnline = true,
            friendsCount = 4,
            postsCount = 3
        )
        _currentUser.value = defaultCurrent

        val initialFriends = listOf(
            User(
                id = "user_sarah",
                fullName = "Sarah Jenkins",
                username = "sarah_j",
                email = "sarah@example.com",
                bio = "UI/UX Designer & sunset seeker 🌅",
                gender = "Female",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                isOnline = true,
                lastSeen = System.currentTimeMillis(),
                friendsCount = 12,
                postsCount = 8
            ),
            User(
                id = "user_liam",
                fullName = "Liam Chen",
                username = "liam_tech",
                email = "liam@example.com",
                bio = "Mobile developer & guitar hobbyist 🎸",
                gender = "Male",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                isOnline = true,
                lastSeen = System.currentTimeMillis() - 300000,
                friendsCount = 28,
                postsCount = 15
            ),
            User(
                id = "user_elena",
                fullName = "Elena Rostova",
                username = "elena_r",
                email = "elena@example.com",
                bio = "Nature lover, hiker, and bookworm 📚🍃",
                gender = "Female",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 3600000,
                friendsCount = 19,
                postsCount = 6
            ),
            User(
                id = "user_marcus",
                fullName = "Marcus Johnson",
                username = "marcus_j",
                email = "marcus@example.com",
                bio = "Fitness, food, and chill beats 🎧🥑",
                gender = "Male",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                isOnline = true,
                lastSeen = System.currentTimeMillis(),
                friendsCount = 31,
                postsCount = 12
            ),
            User(
                id = "user_maya",
                fullName = "Maya Patel",
                username = "maya_p",
                email = "maya@example.com",
                bio = "Creative director & indie art curator 🎨",
                gender = "Female",
                avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop&q=80",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 7200000,
                friendsCount = 45,
                postsCount = 20
            ),
            User(
                id = "user_david",
                fullName = "David Kim",
                username = "david_k",
                email = "david@example.com",
                bio = "Urban exploration & minimalist aesthetics 🏙️",
                gender = "Male",
                avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 86400000,
                friendsCount = 9,
                postsCount = 4
            )
        )
        _users.value = initialFriends
        _friendIds.value = setOf("user_sarah", "user_liam", "user_elena", "user_marcus")

        _friendRequests.value = listOf(
            FriendRequest(
                id = "req_maya",
                senderId = "user_maya",
                senderName = "Maya Patel",
                senderUsername = "maya_p",
                senderAvatar = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop&q=80",
                receiverId = defaultCurrent.id,
                status = FriendRequest.STATUS_PENDING,
                timestamp = System.currentTimeMillis() - 1800000
            )
        )

        _posts.value = listOf(
            Post(
                id = "post_1",
                authorId = "user_sarah",
                authorName = "Sarah Jenkins",
                authorUsername = "sarah_j",
                authorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                caption = "Golden hour walks make everything feel peaceful ✨ What is your favorite time of day?",
                mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
                likesCount = 42,
                commentsCount = 7,
                isLiked = false,
                timestamp = System.currentTimeMillis() - 1200000
            ),
            Post(
                id = "post_2",
                authorId = "user_liam",
                authorName = "Liam Chen",
                authorUsername = "liam_tech",
                authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                caption = "Coffee and clean code setup for Sunday morning ☕️💻",
                mediaUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&auto=format&fit=crop&q=80",
                likesCount = 89,
                commentsCount = 14,
                isLiked = true,
                timestamp = System.currentTimeMillis() - 5400000
            ),
            Post(
                id = "post_3",
                authorId = "user_me",
                authorName = defaultCurrent.fullName,
                authorUsername = defaultCurrent.username,
                authorAvatar = defaultCurrent.avatarUrl,
                caption = "Excited to launch Friend Chat! Seamless friendships, instant chat, voice, and video calls 🎉",
                mediaUrl = "https://images.unsplash.com/photo-1511632765486-a01980e01a18?w=800&auto=format&fit=crop&q=80",
                likesCount = 128,
                commentsCount = 22,
                isLiked = true,
                timestamp = System.currentTimeMillis() - 10800000
            )
        )

        _conversations.value = listOf(
            ChatConversation(
                id = "chat_sarah",
                participantIds = listOf("user_me", "user_sarah"),
                lastMessage = "Hey Alex! Are we still catching up tomorrow?",
                lastSenderId = "user_sarah",
                lastTimestamp = System.currentTimeMillis() - 180000,
                unreadCount = 1,
                otherUser = initialFriends[0]
            ),
            ChatConversation(
                id = "chat_liam",
                participantIds = listOf("user_me", "user_liam"),
                lastMessage = "Check out the new audio filters!",
                lastSenderId = "user_liam",
                lastTimestamp = System.currentTimeMillis() - 3600000,
                unreadCount = 0,
                otherUser = initialFriends[1]
            ),
            ChatConversation(
                id = "chat_marcus",
                participantIds = listOf("user_me", "user_marcus"),
                lastMessage = "Awesome video call earlier bro 👍",
                lastSenderId = "user_me",
                lastTimestamp = System.currentTimeMillis() - 86400000,
                unreadCount = 0,
                otherUser = initialFriends[3]
            )
        )

        _notifications.value = listOf(
            AppNotification(
                id = "notif_1",
                userId = "user_me",
                title = "Friend Request",
                body = "Maya Patel sent you a friend request.",
                type = AppNotification.TYPE_FRIEND_REQUEST,
                senderId = "user_maya",
                senderName = "Maya Patel",
                senderAvatar = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop&q=80",
                isRead = false,
                timestamp = System.currentTimeMillis() - 1800000
            ),
            AppNotification(
                id = "notif_2",
                userId = "user_me",
                title = "New Message",
                body = "Sarah Jenkins: Hey Alex! Are we still catching up tomorrow?",
                type = AppNotification.TYPE_NEW_MESSAGE,
                senderId = "user_sarah",
                senderName = "Sarah Jenkins",
                senderAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                isRead = false,
                timestamp = System.currentTimeMillis() - 180000
            ),
            AppNotification(
                id = "notif_3",
                userId = "user_me",
                title = "Friend Request Accepted",
                body = "Marcus Johnson accepted your friend request.",
                type = AppNotification.TYPE_REQUEST_ACCEPTED,
                senderId = "user_marcus",
                senderName = "Marcus Johnson",
                senderAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                isRead = true,
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
    }

    // 1. AUTHENTICATION
    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            if (auth != null) {
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = authResult.user?.uid ?: "user_${UUID.randomUUID()}"
                val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
                val user = doc?.toObject(User::class.java) ?: User(
                    id = uid,
                    fullName = email.substringBefore("@").replace(".", " ").capitalize(),
                    username = email.substringBefore("@"),
                    email = email,
                    isOnline = true
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                // Standalone / preview authentication
                val user = User(
                    id = "user_me",
                    fullName = email.substringBefore("@").replace(".", " ").capitalize(),
                    username = email.substringBefore("@"),
                    email = email,
                    bio = "Friend Chat member | Connected!",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                    isOnline = true,
                    friendsCount = _friendIds.value.size,
                    postsCount = 1
                )
                _currentUser.value = user
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(fullName: String, username: String, email: String, pass: String): Result<User> {
        return try {
            if (auth != null) {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = authResult.user?.uid ?: "user_${UUID.randomUUID()}"
                val newUser = User(
                    id = uid,
                    fullName = fullName,
                    username = username.lowercase().trim(),
                    email = email,
                    bio = "Hey there! I am using Friend Chat.",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
                    isOnline = true
                )
                firestore?.collection("users")?.document(uid)?.set(newUser)?.await()
                _currentUser.value = newUser
                Result.success(newUser)
            } else {
                val newUser = User(
                    id = "user_${UUID.randomUUID().toString().take(8)}",
                    fullName = fullName,
                    username = username.lowercase().trim(),
                    email = email,
                    bio = "Hey there! I am using Friend Chat.",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
                    isOnline = true
                )
                _currentUser.value = newUser
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            auth?.sendPasswordResetEmail(email)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit) // Handled gracefully
        }
    }

    fun logout() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
    }

    // 2. PROFILE EDIT & MEDIA
    fun updateProfile(fullName: String, username: String, bio: String, gender: String, avatarUrl: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            fullName = fullName,
            username = username,
            bio = bio,
            gender = gender,
            avatarUrl = avatarUrl.ifEmpty { current.avatarUrl }
        )
        _currentUser.value = updated
        scope.launch {
            try {
                firestore?.collection("users")?.document(current.id)?.set(updated)
            } catch (_: Exception) {}
        }
    }

    // 3. FRIEND REQUESTS & FRIENDSHIPS
    fun sendFriendRequest(targetUser: User) {
        val me = _currentUser.value ?: return
        val newRequest = FriendRequest(
            id = "req_${System.currentTimeMillis()}",
            senderId = me.id,
            senderName = me.fullName,
            senderUsername = me.username,
            senderAvatar = me.avatarUrl,
            receiverId = targetUser.id,
            status = FriendRequest.STATUS_PENDING,
            timestamp = System.currentTimeMillis()
        )
        _friendRequests.update { it + newRequest }

        // Trigger notification for recipient
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID()}",
            userId = targetUser.id,
            title = "Friend Request",
            body = "${me.fullName} sent you a friend request.",
            type = AppNotification.TYPE_FRIEND_REQUEST,
            senderId = me.id,
            senderName = me.fullName,
            senderAvatar = me.avatarUrl,
            timestamp = System.currentTimeMillis()
        )
        _notifications.update { listOf(notif) + it }

        scope.launch {
            try {
                firestore?.collection("friendRequests")?.document(newRequest.id)?.set(newRequest)
            } catch (_: Exception) {}
        }
    }

    fun cancelFriendRequest(targetUserId: String) {
        val me = _currentUser.value ?: return
        _friendRequests.update { list ->
            list.filterNot { it.senderId == me.id && it.receiverId == targetUserId }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        _friendRequests.update { it.filterNot { r -> r.id == request.id } }
        _friendIds.update { it + request.senderId }
        _currentUser.update { it?.copy(friendsCount = (it.friendsCount + 1)) }

        val notif = AppNotification(
            id = "notif_${UUID.randomUUID()}",
            userId = request.senderId,
            title = "Friend Request Accepted",
            body = "${_currentUser.value?.fullName ?: "Someone"} accepted your friend request.",
            type = AppNotification.TYPE_REQUEST_ACCEPTED,
            senderId = _currentUser.value?.id ?: "",
            senderName = _currentUser.value?.fullName ?: "",
            senderAvatar = _currentUser.value?.avatarUrl ?: "",
            timestamp = System.currentTimeMillis()
        )
        _notifications.update { listOf(notif) + it }
    }

    fun rejectFriendRequest(request: FriendRequest) {
        _friendRequests.update { it.filterNot { r -> r.id == request.id } }
    }

    fun removeFriend(friendId: String) {
        _friendIds.update { it - friendId }
        _currentUser.update { it?.copy(friendsCount = maxOf(0, it.friendsCount - 1)) }
    }

    fun getFriendshipStatus(otherUserId: String): String {
        val me = _currentUser.value ?: return "none"
        if (_friendIds.value.contains(otherUserId)) return "friends"
        val pendingSent = _friendRequests.value.any { it.senderId == me.id && it.receiverId == otherUserId }
        if (pendingSent) return "pending_sent"
        val pendingReceived = _friendRequests.value.any { it.receiverId == me.id && it.senderId == otherUserId }
        if (pendingReceived) return "pending_received"
        return "none"
    }

    // 4. POSTS & FEED
    fun createPost(caption: String, mediaUrl: String) {
        val me = _currentUser.value ?: return
        val newPost = Post(
            id = "post_${System.currentTimeMillis()}",
            authorId = me.id,
            authorName = me.fullName,
            authorUsername = me.username,
            authorAvatar = me.avatarUrl,
            caption = caption,
            mediaUrl = mediaUrl,
            likesCount = 0,
            commentsCount = 0,
            isLiked = false,
            timestamp = System.currentTimeMillis()
        )
        _posts.update { listOf(newPost) + it }
        _currentUser.update { it?.copy(postsCount = (it.postsCount + 1)) }
    }

    fun toggleLikePost(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val liked = !post.isLiked
                    val newLikes = if (liked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                    post.copy(isLiked = liked, likesCount = newLikes)
                } else post
            }
        }
    }

    // 5. CHAT & REAL-TIME MESSAGING
    fun loadMessagesForChat(chatId: String, otherUser: User) {
        val me = _currentUser.value ?: return
        val messages = if (chatId == "chat_sarah") {
            listOf(
                ChatMessage(
                    id = "msg_1",
                    chatId = chatId,
                    senderId = otherUser.id,
                    senderName = otherUser.fullName,
                    text = "Hey Alex! Loved your latest post on Friend Chat 🙌",
                    timestamp = System.currentTimeMillis() - 720000,
                    status = ChatMessage.STATUS_SEEN
                ),
                ChatMessage(
                    id = "msg_2",
                    chatId = chatId,
                    senderId = me.id,
                    senderName = me.fullName,
                    text = "Thanks Sarah! The photo of the sunset you shared was incredible too.",
                    timestamp = System.currentTimeMillis() - 600000,
                    status = ChatMessage.STATUS_SEEN
                ),
                ChatMessage(
                    id = "msg_3",
                    chatId = chatId,
                    senderId = otherUser.id,
                    senderName = otherUser.fullName,
                    text = "Hey Alex! Are we still catching up tomorrow?",
                    timestamp = System.currentTimeMillis() - 180000,
                    status = ChatMessage.STATUS_DELIVERED
                )
            )
        } else {
            listOf(
                ChatMessage(
                    id = "msg_init",
                    chatId = chatId,
                    senderId = otherUser.id,
                    senderName = otherUser.fullName,
                    text = "Hi there! Let's connect on Friend Chat.",
                    timestamp = System.currentTimeMillis() - 60000,
                    status = ChatMessage.STATUS_SEEN
                )
            )
        }
        _currentChatMessages.value = messages
    }

    fun sendMessage(
        chatId: String,
        text: String,
        mediaUrl: String = "",
        mediaType: String = ChatMessage.TYPE_TEXT,
        replyToId: String? = null,
        replyToText: String? = null,
        targetUser: User? = null
    ) {
        val me = _currentUser.value ?: return
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_${(100..999).random()}",
            chatId = chatId,
            senderId = me.id,
            senderName = me.fullName,
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = System.currentTimeMillis(),
            status = ChatMessage.STATUS_SENT,
            replyToId = replyToId,
            replyToText = replyToText
        )
        _currentChatMessages.update { it + newMsg }

        // Update conversation summary
        _conversations.update { list ->
            val existing = list.find { it.id == chatId }
            val updated = if (existing != null) {
                existing.copy(
                    lastMessage = if (text.isNotEmpty()) text else "Sent a $mediaType",
                    lastSenderId = me.id,
                    lastTimestamp = System.currentTimeMillis()
                )
            } else {
                ChatConversation(
                    id = chatId,
                    participantIds = listOf(me.id, targetUser?.id ?: ""),
                    lastMessage = if (text.isNotEmpty()) text else "Sent a $mediaType",
                    lastSenderId = me.id,
                    lastTimestamp = System.currentTimeMillis(),
                    otherUser = targetUser
                )
            }
            listOf(updated) + list.filterNot { it.id == chatId }
        }

        // Simulate echo reply if with demo user
        if (targetUser != null && text.isNotEmpty()) {
            scope.launch {
                setTyping(chatId, true)
                kotlinx.coroutines.delay(2000)
                setTyping(chatId, false)
                val replyMsg = ChatMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    chatId = chatId,
                    senderId = targetUser.id,
                    senderName = targetUser.fullName,
                    text = getSmartFriendReply(text, targetUser.fullName),
                    timestamp = System.currentTimeMillis(),
                    status = ChatMessage.STATUS_SENT
                )
                _currentChatMessages.update { it + replyMsg }
                _conversations.update { list ->
                    list.map {
                        if (it.id == chatId) it.copy(
                            lastMessage = replyMsg.text,
                            lastSenderId = targetUser.id,
                            lastTimestamp = System.currentTimeMillis()
                        ) else it
                    }
                }
            }
        }
    }

    private fun getSmartFriendReply(message: String, friendName: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hey! So good to hear from you on Friend Chat! How is your day going? 😊"
            lower.contains("tomorrow") || lower.contains("meet") || lower.contains("catch up") ->
                "Yes, definitely! Let's do 3 PM at our regular spot. Looking forward to it! ☕️"
            lower.contains("call") || lower.contains("video") ->
                "Sure thing, feel free to tap the call icon anytime! 📞"
            lower.contains("how are you") ->
                "I am doing wonderful, thanks for asking! Exploring some new social feeds right now."
            else ->
                "That sounds awesome! Totally agree with you. Let me know when you are free next!"
        }
    }

    fun setTyping(chatId: String, isTyping: Boolean) {
        _isTypingMap.update { it + (chatId to isTyping) }
    }

    fun deleteMessage(messageId: String) {
        _currentChatMessages.update { list -> list.filterNot { it.id == messageId } }
    }

    // 6. BLOCK & REPORT (Safety)
    fun blockUser(userId: String) {
        _blockedUserIds.update { it + userId }
        _friendIds.update { it - userId }
        _conversations.update { list -> list.filterNot { it.otherUser?.id == userId } }
    }

    fun unblockUser(userId: String) {
        _blockedUserIds.update { it - userId }
    }

    fun reportUser(reportedUserId: String, reason: String) {
        val me = _currentUser.value ?: return
        val report = ReportUser(
            reporterId = me.id,
            reportedUserId = reportedUserId,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        scope.launch {
            try {
                firestore?.collection("reports")?.add(report)
            } catch (_: Exception) {}
        }
    }

    // 7. NOTIFICATIONS
    fun markNotificationAsRead(notificationId: String) {
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }
}
