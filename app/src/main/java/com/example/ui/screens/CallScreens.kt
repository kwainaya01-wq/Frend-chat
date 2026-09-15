package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.service.AgoraCallManager
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryPurple

@Composable
fun IncomingCallScreen(
    callManager: AgoraCallManager,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val callState by callManager.callState.collectAsState()
    val state = callState ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (state.callType == "video") "Incoming Video Call..." else "Incoming Voice Call...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.callerName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Animated pulsing caller avatar
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.25f))
                )
                UserAvatar(
                    avatarUrl = state.callerAvatar,
                    displayName = state.callerName,
                    size = 120.dp
                )
            }

            // Accept & Decline Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reject Button (Red)
                FloatingActionButton(
                    onClick = {
                        callManager.rejectCall()
                        onReject()
                    },
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp).testTag("decline_call_button")
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "Decline Call", modifier = Modifier.size(32.dp))
                }

                // Accept Button (Green)
                FloatingActionButton(
                    onClick = {
                        callManager.acceptCall()
                        onAccept()
                    },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp).testTag("accept_call_button")
                ) {
                    Icon(
                        if (state.callType == "video") Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Accept Call",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceCallScreen(
    callManager: AgoraCallManager,
    onCallEnded: () -> Unit
) {
    val callState by callManager.callState.collectAsState()
    val state = callState

    if (state == null || state.isEnded) {
        onCallEnded()
        return
    }

    val targetName = if (state.isIncoming) state.callerName else state.receiverName
    val targetAvatar = if (state.isIncoming) state.callerAvatar else state.receiverAvatar

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = targetName,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (state.isConnected) callManager.formatDuration(state.durationSeconds) else "Connecting...",
                    color = if (state.isConnected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Big Avatar with ambient glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(PrimaryPurple.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
                UserAvatar(
                    avatarUrl = targetAvatar,
                    displayName = targetName,
                    size = 120.dp
                )
            }

            // Voice In-Call Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute
                    IconButton(
                        onClick = { callManager.toggleMute() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (state.isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                            .testTag("voice_toggle_mute")
                    ) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (state.isMuted) Color.Black else Color.White
                        )
                    }

                    // Speaker
                    IconButton(
                        onClick = { callManager.toggleSpeaker() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (state.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f))
                            .testTag("voice_toggle_speaker")
                    ) {
                        Icon(
                            imageVector = if (state.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Speaker",
                            tint = if (state.isSpeakerOn) Color.Black else Color.White
                        )
                    }

                    // End Call
                    FloatingActionButton(
                        onClick = {
                            callManager.endCall()
                            onCallEnded()
                        },
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp).testTag("voice_end_call_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call")
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCallScreen(
    callManager: AgoraCallManager,
    onCallEnded: () -> Unit
) {
    val callState by callManager.callState.collectAsState()
    val state = callState

    if (state == null || state.isEnded) {
        onCallEnded()
        return
    }

    val targetName = if (state.isIncoming) state.callerName else state.receiverName
    val targetAvatar = if (state.isIncoming) state.callerAvatar else state.receiverAvatar

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Remote Video Feed (Simulation / Agora Canvas)
        if (state.isCameraOn) {
            AsyncImage(
                model = targetAvatar.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop&q=80" },
                contentDescription = "Remote video stream",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                UserAvatar(avatarUrl = targetAvatar, displayName = targetName, size = 120.dp)
            }
        }

        // Top Overlay: Friend name, duration, network quality
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = targetName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.isConnected) callManager.formatDuration(state.durationSeconds) else "Connecting...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "HD 60fps",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // PIP Local Video Preview (Picture in Picture)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 110.dp, end = 16.dp)
                .size(width = 100.dp, height = 150.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            if (state.isCameraOn) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
                    contentDescription = "My camera preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.VideocamOff, contentDescription = "Camera off", tint = Color.White)
                }
            }
        }

        // Bottom Video Call Action Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch Camera (Front/Back)
                IconButton(
                    onClick = { callManager.switchCamera() },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .testTag("video_switch_camera")
                ) {
                    Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
                }

                // Toggle Camera On/Off
                IconButton(
                    onClick = { callManager.toggleCamera() },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (!state.isCameraOn) Color.White else Color.White.copy(alpha = 0.25f))
                        .testTag("video_toggle_camera")
                ) {
                    Icon(
                        imageVector = if (state.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Toggle Video",
                        tint = if (!state.isCameraOn) Color.Black else Color.White
                    )
                }

                // Toggle Mic
                IconButton(
                    onClick = { callManager.toggleMute() },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (state.isMuted) Color.White else Color.White.copy(alpha = 0.25f))
                        .testTag("video_toggle_mute")
                ) {
                    Icon(
                        imageVector = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (state.isMuted) Color.Black else Color.White
                    )
                }

                // Toggle Speaker
                IconButton(
                    onClick = { callManager.toggleSpeaker() },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (state.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.25f))
                        .testTag("video_toggle_speaker")
                ) {
                    Icon(
                        imageVector = if (state.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = if (state.isSpeakerOn) Color.Black else Color.White
                    )
                }

                // End Call (Red)
                FloatingActionButton(
                    onClick = {
                        callManager.endCall()
                        onCallEnded()
                    },
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(58.dp).testTag("video_end_call_button")
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call")
                }
            }
        }
    }
}
