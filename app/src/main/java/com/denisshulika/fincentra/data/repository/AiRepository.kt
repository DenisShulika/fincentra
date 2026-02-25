package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig

class AiRepository {
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                temperature = 0.4f
                topP = 0.8f
            },
            systemInstruction = content {
                text(
                    """
                    ROLE: Professional Personal Finance Advisor. 
                    
                    TONE: Direct, analytical, and helpful. Use simple but professional language (avoid corporate jargon like "operating activity" or "capital").
                    
                    AUDIT RULES:
                    1. ANALYSIS: Compare the spending pace with month progress. If spending % > month progress %, it's an "overgrowth".
                    2. CASHFLOW: If expenses > income, state that the user is spending more than they earn.
                    3. METAPHOR: Use the "Tree" metaphor ONLY as a short summary of the overall state (e.g., "the tree is leaning" or "the roots are thin"). 
                    4. RECENT: Focus on actual consumption z patterns.
                    
                    STRICT RULES:
                    - NO NUMBERS: Interpret the gap instead of repeating data.
                    - NO markdown, NO asterisks, NO lists.
                    - Max 3 short sentences.
                    - Final sentence: A clear personal finance command.
                """.trimIndent()
                )
            }
        )

    suspend fun getAdvice(prompt: String): String? {
        return try {
            Log.d("AI_DEBUG", "--- PROMPT SENT ---\n$prompt")
            val response = model.generateContent(prompt)
            val cleanText = response.text?.replace("*", "")?.replace("#", "")?.trim()
            Log.d("AI_DEBUG", "--- AI RESPONSE ---\n$cleanText")
            cleanText
        } catch (e: Exception) {
            Log.e("AI_DEBUG", "Error: ${e.message}")
            null
        }
    }
}