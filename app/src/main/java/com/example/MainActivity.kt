package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.User
import com.example.data.repository.FriendChatRepository
import com.example.data.service.AgoraCallManager
import com.example.ui.components.FriendChatBottomNav
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomingCallScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.VideoCallScreen
import com.example.ui.screens.VoiceCallScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: FriendChatRepository
    private lateinit var callManager: AgoraCallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = FriendChatRepository.getInstance(applicationContext)
        callManager = AgoraCallManager.getInstance(applicationContext)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by rememberSaveable { mutableStateOf(systemDark) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                FriendChatApp(
                    repository = repository,
                    callManager = callManager,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = it }
                )
            }
        }
    }
}

@Composable
fun FriendChatApp(
    repository: FriendChatRepository,
    callManager: AgoraCallManager,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    // Request permissions for camera, mic, notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    val currentUser by repository.currentUser.collectAsState()
    val allUsers by repository.users.collectAsState()
    val conversations by repository.conversations.collectAsState()
    val callState by callManager.callState.collectAsState()

    // Screen navigation state
    var currentScreen by rememberSaveable { mutableStateOf(if (currentUser != null) "home" else "login") }
    var selectedUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedChatUser by remember { mutableStateOf<User?>(null) }

    // Synchronize login state
    LaunchedEffect(currentUser) {
        if (currentUser == null && currentScreen != "login" && currentScreen != "signup" && currentScreen != "forgot_password") {
            currentScreen = "login"
        }
    }

    val totalUnreadChats = conversations.sumOf { it.unreadCount }
    val isBottomNavScreen = currentScreen in listOf("home", "discover", "chats", "profile")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isBottomNavScreen && currentUser != null) {
                    FriendChatBottomNav(
                        currentRoute = currentScreen,
                        onNavigate = { route -> currentScreen = route },
                        unreadChatsCount = totalUnreadChats
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isBottomNavScreen) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.Dp.Hairline)
            ) {
                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            repository = repository,
                            onLoginSuccess = { currentScreen = "home" },
                            onNavigateToSignup = { currentScreen = "signup" },
                            onNavigateToForgotPassword = { currentScreen = "forgot_password" }
                        )
                    }
                    "signup" -> {
                        SignupScreen(
                            repository = repository,
                            onSignupSuccess = { currentScreen = "home" },
                            onNavigateToLogin = { currentScreen = "login" }
                        )
                    }
                    "forgot_password" -> {
                        ForgotPasswordScreen(
                            repository = repository,
                            onBackToLogin = { currentScreen = "login" }
                        )
                    }
                    "home" -> {
                        HomeScreen(
                            repository = repository,
                            onNavigateToNotifications = { currentScreen = "notifications" },
                            onNavigateToSearch = { currentScreen = "discover" },
                            onNavigateToUserProfile = { uid ->
                                selectedUserId = uid
                                currentScreen = "user_profile"
                            },
                            onNavigateToChat = { user ->
                                selectedChatUser = user
                                currentScreen = "chat_detail"
                            }
                        )
                    }
                    "discover" -> {
                        DiscoverScreen(
                            repository = repository,
                            onNavigateToNotifications = { currentScreen = "notifications" },
                            onNavigateToUserProfile = { uid ->
                                selectedUserId = uid
                                currentScreen = "user_profile"
                            }
                        )
                    }
                    "chats" -> {
                        ChatListScreen(
                            repository = repository,
                            onNavigateToChat = { user ->
                                selectedChatUser = user
                                currentScreen = "chat_detail"
                            },
                            onNavigateToNotifications = { currentScreen = "notifications" }
                        )
                    }
                    "profile" -> {
                        UserProfileScreen(
                            repository = repository,
                            userId = "self",
                            onNavigateBack = { currentScreen = "home" },
                            onNavigateToEditProfile = { currentScreen = "edit_profile" },
                            onNavigateToSettings = { currentScreen = "settings" },
                            onNavigateToFriends = { currentScreen = "friends" },
                            onNavigateToChat = { user ->
                                selectedChatUser = user
                                currentScreen = "chat_detail"
                            },
                            onStartVoiceCall = { user ->
                                currentUser?.let { me ->
                                    callManager.startOutgoingCall(me, user, "voice")
                                }
                            },
                            onStartVideoCall = { user ->
                                currentUser?.let { me ->
                                    callManager.startOutgoingCall(me, user, "video")
                                }
                            }
                        )
                    }
                    "user_profile" -> {
                        selectedUserId?.let { uid ->
                            UserProfileScreen(
                                repository = repository,
                                userId = uid,
                                onNavigateBack = { currentScreen = "home" },
                                onNavigateToEditProfile = { currentScreen = "edit_profile" },
                                onNavigateToSettings = { currentScreen = "settings" },
                                onNavigateToFriends = { currentScreen = "friends" },
                                onNavigateToChat = { user ->
                                    selectedChatUser = user
                                    currentScreen = "chat_detail"
                                },
                                onStartVoiceCall = { user ->
                                    currentUser?.let { me ->
                                        callManager.startOutgoingCall(me, user, "voice")
                                    }
                                },
                                onStartVideoCall = { user ->
                                    currentUser?.let { me ->
                                        callManager.startOutgoingCall(me, user, "video")
                                    }
                                }
                            )
                        }
                    }
                    "friends" -> {
                        FriendsScreen(
                            repository = repository,
                            onNavigateBack = { currentScreen = "profile" },
                            onNavigateToUserProfile = { uid ->
                                selectedUserId = uid
                                currentScreen = "user_profile"
                            },
                            onNavigateToChat = { user ->
                                selectedChatUser = user
                                currentScreen = "chat_detail"
                            },
                            onStartVoiceCall = { user ->
                                currentUser?.let { me ->
                                    callManager.startOutgoingCall(me, user, "voice")
                                }
                            },
                            onStartVideoCall = { user ->
                                currentUser?.let { me ->
                                    callManager.startOutgoingCall(me, user, "video")
                                }
                            }
                        )
                    }
                    "chat_detail" -> {
                        val targetUser = selectedChatUser ?: allUsers.firstOrNull()
                        if (targetUser != null) {
                            ChatDetailScreen(
                                repository = repository,
                                targetUser = targetUser,
                                onNavigateBack = { currentScreen = "chats" },
                                onStartVoiceCall = {
                                    currentUser?.let { me ->
                                        callManager.startOutgoingCall(me, targetUser, "voice")
                                    }
                                },
                                onStartVideoCall = {
                                    currentUser?.let { me ->
                                        callManager.startOutgoingCall(me, targetUser, "video")
                                    }
                                },
                                onUserProfileClick = {
                                    selectedUserId = targetUser.id
                                    currentScreen = "user_profile"
                                }
                            )
                        }
                    }
                    "edit_profile" -> {
                        EditProfileScreen(
                            repository = repository,
                            onNavigateBack = { currentScreen = "profile" }
                        )
                    }
                    "notifications" -> {
                        NotificationsScreen(
                            repository = repository,
                            onNavigateBack = { currentScreen = "home" },
                            onNotificationClick = { notif ->
                                when (notif.type) {
                                    "new_message" -> {
                                        val user = allUsers.find { it.id == notif.senderId }
                                        if (user != null) {
                                            selectedChatUser = user
                                            currentScreen = "chat_detail"
                                        }
                                    }
                                    "friend_request", "request_accepted" -> {
                                        currentScreen = "friends"
                                    }
                                    "voice_call", "video_call" -> {
                                        // Trigger incoming call dialog
                                        val user = allUsers.find { it.id == notif.senderId }
                                        if (user != null && currentUser != null) {
                                            callManager.simulateIncomingCall(user, currentUser!!, if (notif.type == "video_call") "video" else "voice")
                                        }
                                    }
                                }
                            }
                        )
                    }
                    "settings" -> {
                        SettingsScreen(
                            repository = repository,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = onToggleDarkMode,
                            onNavigateBack = { currentScreen = "profile" },
                            onLogout = { currentScreen = "login" }
                        )
                    }
                }
            }
        }

        // Live Voice / Video / Incoming Call Overlay
        callState?.let { call ->
            if (!call.isEnded) {
                if (call.isIncoming && !call.isConnected) {
                    IncomingCallScreen(
                        callManager = callManager,
                        onAccept = {},
                        onReject = {}
                    )
                } else if (call.callType == "video") {
                    VideoCallScreen(
                        callManager = callManager,
                        onCallEnded = {}
                    )
                } else {
                    VoiceCallScreen(
                        callManager = callManager,
                        onCallEnded = {}
                    )
                }
            }
        }
    }
}
