package com.noobdevs.osint.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noobdevs.osint.data.AuthManager
import com.noobdevs.osint.data.DeckItem
import com.noobdevs.osint.data.OsintApiClient
import com.noobdevs.osint.data.PostItem
import com.noobdevs.osint.data.StatsResponse
import com.noobdevs.osint.util.MediaDownloadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    DASHBOARD("Overview"),
    FEED("Intel Feed"),
    OSINT_PICS("OSINT Media"),
    DOR("DOR Reports"),
    SETTINGS("Settings")
}

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val authManager = AuthManager(application)
    val apiClient = OsintApiClient(authManager)

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(authManager.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _statsState = MutableStateFlow<UiState<StatsResponse>>(UiState.Loading)
    val statsState: StateFlow<UiState<StatsResponse>> = _statsState.asStateFlow()

    private val _posts = MutableStateFlow<List<PostItem>>(emptyList())
    val posts: StateFlow<List<PostItem>> = _posts.asStateFlow()

    private val _isPostsLoading = MutableStateFlow(false)
    val isPostsLoading: StateFlow<Boolean> = _isPostsLoading.asStateFlow()

    private val _postsError = MutableStateFlow<String?>(null)
    val postsError: StateFlow<String?> = _postsError.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _selectedProvince = MutableStateFlow("All")
    val selectedProvince: StateFlow<String> = _selectedProvince.asStateFlow()

    private val _selectedAttackType = MutableStateFlow("ALL")
    val selectedAttackType: StateFlow<String> = _selectedAttackType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _decksState = MutableStateFlow<UiState<List<DeckItem>>>(UiState.Loading)
    val decksState: StateFlow<UiState<List<DeckItem>>> = _decksState.asStateFlow()

    init {
        loadDashboardStats()
        loadPosts(page = 1, append = false)
        loadDecks()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleDarkMode(enabled: Boolean) {
        authManager.isDarkMode = enabled
        _isDarkMode.value = enabled
    }

    fun setProvinceFilter(province: String) {
        _selectedProvince.value = province
    }

    fun setAttackTypeFilter(type: String) {
        _selectedAttackType.value = type
        loadPosts(page = 1, append = false)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadPosts(page = 1, append = false)
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            apiClient.getStats()
                .onSuccess { _statsState.value = UiState.Success(it) }
                .onFailure { _statsState.value = UiState.Error(it.localizedMessage ?: "Failed to load stats") }
        }
    }

    fun loadPosts(page: Int = 1, append: Boolean = false) {
        viewModelScope.launch {
            _isPostsLoading.value = true
            _postsError.value = null

            val attackFilter = if (_selectedAttackType.value == "ALL") null else _selectedAttackType.value
            val search = if (_searchQuery.value.isBlank()) null else _searchQuery.value

            apiClient.getPosts(
                page = page,
                limit = 25,
                attackType = attackFilter,
                search = search
            ).onSuccess { response ->
                _currentPage.value = response.page
                _totalPages.value = response.pages
                if (append) {
                    _posts.value = _posts.value + response.data
                } else {
                    _posts.value = response.data
                }
                _isPostsLoading.value = false
            }.onFailure { err ->
                _postsError.value = err.localizedMessage ?: "Failed to load posts"
                _isPostsLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (!_isPostsLoading.value && _currentPage.value < _totalPages.value) {
            loadPosts(page = _currentPage.value + 1, append = true)
        }
    }

    fun loadDecks() {
        viewModelScope.launch {
            _decksState.value = UiState.Loading
            apiClient.getDecks()
                .onSuccess { _decksState.value = UiState.Success(it.decks) }
                .onFailure { _decksState.value = UiState.Error(it.localizedMessage ?: "Failed to load decks") }
        }
    }

    fun downloadDeck(deck: DeckItem) {
        MediaDownloadHelper.downloadDeckWithManager(
            getApplication(),
            authManager,
            deck.filename
        )
    }

    fun downloadImage(post: PostItem) {
        val mediaUrl = apiClient.getMediaUrl(post.sentimentImagePath) ?: return
        viewModelScope.launch {
            val fileName = "OSINT_IMG_${post.id}.png"
            MediaDownloadHelper.downloadImageFile(
                getApplication(),
                apiClient.okHttpClient,
                mediaUrl,
                fileName
            ).onSuccess {
                Toast.makeText(getApplication(), "Saved $fileName to Downloads", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(getApplication(), "Download failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateCredentials(url: String, user: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            apiClient.testLogin(url, user, pass)
                .onSuccess { msg ->
                    authManager.saveCredentials(url, user, pass)
                    loadDashboardStats()
                    loadPosts(page = 1, append = false)
                    loadDecks()
                    onResult(true, msg)
                }
                .onFailure { err ->
                    onResult(false, err.localizedMessage ?: "Authentication failed")
                }
        }
    }
}
