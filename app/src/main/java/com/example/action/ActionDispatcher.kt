package com.example.action

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.util.Locale

object ActionDispatcher {

    fun executeAction(context: Context, action: AndroidAction): Result<String> {
        return try {
            when (action) {
                is AndroidAction.OpenCamera -> openCamera(context)
                is AndroidAction.RecordVideo -> recordVideo(context)
                is AndroidAction.OpenGallery -> openGallery(context)
                is AndroidAction.PlayVideo -> playVideo(context, action.query)
                is AndroidAction.OpenYouTube -> openYouTube(context, action.query)
                is AndroidAction.ControlVolume -> controlVolume(context, action.action, action.percent)
                is AndroidAction.ToggleFlashlight -> toggleFlashlight(context, action.enable)
                is AndroidAction.Dictation -> handleDictation(context, action.transcribedText)
                is AndroidAction.MakeCall -> makeCall(context, action.phoneNumber)
                is AndroidAction.SendMessage -> sendMessage(context, action.phoneNumber, action.messageText)
                is AndroidAction.OpenSettings -> openSettings(context, action.subSetting)
                is AndroidAction.OpenChrome -> openChrome(context, action.queryOrUrl)
                is AndroidAction.OpenMusic -> openMusic(context, action.query)
                is AndroidAction.OpenApp -> openApp(context, action.appName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun openYouTube(context: Context, query: String?): Result<String> {
        return try {
            val ytAppIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            if (ytAppIntent != null) {
                if (!query.isNullOrBlank()) {
                    val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                        setPackage("com.google.android.youtube")
                        putExtra("query", query)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(searchIntent)
                } else {
                    ytAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(ytAppIntent)
                }
                Result.success("YouTube opened")
            } else {
                val url = if (!query.isNullOrBlank()) {
                    "https://www.youtube.com/results?search_query=" + Uri.encode(query)
                } else {
                    "https://www.youtube.com"
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                Result.success("YouTube opened in browser")
            }
        } catch (e: Exception) {
            Result.failure(Exception("Could not open YouTube: ${e.message}"))
        }
    }

    private fun recordVideo(context: Context): Result<String> {
        val intent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Result.success("Video recorder opened")
        } else {
            val fallback = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (fallback.resolveActivity(context.packageManager) != null) {
                context.startActivity(fallback)
                Result.success("Video capture opened")
            } else {
                Result.failure(Exception("No video recording app found on this device."))
            }
        }
    }

    private fun playVideo(context: Context, query: String?): Result<String> {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Result.success("Video player opened")
        } else {
            val fallback = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_GALLERY)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
            Result.success("Media gallery opened")
        }
    }

    private fun controlVolume(context: Context, action: AndroidAction.VolumeAction, percent: Int?): Result<String> {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return Result.failure(Exception("Audio service not available on device"))

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        return try {
            when (action) {
                AndroidAction.VolumeAction.UP -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume increased")
                }
                AndroidAction.VolumeAction.DOWN -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume decreased")
                }
                AndroidAction.VolumeAction.MUTE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                    } else {
                        @Suppress("DEPRECATION")
                        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
                    }
                    Result.success("Volume muted")
                }
                AndroidAction.VolumeAction.UNMUTE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
                    } else {
                        @Suppress("DEPRECATION")
                        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
                    }
                    Result.success("Volume unmuted")
                }
                AndroidAction.VolumeAction.MAX -> {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume set to maximum (100%)")
                }
                AndroidAction.VolumeAction.SET -> {
                    val target = ((percent ?: 50) / 100f * maxVolume).toInt().coerceIn(0, maxVolume)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume set to ${percent ?: 50}%")
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to adjust volume: ${e.message}"))
        }
    }

    private fun toggleFlashlight(context: Context, enable: Boolean): Result<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return Result.failure(Exception("Camera manager unavailable"))
            try {
                val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, enable)
                    return Result.success(if (enable) "Flashlight turned on" else "Flashlight turned off")
                } else {
                    return Result.failure(Exception("Device has no available camera flashlight hardware."))
                }
            } catch (e: Exception) {
                return Result.failure(Exception("Flashlight control failed: ${e.message}"))
            }
        } else {
            return Result.failure(Exception("Flashlight requires Android 6.0 or higher."))
        }
    }

    private fun handleDictation(context: Context, text: String): Result<String> {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Assistant Dictation", text)
            clipboard?.setPrimaryClip(clip)
            Result.success("Preserved & copied to clipboard: \"$text\"")
        } catch (e: Exception) {
            Result.success("Dictation captured: \"$text\"")
        }
    }

    private fun openCamera(context: Context): Result<String> {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Result.success("Camera opened")
        } else {
            // Fallback to standard capture intent
            val fallback = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
            Result.success("Camera opened")
        }
    }

    private fun openGallery(context: Context): Result<String> {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Result.success("Gallery opened")
        } else {
            val mainGallery = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_GALLERY)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mainGallery)
            Result.success("Gallery opened")
        }
    }

    private fun makeCall(context: Context, phoneNumber: String): Result<String> {
        val cleanNumber = phoneNumber.trim().replace(" ", "").replace("-", "")
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        context.startActivity(intent)
        return Result.success("Calling $cleanNumber")
    }

    private fun sendMessage(context: Context, phoneNumber: String, messageText: String): Result<String> {
        val cleanNumber = phoneNumber.trim().replace(" ", "").replace("-", "")
        val uri = if (cleanNumber.isNotBlank()) Uri.parse("smsto:$cleanNumber") else Uri.parse("smsto:")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return Result.success("Message intent opened for $cleanNumber")
    }

    private fun openSettings(context: Context, subSetting: String?): Result<String> {
        val action = when (subSetting?.lowercase(Locale.ROOT)) {
            "wifi", "wi-fi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display", "brightness", "screen" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound", "volume", "audio" -> Settings.ACTION_SOUND_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "apps", "applications" -> Settings.ACTION_APPLICATION_SETTINGS
            "storage" -> Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            "location", "gps" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return Result.success("Settings opened (${subSetting ?: "main"})")
    }

    private fun openChrome(context: Context, queryOrUrl: String?): Result<String> {
        val url = when {
            queryOrUrl.isNullOrBlank() -> "https://www.google.com"
            queryOrUrl.startsWith("http://") || queryOrUrl.startsWith("https://") -> queryOrUrl
            queryOrUrl.contains(".") && !queryOrUrl.contains(" ") -> "https://$queryOrUrl"
            else -> "https://www.google.com/search?q=" + Uri.encode(queryOrUrl)
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Prefer chrome if available
            setPackage("com.android.chrome")
        }
        return try {
            context.startActivity(intent)
            Result.success("Chrome opened with $url")
        } catch (e: Exception) {
            // Fallback to any browser
            val generic = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(generic)
            Result.success("Browser opened with $url")
        }
    }

    private fun openMusic(context: Context, query: String?): Result<String> {
        return try {
            val intent = if (!query.isNullOrBlank()) {
                Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
                    putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
                    putExtra(MediaStore.EXTRA_MEDIA_TITLE, query)
                    putExtra(MediaStore.EXTRA_MEDIA_ARTIST, query)
                    putExtra(android.app.SearchManager.QUERY, query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent.makeMainSelectorActivity(
                    Intent.ACTION_MAIN,
                    Intent.CATEGORY_APP_MUSIC
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
            Result.success("Music player launched")
        } catch (e: Exception) {
            // Fallback to searching music apps or YouTube music
            val ytMusic = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.youtube.music")
            val spotify = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
            val fallback = ytMusic ?: spotify
            if (fallback != null) {
                fallback.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(fallback)
                Result.success("Music app launched")
            } else {
                // Open YouTube or search music in browser
                openChrome(context, "https://music.youtube.com")
            }
        }
    }

    private fun openApp(context: Context, appName: String): Result<String> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val cleanName = appName.trim().lowercase(Locale.ROOT)

        for (app in packages) {
            val label = pm.getApplicationLabel(app).toString().lowercase(Locale.ROOT)
            if (label == cleanName || label.contains(cleanName)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return Result.success("Launched ${pm.getApplicationLabel(app)}")
                }
            }
        }
        return Result.failure(Exception("Could not find installed application named '$appName'"))
    }
}
