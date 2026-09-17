package com.example.action

import java.util.Locale

object ActionParser {

    enum class LanguageType {
        ENGLISH, TAMIL, THANGLISH
    }

    data class ParseResult(
        val action: AndroidAction?,
        val explanation: String,
        val detectedLanguage: LanguageType = LanguageType.ENGLISH
    )

    fun detectLanguage(input: String): LanguageType {
        // Check for Tamil Unicode block (0B80 - 0BFF)
        for (ch in input) {
            if (ch in '\u0B80'..'\u0BFF') {
                return LanguageType.TAMIL
            }
        }
        // Check for common Thanglish words
        val lower = input.lowercase(Locale.ROOT)
        val thanglishTokens = listOf(
            "vanakkam", "epdi", "irukka", "irukinga", "pannu", "solunga", "sollu", "enna",
            "seiringa", "nalla", "pattu", "paaru", "kora", "yethu", "podu", "edu", "thira",
            "thiravum", "kooda", "illai", "aama", "illa", "seri", "poidu", "padu"
        )
        for (token in thanglishTokens) {
            if (lower.contains(token)) {
                return LanguageType.THANGLISH
            }
        }
        return LanguageType.ENGLISH
    }

    fun parse(
        input: String,
        assistantName: String = "NOVA",
        userNickname: String = "Boss"
    ): ParseResult {
        val text = input.trim()
        val lower = text.lowercase(Locale.ROOT)
        val lang = detectLanguage(text)

        // 1. Dictation Mode: "Type exactly what I say: ..." / "Type what I say ..." / "நான் சொல்றதை டைப் பண்ணு"
        val dictationRegex = Regex("""^(?:type\s+exactly\s+what\s+i\s+say(?:\s*:|\s+that)?|type\s+what\s+i\s+say(?:\s*:|\s+that)?|dictate(?:\s*:|\s+that)?)\s*(.*)""", RegexOption.IGNORE_CASE)
        val dictationMatch = dictationRegex.find(text)
        if (dictationMatch != null || lower.contains("நான் சொல்றதை டைப் பண்ணு") || lower.contains("solradha type pannu")) {
            val content = when {
                dictationMatch != null -> dictationMatch.groupValues[1].trim()
                lower.contains("நான் சொல்றதை டைப் பண்ணு") -> text.substringAfter("நான் சொல்றதை டைப் பண்ணு").trim().removePrefix(":").trim()
                lower.contains("solradha type pannu") -> text.substringAfter("solradha type pannu").trim().removePrefix(":").trim()
                else -> text
            }
            val dictated = if (content.isNotBlank()) content else text
            val reply = when (lang) {
                LanguageType.TAMIL -> "நீங்கள் சொன்னதை அப்படியே சேமித்து கிளிப்போர்டில் நகலெடுத்துள்ளேன் $userNickname: \"$dictated\""
                LanguageType.THANGLISH -> "Neenga sonnadha appadiye save panni clipboard-la copy panniten $userNickname: \"$dictated\""
                LanguageType.ENGLISH -> "Preserving your exact words and copied to clipboard, $userNickname: \"$dictated\""
            }
            return ParseResult(
                action = AndroidAction.Dictation(dictated),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 2. YouTube actions: "Open YouTube", "YouTube open pannu", "யூடியூப் திற"
        if (lower.contains("youtube") || lower.contains("யூடியூப்")) {
            val query = when {
                lower.contains("search youtube for ") -> text.substringAfter("search youtube for ").trim()
                lower.contains("search youtube ") -> text.substringAfter("search youtube ").trim()
                lower.contains("youtube-il thedu ") -> text.substringAfter("youtube-il thedu ").trim()
                lower.contains("youtube thedu ") -> text.substringAfter("youtube thedu ").trim()
                else -> null
            }
            val reply = when (lang) {
                LanguageType.TAMIL -> if (!query.isNullOrBlank()) "யூடியூப்பில் '$query' தேடுகிறேன் $userNickname." else "யூடியூப் திறக்கிறேன் $userNickname."
                LanguageType.THANGLISH -> if (!query.isNullOrBlank()) "YouTube-la '$query' theduren $userNickname." else "YouTube open panren $userNickname."
                LanguageType.ENGLISH -> if (!query.isNullOrBlank()) "Searching YouTube for '$query', $userNickname." else "Opening YouTube, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.OpenYouTube(query),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 3. Video Recording: "Record video", "Start video recording", "வீடியோ எடு", "Video record pannu"
        if (lower.contains("record video") || lower.contains("start video recording") ||
            lower.contains("take a video") || lower.contains("take video") ||
            lower.contains("video record") || lower.contains("வீடியோ எடு") ||
            lower.contains("video edu")
        ) {
            val reply = when (lang) {
                LanguageType.TAMIL -> "வீடியோ ரெக்கார்டிங் கேமராவைத் திறக்கிறேன் $userNickname."
                LanguageType.THANGLISH -> "Video recording camera open panren $userNickname."
                LanguageType.ENGLISH -> "Launching video camera recorder, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.RecordVideo,
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 4. Camera actions: "Open camera", "Take photo", "போட்டோ எடு", "கேமரா திற", "Camera open pannu"
        if (lower.contains("open camera") || lower.contains("launch camera") ||
            lower.contains("take a photo") || lower.contains("take a picture") ||
            lower.contains("take photo") || lower.contains("snap a photo") ||
            lower.contains("camera open") || lower == "camera" ||
            lower.contains("கேமரா திற") || lower.contains("போட்டோ எடு") ||
            lower.contains("photo edu") || lower.contains("padam edu")
        ) {
            val reply = when (lang) {
                LanguageType.TAMIL -> "கேமரா திறக்கிறேன் $userNickname, புகைப்படம் எடுக்கத் தயார்!"
                LanguageType.THANGLISH -> "Camera open panren $userNickname, photo edukka ready!"
                LanguageType.ENGLISH -> "Opening camera to capture photos, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.OpenCamera,
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 5. Play Video: "Play local video", "Play video", "வீடியோ பார்", "Video play pannu"
        if (lower.contains("play video") || lower.contains("play local video") ||
            lower.contains("watch video") || lower.contains("வீடியோ பார்") ||
            lower.contains("video play")
        ) {
            val reply = when (lang) {
                LanguageType.TAMIL -> "வீடியோ பிளேயரைத் திறக்கிறேன் $userNickname."
                LanguageType.THANGLISH -> "Video player open panren $userNickname."
                LanguageType.ENGLISH -> "Opening video player, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.PlayVideo(),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 6. Gallery / Local Photo actions: "Open this photo", "Open gallery", "View photos", "போட்டோ காட்டு", "Gallery open pannu"
        if (lower.contains("gallery") || lower.contains("photo") || lower.contains("photos") ||
            lower.contains("pictures") || lower.contains("images") ||
            lower.contains("போட்டோ காட்டு") || lower.contains("படங்கள் காட்டு") ||
            lower.contains("photo kaatu") || lower.contains("gallery open")
        ) {
            val reply = when (lang) {
                LanguageType.TAMIL -> "உங்கள் புகைப்பட கேலரியைத் திறக்கிறேன் $userNickname."
                LanguageType.THANGLISH -> "Unga photo gallery-ah open panren $userNickname."
                LanguageType.ENGLISH -> "Opening photo gallery for you, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.OpenGallery,
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 7. Flashlight / Torch: "Turn on flashlight", "Torch on", "டார்ச் ஆன்", "டார்ச் ஆஃப்"
        if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("டார்ச்")) {
            val enable = !lower.contains("off") && !lower.contains("stop") && !lower.contains("மூடு") && !lower.contains("ஆஃப்")
            val reply = when (lang) {
                LanguageType.TAMIL -> if (enable) "டார்ச் ஆன் செய்கிறேன் $userNickname." else "டார்ச் ஆஃப் செய்கிறேன் $userNickname."
                LanguageType.THANGLISH -> if (enable) "Torch on panren $userNickname." else "Torch off panren $userNickname."
                LanguageType.ENGLISH -> if (enable) "Turning on flashlight, $userNickname." else "Turning off flashlight, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.ToggleFlashlight(enable),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 8. Volume Controls: "Volume up", "Volume down", "Mute", "Unmute", "Max volume", "சத்தம் அதிகம் செய்"
        if (lower.contains("volume") || lower.contains("sound") || lower.contains("சத்தம்") || lower.contains("satham")) {
            val (volAction, percent) = when {
                lower.contains("max") || lower.contains("100") || lower.contains("full") -> AndroidAction.VolumeAction.MAX to 100
                lower.contains("mute") || lower.contains("silence") || lower.contains("அமைதி") -> AndroidAction.VolumeAction.MUTE to 0
                lower.contains("unmute") -> AndroidAction.VolumeAction.UNMUTE to null
                lower.contains("down") || lower.contains("lower") || lower.contains("குறை") || lower.contains("kora") -> AndroidAction.VolumeAction.DOWN to null
                lower.contains("up") || lower.contains("raise") || lower.contains("increase") || lower.contains("அதிகம்") || lower.contains("yethu") -> AndroidAction.VolumeAction.UP to null
                else -> AndroidAction.VolumeAction.UP to null
            }
            val reply = when (lang) {
                LanguageType.TAMIL -> when (volAction) {
                    AndroidAction.VolumeAction.MAX -> "சத்தத்தை அதிகபட்சமாக வைக்கிறேன் $userNickname."
                    AndroidAction.VolumeAction.MUTE -> "சத்தத்தை மியூட் செய்கிறேன் $userNickname."
                    AndroidAction.VolumeAction.UNMUTE -> "சத்தத்தை அன்மியூட் செய்கிறேன் $userNickname."
                    AndroidAction.VolumeAction.DOWN -> "சத்தத்தைக் குறைக்கிறேன் $userNickname."
                    AndroidAction.VolumeAction.UP -> "சத்தத்தை அதிகரிக்கிறேன் $userNickname."
                    AndroidAction.VolumeAction.SET -> "சத்தத்தை மாற்றுகிறேன் $userNickname."
                }
                LanguageType.THANGLISH -> when (volAction) {
                    AndroidAction.VolumeAction.MAX -> "Volume full-ah vakkuren $userNickname."
                    AndroidAction.VolumeAction.MUTE -> "Volume mute panren $userNickname."
                    AndroidAction.VolumeAction.UNMUTE -> "Volume unmute panren $userNickname."
                    AndroidAction.VolumeAction.DOWN -> "Volume koraikkuren $userNickname."
                    AndroidAction.VolumeAction.UP -> "Volume yethuren $userNickname."
                    AndroidAction.VolumeAction.SET -> "Volume set panren $userNickname."
                }
                LanguageType.ENGLISH -> when (volAction) {
                    AndroidAction.VolumeAction.MAX -> "Setting volume to maximum, $userNickname."
                    AndroidAction.VolumeAction.MUTE -> "Muting device audio, $userNickname."
                    AndroidAction.VolumeAction.UNMUTE -> "Unmuting device audio, $userNickname."
                    AndroidAction.VolumeAction.DOWN -> "Decreasing volume, $userNickname."
                    AndroidAction.VolumeAction.UP -> "Increasing volume, $userNickname."
                    AndroidAction.VolumeAction.SET -> "Setting volume, $userNickname."
                }
            }
            return ParseResult(
                action = AndroidAction.ControlVolume(volAction, percent),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 9. Phone Calls: "Call [target]", "Dial [number]", "கூப்பிடு", "Call pannu"
        val callRegex = Regex("""(?:call|dial|phone|கூப்பிடு)\s+([a-zA-Z0-9\+\-\s]+)""", RegexOption.IGNORE_CASE)
        val callMatch = callRegex.find(text)
        if (callMatch != null || lower.contains("call pannu")) {
            val rawTarget = if (callMatch != null) callMatch.groupValues[1].trim() else text.substringBefore("call pannu").trim()
            val target = rawTarget.replace("to ", "").replace("ku ", "").trim()
            if (target.isNotBlank() && target.length <= 30 &&
                !target.equals("settings", ignoreCase = true) &&
                !target.equals("camera", ignoreCase = true)
            ) {
                val reply = when (lang) {
                    LanguageType.TAMIL -> "$target என்பவரை அழைக்க பாதுகாப்பு உறுதிப்படுத்தல் கேட்கிறேன் $userNickname."
                    LanguageType.THANGLISH -> "$target-ku call panna safety confirmation kekkuren $userNickname."
                    LanguageType.ENGLISH -> "Preparing call to $target with confirmation, $userNickname."
                }
                return ParseResult(
                    action = AndroidAction.MakeCall(phoneNumber = target, contactName = target),
                    explanation = reply,
                    detectedLanguage = lang
                )
            }
        }

        // 10. Send Message / SMS: "Send message to [target] saying [message]"
        val messageRegex = Regex("""(?:send\s+(?:a\s+)?(?:message|sms|text)(?:\s+to)?\s+([a-zA-Z0-9\+\-\s]+?)(?:\s+saying\s+|\s*:\s*|\s+that\s+)(.+))""", RegexOption.IGNORE_CASE)
        val msgMatch = messageRegex.find(text)
        if (msgMatch != null) {
            val target = msgMatch.groupValues[1].trim()
            val content = msgMatch.groupValues[2].trim()
            val reply = when (lang) {
                LanguageType.TAMIL -> "$target-க்கு மெசேஜ் அனுப்புகிறேன் $userNickname: \"$content\""
                LanguageType.THANGLISH -> "$target-ku SMS anupren $userNickname: \"$content\""
                LanguageType.ENGLISH -> "Composing SMS to $target, $userNickname: \"$content\""
            }
            return ParseResult(
                action = AndroidAction.SendMessage(phoneNumber = target, messageText = content),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 11. Settings actions: "Open settings", "WiFi settings", etc.
        if (lower.contains("settings") || lower.contains("அமைப்புகள்") || lower.contains("setting")) {
            val sub = when {
                lower.contains("wifi") || lower.contains("wi-fi") -> "wifi"
                lower.contains("bluetooth") -> "bluetooth"
                lower.contains("display") || lower.contains("screen") || lower.contains("brightness") -> "display"
                lower.contains("sound") || lower.contains("audio") -> "sound"
                lower.contains("battery") -> "battery"
                lower.contains("location") || lower.contains("gps") -> "location"
                else -> null
            }
            val reply = when (lang) {
                LanguageType.TAMIL -> "கணினி அமைப்புகளைத் திறக்கிறேன் $userNickname."
                LanguageType.THANGLISH -> "System settings open panren $userNickname."
                LanguageType.ENGLISH -> if (sub != null) "Opening $sub settings, $userNickname." else "Opening system settings, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.OpenSettings(sub),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 12. Chrome / Search: "Open Chrome", "Google ..."
        if (lower.contains("chrome") || lower.startsWith("google ") || lower.startsWith("search web") || lower.contains("கூகுள்")) {
            val query = when {
                lower.startsWith("search chrome for ") -> text.substringAfter("search chrome for ").trim()
                lower.startsWith("search chrome ") -> text.substringAfter("search chrome ").trim()
                lower.startsWith("google ") -> text.substringAfter("google ").trim()
                lower.startsWith("search web for ") -> text.substringAfter("search web for ").trim()
                else -> null
            }
            val reply = when (lang) {
                LanguageType.TAMIL -> if (!query.isNullOrBlank()) "Chrome-ல் '$query' தேடுகிறேன் $userNickname." else "Chrome உலாவியைத் திறக்கிறேன் $userNickname."
                LanguageType.THANGLISH -> if (!query.isNullOrBlank()) "Chrome-la '$query' theduren $userNickname." else "Chrome browser open panren $userNickname."
                LanguageType.ENGLISH -> if (!query.isNullOrBlank()) "Searching for '$query' on Chrome, $userNickname." else "Opening Google Chrome, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.OpenChrome(query),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 13. Music: "Play music", "Play song ...", "பாட்டு போடு", "Pattu podu"
        if (lower.contains("music") || lower.contains("spotify") || lower.contains("பாட்டு") || lower.contains("pattu")) {
            val song = when {
                lower.startsWith("play song ") -> text.substringAfter("play song ").trim()
                lower.startsWith("play music ") -> text.substringAfter("play music ").trim()
                lower.contains("pattu podu") -> text.substringBefore("pattu podu").trim()
                lower.contains("பாட்டு போடு") -> text.substringBefore("பாட்டு போடு").trim()
                lower.startsWith("play ") && !lower.contains("video") -> text.substringAfter("play ").trim()
                else -> null
            }
            val reply = when (lang) {
                LanguageType.TAMIL -> if (!song.isNullOrBlank()) "'$song' பாடலை இயக்குகிறேன் $userNickname." else "இசை பயன்பாட்டைத் திறக்கிறேன் $userNickname."
                LanguageType.THANGLISH -> if (!song.isNullOrBlank()) "'$song' paata play panren $userNickname." else "Music app open panren $userNickname."
                LanguageType.ENGLISH -> if (!song.isNullOrBlank()) "Playing '$song' on music player, $userNickname." else "Launching music player, $userNickname."
            }
            return ParseResult(
                action = AndroidAction.OpenMusic(song),
                explanation = reply,
                detectedLanguage = lang
            )
        }

        // 14. Generic App Launcher: "Open WhatsApp", "Launch Instagram", etc.
        val openAppRegex = Regex("""(?:open|launch|திற)\s+([a-zA-Z0-9\s]+?)(?:\s+app)?$""", RegexOption.IGNORE_CASE)
        val openAppMatch = openAppRegex.find(text)
        if (openAppMatch != null) {
            val app = openAppMatch.groupValues[1].trim()
            if (app.isNotBlank() && app.length <= 25 && !app.equals("the", ignoreCase = true)) {
                val reply = when (lang) {
                    LanguageType.TAMIL -> "$app செயலியைத் திறக்கிறேன் $userNickname."
                    LanguageType.THANGLISH -> "$app app-ah open panren $userNickname."
                    LanguageType.ENGLISH -> "Opening $app application, $userNickname."
                }
                return ParseResult(
                    action = AndroidAction.OpenApp(app),
                    explanation = reply,
                    detectedLanguage = lang
                )
            }
        }

        return ParseResult(action = null, explanation = "", detectedLanguage = lang)
    }
}
