package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Product
import com.example.data.ProductDirectory
import com.example.data.database.CartItem
import com.example.data.database.PromoNotification
import com.example.data.database.UserReview
import com.example.data.repository.FragranceRepository
import com.example.data.database.AppDatabase
import com.example.data.repository.GeminiHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class FragranceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FragranceRepository(database)

    // Live State Streams from Room
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<String>> = repository.favorites
        .map { favs -> favs.map { it.productId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<PromoNotification>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    val allProducts = ProductDirectory.items

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    // Filtered Products
    val filteredProducts = combine(_searchText, _selectedCategory) { text, cat ->
        allProducts.filter { product ->
            val matchesSearch = product.name.contains(text, ignoreCase = true) ||
                    product.description.contains(text, ignoreCase = true) ||
                    product.notesTop.contains(text, ignoreCase = true) ||
                    product.notesHeart.contains(text, ignoreCase = true) ||
                    product.notesBase.contains(text, ignoreCase = true)
            
            val matchesCategory = cat == null || product.category == cat
            
            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allProducts)

    // Current reviews for selected product
    val selectedProductReviews = _selectedProduct.flatMapLatest { product ->
        if (product == null) flowOf(emptyList())
        else repository.getReviewsForProduct(product.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Profiler States
    private val _profilerInput = MutableStateFlow("")
    val profilerInput = _profilerInput.asStateFlow()

    private val _profilerResponse = MutableStateFlow<String?>(null)
    val profilerResponse = _profilerResponse.asStateFlow()

    private val _profilerLoading = MutableStateFlow(false)
    val profilerLoading = _profilerLoading.asStateFlow()

    // Notification Banner HUD State for Immediate Simulated Drops
    private val _activeNotificationBanner = MutableStateFlow<PromoNotification?>(null)
    val activeNotificationBanner = _activeNotificationBanner.asStateFlow()

    init {
        // Seed initial database content on start
        viewModelScope.launch {
            repository.seedDatabase()
        }
    }

    // Search and Filters
    fun setSearchText(text: String) {
        _searchText.value = text
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    // Favorites
    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }

    // Cart
    fun addToCart(productId: String, size: String, price: Double) {
        viewModelScope.launch {
            repository.addCartItem(productId, size, price)
        }
    }

    fun removeFromCart(id: Int) {
        viewModelScope.launch {
            repository.removeCartItem(id)
        }
    }

    fun checkoutCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Reviews
    fun addReview(productId: String, userName: String, rating: Float, comment: String) {
        viewModelScope.launch {
            repository.insertReview(
                UserReview(
                    productId = productId,
                    userName = if (userName.isBlank()) "Anonymous Connoisseur" else userName,
                    rating = rating,
                    comment = comment
                )
            )
        }
    }

    // AI Profiler Trigger
    fun setProfilerInput(text: String) {
        _profilerInput.value = text
    }

    fun clearProfiler() {
        _profilerResponse.value = null
        _profilerInput.value = ""
    }

    fun runScentConsultation() {
        val prompt = _profilerInput.value
        if (prompt.isBlank()) return
        
        viewModelScope.launch {
            _profilerLoading.value = true
            _profilerResponse.value = null
            val response = GeminiHelper.consultProfiler(prompt)
            _profilerResponse.value = response
            _profilerLoading.value = false
        }
    }

    // Simulated Notification System
    fun dismissNotificationBanner() {
        _activeNotificationBanner.value = null
    }

    fun triggerSimulatedPushNotification() {
        viewModelScope.launch {
            val templates = listOf(
                Pair(
                    "Sovereign Celebration: 20% Off",
                    "For the next 3 hours, unlock 20% off Aura of Victory EDP using voucher VICTORY20. Express shipping guaranteed."
                ),
                Pair(
                    "Exquisite New Arrival: Al Nasr Pure Musk",
                    "Directly imported from Taif gardens: Al Nasr Pure Jasmin-Musk solid attar blocks are now in stock in limited quantities."
                ),
                Pair(
                    "Weekend VIP Private Sale",
                    "Exquisite gold-capped decanter bottles of Oud Al Nasr (12ml) are reserved at 15% reduction for registered loyalty accounts."
                ),
                Pair(
                    "The Art of Layering: Amber & Oud",
                    "Perfumist Tip: Mix Oud Al Nasr Pure Oil with Saba Warm Amber for an incredibly comforting, smoky winter warmth."
                )
            )

            val select = templates[Random.nextInt(templates.size)]
            val badgeTypes = listOf("EXCLUSIVE", "PROMOTION", "NEW ARRIVAL")
            val badge = badgeTypes[Random.nextInt(badgeTypes.size)]

            // Write to database so it populates in the history list
            repository.addNotification(
                title = select.first,
                description = select.second,
                badge = badge
            )

            // Trigger HUD banner overlay immediately
            val notif = PromoNotification(
                id = Random.nextInt(1000, 99999),
                title = select.first,
                description = select.second,
                badge = badge,
                timestamp = System.currentTimeMillis()
            )
            _activeNotificationBanner.value = notif
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}
