package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class User(
    @DocumentId val id: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val gender: String = "Not specified",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val friendsCount: Int = 0,
    val postsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FriendRequest(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderUsername: String = "",
    val senderAvatar: String = "",
    val receiverId: String = "",
    val status: String = STATUS_PENDING, // "pending", "accepted", "rejected"
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_REJECTED = "rejected"
    }
}

@IgnoreExtraProperties
data class Friendship(
    @DocumentId val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val mediaType: String = TYPE_TEXT, // "text", "image", "video"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = STATUS_SENT, // "sent", "delivered", "seen"
    val replyToId: String? = null,
    val replyToText: String? = null
) {
    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
        const val TYPE_VIDEO = "video"

        const val STATUS_SENT = "sent"
        const val STATUS_DELIVERED = "delivered"
        const val STATUS_SEEN = "seen"
    }
}

@IgnoreExtraProperties
data class ChatConversation(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val otherUser: User? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = TYPE_FRIEND_REQUEST, // friend_request, request_accepted, new_message, voice_call, video_call
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_FRIEND_REQUEST = "friend_request"
        const val TYPE_REQUEST_ACCEPTED = "request_accepted"
        const val TYPE_NEW_MESSAGE = "new_message"
        const val TYPE_VOICE_CALL = "voice_call"
        const val TYPE_VIDEO_CALL = "video_call"
    }
}

@IgnoreExtraProperties
data class Post(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorAvatar: String = "",
    val caption: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "image",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class CallSession(
    @DocumentId val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerAvatar: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverAvatar: String = "",
    val callType: String = TYPE_VOICE, // "voice", "video"
    val status: String = STATUS_RINGING, // "ringing", "connected", "ended", "rejected"
    val channelName: String = "",
    val token: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_VOICE = "voice"
        const val TYPE_VIDEO = "video"

        const val STATUS_RINGING = "ringing"
        const val STATUS_CONNECTED = "connected"
        const val STATUS_ENDED = "ended"
        const val STATUS_REJECTED = "rejected"
    }
}

data class BlockedUser(
    val userId: String = "",
    val blockedUserId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ReportUser(
    val reporterId: String = "",
    val reportedUserId: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
