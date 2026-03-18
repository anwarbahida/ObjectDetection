package com.example.test.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.test.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class DetectionViewModel : ViewModel() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyCZGdoYpQhBfU7lewj4O__DNMtwvhdPyAI"
    )

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val objects: List<DetectedObject>) : UiState()
        data class Error(val message: String) : UiState()
    }

    data class DetectedObject(
        val name: String,
        val confidence: String,
        val description: String
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun detectObjects(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val prompt = """
                    Analyse cette image et liste tous les objets visibles.
                    Réponds UNIQUEMENT en JSON valide avec ce format exact :
                    {"objects": [{"name": "nom", "confidence": "haute", "description": "courte description"}]}
                    Les valeurs de confidence doivent être : haute, moyenne ou faible.
                    Ne mets aucun texte avant ou après le JSON.
                """.trimIndent()

                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )

                val rawText = response.text ?: throw Exception("Réponse vide de Gemini")

                val json = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val parsed = parseResponse(json)
                _uiState.value = UiState.Success(parsed)

            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    private fun parseResponse(json: String): List<DetectedObject> {
        return try {
            val jsonObj = JSONObject(json)
            val array = jsonObj.getJSONArray("objects")
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                DetectedObject(
                    name = obj.optString("name", "Inconnu"),
                    confidence = obj.optString("confidence", "faible"),
                    description = obj.optString("description", "")
                )
            }
        } catch (e: Exception) {
            listOf(DetectedObject("Erreur parsing", "faible", e.message ?: ""))
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
    }
}