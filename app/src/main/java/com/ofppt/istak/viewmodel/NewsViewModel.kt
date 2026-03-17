package com.ofppt.istak.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.NewsArticle
import com.ofppt.istak.data.repository.StagiaireRepository
import com.ofppt.istak.data.websocket.ReverbClient
import com.ofppt.istak.worker.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: StagiaireRepository,
    private val reverbClient: ReverbClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState

    init {
        loadNews()
        setupWebSocket()
    }

    private fun setupWebSocket() {
        viewModelScope.launch {
            reverbClient.setupAndConnect()
            val pusher = reverbClient.getPusher()
            
            val channel = pusher?.subscribe("news")
            
            channel?.bind("news.published") { event ->
                // When a new article is published, simply reload the list from the server
                // (or parse the JSON event to prepend it instantly)
                loadNews()

                // Trigger notification
                NotificationWorker.showNotification(
                    context,
                    "Nouvelle Actualité",
                    "Une nouvelle actualité a été publiée.",
                    "news"
                )
            }
        }
    }

    fun loadNews() {
        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            try {
                val response = repository.getNews()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = NewsUiState.Success(response.body()!!.data)
                } else {
                    _uiState.value = NewsUiState.Error("Erreur de chargement des actualités")
                }
            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error("Erreur réseau: ${e.message}")
            }
        }
    }
}

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val articles: List<NewsArticle>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}
