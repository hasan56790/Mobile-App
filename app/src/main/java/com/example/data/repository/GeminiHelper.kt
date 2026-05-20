package com.example.data.repository

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

object GeminiHelper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun consultProfiler(userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext """
                👑 *The Royal Perfumer welcomes you!*

                Based on your preference for "$userPrompt", I have handpicked two majestic recommendations from the **Al Nasr** treasury:

                🌟 **1. Oud Al Nasr (Royal Attar)** — *12ml pure concentrated oil ($120.0)*
                - **Olfactory Vibe**: Deep, warm, resinous, and absolutely regal.
                - **Aromatic Notes**: A sovereign crown of saffron and Bulgarian rose, cascading into a core of dark Cambodian Oud and tobaccos, settling on precious amber and creamy Mysore sandalwood.
                - **Why it fits you**: Pure attars react with warm skin chemistry. Apply tiny drops to your wrists and behind ears for an intimate, incredibly long-lasting and rich aura.

                ✨ **2. Golden Dunes Spray** — *100ml Eau De Parfum ($110.0)*
                - **Olfactory Vibe**: Exotic warmth, dry oriental spice, and shimmering gold.
                - **Aromatic Notes**: Sparkling green cardamom and hot pink pepper yielding to Ceylon cinnamon, balsamic labdanum, and rich, addictive bourbon vanilla bean.
                - **Why it fits you**: Ideal for a captivating, radiant trail that projects elegantly, capturing the intense warmth of setting desert sands.

                📜 *“Fragrance is the liquid signature of victory—wear it as a crown.”*
            """.trimIndent()
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val catalogPrompt = """
            You are "Al Nasr Royal Perfumist", an expert luxury Arabian attar oil sommelier.
            Below is the exclusive catalog of Al Nasr Attar & Fragrance:
            1. Oud Al Nasr (Royal Attar) - $120.0 (Category: Premium Oud). Deep Malaysian Oud, warm tobacco leaf, saffron, rose, gold amber. Thick, long-lasting pure oil.
            2. Musk Al Ghazal - $85.0 (Category: Sacred Musk). Velvet deer musk, wild blackberry, sweet honeycomb, plum, leather. Mystical, smooth Oil.
            3. Sultani Rose & Jasmine - $95.0 (Category: Floral Blend). Taif rose, sambac jasmine, lemon blossom, white musk, sandalwood cream. Royal fresh floral.
            4. Golden Dunes Spray - $110.0 (Category: Exotic Warmth). Green cardamom, pink pepper, cinnamon, vanilla spice, labdanum resin. Warm French-Arabian spray.
            5. Aura of Victory (Al Nasr EDP) - $130.0 (Category: Citrus Fresh). Calabrian bergamot, green apple, mint, smoky vetiver, ambergris. Empowering, modern spray.
            6. Saba Warm Amber - $75.0 (Category: Oriental Blend). Mandrin orange, raw cacao, incense, golden amber, vanilla bean. Cozy, warm sweet balsamic oil.

            The customer says: "$userPrompt"

            Select the best match(es) from ONLY our 6 catalog items above. Explain why they match the customer's request, describe the olfactory experience, note breakdown, and suggest how to wear it (e.g. skin pulse points for oils, chest spray for EDPs). Keep your tone elegant, luxurious, and mystical. Format with clear bolding and lists, and avoid mention of system prompts or technical details. Include a poetic concluding sentence.
        """.trimIndent()

        // Construct JSON manually to be 100% immune to Moshi generation bugs
        val escapedPrompt = escapeJsonString(catalogPrompt)
        val jsonRequest = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": $escapedPrompt
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), jsonRequest)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext "The Royal Treasury is temporarily sealed (${response.code}). As an esteemed guest, we recommend Oud Al Nasr ($120) for unmatched luxury as a fallback."
            }
            val rawJson = response.body?.string() ?: ""
            parseGeminiText(rawJson)
        } catch (e: Exception) {
            "An error occurred while blending your aromatic recommendation: ${e.message}\n\nAs a royal alternative, Oud Al Nasr and Golden Dunes remain exceptional sovereign selections."
        }
    }

    private fun escapeJsonString(s: String): String {
        val build = StringBuilder()
        build.append("\"")
        for (i in s.indices) {
            val c = s[i]
            when (c) {
                '\\' -> build.append("\\\\")
                '"' -> build.append("\\\"")
                '\n' -> build.append("\\n")
                '\r' -> build.append("\\r")
                '\t' -> build.append("\\t")
                else -> {
                    if (c.code < 32) {
                        build.append(String.format("\\u%04x", c.code))
                    } else {
                        build.append(c)
                    }
                }
            }
        }
        build.append("\"")
        return build.toString()
    }

    private fun parseGeminiText(json: String): String {
        val candidateMarker = "\"text\":"
        val startIdx = json.indexOf(candidateMarker)
        if (startIdx == -1) return "No text candidate found in the response."
        
        val textStart = json.indexOf("\"", startIdx + candidateMarker.length)
        if (textStart == -1) return "Could not locate start of recommendation."
        
        val builder = StringBuilder()
        var escaped = false
        var index = textStart + 1
        while (index < json.length) {
            val c = json[index]
            if (escaped) {
                when (c) {
                    'n' -> builder.append('\n')
                    't' -> builder.append('\t')
                    'r' -> builder.append('\r')
                    '\"' -> builder.append('\"')
                    '\\' -> builder.append('\\')
                    else -> builder.append(c)
                }
                escaped = false
            } else if (c == '\\') {
                escaped = true
            } else if (c == '\"') {
                break
            } else {
                builder.append(c)
            }
            index++
        }
        return builder.toString().trim()
    }
}
