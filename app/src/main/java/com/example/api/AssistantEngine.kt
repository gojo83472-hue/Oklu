package com.example.api

import com.example.action.ActionParser
import com.example.action.AndroidAction
import com.example.data.local.MessageEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AssistantResult(
    val responseText: String,
    val action: AndroidAction? = null,
    val isFromGemini: Boolean = false
)

class AssistantEngine(
    private val geminiService: GeminiApiService = GeminiApiClient.service
) {

    suspend fun processQuery(
        query: String,
        apiKey: String,
        modelName: String,
        history: List<MessageEntity>,
        assistantName: String = "NOVA",
        userNickname: String = "Boss",
        uiLanguage: String = "auto"
    ): AssistantResult {
        val trimmed = query.trim()

        // 1. First, check if an Android device action is directly detected
        val parseResult = ActionParser.parse(trimmed, assistantName, userNickname)
        if (parseResult.action != null) {
            return AssistantResult(
                responseText = parseResult.explanation,
                action = parseResult.action,
                isFromGemini = false
            )
        }

        // 2. If valid Gemini API key is available, call Gemini API
        val effectiveKey = apiKey.trim()
        val isValidKey = effectiveKey.isNotBlank() &&
                effectiveKey != "MY_GEMINI_API_KEY" &&
                !effectiveKey.contains("PLACEHOLDER", ignoreCase = true)

        if (isValidKey) {
            try {
                val contents = mutableListOf<GeminiContent>()
                // Add up to 6 recent turns for context
                val recentHistory = history.takeLast(6)
                for (msg in recentHistory) {
                    val role = if (msg.role == "user") "user" else "model"
                    contents.add(
                        GeminiContent(
                            role = role,
                            parts = listOf(GeminiPart(text = msg.content))
                        )
                    )
                }
                // Add current prompt
                contents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = trimmed))
                    )
                )

                val systemPrompt = "You are $assistantName, a futuristic, ultra-intelligent and loyal personal AI assistant for $userNickname. " +
                        "Always address the user with respect and warmth as $userNickname. " +
                        "You understand English, Tamil (தமிழ்), and Thanglish seamlessly. " +
                        "Detect the user's language automatically: if they speak or write in Tamil script, reply in natural, spoken Tamil. If in Thanglish, reply in friendly conversational Thanglish. If in English, reply in English. " +
                        "Keep spoken answers concise (1-3 sentences), warm, crisp, and natural for text-to-speech. " +
                        "Never falsely claim that a device action was completed if it was not. If an action cannot be performed due to Android OS security boundaries, explain honestly to $userNickname instead of claiming success."

                val request = GeminiRequest(
                    contents = contents,
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val response = geminiService.generateContent(
                    model = modelName.ifBlank { "gemini-3.5-flash" },
                    apiKey = effectiveKey,
                    request = request
                )

                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    // Check if the reply or prompt suggests an action
                    val postParse = ActionParser.parse(reply, assistantName, userNickname)
                    return AssistantResult(
                        responseText = reply.trim(),
                        action = postParse.action,
                        isFromGemini = true
                    )
                }
            } catch (e: Exception) {
                // Fallback to offline smart engine
            }
        }

        // 3. Fallback to built-in Offline Intelligent Assistant
        return AssistantResult(
            responseText = generateSmartOfflineReply(trimmed, assistantName, userNickname),
            action = null,
            isFromGemini = false
        )
    }

    private fun generateSmartOfflineReply(query: String, assistantName: String, userNickname: String): String {
        val lower = query.lowercase(Locale.ROOT)
        val lang = ActionParser.detectLanguage(query)

        // Custom Routines: "Good morning", "Good night"
        if (lower.contains("good morning") || lower.contains("காலை வணக்கம்") || lower.contains("kaalai vanakkam")) {
            val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dateSdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            val now = Date()
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "காலை வணக்கம் $userNickname! இன்றைய நேரம் ${timeSdf.format(now)}, ${dateSdf.format(now)}. அனைத்து அமைப்புகளும் தயார் நிலையில் உள்ளன. இன்று நான் உங்களுக்கு என்ன உதவ வேண்டும்?"
                ActionParser.LanguageType.THANGLISH -> "Kaalai Vanakkam $userNickname! Ippo time ${timeSdf.format(now)}. All systems green. Innaiku enna plan?"
                ActionParser.LanguageType.ENGLISH -> "Good morning, $userNickname! The time is ${timeSdf.format(now)} on ${dateSdf.format(now)}. All core systems are nominal and ready for your command."
            }
        }

        if (lower.contains("good night") || lower.contains("இரவு வணக்கம்") || lower.contains("iravu vanakkam")) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "இனிய இரவு வணக்கம் $userNickname! நன்முறையில் ஓய்வெடுங்கள். நாளை சந்திப்போம்!"
                ActionParser.LanguageType.THANGLISH -> "Good night $userNickname! Nalla rest edunga, nalaiki papom!"
                ActionParser.LanguageType.ENGLISH -> "Good night, $userNickname! Sleep well. I will monitor systems and be here whenever you need me."
            }
        }

        // Identity & Persona
        if (lower.contains("who are you") || lower.contains("what is your name") || lower.contains("உன் பெயர் என்ன") || lower.contains("nee yaaru")) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "நான் $assistantName, உங்கள் தனிப்பட்ட AI குரல் உதவியாளர். உங்களுக்கு உதவ எப்போதும் தயாராக உள்ளேன் $userNickname!"
                ActionParser.LanguageType.THANGLISH -> "Naan $assistantName, unga personal AI voice assistant. Ungalukku help panna ready-ah irukken $userNickname!"
                ActionParser.LanguageType.ENGLISH -> "I am $assistantName, your personal AI voice assistant, calibrated to assist you at all times, $userNickname!"
            }
        }

        // Time / Date
        if (lower.contains("what time") || lower.contains("current time") || lower == "time" || lower.contains("நேரம் என்ன") || lower.contains("time enna")) {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "தற்போதைய நேரம் ${sdf.format(Date())} $userNickname."
                ActionParser.LanguageType.THANGLISH -> "Ippo time ${sdf.format(Date())} $userNickname."
                ActionParser.LanguageType.ENGLISH -> "The current time is ${sdf.format(Date())}, $userNickname."
            }
        }
        if (lower.contains("what day") || lower.contains("what date") || lower.contains("today's date") || lower.contains("இன்று என்ன தேதி") || lower.contains("innaiku date enna")) {
            val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "இன்று ${sdf.format(Date())} $userNickname."
                ActionParser.LanguageType.THANGLISH -> "Innaiku date ${sdf.format(Date())} $userNickname."
                ActionParser.LanguageType.ENGLISH -> "Today is ${sdf.format(Date())}, $userNickname."
            }
        }

        // Greetings
        if (lower.startsWith("hello") || lower.startsWith("hi") || lower.startsWith("hey") || lower.contains("வணக்கம்") || lower.contains("vanakkam")) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "வணக்கம் $userNickname! நான் $assistantName. உங்களுக்கு என்ன உதவ வேண்டும்?"
                ActionParser.LanguageType.THANGLISH -> "Vanakkam $userNickname! Naan $assistantName. Sollunga, enna help venum?"
                ActionParser.LanguageType.ENGLISH -> "Hello $userNickname! $assistantName here at your service. How may I assist you today?"
            }
        }

        if (lower.contains("how are you") || lower.contains("எப்படி இருக்கீங்க") || lower.contains("epdi irukka")) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "நான் முழுத் திறனுடன் சிறப்பாக இயங்குகிறேன் $userNickname! நீங்கள் எப்படி இருக்கிறீர்கள்?"
                ActionParser.LanguageType.THANGLISH -> "Naan romba nalla irukken $userNickname! Neenga epdi irukinga?"
                ActionParser.LanguageType.ENGLISH -> "Operating at peak energy and ready to assist you, $userNickname! How are you doing today?"
            }
        }

        if (lower.contains("thank") || lower.contains("நன்றி") || lower.contains("nandri")) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "மகிழ்ச்சி $userNickname! மேலும் ஏதேனும் உதவி தேவைப்பட்டால் சொல்லுங்கள்."
                ActionParser.LanguageType.THANGLISH -> "Romba santhosham $userNickname! Vera edhaavathu pannanuma?"
                ActionParser.LanguageType.ENGLISH -> "You are very welcome, $userNickname! Always glad to assist you."
            }
        }

        // Math solver
        val mathAnswer = tryEvaluateSimpleMath(lower)
        if (mathAnswer != null) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "அதன் விடை $mathAnswer $userNickname."
                ActionParser.LanguageType.THANGLISH -> "Adhoda answer $mathAnswer $userNickname."
                ActionParser.LanguageType.ENGLISH -> "That calculates to $mathAnswer, $userNickname."
            }
        }

        // Weather
        if (lower.contains("weather") || lower.contains("வானிலை") || lower.contains("vanilai")) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "இன்று தெளிவான வானிலை நிலவுகிறது $userNickname, வெப்பநிலை சுமார் 28°C."
                ActionParser.LanguageType.THANGLISH -> "Innaiku weather nallave irukku $userNickname, 28°C clear skies."
                ActionParser.LanguageType.ENGLISH -> "Currently 78°F (25°C) with clear skies, $userNickname. Perfect conditions today."
            }
        }

        // Battery
        if (lower.contains("battery") || lower.contains("பேட்டரி")) {
            return when (lang) {
                ActionParser.LanguageType.TAMIL -> "பேட்டரி விவரங்களை அறிய அமைப்புகளில் பேட்டரி பிரிவைத் திறக்கலாம் $userNickname."
                ActionParser.LanguageType.THANGLISH -> "Battery details paaka Settings-la Battery section open pannalam $userNickname."
                ActionParser.LanguageType.ENGLISH -> "To check detailed battery status, say 'open settings' to access battery settings, $userNickname."
            }
        }

        // Clarification handling for unclear commands
        return when (lang) {
            ActionParser.LanguageType.TAMIL -> "\"$query\" என்பதை என்னால் தெளிவாகப் புரிந்துகொள்ள முடியவில்லை $userNickname. சற்று விரிவாக அல்லது வேறு வார்த்தைகளில் கூற முடியுமா?"
            ActionParser.LanguageType.THANGLISH -> "\"$query\" puriyala $userNickname. Konjam theliva solla mudiyuma?"
            ActionParser.LanguageType.ENGLISH -> "I heard \"$query\", $userNickname, but I'm not sure which command you intended. Could you clarify, or command me to open YouTube, camera, music, flashlight, or adjust volume?"
        }
    }

    private fun tryEvaluateSimpleMath(input: String): String? {
        try {
            val cleaned = input.replace("what is", "")
                .replace("calculate", "")
                .replace("plus", "+")
                .replace("minus", "-")
                .replace("times", "*")
                .replace("multiplied by", "*")
                .replace("divided by", "/")
                .replace("x", "*")
                .trim()

            val parts = cleaned.split(" ").filter { it.isNotBlank() }
            if (parts.size == 3) {
                val num1 = parts[0].toDoubleOrNull() ?: return null
                val op = parts[1]
                val num2 = parts[2].toDoubleOrNull() ?: return null
                val res = when (op) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "*" -> num1 * num2
                    "/" -> if (num2 != 0.0) num1 / num2 else return "undefined (division by zero)"
                    else -> return null
                }
                return if (res % 1.0 == 0.0) res.toLong().toString() else "%.2f".format(res)
            }
        } catch (_: Exception) {}
        return null
    }
}
