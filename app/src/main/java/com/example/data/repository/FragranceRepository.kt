package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class FragranceRepository(private val database: AppDatabase) {
    private val reviewDao = database.reviewDao()
    private val cartDao = database.cartDao()
    private val favoriteDao = database.favoriteDao()
    private val notificationDao = database.notificationDao()

    // Reactive Flows
    fun getReviewsForProduct(productId: String): Flow<List<UserReview>> =
        reviewDao.getReviewsForProduct(productId)

    fun getAverageRating(productId: String): Flow<Float?> =
        reviewDao.getAverageRating(productId)

    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()
    val favorites: Flow<List<FavoriteProduct>> = favoriteDao.getFavorites()
    val notifications: Flow<List<PromoNotification>> = notificationDao.getNotifications()

    fun isFavorite(productId: String): Flow<Boolean> = favoriteDao.isFavorite(productId)

    // Write Operations
    suspend fun insertReview(review: UserReview): Long = reviewDao.insertReview(review)

    suspend fun addCartItem(productId: String, size: String, price: Double) {
        val currentCart = cartDao.getCartItems().first()
        val existing = currentCart.find { it.productId == productId && it.size == size }
        if (existing != null) {
            cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            cartDao.insertCartItem(CartItem(productId = productId, size = size, quantity = 1, price = price))
        }
    }

    suspend fun removeCartItem(id: Int) = cartDao.deleteCartItem(id)
    suspend fun clearCart() = cartDao.clearCart()

    suspend fun toggleFavorite(productId: String) {
        val isFav = favoriteDao.isFavorite(productId).first()
        if (isFav) {
            favoriteDao.deleteFavorite(productId)
        } else {
            favoriteDao.insertFavorite(FavoriteProduct(productId = productId))
        }
    }

    suspend fun addNotification(title: String, description: String, badge: String) {
        notificationDao.insertNotification(
            PromoNotification(
                title = title,
                description = description,
                badge = badge
            )
        )
    }

    suspend fun markNotificationRead(id: Int) = notificationDao.markAsRead(id)
    suspend fun clearAllNotifications() = notificationDao.clearNotifications()

    // Seeds initial data if empty
    suspend fun seedDatabase() {
        // Seed reviews if empty
        val sampleReviews = listOf(
            UserReview(
                productId = "oud_al_nasr",
                userName = "Farid Al-Masri",
                rating = 5f,
                comment = "Sublime warm Cambodian oud. It lasts for over 24 hours on my skin. The golden saffron and dark tobacco leave a monumental elegant trail."
            ),
            UserReview(
                productId = "oud_al_nasr",
                userName = "Sana Khan",
                rating = 4.5f,
                comment = "A very strong and authentic woody scent. Magnificent for cold evenings or pairing with a light rose oil."
            ),
            UserReview(
                productId = "musk_al_ghazal",
                userName = "Zayd Hariri",
                rating = 5f,
                comment = "The finest rendition of deer musk I have ever smelled. Sweet forest berries and honeycomb elevate the dark velvety background."
            ),
            UserReview(
                productId = "sultani_rose",
                userName = "Aaliyah J.",
                rating = 5f,
                comment = "Pure Taif Rose absolute at its premium peak. It opens fresh and floral and dries down to a rich white musk and sandalwood cream."
            ),
            UserReview(
                productId = "golden_dunes",
                userName = "Karim Benz",
                rating = 4f,
                comment = "Spicy and warm! Smells like luxurious Arabian deserts mixed with classical French formulation. Cardamom and vanilla mix is fantastic."
            )
        )

        for (review in sampleReviews) {
            val existing = reviewDao.getReviewsForProduct(review.productId).first()
            if (existing.none { it.userName == review.userName }) {
                reviewDao.insertReview(review)
            }
        }

        // Seed notifications if empty
        val existingNotifs = notificationDao.getNotifications().first()
        if (existingNotifs.isEmpty()) {
            notificationDao.insertNotification(
                PromoNotification(
                    title = "Royal Welcoming Offer",
                    description = "Welcome to Al Nasr Attar & Fragrance. Browse our curated catalog of rare Oud extracts, traditional pure oils (Attars) and modern French spray blends.",
                    badge = "EXCLUSIVE"
                )
            )
            notificationDao.insertNotification(
                PromoNotification(
                    title = "Oud Al Nasr Golden Bundle",
                    description = "Claim a free custom gold-plated crystal crystal oil applicator bottle with any order over $120. Code: REGALGOLD",
                    badge = "PROMOTION"
                )
            )
        }
    }
}
