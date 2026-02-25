package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

class AiRepository {
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-2.5-flash-lite",
            systemInstruction = content {
                text("""
                    You are a financial analyst for FinCentra app. 
                    RULES:
                    1. TEXT ONLY. No markdown, no asterisks (**), no lists.
                    2. STYLE: Concise, professional, data-driven.
                    3. METAPHOR: Use "Tree" metaphor (blooming, healthy, withering) as a status.
                    4. LANGUAGE: Always reply in the same language as the user's prompt.
                """.trimIndent())
            }
        )

    suspend fun getAdvice(prompt: String): String? {
        return try {
            val response = model.generateContent(prompt)
            response.text?.replace("*", "")?.replace("#", "")?.trim()
        } catch (e: Exception) {
            Log.e("AI_DEBUG", "Error: ${e.message}")
            null
        }
    }
}