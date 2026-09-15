package com.example.data.service

import android.content.Context
import android.media.AudioManager
import com.example.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CallUiState(
    val callId: String = "",
    val channelName: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerAvatar: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverAvatar: String = "",
    val callType: String = "voice", // "voice" or "video"
    val isIncoming: Boolean = false,
    val isConnected: Boolean = false,
    val isEnded: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isCameraOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val durationSeconds: Long = 0,
    val networkQuality: String = "Good",
    val errorMessage: String? = null
)

/**
 * AgoraCallManager handles real-time voice and video calls.
 * Agora App ID and Token configurations are kept strictly decoupled from the UI.
 * In Sketchware Pro, users can substitute their AGORA_APP_ID in this manager.
 */
class AgoraCallManager private constructor(private val context: Context) {

    companion object {
        // Agora Configuration (replace with your Agora App ID from console.agora.io)
        const val DEFAULT_AGORA_APP_ID = "YOUR_AGORA_APP_ID"

        @Volatile
        private var INSTANCE: AgoraCallManager? = null

        fun getInstance(context: Context): AgoraCallManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AgoraCallManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _callState = MutableStateFlow<CallUiState?>(null)
    val callState: StateFlow<CallUiState?> = _callState.asStateFlow()

    fun startOutgoingCall(fromUser: User, toUser: User, callType: String) {
        startOutgoingCall(
            targetUserId = toUser.id,
            targetUserName = toUser.fullName,
            targetUserAvatar = toUser.avatarUrl,
            currentUserId = fromUser.id,
            currentUserName = fromUser.fullName,
            currentUserAvatar = fromUser.avatarUrl,
            callType = callType
        )
    }

    fun simulateIncomingCall(fromUser: User, toUser: User, callType: String) {
        val channelId = "fc_in_${System.currentTimeMillis()}"
        receiveIncomingCall(
            callId = channelId,
            callerId = fromUser.id,
            callerName = fromUser.fullName,
            callerAvatar = fromUser.avatarUrl,
            receiverId = toUser.id,
            callType = callType
        )
    }

    fun startOutgoingCall(
        targetUserId: String,
        targetUserName: String,
        targetUserAvatar: String,
        currentUserId: String,
        currentUserName: String,
        currentUserAvatar: String,
        callType: String
    ) {
        val channelId = "fc_${System.currentTimeMillis()}_${(1000..9999).random()}"
        _callState.value = CallUiState(
            callId = channelId,
            channelName = channelId,
            callerId = currentUserId,
            callerName = currentUserName,
            callerAvatar = currentUserAvatar,
            receiverId = targetUserId,
            receiverName = targetUserName,
            receiverAvatar = targetUserAvatar,
            callType = callType,
            isIncoming = false,
            isConnected = false,
            isEnded = false,
            isSpeakerOn = callType == "video"
        )

        // Simulate network connection handshake or initialize Agora RTC Engine
        scope.launch {
            delay(2500) // Simulated connection latency
            if (_callState.value?.isEnded == false) {
                _callState.update { it?.copy(isConnected = true) }
                startCallTimer()
            }
        }
    }

    fun receiveIncomingCall(
        callId: String,
        callerId: String,
        callerName: String,
        callerAvatar: String,
        receiverId: String,
        callType: String
    ) {
        _callState.value = CallUiState(
            callId = callId,
            channelName = callId,
            callerId = callerId,
            callerName = callerName,
            callerAvatar = callerAvatar,
            receiverId = receiverId,
            callType = callType,
            isIncoming = true,
            isConnected = false,
            isEnded = false
        )
    }

    fun acceptCall() {
        _callState.update {
            it?.copy(
                isIncoming = false,
                isConnected = true
            )
        }
        startCallTimer()
    }

    fun rejectCall() {
        endCall()
    }

    fun endCall() {
        stopCallTimer()
        _callState.update { it?.copy(isEnded = true, isConnected = false) }
        scope.launch {
            delay(500)
            _callState.value = null
        }
    }

    fun toggleMute() {
        _callState.update { current ->
            current?.let {
                val newMute = !it.isMuted
                it.copy(isMuted = newMute)
            }
        }
    }

    fun toggleSpeaker() {
        _callState.update { current ->
            current?.let {
                val newSpeaker = !it.isSpeakerOn
                try {
                    audioManager?.isSpeakerphoneOn = newSpeaker
                } catch (_: Exception) {}
                it.copy(isSpeakerOn = newSpeaker)
            }
        }
    }

    fun toggleCamera() {
        _callState.update { current ->
            current?.let { it.copy(isCameraOn = !it.isCameraOn) }
        }
    }

    fun switchCamera() {
        _callState.update { current ->
            current?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
        }
    }

    private fun startCallTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                _callState.update { it?.copy(durationSeconds = (it.durationSeconds + 1)) }
            }
        }
    }

    private fun stopCallTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun formatDuration(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
