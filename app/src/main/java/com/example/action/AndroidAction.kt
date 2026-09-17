package com.example.action

sealed class AndroidAction(
    val type: String,
    val title: String,
    val description: String,
    val iconName: String
) {
    object OpenCamera : AndroidAction(
        type = "OPEN_CAMERA",
        title = "Open Camera",
        description = "Launch device camera to take photos",
        iconName = "camera"
    )

    object OpenGallery : AndroidAction(
        type = "OPEN_GALLERY",
        title = "Open Gallery",
        description = "Browse photos and media library",
        iconName = "gallery"
    )

    data class MakeCall(
        val phoneNumber: String,
        val contactName: String? = null
    ) : AndroidAction(
        type = "CALL_PHONE",
        title = "Call ${contactName ?: phoneNumber}",
        description = "Phone call to $phoneNumber",
        iconName = "phone"
    )

    data class SendMessage(
        val phoneNumber: String = "",
        val messageText: String = ""
    ) : AndroidAction(
        type = "SEND_SMS",
        title = if (phoneNumber.isNotBlank()) "Message $phoneNumber" else "Send Message",
        description = if (messageText.isNotBlank()) "\"$messageText\"" else "Compose SMS",
        iconName = "message"
    )

    data class OpenSettings(
        val subSetting: String? = null
    ) : AndroidAction(
        type = "OPEN_SETTINGS",
        title = if (subSetting != null) "Open ${subSetting.replaceFirstChar { it.uppercase() }} Settings" else "Open Settings",
        description = "Access Android system preferences",
        iconName = "settings"
    )

    data class OpenChrome(
        val queryOrUrl: String? = null
    ) : AndroidAction(
        type = "OPEN_CHROME",
        title = if (!queryOrUrl.isNullOrBlank()) "Search Chrome: $queryOrUrl" else "Open Chrome",
        description = "Launch web browser",
        iconName = "chrome"
    )

    data class OpenMusic(
        val query: String? = null
    ) : AndroidAction(
        type = "OPEN_MUSIC",
        title = if (!query.isNullOrBlank()) "Play Music: $query" else "Open Music App",
        description = "Launch default music player",
        iconName = "music"
    )

    data class OpenYouTube(
        val query: String? = null
    ) : AndroidAction(
        type = "OPEN_YOUTUBE",
        title = if (!query.isNullOrBlank()) "Search YouTube: $query" else "Open YouTube",
        description = "Launch YouTube app or website",
        iconName = "youtube"
    )

    object RecordVideo : AndroidAction(
        type = "RECORD_VIDEO",
        title = "Record Video",
        description = "Launch camera in video recording mode",
        iconName = "videocam"
    )

    data class PlayVideo(
        val query: String? = null
    ) : AndroidAction(
        type = "PLAY_VIDEO",
        title = if (!query.isNullOrBlank()) "Play Video: $query" else "Open Videos",
        description = "Open device video player / gallery",
        iconName = "video"
    )

    enum class VolumeAction { UP, DOWN, MUTE, UNMUTE, MAX, SET }

    data class ControlVolume(
        val action: VolumeAction,
        val percent: Int? = null
    ) : AndroidAction(
        type = "CONTROL_VOLUME",
        title = when (action) {
            VolumeAction.UP -> "Volume Up"
            VolumeAction.DOWN -> "Volume Down"
            VolumeAction.MUTE -> "Mute Audio"
            VolumeAction.UNMUTE -> "Unmute Audio"
            VolumeAction.MAX -> "Max Volume"
            VolumeAction.SET -> "Set Volume ${percent ?: 50}%"
        },
        description = "Adjust device media audio volume",
        iconName = "volume"
    )

    data class ToggleFlashlight(
        val enable: Boolean
    ) : AndroidAction(
        type = "TOGGLE_FLASHLIGHT",
        title = if (enable) "Turn On Flashlight" else "Turn Off Flashlight",
        description = if (enable) "Activate rear torch" else "Deactivate torch",
        iconName = "flashlight"
    )

    data class Dictation(
        val transcribedText: String
    ) : AndroidAction(
        type = "DICTATION",
        title = "Dictated Text",
        description = "\"$transcribedText\"",
        iconName = "edit"
    )

    data class OpenApp(
        val appName: String
    ) : AndroidAction(
        type = "OPEN_APP",
        title = "Open $appName",
        description = "Launch application $appName",
        iconName = "apps"
    )

    companion object {
        fun fromType(type: String, payload: String?): AndroidAction? {
            return when (type) {
                "OPEN_CAMERA" -> OpenCamera
                "RECORD_VIDEO" -> RecordVideo
                "OPEN_GALLERY" -> OpenGallery
                "PLAY_VIDEO" -> PlayVideo(payload)
                "OPEN_YOUTUBE" -> OpenYouTube(payload)
                "CALL_PHONE" -> MakeCall(payload ?: "")
                "SEND_SMS" -> {
                    val parts = payload?.split("|||") ?: emptyList()
                    val number = parts.getOrNull(0) ?: ""
                    val text = parts.getOrNull(1) ?: ""
                    SendMessage(number, text)
                }
                "OPEN_SETTINGS" -> OpenSettings(payload)
                "OPEN_CHROME" -> OpenChrome(payload)
                "OPEN_MUSIC" -> OpenMusic(payload)
                "CONTROL_VOLUME" -> {
                    val act = when (payload) {
                        "UP" -> VolumeAction.UP
                        "DOWN" -> VolumeAction.DOWN
                        "MUTE" -> VolumeAction.MUTE
                        "UNMUTE" -> VolumeAction.UNMUTE
                        "MAX" -> VolumeAction.MAX
                        else -> VolumeAction.UP
                    }
                    ControlVolume(act)
                }
                "TOGGLE_FLASHLIGHT" -> ToggleFlashlight(payload == "true")
                "DICTATION" -> Dictation(payload ?: "")
                "OPEN_APP" -> OpenApp(payload ?: "")
                else -> null
            }
        }
    }
}
