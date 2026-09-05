package com.noobdevs.osint.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noobdevs.osint.data.AuthManager
import com.noobdevs.osint.data.BookmarksManager
import com.noobdevs.osint.data.KtorOsintApiClient
import com.noobdevs.osint.data.models.*
import com.noobdevs.osint.platform.PlatformActions
import com.noobdevs.osint.platform.ThreatAlertManager
import com.noobdevs.osint.util.SitrepFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    DASHBOARD("Overview"),
    FEED("Intel Feed"),
    OSINT_PICS("OSINT Media"),
    DOR("DOR Reports"),
    SETTINGS("Settings")
}

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel(
    val authManager: AuthManager = AuthManager(),
    val apiClient: KtorOsintApiClient = KtorOsintApiClient(authManager),
    val bookmarksManager: BookmarksManager = BookmarksManager()
) : ViewModel() {

    private val _toastEvents = Channel<String>(Channel.BUFFERED)
    val toastEvents = _toastEvents.receiveAsFlow()

    fun postMessage(msg: String) {
        viewModelScope.launch {
            _toastEvents.send(msg)
        }
    }

    val bookmarks: StateFlow<List<PostItem>> = bookmarksManager.bookmarksFlow

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(authManager.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(authManager.notificationsEnabled)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _showOnlyBookmarks = MutableStateFlow(false)
    val showOnlyBookmarks: StateFlow<Boolean> = _showOnlyBookmarks.asStateFlow()

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

    private val _selectedTimeRange = MutableStateFlow(TimeRange.LIVE)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _selectedPipeline = MutableStateFlow(PipelineFilter.ALL)
    val selectedPipeline: StateFlow<PipelineFilter> = _selectedPipeline.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _decksState = MutableStateFlow<UiState<List<DeckItem>>>(UiState.Loading)
    val decksState: StateFlow<UiState<List<DeckItem>>> = _decksState.asStateFlow()

    init {
        try {
            ThreatAlertManager.scheduleBackgroundSync()
        } catch (e: Throwable) {
            println("ThreatAlertManager init warning: ${e.message}")
        }
        try {
            loadDashboardStats()
            loadPosts(page = 1, append = false)
            loadDecks()
        } catch (e: Throwable) {
            println("Initial data fetch warning: ${e.message}")
        }
    }

    fun isBookmarked(postId: Long): Boolean = bookmarksManager.isBookmarked(postId)

    fun toggleBookmark(post: PostItem) {
        val added = bookmarksManager.toggleBookmark(post)
        val msg = if (added) "Saved to Evidence Bag" else "Removed from Evidence Bag"
        postMessage(msg)
    }

    fun toggleShowOnlyBookmarks() {
        _showOnlyBookmarks.value = !_showOnlyBookmarks.value
    }

    fun exportEvidenceDossier() {
        val saved = bookmarks.value
        if (saved.isEmpty()) {
            postMessage("Evidence Bag is empty. Bookmark posts first!")
            return
        }
        val text = SitrepFormatter.formatEvidenceDossier(saved)
        PlatformActions.shareText(text, "Export Intelligence Dossier to...")
    }

    fun sharePost(post: PostItem) {
        val text = SitrepFormatter.formatMilitarySitrep(post)
        PlatformActions.shareText(text, "Dispatch Tactical SITREP to...")
    }

    fun sendPostToWhatsApp(post: PostItem) {
        val text = SitrepFormatter.formatWhatsAppSitrep(post)
        PlatformActions.sendWhatsApp(text)
    }

    fun triggerTestAlert() {
        ThreatAlertManager.triggerNotification(
            title = "Balochistan [AMBUSH FIRE Alert]",
            body = "Security Forces intercepted kinetic movement near Kech sector. 2x suspects apprehended."
        )
        postMessage("Alert notification sent!")
    }

    fun triggerOneTimeSync() {
        loadPosts(page = 1, append = false)
        loadDashboardStats()
        postMessage("Polling office server for fresh intel...")
    }

    fun toggleNotifications(enabled: Boolean) {
        authManager.notificationsEnabled = enabled
        _notificationsEnabled.value = enabled
        if (enabled) {
            ThreatAlertManager.scheduleBackgroundSync()
            postMessage("Tactical Threat Radar activated")
        } else {
            ThreatAlertManager.cancelBackgroundSync()
            postMessage("Tactical Threat Radar deactivated")
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleDarkMode(enabled: Boolean) {
        authManager.isDarkMode = enabled
        _isDarkMode.value = enabled
    }

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
        loadPosts(page = 1, append = false)
    }

    fun setPipelineFilter(filter: PipelineFilter) {
        _selectedPipeline.value = filter
        loadPosts(page = 1, append = false)
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
        loadPosts(page = 1, append = false)
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
                .onFailure { _statsState.value = UiState.Error(it.message ?: "Failed to load stats") }
        }
    }

    fun loadPosts(page: Int = 1, append: Boolean = false) {
        viewModelScope.launch {
            _isPostsLoading.value = true
            _postsError.value = null

            val attackFilter = if (_selectedAttackType.value == "ALL") null else _selectedAttackType.value
            val category = if (_selectedCategory.value == "ALL") null else _selectedCategory.value
            val pipeline = _selectedPipeline.value.value
            val hours = _selectedTimeRange.value.hours
            val search = if (_searchQuery.value.isBlank()) null else _searchQuery.value

            apiClient.getPosts(
                page = page,
                limit = 25,
                category = category,
                attackType = attackFilter,
                search = search,
                pipeline = pipeline,
                hours = hours
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
                _postsError.value = err.message ?: "Failed to load posts"
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
                .onFailure { _decksState.value = UiState.Error(it.message ?: "Failed to load decks") }
        }
    }

    fun downloadDeck(deck: DeckItem) {
        viewModelScope.launch {
            postMessage("Downloading ${deck.filename}...")
            val downloadUrl = apiClient.getDeckDownloadUrl(deck.filename)
            apiClient.downloadBytes(downloadUrl)
                .onSuccess { bytes ->
                    PlatformActions.saveFileToDownloads(deck.filename, bytes)
                        .onSuccess { path -> postMessage("Saved ${deck.filename} to $path") }
                        .onFailure { err -> postMessage("Failed to save: ${err.message}") }
                }
                .onFailure { err -> postMessage("Download failed: ${err.message}") }
        }
    }

    fun downloadImage(post: PostItem) {
        val mediaUrl = apiClient.getMediaUrl(post.sentimentImagePath) ?: return
        viewModelScope.launch {
            postMessage("Downloading image...")
            val fileName = "OSINT_IMG_${post.id}.png"
            apiClient.downloadBytes(mediaUrl)
                .onSuccess { bytes ->
                    PlatformActions.saveFileToDownloads(fileName, bytes)
                        .onSuccess { path -> postMessage("Saved image to $path") }
                        .onFailure { err -> postMessage("Failed to save: ${err.message}") }
                }
                .onFailure { err -> postMessage("Download failed: ${err.message}") }
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
                    onResult(false, err.message ?: "Authentication failed")
                }
        }
    }
}
